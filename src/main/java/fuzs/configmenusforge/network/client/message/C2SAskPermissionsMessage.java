package fuzs.configmenusforge.network.client.message;

import fuzs.configmenusforge.lib.network.NetworkHandler;
import fuzs.configmenusforge.lib.network.message.Message;
import fuzs.configmenusforge.network.message.S2CGrantPermissionsMessage;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.server.MinecraftServer;

public class C2SAskPermissionsMessage implements Message {

    public C2SAskPermissionsMessage() {
    }

    @Override
    public void write(PacketBuffer buf) {

    }

    @Override
    public void read(PacketBuffer buf) {

    }

    @Override
    public AskPermissionsHandler makeHandler() {
        return new AskPermissionsHandler();
    }

    private static class AskPermissionsHandler extends PacketHandler<C2SAskPermissionsMessage> {
        @Override
        public void handle(C2SAskPermissionsMessage packet, PlayerEntity player, Object gameInstance) {
            // this technically isn't necessary as the client is fully aware of its own permission level on the server
            // it's still here so there can be e.g. a config option for denying clients to edit server configs in the future
            if (player.hasPermissions(((MinecraftServer) gameInstance).getOperatorUserPermissionLevel())) {
                NetworkHandler.INSTANCE.sendTo(new S2CGrantPermissionsMessage(), (ServerPlayerEntity) player);
            }
        }
    }
}
