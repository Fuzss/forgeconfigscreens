package fuzs.configmenusforge.client.util;

import com.electronwill.nightconfig.core.CommentedConfig;
import net.minecraftforge.fml.config.ConfigTracker;
import net.minecraftforge.fml.config.ModConfig;

import javax.annotation.Nullable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Optional;

@SuppressWarnings({"unchecked", "SameParameterValue"})
public class ReflectionHelper {
    public static final Field FILE_MAP_FIELD = getDeclaredField(ConfigTracker.class, "fileMap");
    public static final Field CONFIG_SETS_FIELD = getDeclaredField(ConfigTracker.class, "configSets");
    public static final Method SET_CONFIG_DATA_METHOD = getDeclaredMethod(ModConfig.class, "setConfigData", CommentedConfig.class);
    public static final Method FIRE_EVENT_METHOD = getDeclaredMethod(ModConfig.class, "fireEvent", ModConfig.ModConfigEvent.class);
    public static final Constructor<ModConfig.Reloading> MOD_CONFIG_RELOADING_CONSTRUCTOR = getDeclaredConstructor(ModConfig.Reloading.class, ModConfig.class);

    private static Field getDeclaredField(Class<?> clazz, String name) {
        try {
            Field field = clazz.getDeclaredField(name);
            field.setAccessible(true);
            return field;
        } catch (NoSuchFieldException ignored) {
        }
        return null;
    }

    private static Method getDeclaredMethod(Class<?> clazz, String name, Class<?>... parameterTypes) {
        try {
            Method method = clazz.getDeclaredMethod(name, parameterTypes);
            method.setAccessible(true);
            return method;
        } catch (NoSuchMethodException ignored) {
        }
        return null;
    }

    private static <T> Constructor<T> getDeclaredConstructor(Class<?> clazz, Class<?>... parameterTypes) {
        try {
            Constructor<T> constructor = (Constructor<T>) clazz.getDeclaredConstructor(parameterTypes);
            constructor.setAccessible(true);
            return constructor;
        } catch (NoSuchMethodException ignored) {
        }
        return null;
    }

    public static <T> Optional<T> get(@Nullable Field field, Object instance) {
        if (field != null) {
            try {
                return Optional.of((T) field.get(instance));
            } catch (IllegalAccessException ignored) {
            }
        }
        return Optional.empty();
    }

    public static <T> Optional<T> invoke(@Nullable Method method, Object instance, Object... args) {
        if (method != null) {
            try {
                return Optional.ofNullable((T) method.invoke(instance, args));
            } catch (InvocationTargetException | IllegalAccessException ignored) {
            }
        }
        return Optional.empty();
    }

    public static <T> Optional<T> newInstance(@Nullable Constructor<T> constructor, Object... args) {
        if (constructor != null) {
            try {
                return Optional.of(constructor.newInstance(args));
            } catch (InvocationTargetException | IllegalAccessException | InstantiationException ignored) {
            }
        }
        return Optional.empty();
    }
}
