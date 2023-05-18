package fuzs.forgeconfigscreens.client.handler;

import fuzs.forgeconfigscreens.client.gui.screens.SelectConfigScreen;
import fuzs.forgeconfigscreens.client.gui.util.ScreenUtil;
import fuzs.forgeconfigscreens.lib.core.ModLoaderEnvironment;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.config.ConfigTracker;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.forgespi.language.IModInfo;

import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ConfigScreenFactory {

    public static Optional<Function<Screen, Screen>> createConfigScreen(final String modId) {
        return createConfigScreen(modId, GuiComponent.BACKGROUND_LOCATION);
    }

    public static Optional<Function<Screen, Screen>> createConfigScreen(final String modId, ResourceLocation optionsBackground) {
        return createConfigScreen(modId, ModLoaderEnvironment.getModContainer(modId).map(ModContainer::getModInfo).map(IModInfo::getDisplayName).orElse(ScreenUtil.toFormattedString(modId)), optionsBackground);
    }

    public static Optional<Function<Screen, Screen>> createConfigScreen(final String modId, String displayName) {
        return createConfigScreen(modId, displayName, GuiComponent.BACKGROUND_LOCATION);
    }

    public static Optional<Function<Screen, Screen>> createConfigScreen(final String modId, String displayName, ResourceLocation optionsBackground) {
        final Set<ModConfig> configs = ConfigTracker.INSTANCE.configSets().values().stream()
                .flatMap(Set::stream)
                .filter(config -> config.getModId().equals(modId))
                .collect(Collectors.toSet());
        if (!configs.isEmpty()) {
            return Optional.of(lastScreen -> new SelectConfigScreen(lastScreen, Component.literal(displayName), optionsBackground, configs));
        }
        return Optional.empty();
    }

}
