package fuzs.configmenusforge.lib.proxy;

import fuzs.configmenusforge.lib.core.EnvTypeExecutor;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;

/**
 * proxy base class
 */
public interface IProxy {
    /**
     * sided proxy depending on physical side
     */
    @SuppressWarnings("Convert2MethodRef")
    IProxy INSTANCE = EnvTypeExecutor.runForDist(() -> () -> new ClientProxy(), () -> () -> new ServerProxy());

    /**
     * @return client player from Minecraft singleton when on physical client, otherwise null
     */
    Player getClientPlayer();

    /**
     * @return Minecraft singleton instance on physical client, otherwise null
     */
    Object getClientInstance();

    /**
     * @return current game server, null when not in a world
     */
    MinecraftServer getGameServer();
}
