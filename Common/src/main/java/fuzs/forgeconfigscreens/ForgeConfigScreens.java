package fuzs.forgeconfigscreens;

import fuzs.forgeconfigscreens.config.SimpleConfig;
import fuzs.forgeconfigscreens.core.CommonAbstractions;
import fuzs.forgeconfigscreens.network.S2CGrantPermissionsMessage;
import fuzs.forgeconfigscreens.network.S2CUpdateConfigMessage;
import fuzs.forgeconfigscreens.network.ServerMessageHandles;
import fuzs.forgeconfigscreens.network.client.C2SAskPermissionsMessage;
import fuzs.forgeconfigscreens.network.client.C2SSendConfigMessage;
import fuzs.forgeconfigscreens.network.client.ClientMessageHandles;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.config.ModConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class ForgeConfigScreens {
    public static final String MOD_ID = "forgeconfigscreens";
    public static final String MOD_NAME = "Forge Config Screens";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);

    public static void onConstructMod() {
        registerMessages();
        registerConfigs();
    }

    private static void registerMessages() {
        CommonAbstractions.INSTANCE.registerClientboundMessage(S2CGrantPermissionsMessage.class, () -> ClientMessageHandles::handleGrantPermissions);
        CommonAbstractions.INSTANCE.registerClientboundMessage(S2CUpdateConfigMessage.class, () -> ClientMessageHandles::handleUpdateConfig);
        CommonAbstractions.INSTANCE.registerServerboundMessage(C2SAskPermissionsMessage.class, () -> ServerMessageHandles::handleAskPermissions);
        CommonAbstractions.INSTANCE.registerServerboundMessage(C2SSendConfigMessage.class, () -> ServerMessageHandles::handleSendConfig);
    }

    private static void registerConfigs() {
        if (!CommonAbstractions.INSTANCE.isDevelopmentEnvironment()) return;
        CommonAbstractions.INSTANCE.registerConfig(MOD_ID, ModConfig.Type.CLIENT, SimpleConfig.CLIENT_SPEC, String.format("%s%s%s-%s.toml", ForgeConfigScreens.MOD_ID, File.separator, ForgeConfigScreens.MOD_ID, ModConfig.Type.CLIENT.extension()));
        CommonAbstractions.INSTANCE.registerConfig(MOD_ID, ModConfig.Type.COMMON, SimpleConfig.COMMON_SPEC, String.format("%s-%s.toml", ForgeConfigScreens.MOD_ID, ModConfig.Type.COMMON.extension()));
        CommonAbstractions.INSTANCE.registerConfig(MOD_ID, ModConfig.Type.SERVER, SimpleConfig.SERVER_SPEC, String.format("%s-%s.toml", ForgeConfigScreens.MOD_ID, ModConfig.Type.SERVER.extension()));
    }

    public static ResourceLocation id(String path) {
        return new ResourceLocation(MOD_ID, path);
    }
}
