package fuzs.forgeconfigscreens.lib;

import net.fabricmc.api.ModInitializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * utility class for common helper methods
 * also main puzzles lib mod, only really need, so it shows in the mods list
 */
public class PuzzlesLib implements ModInitializer {
    public static final String MOD_ID = "puzzleslib";
    public static final String MOD_NAME = "Puzzles Lib";
    public static final Logger LOGGER = LogManager.getLogger(PuzzlesLib.MOD_NAME);

    @Override
    public void onInitialize() {

    }
}
