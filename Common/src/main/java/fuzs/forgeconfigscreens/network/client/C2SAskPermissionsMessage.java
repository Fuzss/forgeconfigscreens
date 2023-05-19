package fuzs.forgeconfigscreens.network.client;

import fuzs.forgeconfigscreens.core.WritableMessage;
import net.minecraft.network.FriendlyByteBuf;

public record C2SAskPermissionsMessage() implements WritableMessage {

    @Override
    public void write(FriendlyByteBuf buf) {

    }
}
