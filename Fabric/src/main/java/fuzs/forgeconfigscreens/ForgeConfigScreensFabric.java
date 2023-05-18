package fuzs.forgeconfigscreens;

import fuzs.forgeconfigscreens.config.TestConfig;
import fuzs.forgeconfigscreens.lib.core.ModLoaderEnvironment;
import fuzs.forgeconfigscreens.lib.network.MessageDirection;
import fuzs.forgeconfigscreens.lib.network.NetworkHandler;
import fuzs.forgeconfigscreens.network.client.message.C2SAskPermissionsMessage;
import fuzs.forgeconfigscreens.network.client.message.C2SSendConfigMessage;
import fuzs.forgeconfigscreens.network.message.S2CGrantPermissionsMessage;
import fuzs.forgeconfigscreens.network.message.S2CUpdateConfigMessage;
import fuzs.forgeconfigapiport.api.config.v2.ForgeConfigRegistry;
import net.fabricmc.api.ModInitializer;
import net.minecraftforge.fml.config.ModConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;

public class ForgeConfigScreensFabric implements ModInitializer {
    public static final String MOD_ID = "forgeconfigscreens";
    public static final String MOD_NAME = "Forge Config Screens";
    public static final String MOD_URL = "https://www.curseforge.com/minecraft/mc-mods/config-menus-forge";
    public static final Logger LOGGER = LogManager.getLogger(ForgeConfigScreensFabric.MOD_NAME);

    public static final NetworkHandler NETWORK = NetworkHandler.of(MOD_ID);

    @Override
    public void onInitialize() {
        this.registerMessages();
        this.initTestConfigs();
    }

    private void registerMessages() {
        NETWORK.register(C2SAskPermissionsMessage.class, C2SAskPermissionsMessage::new, MessageDirection.TO_SERVER);
        NETWORK.register(S2CGrantPermissionsMessage.class, S2CGrantPermissionsMessage::new, MessageDirection.TO_CLIENT);
        NETWORK.register(C2SSendConfigMessage.class, C2SSendConfigMessage::new, MessageDirection.TO_SERVER);
        NETWORK.register(S2CUpdateConfigMessage.class, S2CUpdateConfigMessage::new, MessageDirection.TO_CLIENT);
    }

    private void initTestConfigs() {
        if (ModLoaderEnvironment.isDevelopmentEnvironment()) {
            ForgeConfigRegistry.INSTANCE.register(MOD_ID, ModConfig.Type.CLIENT, TestConfig.CLIENT_SPEC, String.format("%s%s%s-%s.toml", MOD_ID, File.separator, MOD_ID, ModConfig.Type.CLIENT.extension()));
            ForgeConfigRegistry.INSTANCE.register(MOD_ID, ModConfig.Type.COMMON, TestConfig.COMMON_SPEC, String.format("%s-%s.toml", MOD_ID, ModConfig.Type.COMMON.extension()));
            ForgeConfigRegistry.INSTANCE.register(MOD_ID, ModConfig.Type.SERVER, TestConfig.SERVER_SPEC, String.format("%s-%s.toml", MOD_ID, ModConfig.Type.SERVER.extension()));
        }
    }
}
