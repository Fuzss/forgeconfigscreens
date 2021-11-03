package fuzs.configmenusforge.client;

import fuzs.configmenusforge.ConfigMenusForge;
import fuzs.configmenusforge.client.handler.ConfigFactoryHandler;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = ConfigMenusForge.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ConfigMenusForgeClient {

    @SubscribeEvent
    public static void onClientSetup(final FMLClientSetupEvent evt) {
        // registering this early is alright; mods that have already registered their own screen won't be overwritten, mods that register later will just overwrite us
        ConfigFactoryHandler.registerConfigFactories();
    }
}