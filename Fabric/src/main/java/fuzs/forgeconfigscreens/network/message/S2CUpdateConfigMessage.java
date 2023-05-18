package fuzs.forgeconfigscreens.network.message;

import fuzs.forgeconfigscreens.lib.network.message.Message;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.fml.config.ConfigTracker;

import java.util.Optional;

public class S2CUpdateConfigMessage implements Message {

    private String fileName;
    private byte[] fileData;

    public S2CUpdateConfigMessage() {
    }

    public S2CUpdateConfigMessage(String fileName, byte[] fileData) {
        this.fileName = fileName;
        this.fileData = fileData;
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeUtf(this.fileName);
        buf.writeByteArray(this.fileData);
    }

    @Override
    public void read(FriendlyByteBuf buf) {
        this.fileName = buf.readUtf();
        this.fileData = buf.readByteArray();
    }

    @Override
    public UpdateConfigHandler makeHandler() {
        return new UpdateConfigHandler();
    }

    private static class UpdateConfigHandler extends PacketHandler<S2CUpdateConfigMessage> {

        @Override
        public void handle(S2CUpdateConfigMessage packet, Player player, Object gameInstance) {
            // should never happen, but just to be safe as there would be a classcastexception otherwise
            // (class com.electronwill.nightconfig.core.SimpleCommentedConfig cannot be cast to class com.electronwill.nightconfig.core.file.CommentedFileConfig)
            if (!Minecraft.getInstance().isLocalServer()) {
                Optional.ofNullable(ConfigTracker.INSTANCE.fileMap().get(packet.fileName)).ifPresent(config -> config.acceptSyncedConfig(packet.fileData));
            }
        }
    }
}
