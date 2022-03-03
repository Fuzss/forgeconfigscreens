package fuzs.configmenusforge;

import fuzs.configmenusforge.config.TestConfig;
import fuzs.configmenusforge.lib.core.ModLoaderEnvironment;
import fuzs.configmenusforge.lib.network.NetworkHandler;
import fuzs.configmenusforge.network.client.message.C2SAskPermissionsMessage;
import fuzs.configmenusforge.network.client.message.C2SSendConfigMessage;
import fuzs.configmenusforge.network.message.S2CGrantPermissionsMessage;
import fuzs.configmenusforge.network.message.S2CUpdateConfigMessage;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.IExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLConstructModEvent;
import net.minecraftforge.fml.loading.FileUtils;
import net.minecraftforge.network.NetworkConstants;
import net.minecraftforge.network.NetworkDirection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

@Mod(ConfigMenusForge.MOD_ID)
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class ConfigMenusForge {

    public static final String MOD_ID = "configmenusforge";
    public static final String MOD_NAME = "Config Menus for Forge";
    public static final String MOD_URL = "https://www.curseforge.com/minecraft/mc-mods/config-menus-forge";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);

    public static final NetworkHandler NETWORK = NetworkHandler.of(MOD_ID, true, false);

    @SubscribeEvent
    public static void onConstructMod(final FMLConstructModEvent evt) {
        ModLoadingContext.get().registerExtensionPoint(IExtensionPoint.DisplayTest.class, () -> new IExtensionPoint.DisplayTest(() -> NetworkConstants.IGNORESERVERONLY, (remote, server) -> true));
        registerMessages();
        addTestConfigs();
    }

    private static void registerMessages() {
        NETWORK.register(C2SAskPermissionsMessage.class, C2SAskPermissionsMessage::new, NetworkDirection.PLAY_TO_SERVER);
        NETWORK.register(S2CGrantPermissionsMessage.class, S2CGrantPermissionsMessage::new, NetworkDirection.PLAY_TO_CLIENT);
        NETWORK.register(C2SSendConfigMessage.class, C2SSendConfigMessage::new, NetworkDirection.PLAY_TO_SERVER);
        NETWORK.register(S2CUpdateConfigMessage.class, S2CUpdateConfigMessage::new, NetworkDirection.PLAY_TO_CLIENT);
    }

    private static void addTestConfigs() {
        if (ModLoaderEnvironment.isDevelopmentEnvironment()) {
            FileUtils.getOrCreateDirectory(ModLoaderEnvironment.getConfigDir().resolve(MOD_ID), String.format("%s config directory", MOD_ID));
            ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, TestConfig.CLIENT_SPEC, String.format("%s%s%s-%s.toml", MOD_ID, File.separator, MOD_ID, ModConfig.Type.CLIENT.extension()));
            ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, TestConfig.COMMON_SPEC, String.format("%s-%s.toml", MOD_ID, ModConfig.Type.COMMON.extension()));
            ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, TestConfig.SERVER_SPEC, String.format("%s-%s.toml", MOD_ID, ModConfig.Type.SERVER.extension()));
        }
    }
}
