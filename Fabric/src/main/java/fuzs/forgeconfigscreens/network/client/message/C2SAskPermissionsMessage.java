package fuzs.forgeconfigscreens.network.client.message;

import fuzs.forgeconfigscreens.ForgeConfigScreensFabric;
import fuzs.forgeconfigscreens.lib.network.message.Message;
import fuzs.forgeconfigscreens.network.message.S2CGrantPermissionsMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public class C2SAskPermissionsMessage implements Message {

    public C2SAskPermissionsMessage() {
    }

    @Override
    public void write(FriendlyByteBuf buf) {
    }

    @Override
    public void read(FriendlyByteBuf buf) {
    }

    @Override
    public AskPermissionsHandler makeHandler() {
        return new AskPermissionsHandler();
    }

    private static class AskPermissionsHandler extends PacketHandler<C2SAskPermissionsMessage> {

        @Override
        public void handle(C2SAskPermissionsMessage packet, Player player, Object gameInstance) {
            // this technically isn't necessary as the client is fully aware of its own permission level on the server
            // it's still here so there can be e.g. a config option for denying clients to edit server configs in the future
            if (player.hasPermissions(((MinecraftServer) gameInstance).getOperatorUserPermissionLevel())) {
                ForgeConfigScreensFabric.NETWORK.sendTo(new S2CGrantPermissionsMessage(), (ServerPlayer) player);
            }
        }
    }
}
