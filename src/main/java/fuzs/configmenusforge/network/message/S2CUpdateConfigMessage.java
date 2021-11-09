package fuzs.configmenusforge.network.message;

import fuzs.configmenusforge.client.util.ModConfigSync;
import fuzs.configmenusforge.client.util.ReflectionHelper;
import fuzs.configmenusforge.lib.network.message.Message;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.config.ConfigTracker;
import net.minecraftforge.fml.config.ModConfig;

import java.util.concurrent.ConcurrentHashMap;

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
    public void write(PacketBuffer buf) {
        buf.writeUtf(this.fileName);
        buf.writeByteArray(this.fileData);
    }

    @Override
    public void read(PacketBuffer buf) {
        this.fileName = buf.readUtf();
        this.fileData = buf.readByteArray();
    }

    @Override
    public UpdateConfigHandler makeHandler() {
        return new UpdateConfigHandler();
    }

    private static class UpdateConfigHandler extends Message.PacketHandler<S2CUpdateConfigMessage> {

        @Override
        public void handle(S2CUpdateConfigMessage packet, PlayerEntity player, Object gameInstance) {
            // should never happen, but just to be safe as there would be a classcastexception otherwise
            // (class com.electronwill.nightconfig.core.SimpleCommentedConfig cannot be cast to class com.electronwill.nightconfig.core.file.CommentedFileConfig)
            if (!Minecraft.getInstance().isLocalServer()) {
                ReflectionHelper.<ConcurrentHashMap<String, ModConfig>>get(ModConfigSync.FILE_MAP_FIELD, ConfigTracker.INSTANCE)
                        .map(fileMap -> fileMap.get(packet.fileName))
                        .ifPresent(config -> ModConfigSync.acceptSyncedConfig(config, packet.fileData));
            }
        }
    }
}
