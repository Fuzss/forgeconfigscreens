package fuzs.forgeconfigscreens.network.client;

import fuzs.forgeconfigscreens.client.gui.screens.SelectConfigScreen;
import fuzs.forgeconfigscreens.network.S2CGrantPermissionsMessage;
import fuzs.forgeconfigscreens.network.S2CUpdateConfigMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraftforge.fml.config.ConfigTracker;

import java.util.Optional;

public final class ClientMessageHandles {

    private ClientMessageHandles() {

    }

    public static void handleUpdateConfig(S2CUpdateConfigMessage message, Minecraft client, ClientPacketListener handler, LocalPlayer player, ClientLevel level) {
        // should never happen, but just to be safe as there would be a classcastexception otherwise
        // (class com.electronwill.nightconfig.core.SimpleCommentedConfig cannot be cast to class com.electronwill.nightconfig.core.file.CommentedFileConfig)
        if (Minecraft.getInstance().isLocalServer()) return;
        Optional.ofNullable(ConfigTracker.INSTANCE.fileMap().get(message.fileName())).ifPresent(config -> config.acceptSyncedConfig(message.fileData()));
    }

    public static void handleGrantPermissions(S2CGrantPermissionsMessage message, Minecraft client, ClientPacketListener handler, LocalPlayer player, ClientLevel level) {
        if (client.screen instanceof SelectConfigScreen screen) {
            screen.setServerPermissions();
        }
    }
}
