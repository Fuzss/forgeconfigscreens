package fuzs.configmenusforge.network.message;

import fuzs.configmenusforge.client.gui.screens.SelectConfigScreen;
import fuzs.configmenusforge.lib.network.message.Message;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;

public class S2CGrantPermissionsMessage implements Message {

    public S2CGrantPermissionsMessage() {

    }

    @Override
    public void write(PacketBuffer buf) {

    }

    @Override
    public void read(PacketBuffer buf) {

    }

    @Override
    public GrantPermissionsHandler makeHandler() {
        return new GrantPermissionsHandler();
    }

    private static class GrantPermissionsHandler extends PacketHandler<S2CGrantPermissionsMessage> {
        @Override
        public void handle(S2CGrantPermissionsMessage packet, PlayerEntity player, Object gameInstance) {
            if (((Minecraft) gameInstance).screen instanceof SelectConfigScreen) {
                ((SelectConfigScreen) ((Minecraft) gameInstance).screen).setServerPermissions();
            }
        }
    }
}
