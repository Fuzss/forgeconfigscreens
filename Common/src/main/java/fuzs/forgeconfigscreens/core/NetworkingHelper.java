package fuzs.forgeconfigscreens.core;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class NetworkingHelper {

    /**
     * creates a packet heading to the client side
     *
     * @param message message to create packet from
     * @return packet for message
     */
    private static Packet<?> toClientboundPacket(WritableMessage message) {
        return CommonAbstractions.INSTANCE.toClientboundPacket(message);
    }

    /**
     * creates a packet heading to the server side
     *
     * @param message message to create packet from
     * @return packet for message
     */
    private static Packet<?> toServerboundPacket(WritableMessage message) {
        return CommonAbstractions.INSTANCE.toServerboundPacket(message);
    }

    /**
     * send message from client to server
     *
     * @param message message to send
     */
    public static void sendToServer(Connection connection, WritableMessage message) {
        connection.send(toServerboundPacket(message));
    }

    /**
     * send message from server to client
     *
     * @param player  client player to send to
     * @param message message to send
     */
    public static void sendTo(ServerPlayer player, WritableMessage message) {
        player.connection.send(toClientboundPacket(message));
    }

    /**
     * send message from server to all clients
     *
     * @param message message to send
     */
    public static void sendToAll(MinecraftServer server, WritableMessage message) {
        server.getPlayerList().broadcastAll(toClientboundPacket(message));
    }

    /**
     * send message from server to all clients except one
     *
     * @param exclude client to exclude
     * @param message message to send
     */
    public static void sendToAllExcept(MinecraftServer server, ServerPlayer exclude, WritableMessage message) {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (player != exclude) sendTo(player, message);
        }
    }

    /**
     * send message from server to all clients near given position
     *
     * @param pos     source position
     * @param level   dimension key provider level
     * @param message message to send
     */
    public static void sendToAllNear(MinecraftServer server, BlockPos pos, Level level, WritableMessage message) {
        sendToAllNearExcept(server, null, pos.getX(), pos.getY(), pos.getZ(), 64.0, level, message);
    }

    /**
     * send message from server to all clients near given position
     *
     * @param posX     source position x
     * @param posY     source position y
     * @param posZ     source position z
     * @param distance distance from source to receive message
     * @param level    dimension key provider level
     * @param message  message to send
     */
    public static void sendToAllNear(MinecraftServer server, double posX, double posY, double posZ, double distance, Level level, WritableMessage message) {
        sendToAllNearExcept(server, null, posX, posY, posZ, 64.0, level, message);
    }

    /**
     * send message from server to all clients near given position
     *
     * @param exclude  exclude player having caused this event
     * @param posX     source position x
     * @param posY     source position y
     * @param posZ     source position z
     * @param distance distance from source to receive message
     * @param level    dimension key provider level
     * @param message  message to send
     */
    public static void sendToAllNearExcept(MinecraftServer server, @Nullable ServerPlayer exclude, double posX, double posY, double posZ, double distance, Level level, WritableMessage message) {
        server.getPlayerList().broadcast(exclude, posX, posY, posZ, distance, level.dimension(), toClientboundPacket(message));
    }

    /**
     * send message from server to all clients tracking <code>entity</code>
     *
     * @param entity  the tracked entity
     * @param message message to send
     */
    public static void sendToAllTracking(Entity entity, WritableMessage message) {
        ((ServerChunkCache) entity.getCommandSenderWorld().getChunkSource()).broadcast(entity, toClientboundPacket(message));
    }

    /**
     * send message from server to all clients tracking <code>entity</code> including the entity itself
     *
     * @param entity  the tracked entity
     * @param message message to send
     */
    public static void sendToAllTrackingAndSelf(Entity entity, WritableMessage message) {
        ((ServerChunkCache) entity.getCommandSenderWorld().getChunkSource()).broadcastAndSend(entity, toClientboundPacket(message));
    }

    /**
     * send message from server to all clients in dimension
     *
     * @param level   dimension key provider level
     * @param message message to send
     */
    public static void sendToDimension(MinecraftServer server, Level level, WritableMessage message) {
        sendToDimension(server, level.dimension(), message);
    }

    /**
     * send message from server to all clients in dimension
     *
     * @param dimension dimension to send message in
     * @param message   message to send
     */
    public static void sendToDimension(MinecraftServer server, ResourceKey<Level> dimension, WritableMessage message) {
        server.getPlayerList().broadcastAll(toClientboundPacket(message), dimension);
    }

    /**
     * Client-side handler for messages received by the client.
     */
    public interface ClientMessageListener<T> {

        /**
         * Called to handle the given message.
         *
         * @param message message to handle
         * @param client  minecraft client instance
         * @param handler handler for vanilla packets
         * @param player  client player entity
         * @param level   the local client level
         */
        void handle(T message, Minecraft client, ClientPacketListener handler, LocalPlayer player, ClientLevel level);
    }

    /**
     * Server-side handler for messages received by the server.
     */
    @FunctionalInterface
    public interface ServerMessageListener<T> {

        /**
         * Called to handle the given message.
         *
         * @param message message to handle
         * @param server  minecraft server instance
         * @param handler handler for vanilla packets
         * @param player  server player entity
         * @param level   the current level of <code>player</code>
         */
        void handle(T message, MinecraftServer server, ServerGamePacketListenerImpl handler, ServerPlayer player, ServerLevel level);
    }
}
