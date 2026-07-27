package net.zic.ascension.common.visual;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;
import net.zic.ascension.api.client.visual.RuntimeVisualAction;
import net.zic.ascension.api.client.visual.RuntimeVisualState;
import net.zic.ascension.network.RuntimeVisualPacket;

public final class RuntimeVisualSyncManager {
    private static final double SYNC_DISTANCE_SQR = 128.0D * 128.0D;

    private RuntimeVisualSyncManager() {
    }

    public static void spawn(ServerLevel level, RuntimeVisualState state) {
        send(level, state.position(), new RuntimeVisualPacket(RuntimeVisualAction.SPAWN, state));
    }

    public static void update(ServerLevel level, RuntimeVisualState state) {
        send(level, state.position(), new RuntimeVisualPacket(RuntimeVisualAction.UPDATE, state));
    }

    public static void remove(ServerLevel level, RuntimeVisualState state) {
        send(level, state.position(), new RuntimeVisualPacket(RuntimeVisualAction.REMOVE, state));
    }

    private static void send(
            ServerLevel level,
            Vec3 position,
            RuntimeVisualPacket packet
    ) {
        for (ServerPlayer player : level.players()) {
            if (player.position().distanceToSqr(position) <= SYNC_DISTANCE_SQR) {
                PacketDistributor.sendToPlayer(player, packet);
            }
        }
    }
}
