package fuzs.forgeconfigscreens.core;

import net.minecraft.network.FriendlyByteBuf;

public interface WritableMessage {

    default void write(final FriendlyByteBuf buf) {

    }
}
