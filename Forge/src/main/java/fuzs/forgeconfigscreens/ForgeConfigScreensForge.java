package fuzs.forgeconfigscreens;

import fuzs.forgeconfigscreens.config.TestConfig;
import fuzs.forgeconfigscreens.lib.network.NetworkHandler;
import fuzs.forgeconfigscreens.network.client.message.C2SAskPermissionsMessage;
import fuzs.forgeconfigscreens.network.client.message.C2SSendConfigMessage;
import fuzs.forgeconfigscreens.network.message.S2CGrantPermissionsMessage;
import fuzs.forgeconfigscreens.network.message.S2CUpdateConfigMessage;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.IExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLConstructModEvent;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.network.NetworkConstants;
import net.minecraftforge.network.NetworkDirection;

import java.io.File;

@Mod(ForgeConfigScreens.MOD_ID)
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class ForgeConfigScreensForge {
    public static final NetworkHandler NETWORK = NetworkHandler.of(ForgeConfigScreens.MOD_ID, true, false);

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
        if (!FMLLoader.isProduction()) {
            ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, TestConfig.CLIENT_SPEC, String.format("%s%s%s-%s.toml", ForgeConfigScreens.MOD_ID, File.separator, ForgeConfigScreens.MOD_ID, ModConfig.Type.CLIENT.extension()));
            ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, TestConfig.COMMON_SPEC, String.format("%s-%s.toml", ForgeConfigScreens.MOD_ID, ModConfig.Type.COMMON.extension()));
            ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, TestConfig.SERVER_SPEC, String.format("%s-%s.toml", ForgeConfigScreens.MOD_ID, ModConfig.Type.SERVER.extension()));
        }
    }
}
