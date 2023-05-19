package fuzs.forgeconfigscreens.core;

import net.minecraft.network.FriendlyByteBuf;

public interface WritableMessage {

    void write(final FriendlyByteBuf buf);
}
