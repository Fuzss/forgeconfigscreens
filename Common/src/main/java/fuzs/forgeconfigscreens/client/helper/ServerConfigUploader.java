package fuzs.forgeconfigscreens.client.helper;

import com.electronwill.nightconfig.toml.TomlFormat;
import fuzs.forgeconfigscreens.core.CommonAbstractions;
import fuzs.forgeconfigscreens.core.NetworkingHelper;
import fuzs.forgeconfigscreens.network.client.C2SSendConfigMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.config.ModConfig;

import java.io.ByteArrayOutputStream;

public class ServerConfigUploader {

    public static void saveAndUpload(ModConfig config) {
        ((ForgeConfigSpec) config.getSpec()).save();
        CommonAbstractions.INSTANCE.fireReloadingEvent(config);
        if (config.getType() == ModConfig.Type.SERVER) {
            Minecraft minecraft = Minecraft.getInstance();
            ClientPacketListener connection = minecraft.getConnection();
            if (connection != null && !minecraft.isLocalServer()) {
                final ByteArrayOutputStream stream = new ByteArrayOutputStream();
                TomlFormat.instance().createWriter().write(config.getConfigData(), stream);
                NetworkingHelper.sendToServer(connection.getConnection(), new C2SSendConfigMessage(config.getFileName(), stream.toByteArray()));
            }
        }
    }
}
