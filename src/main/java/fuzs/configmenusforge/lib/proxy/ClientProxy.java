package fuzs.configmenusforge.lib.proxy;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;

/**
 * client proxy class
 */
public class ClientProxy extends ServerProxy {

    @Override
    public Player getClientPlayer() {
        return Minecraft.getInstance().player;
    }

    @Override
    public Object getClientInstance() {
        return Minecraft.getInstance();
    }
}
