package fuzs.configmenusforge.network.client.message;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.toml.TomlFormat;
import fuzs.configmenusforge.ConfigMenusForge;
import fuzs.configmenusforge.client.util.ModConfigSync;
import fuzs.configmenusforge.lib.network.message.Message;
import fuzs.configmenusforge.network.message.S2CUpdateConfigMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.fml.config.ConfigTracker;
import net.minecraftforge.fml.config.ModConfig;

import java.io.ByteArrayInputStream;

public class C2SSendConfigMessage implements Message {

    private String fileName;
    private byte[] fileData;

    public C2SSendConfigMessage() {
    }

    public C2SSendConfigMessage(String fileName, byte[] fileData) {
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
    public SendConfigHandler makeHandler() {
        return new SendConfigHandler();
    }

    private static class SendConfigHandler extends PacketHandler<C2SSendConfigMessage> {

        @Override
        public void handle(C2SSendConfigMessage packet, Player player, Object gameInstance) {
            final MinecraftServer server = (MinecraftServer) gameInstance;
            if (server.isDedicatedServer() && player.hasPermissions(server.getOperatorUserPermissionLevel())) {
                final ModConfig config = ConfigTracker.INSTANCE.fileMap().get(packet.fileName);
                if (config != null) {
                    // this is basically ModConfig::acceptSyncedConfig which we can't use as the config sent from a client only exists in memory,
                    // but we need to update the actual file config on the server
                    final CommentedConfig receivedConfig = TomlFormat.instance().createParser().parse(new ByteArrayInputStream(packet.fileData));
                    config.getConfigData().putAll(receivedConfig);
                    ModConfigSync.fireReloadingEvent(config);
                    ConfigMenusForge.NETWORK.sendToAllExcept(new S2CUpdateConfigMessage(packet.fileName, packet.fileData), (ServerPlayer) player);
                    ConfigMenusForge.LOGGER.info("Server config has been updated by {}", player.getDisplayName().getString());
                } else {
                    ConfigMenusForge.LOGGER.error("Failed to update server config with data received from {}", player.getDisplayName().getString());
                }
            }
        }
    }
}
