package fuzs.forgeconfigscreens;

import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ForgeConfigScreens {
    public static final String MOD_ID = "forgeconfigscreens";
    public static final String MOD_NAME = "Forge Config Screens";
    public static final String MOD_URL = "https://www.curseforge.com/minecraft/mc-mods/config-menus-forge";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);

    public static ResourceLocation id(String path) {
        return new ResourceLocation(MOD_ID, path);
    }
}
