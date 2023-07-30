package fuzs.forgeconfigscreens.client.helper;

import com.electronwill.nightconfig.core.utils.UnmodifiableConfigWrapper;
import com.electronwill.nightconfig.toml.TomlFormat;
import fuzs.forgeconfigscreens.core.CommonAbstractions;
import fuzs.forgeconfigscreens.core.NetworkingHelper;
import fuzs.forgeconfigscreens.network.client.C2SSendConfigMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.config.ModConfig;

import java.io.ByteArrayOutputStream;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.util.Optional;

public class ServerConfigUploader {
    private static final MethodHandle UNMODIFIABLE_CONFIG_WRAPPER_CONFIG_GETTER;

    static {
        try {
            Field field = UnmodifiableConfigWrapper.class.getDeclaredField("config");
            field.setAccessible(true);
            UNMODIFIABLE_CONFIG_WRAPPER_CONFIG_GETTER = MethodHandles.lookup().unreflectGetter(field);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    public static void saveAndUpload(ModConfig config) {
        findForgeConfigSpec(config.getSpec()).ifPresent(ForgeConfigSpec::save);
        CommonAbstractions.INSTANCE.fireReloadingEvent(config);
        if (config.getType() == ModConfig.Type.SERVER) {
            Minecraft minecraft = Minecraft.getInstance();
            ClientPacketListener connection = minecraft.getConnection();
            if (connection != null && !minecraft.isLocalServer()) {
                final ByteArrayOutputStream stream = new ByteArrayOutputStream();
                TomlFormat.instance().createWriter().write(config.getConfigData(), stream);
                NetworkingHelper.sendToServer(connection.getConnection(), new C2SSendConfigMessage(config.getFileName(), stream.toByteArray()));
            }
        }
    }

    public static Optional<ForgeConfigSpec> findForgeConfigSpec(Object o) {
        while (!(o instanceof ForgeConfigSpec) && o instanceof UnmodifiableConfigWrapper<?>) {
            try {
                o = UNMODIFIABLE_CONFIG_WRAPPER_CONFIG_GETTER.invoke(o);
            } catch (Throwable ignored) {

            }
        }
        return o instanceof ForgeConfigSpec spec ? Optional.of(spec) : Optional.empty();
    }
}
