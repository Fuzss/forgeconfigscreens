package fuzs.configmenusforge.client.handler;

import fuzs.configmenusforge.client.gui.screens.SelectConfigScreen;
import fuzs.configmenusforge.client.gui.util.ScreenUtil;
import fuzs.configmenusforge.client.util.ReflectionHelper;
import fuzs.configmenusforge.lib.core.ModLoaderEnvironment;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.config.ConfigTracker;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.forgespi.language.IModInfo;

import java.util.EnumMap;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ConfigScreenFactory {

    public static Optional<Function<Screen, Screen>> createConfigScreen(final String modId) {
        return createConfigScreen(modId, AbstractGui.BACKGROUND_LOCATION);
    }

    public static Optional<Function<Screen, Screen>> createConfigScreen(final String modId, ResourceLocation optionsBackground) {
        return createConfigScreen(modId, ModLoaderEnvironment.getModContainer(modId).map(ModContainer::getModInfo).map(IModInfo::getDisplayName).orElse(ScreenUtil.formatText(modId)), optionsBackground);
    }

    public static Optional<Function<Screen, Screen>> createConfigScreen(final String modId, String displayName) {
        return createConfigScreen(modId, displayName, AbstractGui.BACKGROUND_LOCATION);
    }

    public static Optional<Function<Screen, Screen>> createConfigScreen(final String modId, String displayName, ResourceLocation optionsBackground) {
        final Optional<Set<ModConfig>> configs = ReflectionHelper.<EnumMap<ModConfig.Type, Set<ModConfig>>>get(ReflectionHelper.CONFIG_SETS_FIELD, ConfigTracker.INSTANCE).map(configSets -> configSets.values().stream()
                .flatMap(Set::stream)
                .filter(config -> config.getModId().equals(modId))
                .collect(Collectors.toSet()));
        if (configs.isPresent() && !configs.get().isEmpty()) {
            return Optional.of(lastScreen -> new SelectConfigScreen(lastScreen, new StringTextComponent(displayName), optionsBackground, configs.get()));
        }
        return Optional.empty();
    }
}
