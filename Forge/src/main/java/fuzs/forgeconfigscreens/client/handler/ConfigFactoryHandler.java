package fuzs.forgeconfigscreens.client.handler;

import net.minecraft.client.gui.screens.OptionsScreen;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.forgespi.language.IModInfo;

import java.util.Optional;

public class ConfigFactoryHandler {

    public static void registerConfigFactories() {
        ModList.get().getMods().stream().map(IModInfo::getModId).map(ModList.get()::getModContainerById).filter(Optional::isPresent).map(Optional::get).forEach(container -> {
            if (container.getCustomExtension(ConfigScreenHandler.ConfigScreenFactory.class).isPresent()) return;
            ConfigScreenFactory.createConfigScreen(container.getModId(), container.getModInfo().getDisplayName()).ifPresent(configScreen -> {
                container.registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class, () -> new ConfigScreenHandler.ConfigScreenFactory((mc, screen) -> configScreen.apply(screen)));
            });
        });
    }

    public static void registerMinecraftConfig() {
        ModList.get().getModContainerById("minecraft").ifPresent(container -> {
            container.registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class, () -> new ConfigScreenHandler.ConfigScreenFactory((mc, screen) -> new OptionsScreen(screen, mc.options)));
        });
    }
}
