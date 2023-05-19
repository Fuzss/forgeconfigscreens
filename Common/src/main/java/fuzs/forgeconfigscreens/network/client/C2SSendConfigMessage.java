package fuzs.forgeconfigscreens.network.client;

import fuzs.forgeconfigscreens.core.WritableMessage;
import net.minecraft.network.FriendlyByteBuf;

public record C2SSendConfigMessage(String fileName, byte[] fileData) implements WritableMessage {

    public C2SSendConfigMessage(FriendlyByteBuf buf) {
        this(buf.readUtf(), buf.readByteArray());
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeUtf(this.fileName);
        buf.writeByteArray(this.fileData);
    }
}
