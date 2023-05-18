package fuzs.forgeconfigscreens.lib.network;

import net.fabricmc.api.EnvType;

/**
 * just like Forge's NetworkDirection, only for registering messages
 * we use this because the naming is very intuitive, and it stores some useful information
 */
public enum MessageDirection {
    TO_CLIENT(EnvType.CLIENT),
    TO_SERVER(EnvType.SERVER);

    /**
     * side this direction is received on
     */
    private final EnvType receptionSide;

    /**
     * @param receptionSide side this direction is received on
     */
    MessageDirection(EnvType receptionSide) {
        this.receptionSide = receptionSide;
    }

    /**
     * @return side this direction is received on
     */
    public EnvType getReceptionSide() {
        return this.receptionSide;
    }
}
