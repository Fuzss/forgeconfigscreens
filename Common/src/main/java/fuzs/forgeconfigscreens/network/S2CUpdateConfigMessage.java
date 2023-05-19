package fuzs.forgeconfigscreens.network;

import fuzs.forgeconfigscreens.core.WritableMessage;
import net.minecraft.network.FriendlyByteBuf;

public record S2CUpdateConfigMessage(String fileName, byte[] fileData) implements WritableMessage {

    public S2CUpdateConfigMessage(FriendlyByteBuf buf) {
        this(buf.readUtf(), buf.readByteArray());
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeUtf(this.fileName);
        buf.writeByteArray(this.fileData);
    }
}
