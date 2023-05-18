package fuzs.forgeconfigscreens.client.util;

import com.electronwill.nightconfig.core.CommentedConfig;
import fuzs.forgeconfigscreens.lib.core.ReflectionHelper;
import net.minecraftforge.fml.config.IConfigEvent;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;

import java.lang.reflect.Method;

public class ModConfigSync {
    private static final Method FIRE_EVENT_METHOD = ReflectionHelper.getDeclaredMethod(ModConfig.class, "fireEvent", IConfigEvent.class);
    private static final Method SET_CONFIG_DATA_METHOD = ReflectionHelper.getDeclaredMethod(ModConfig.class, "setConfigData", CommentedConfig.class);

    public static void fireReloadingEvent(ModConfig config) {
        ReflectionHelper.invoke(FIRE_EVENT_METHOD, config, new ModConfigEvent.Reloading(config));
    }

    public static void setConfigData(ModConfig config, CommentedConfig configData) {
        ReflectionHelper.invoke(SET_CONFIG_DATA_METHOD, config, configData);
    }
}
