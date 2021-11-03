package fuzs.configmenusforge.client.handler;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.OptionsScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.ExtensionPoint;
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

    public static void registerMinecraftConfig() {
        ModList.get().getModContainerById("minecraft").ifPresent(container -> {
            container.registerExtensionPoint(ExtensionPoint.CONFIGGUIFACTORY, () -> (mc, screen) -> new OptionsScreen(screen, Minecraft.getInstance().options));
        });
    }

    private static Optional<ResourceLocation> getCustomBackground(ModInfo modInfo) {
        // these are options added by Configured mod, as the mod is quite popular we use them as well
        final Optional<ResourceLocation> configuredBackground = Optional.ofNullable((String) modInfo.getModProperties().get("configuredBackground")).map(ResourceLocation::new);
        if (configuredBackground.isPresent()) return configuredBackground;
        // fallback to old Configured method to getting config background
        return modInfo.<String>getConfigElement("configBackground").map(ResourceLocation::new);
    }
}
