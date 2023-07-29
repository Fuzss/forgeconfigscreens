package fuzs.forgeconfigscreens.core;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import fuzs.forgeconfigapiport.api.config.v2.ForgeConfigPaths;
import fuzs.forgeconfigapiport.api.config.v2.ForgeConfigRegistry;
import fuzs.forgeconfigapiport.api.config.v2.ModConfigEvents;
import fuzs.forgeconfigscreens.ForgeConfigScreens;
import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraftforge.fml.config.IConfigSpec;
import net.minecraftforge.fml.config.ModConfig;

import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

public final class FabricAbstractions implements CommonAbstractions {
    private final BiMap<Class<? extends WritableMessage>, ResourceLocation> messageRegistry = HashBiMap.create();
    private final AtomicInteger discriminator = new AtomicInteger();

    @Override
    public Optional<String> getModDisplayName(String modId) {
        return FabricLoader.getInstance().getModContainer(modId).map(ModContainer::getMetadata).map(ModMetadata::getName);
    }

    @Override
    public boolean isDevelopmentEnvironment() {
        return FabricLoader.getInstance().isDevelopmentEnvironment();
    }

    @Override
    public boolean isClientEnvironment() {
        return FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT;
    }

    @Override
    public Path getDefaultConfigPath() {
        return FabricLoader.getInstance().getGameDir().resolve(ForgeConfigPaths.INSTANCE.getDefaultConfigsDirectory());
    }

    @Override
    public void fireReloadingEvent(ModConfig modConfig) {
        ModConfigEvents.reloading(modConfig.getModId()).invoker().onModConfigReloading(modConfig);
    }

    @Override
    public <T extends WritableMessage> void registerClientboundMessage(Class<T> clazz, Supplier<NetworkingHelper.ClientMessageListener<T>> listener) {
        ResourceLocation channelName = this.registerChannelName(clazz);
        if (FabricLoader.getInstance().getEnvironmentType() != EnvType.CLIENT) return;
        Function<FriendlyByteBuf, T> factory = CommonAbstractions.findMessageConstructor(clazz);
        ClientPlayNetworking.registerGlobalReceiver(channelName, (Minecraft client, ClientPacketListener handler, FriendlyByteBuf buf, PacketSender responseSender) -> {
            client.execute(() -> {
                LocalPlayer player = client.player;
                Objects.requireNonNull(player, "player is null");
                listener.get().handle(factory.apply(buf), client, handler, player, client.level);
            });
        });
    }

    @Override
    public <T extends WritableMessage> void registerServerboundMessage(Class<T> clazz, Supplier<NetworkingHelper.ServerMessageListener<T>> listener) {
        ResourceLocation channelName = this.registerChannelName(clazz);
        Function<FriendlyByteBuf, T> factory = CommonAbstractions.findMessageConstructor(clazz);
        ServerPlayNetworking.registerGlobalReceiver(channelName, (MinecraftServer server, ServerPlayer player, ServerGamePacketListenerImpl handler, FriendlyByteBuf buf, PacketSender responseSender) -> {
            server.execute(() -> {
                listener.get().handle(factory.apply(buf), server, handler, player, player.serverLevel());
            });
        });
    }

    private <T extends WritableMessage> ResourceLocation registerChannelName(Class<T> clazz) {
        ResourceLocation channelName = ForgeConfigScreens.id("play/" + this.discriminator.getAndIncrement());
        this.messageRegistry.put(clazz, channelName);
        return channelName;
    }

    @Override
    public Packet<?> toClientboundPacket(WritableMessage message) {
        return this.toPacket(message, ServerPlayNetworking::createS2CPacket);
    }

    @Override
    public Packet<?> toServerboundPacket(WritableMessage message) {
        return this.toPacket(message, ClientPlayNetworking::createC2SPacket);
    }

    private Packet<?> toPacket(WritableMessage message, BiFunction<ResourceLocation, FriendlyByteBuf, Packet<?>> factory) {
        ResourceLocation identifier = this.messageRegistry.get(message.getClass());
        Objects.requireNonNull(identifier, "identifier for " + message.getClass() + " is null");
        return factory.apply(identifier, Util.make(PacketByteBufs.create(), message::write));
    }

    @Override
    public void registerConfig(String modId, ModConfig.Type type, IConfigSpec<?> spec) {
        ForgeConfigRegistry.INSTANCE.register(modId, type, spec);
    }

    @Override
    public void registerConfig(String modId, ModConfig.Type type, IConfigSpec<?> spec, String fileName) {
        ForgeConfigRegistry.INSTANCE.register(modId, type, spec, fileName);
    }
}
