package net.zic.ascension.skill_casting;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.zic.ascension.api.ascension.core.skill.castable.ActiveCastVisualState;
import net.zic.ascension.network.ActiveCastVisualStatePacket;

public final class ActiveCastVisualSyncManager {
    private static final int HEARTBEAT_INTERVAL = 10;
    private static final double SYNC_DISTANCE_SQR = 64.0D * 64.0D;

    private ActiveCastVisualSyncManager() {
    }

    public static void syncNow(ServerPlayer caster, ActiveCastVisualState state) {
        if (state == null) {
            sendStop(caster);
            return;
        }
        sendNearby(caster, state);
    }

    public static void heartbeat(ServerPlayer caster, ActiveCastVisualState state) {
        if (state == null) {
            return;
        }
        long gameTime = caster.level().getGameTime();
        if (Math.floorMod(gameTime, HEARTBEAT_INTERVAL)
                != Math.floorMod(caster.getId(), HEARTBEAT_INTERVAL)) {
            return;
        }
        sendNearby(caster, state);
    }

    private static void sendNearby(ServerPlayer caster, ActiveCastVisualState state) {
        MinecraftServer server = caster.level().getServer();
        if (server == null) {
            return;
        }
        ActiveCastVisualStatePacket packet = new ActiveCastVisualStatePacket(
                caster.getUUID(),
                state.skill(),
                state.stage(),
                (float) state.progress()
        );
        for (ServerPlayer viewer : server.getPlayerList().getPlayers()) {
            if (viewer == caster || viewer.level() != caster.level()) {
                continue;
            }
            if (viewer.distanceToSqr(caster) <= SYNC_DISTANCE_SQR) {
                PacketDistributor.sendToPlayer(viewer, packet);
            }
        }
    }

    private static void sendStop(ServerPlayer caster) {
        MinecraftServer server = caster.level().getServer();
        if (server == null) {
            return;
        }
        ActiveCastVisualStatePacket packet = new ActiveCastVisualStatePacket(
                caster.getUUID(),
                null,
                0,
                0.0F
        );
        for (ServerPlayer viewer : server.getPlayerList().getPlayers()) {
            if (viewer != caster) {
                PacketDistributor.sendToPlayer(viewer, packet);
            }
        }
    }
}
