package fuzs.forgeconfigscreens.core;

import com.electronwill.nightconfig.core.CommentedConfig;
import fuzs.forgeconfigscreens.ForgeConfigScreens;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraftforge.fml.config.IConfigSpec;
import net.minecraftforge.fml.config.ModConfig;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

public interface CommonAbstractions {
    CommonAbstractions INSTANCE = ServiceProviderHelper.load(CommonAbstractions.class);

    Optional<String> getModDisplayName(String modId);

    boolean isDevelopmentEnvironment();

    boolean isClientEnvironment();

    Path getDefaultConfigPath();

    void fireReloadingEvent(ModConfig modConfig);

    default void setConfigData(ModConfig modConfig, CommentedConfig data) {
        try {
            Method setConfigData = ModConfig.class.getDeclaredMethod("setConfigData", CommentedConfig.class);
            setConfigData.setAccessible(true);
            MethodHandles.lookup().unreflect(setConfigData).invoke(modConfig, data);
        } catch (Throwable e) {
            ForgeConfigScreens.LOGGER.error("Unable to set config data for {}", modConfig.getFileName(), e);
        }
    }

    <T extends WritableMessage> void registerClientboundMessage(Class<T> clazz, Supplier<NetworkingHelper.ClientMessageListener<T>> listener);

    <T extends WritableMessage> void registerServerboundMessage(Class<T> clazz, Supplier<NetworkingHelper.ServerMessageListener<T>> listener);

    static <T extends WritableMessage> Function<FriendlyByteBuf, T> findMessageConstructor(Class<T> clazz) {
        try {
            return findMessageConstructor(clazz, MethodType.methodType(void.class, FriendlyByteBuf.class));
        } catch (Throwable e) {
            try {
                return findMessageConstructor(clazz, MethodType.methodType(void.class));
            } catch (Throwable ignored) {
                throw new RuntimeException(e);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static <T extends WritableMessage> Function<FriendlyByteBuf, T> findMessageConstructor(Class<T> clazz, MethodType methodType) throws ReflectiveOperationException {
        // do this here, so we crash upon registering when the correct constructor is missing and not when the message is sent for the first time
        MethodHandle constructor = MethodHandles.publicLookup().findConstructor(clazz, methodType);
        return friendlyByteBuf -> {
            try {
                return (T) constructor.invoke(friendlyByteBuf);
            } catch (Throwable e) {
                try {
                    return (T) constructor.invoke();
                } catch (Throwable ignored) {
                    throw new RuntimeException(e);
                }
            }
        };
    }

    Packet<?> toClientboundPacket(WritableMessage message);

    Packet<?> toServerboundPacket(WritableMessage message);

    void registerConfig(String modId, ModConfig.Type type, IConfigSpec<?> spec);

    void registerConfig(String modId, ModConfig.Type type, IConfigSpec<?> spec, String fileName);
}
