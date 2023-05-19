package fuzs.forgeconfigscreens.network;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.toml.TomlFormat;
import fuzs.forgeconfigscreens.ForgeConfigScreens;
import fuzs.forgeconfigscreens.core.CommonAbstractions;
import fuzs.forgeconfigscreens.core.NetworkingHelper;
import fuzs.forgeconfigscreens.network.client.C2SAskPermissionsMessage;
import fuzs.forgeconfigscreens.network.client.C2SSendConfigMessage;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraftforge.fml.config.ConfigTracker;
import net.minecraftforge.fml.config.ModConfig;

import java.io.ByteArrayInputStream;

public final class ServerMessageHandles {

    private ServerMessageHandles() {

    }

    public static void handleAskPermissions(C2SAskPermissionsMessage message, MinecraftServer server, ServerGamePacketListenerImpl handler, ServerPlayer player, ServerLevel level) {
        // this technically isn't necessary as the client is fully aware of its own permission level on the server
        // it's still here so there can be e.g. a config option for denying clients to edit server configs in the future
        if (player.hasPermissions(server.getOperatorUserPermissionLevel())) {
            NetworkingHelper.sendTo(player, new S2CGrantPermissionsMessage());
        }
    }

    public static void handleSendConfig(C2SSendConfigMessage packet, MinecraftServer server, ServerGamePacketListenerImpl handler, ServerPlayer player, ServerLevel level) {
        if (server.isDedicatedServer() && player.hasPermissions(server.getOperatorUserPermissionLevel())) {
            final ModConfig config = ConfigTracker.INSTANCE.fileMap().get(packet.fileName());
            if (config != null) {
                // this is basically ModConfig::acceptSyncedConfig which we can't use as the config sent from a client only exists in memory,
                // but we need to update the actual file config on the server
                final CommentedConfig receivedConfig = TomlFormat.instance().createParser().parse(new ByteArrayInputStream(packet.fileData()));
                config.getConfigData().putAll(receivedConfig);
                // save and reset caches
                config.save();
                config.getSpec().afterReload();
                CommonAbstractions.INSTANCE.fireReloadingEvent(config);
                NetworkingHelper.sendToAllExcept(server, player, new S2CUpdateConfigMessage(packet.fileName(), packet.fileData()));
                ForgeConfigScreens.LOGGER.info("Server config has been updated by {}", player.getDisplayName().getString());
            } else {
                ForgeConfigScreens.LOGGER.error("Failed to update server config with data received from {}", player.getDisplayName().getString());
            }
        }
    }
}
