package fuzs.forgeconfigscreens.client.util;

import com.electronwill.nightconfig.core.CommentedConfig;
import fuzs.forgeconfigscreens.ForgeConfigScreens;
import net.minecraftforge.fml.config.IConfigEvent;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;

public class ModConfigAccessor {

    public static void fireReloadingEvent(ModConfig modConfig) {
        try {
            Method fireEvent = ModConfig.class.getDeclaredMethod("fireEvent", IConfigEvent.class);
            fireEvent.setAccessible(true);
            MethodHandles.lookup().unreflect(fireEvent).invoke(modConfig, new ModConfigEvent.Reloading(modConfig));
        } catch (Throwable e) {
            ForgeConfigScreens.LOGGER.error("Unable to fire config reloading event for {}", modConfig.getFileName(), e);
        }
    }

    public static void setConfigData(ModConfig modConfig, CommentedConfig data) {
        try {
            Method setConfigData = ModConfig.class.getDeclaredMethod("setConfigData", CommentedConfig.class);
            setConfigData.setAccessible(true);
            MethodHandles.lookup().unreflect(setConfigData).invoke(modConfig, data);
        } catch (Throwable e) {
            ForgeConfigScreens.LOGGER.error("Unable to set config data for {}", modConfig.getFileName(), e);
        }
    }
}
