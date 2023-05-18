package fuzs.forgeconfigscreens.client.util;

import com.electronwill.nightconfig.toml.TomlFormat;
import fuzs.forgeconfigscreens.ForgeConfigScreensFabric;
import fuzs.forgeconfigscreens.network.client.message.C2SSendConfigMessage;
import net.minecraft.client.Minecraft;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.config.ModConfig;

import java.io.ByteArrayOutputStream;

public class ServerConfigUploader {

    public static void saveAndUpload(ModConfig config) {
        ((ForgeConfigSpec) config.getSpec()).save();
        if (config.getType() == ModConfig.Type.SERVER && !Minecraft.getInstance().isLocalServer()) {
            final ByteArrayOutputStream stream = new ByteArrayOutputStream();
            TomlFormat.instance().createWriter().write(config.getConfigData(), stream);
            ForgeConfigScreensFabric.NETWORK.sendToServer(new C2SSendConfigMessage(config.getFileName(), stream.toByteArray()));
        }
    }
}
