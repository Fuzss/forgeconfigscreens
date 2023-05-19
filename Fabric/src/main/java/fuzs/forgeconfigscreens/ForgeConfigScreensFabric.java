package fuzs.forgeconfigscreens;

import net.fabricmc.api.ModInitializer;

public class ForgeConfigScreensFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        ForgeConfigScreens.onConstructMod();
    }
}
