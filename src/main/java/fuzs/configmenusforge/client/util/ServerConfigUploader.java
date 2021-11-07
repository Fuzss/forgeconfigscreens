package fuzs.configmenusforge.client.util;

import com.electronwill.nightconfig.toml.TomlFormat;
import fuzs.configmenusforge.ConfigMenusForge;
import fuzs.configmenusforge.network.client.message.C2SSendConfigMessage;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.config.ModConfig;

import java.io.ByteArrayOutputStream;

public class ServerConfigUploader {

    public static void saveAndUpload(ModConfig config) {
        config.getSpec().save();
        ModConfigSync.fireReloadingEvent(config);
        if (config.getType() == ModConfig.Type.SERVER && !Minecraft.getInstance().isLocalServer()) {
            final ByteArrayOutputStream stream = new ByteArrayOutputStream();
            TomlFormat.instance().createWriter().write(config.getConfigData(), stream);
            ConfigMenusForge.NETWORK.sendToServer(new C2SSendConfigMessage(config.getFileName(), stream.toByteArray()));
        }
    }
}
