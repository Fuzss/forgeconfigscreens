package fuzs.forgeconfigscreens.lib.proxy;

import fuzs.forgeconfigscreens.lib.core.EnvTypeExecutor;
import fuzs.forgeconfigscreens.lib.network.message.Message;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;

import java.util.function.Function;

/**
 * proxy base class
 */
public interface IProxy {
    /**
     * sided proxy depending on physical side
     */
    @SuppressWarnings("Convert2MethodRef")
    IProxy INSTANCE = EnvTypeExecutor.runForEnvType(() -> () -> new ClientProxy(), () -> () -> new ServerProxy());

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

    /**
     * only used by {@link fuzs.forgeconfigscreens.lib.network.NetworkHandler}
     * @param channelName channel name
     * @param factory message factory when received
     */
    void registerClientReceiver(ResourceLocation channelName, Function<FriendlyByteBuf, Message> factory);

    /**
     * only used by {@link fuzs.forgeconfigscreens.lib.network.NetworkHandler}
     * @param channelName channel name
     * @param factory message factory when received
     */
    void registerServerReceiver(ResourceLocation channelName, Function<FriendlyByteBuf, Message> factory);
}
