package fuzs.configmenusforge.lib.core;

import javax.annotation.Nullable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Optional;

@SuppressWarnings("unchecked")
public class ReflectionHelper {
    public static Field getDeclaredField(Class<?> clazz, String name) {
        try {
            Field field = clazz.getDeclaredField(name);
            field.setAccessible(true);
            return field;
        } catch (NoSuchFieldException ignored) {
        }
        return null;
    }

    public static Method getDeclaredMethod(Class<?> clazz, String name, Class<?>... parameterTypes) {
        try {
            Method method = clazz.getDeclaredMethod(name, parameterTypes);
            method.setAccessible(true);
            return method;
        } catch (NoSuchMethodException ignored) {
        }
        return null;
    }

    public static <T> Constructor<T> getDeclaredConstructor(Class<?> clazz, Class<?>... parameterTypes) {
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
