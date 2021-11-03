package fuzs.configmenusforge.client.handler;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.ExtensionPoint;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.moddiscovery.ModInfo;

import java.util.Optional;

public class ConfigFactoryHandler {

    public static void registerConfigFactories() {
        ModList.get().getMods().stream().map(ModInfo::getModId).map(ModList.get()::getModContainerById).filter(Optional::isPresent).map(Optional::get).forEach(container -> {
            if (container.getCustomExtension(ExtensionPoint.CONFIGGUIFACTORY).isPresent()) return;
            final ResourceLocation background = Optional.ofNullable(container.getModInfo() instanceof ModInfo ? (ModInfo) container.getModInfo() : null).flatMap(ConfigFactoryHandler::getCustomBackground).orElse(Screen.BACKGROUND_LOCATION);
            ConfigScreenFactory.createConfigScreen(container.getModId(), container.getModInfo().getDisplayName(), background).ifPresent(configScreen -> {
                container.registerExtensionPoint(ExtensionPoint.CONFIGGUIFACTORY, () -> (mc, screen) -> configScreen.apply(screen));
            });
        });
    }

    private static ResourceLocation getBackgroundLocation(ModContainer container) {
        if (container.getModInfo() instanceof ModInfo) {
            String background = (String) container.getModInfo().getModProperties().get("configuredBackground");
            if (background != null) {
                return new ResourceLocation(background);
            }
            // Fallback to old method to getting config background (since mods might not have updated)
            Optional<String> optional = ((ModInfo) container.getModInfo()).getConfigElement("configBackground");
            if (optional.isPresent()) {
                return new ResourceLocation(optional.get());
            }
        }
        return Screen.BACKGROUND_LOCATION;
    }

    private static Optional<ResourceLocation> getCustomBackground(ModInfo modInfo) {
        // these are options added by Configured mod, as the mod is quite popular we use them as well
        final Optional<ResourceLocation> configuredBackground = Optional.ofNullable((String) modInfo.getModProperties().get("configuredBackground")).map(ResourceLocation::new);
        if (configuredBackground.isPresent()) return configuredBackground;
        // fallback to old Configured method to getting config background
        return modInfo.<String>getConfigElement("configBackground").map(ResourceLocation::new);
    }
}
