package fuzs.configmenusforge.client.util;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.toml.TomlFormat;
import net.minecraftforge.fml.config.ConfigTracker;
import net.minecraftforge.fml.config.ModConfig;

import java.io.ByteArrayInputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class ModConfigSync {
    public static final Field FILE_MAP_FIELD = ReflectionHelper.getDeclaredField(ConfigTracker.class, "fileMap");
    public static final Field CONFIG_SETS_FIELD = ReflectionHelper.getDeclaredField(ConfigTracker.class, "configSets");
    public static final Method SET_CONFIG_DATA_METHOD = ReflectionHelper.getDeclaredMethod(ModConfig.class, "setConfigData", CommentedConfig.class);
    public static final Method FIRE_EVENT_METHOD = ReflectionHelper.getDeclaredMethod(ModConfig.class, "fireEvent", ModConfig.ModConfigEvent.class);
    public static final Constructor<ModConfig.Reloading> MOD_CONFIG_RELOADING_CONSTRUCTOR = ReflectionHelper.getDeclaredConstructor(ModConfig.Reloading.class, ModConfig.class);

    public static void fireReloadingEvent(ModConfig config) {
        ReflectionHelper.newInstance(MOD_CONFIG_RELOADING_CONSTRUCTOR, config).ifPresent(evt -> ReflectionHelper.invoke(FIRE_EVENT_METHOD, config, evt));
    }

    public static void acceptSyncedConfig(ModConfig config, byte[] bytes) {
        setConfigData(config, TomlFormat.instance().createParser().parse(new ByteArrayInputStream(bytes)));
        fireReloadingEvent(config);
    }

    public static void setConfigData(ModConfig config, CommentedConfig configData) {
        ReflectionHelper.invoke(SET_CONFIG_DATA_METHOD, config, configData);
    }
}
