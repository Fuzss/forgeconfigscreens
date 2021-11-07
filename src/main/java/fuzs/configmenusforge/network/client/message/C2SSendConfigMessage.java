package fuzs.configmenusforge.network.client.message;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.toml.TomlFormat;
import fuzs.configmenusforge.ConfigMenusForge;
import fuzs.configmenusforge.client.util.ModConfigSync;
import fuzs.configmenusforge.client.util.ReflectionHelper;
import fuzs.configmenusforge.lib.network.message.Message;
import fuzs.configmenusforge.network.message.S2CUpdateConfigMessage;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.config.ConfigTracker;
import net.minecraftforge.fml.config.ModConfig;

import java.io.ByteArrayInputStream;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

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
    public SendConfigHandler makeHandler() {
        return new SendConfigHandler();
    }

    private static class SendConfigHandler extends PacketHandler<C2SSendConfigMessage> {
        @Override
        public void handle(C2SSendConfigMessage packet, PlayerEntity player, Object gameInstance) {
            final MinecraftServer server = (MinecraftServer) gameInstance;
            if (server.isDedicatedServer() && player.hasPermissions(server.getOperatorUserPermissionLevel())) {
                final Optional<ModConfig> optConfig = ReflectionHelper.<ConcurrentHashMap<String, ModConfig>>get(ReflectionHelper.FILE_MAP_FIELD, ConfigTracker.INSTANCE).map(fileMap -> fileMap.get(packet.fileName));
                if (optConfig.isPresent()) {
                    final ModConfig config = optConfig.get();
                    // this is basically ModConfig::acceptSyncedConfig which we can't use as the config sent from a client only exists in memory,
                    // but we need to update the actual file config on the server
                    final CommentedConfig receivedConfig = TomlFormat.instance().createParser().parse(new ByteArrayInputStream(packet.fileData));
                    config.getConfigData().putAll(receivedConfig);
                    ModConfigSync.fireReloadingEvent(config);
                    ConfigMenusForge.NETWORK.sendToAllExcept(new S2CUpdateConfigMessage(packet.fileName, packet.fileData), (ServerPlayerEntity) player);
                    ConfigMenusForge.LOGGER.info("Server config has been updated by {}", player.getDisplayName().getString());
                } else {
                    ConfigMenusForge.LOGGER.error("Failed to update server config with data received from {}", player.getDisplayName().getString());
                }
            }
        }
    }
}
