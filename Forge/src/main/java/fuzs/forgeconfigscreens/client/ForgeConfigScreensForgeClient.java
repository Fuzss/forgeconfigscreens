package fuzs.forgeconfigscreens.client;

import fuzs.forgeconfigscreens.ForgeConfigScreens;
import fuzs.forgeconfigscreens.client.handler.ConfigFactoryHandler;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = ForgeConfigScreens.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ForgeConfigScreensForgeClient {

    @SubscribeEvent
    public static void onClientSetup(final FMLClientSetupEvent evt) {
        // registering this early is alright; mods that have already registered their own screen won't be overwritten, mods that register later will just overwrite us
        ConfigFactoryHandler.registerConfigFactories();
        // uhm why not
        ConfigFactoryHandler.registerMinecraftConfig();
    }
}