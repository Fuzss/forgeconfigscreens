package fuzs.configmenusforge.client.util;

import fuzs.configmenusforge.lib.core.ReflectionHelper;
import net.minecraftforge.fml.config.IConfigEvent;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;

import java.lang.reflect.Method;

public class ModConfigSync {
    private static final Method FIRE_EVENT_METHOD = ReflectionHelper.getDeclaredMethod(ModConfig.class, "fireEvent", IConfigEvent.class);

    public static void fireReloadingEvent(ModConfig config) {
        ReflectionHelper.invoke(FIRE_EVENT_METHOD, config, new ModConfigEvent.Reloading(config));
    }
}
