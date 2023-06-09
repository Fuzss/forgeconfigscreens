package fuzs.forgeconfigscreens.core;

import fuzs.forgeconfigscreens.ForgeConfigScreens;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.IConfigEvent;
import net.minecraftforge.fml.config.IConfigSpec;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.loading.FMLConfig;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.forgespi.language.IModInfo;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

public final class ForgeAbstractions implements CommonAbstractions {
    private static final String PROTOCOL_VERSION = Integer.toString(1);

    private final SimpleChannel channel = NetworkRegistry.ChannelBuilder
            .named(ForgeConfigScreens.id("play"))
            .networkProtocolVersion(() -> PROTOCOL_VERSION)
            .clientAcceptedVersions(NetworkRegistry.acceptMissingOr(PROTOCOL_VERSION))
            .serverAcceptedVersions(PROTOCOL_VERSION::equals)
            .simpleChannel();
    private final AtomicInteger discriminator = new AtomicInteger();

    @Override
    public Optional<String> getModDisplayName(String modId) {
        return ModList.get().getModContainerById(modId).map(ModContainer::getModInfo).map(IModInfo::getDisplayName);
    }

    @Override
    public boolean isDevelopmentEnvironment() {
        return !FMLLoader.isProduction();
    }

    @Override
    public boolean isClientEnvironment() {
        return FMLLoader.getDist().isClient();
    }

    @Override
    public Path getDefaultConfigPath() {
        return FMLPaths.GAMEDIR.get().resolve(FMLConfig.defaultConfigPath());
    }

    @Override
    public void fireReloadingEvent(ModConfig modConfig) {
        try {
            Method fireEvent = ModConfig.class.getDeclaredMethod("fireEvent", IConfigEvent.class);
            fireEvent.setAccessible(true);
            MethodHandles.lookup().unreflect(fireEvent).invoke(modConfig, new ModConfigEvent.Reloading(modConfig));
        } catch (Throwable e) {
            ForgeConfigScreens.LOGGER.error("Unable to fire config reloading event for {}", modConfig.getFileName(), e);
        }
    }

    @Override
    public <T extends WritableMessage> void registerClientboundMessage(Class<T> clazz, Supplier<NetworkingHelper.ClientMessageListener<T>> listener) {
        this.registerMessage(clazz, (T message, ServerPlayer player) -> {
            Minecraft minecraft = Minecraft.getInstance();
            LocalPlayer clientPlayer = minecraft.player;
            Objects.requireNonNull(clientPlayer, "player is null");
            listener.get().handle(message, minecraft, clientPlayer.connection, clientPlayer, minecraft.level);
        }, NetworkDirection.PLAY_TO_CLIENT);
    }

    @Override
    public <T extends WritableMessage> void registerServerboundMessage(Class<T> clazz, Supplier<NetworkingHelper.ServerMessageListener<T>> listener) {
        this.registerMessage(clazz, (T message, ServerPlayer player) -> {
            Objects.requireNonNull(player, "player is null");
            listener.get().handle(message, ServerLifecycleHooks.getCurrentServer(), player.connection, player, player.serverLevel());
        }, NetworkDirection.PLAY_TO_SERVER);
    }

    @SuppressWarnings("unchecked")
    private <T extends WritableMessage> void registerMessage(Class<T> clazz, BiConsumer<T, ServerPlayer> consumer, NetworkDirection networkDirection) {
        this.channel.registerMessage(this.discriminator.getAndIncrement(), clazz, WritableMessage::write, friendlyByteBuf -> {
            MethodType methodType = MethodType.methodType(void.class, FriendlyByteBuf.class);
            try {
                return (T) MethodHandles.publicLookup().findConstructor(clazz, methodType).invoke(friendlyByteBuf);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }, (T message, Supplier<NetworkEvent.Context> supplier) -> {
            NetworkEvent.Context context = supplier.get();
            context.enqueueWork(() -> consumer.accept(message, context.getSender()));
            context.setPacketHandled(true);
        }, Optional.of(networkDirection));
    }

    @Override
    public Packet<?> toClientboundPacket(WritableMessage message) {
        return this.channel.toVanillaPacket(message, NetworkDirection.PLAY_TO_CLIENT);
    }

    @Override
    public Packet<?> toServerboundPacket(WritableMessage message) {
        return this.channel.toVanillaPacket(message, NetworkDirection.PLAY_TO_SERVER);
    }

    @Override
    public void registerConfig(String modId, ModConfig.Type type, IConfigSpec<?> spec) {
        ModLoadingContext.get().registerConfig(type, spec);
    }

    @Override
    public void registerConfig(String modId, ModConfig.Type type, IConfigSpec<?> spec, String fileName) {
        ModLoadingContext.get().registerConfig(type, spec, fileName);
    }
}
