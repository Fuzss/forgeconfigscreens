package fuzs.configmenusforge.client.util;

import com.electronwill.nightconfig.toml.TomlFormat;
import net.minecraftforge.fml.config.ModConfig;

import java.io.ByteArrayInputStream;

public class ModConfigSync {

    public static void fireReloadEvent(ModConfig config) {
        ReflectionHelper.invoke(ReflectionHelper.FIRE_EVENT_METHOD, config, ReflectionHelper.newInstance(ReflectionHelper.MOD_CONFIG_RELOADING_CONSTRUCTOR, config));
    }

    public static void acceptSyncedConfig(ModConfig config, byte[] bytes) {
        ReflectionHelper.invoke(ReflectionHelper.SET_CONFIG_DATA_METHOD, config, TomlFormat.instance().createParser().parse(new ByteArrayInputStream(bytes)));
        fireReloadEvent(config);
    }
}
