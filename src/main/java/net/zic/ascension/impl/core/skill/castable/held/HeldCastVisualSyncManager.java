package net.zic.ascension.impl.core.skill.castable.held;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.zic.ascension.api.ascension.core.skill.castable.held.HeldCastVisualPhase;
import net.zic.ascension.api.ascension.core.skill.castable.held.HeldCastVisualState;
import net.zic.ascension.network.HeldCastVisualStatePacket;

public final class HeldCastVisualSyncManager {
    private static final int HEARTBEAT_INTERVAL = 10;
    private static final double SYNC_DISTANCE_SQR = 64.0D * 64.0D;

    private HeldCastVisualSyncManager() {
    }

    public static void syncNow(ServerPlayer caster, HeldCastVisualState state) {
        if (state == null) {
            sendStop(caster);
            return;
        }
        sendNearby(caster, state);
    }

    public static void heartbeat(ServerPlayer caster, HeldCastVisualState state) {
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

    private static void sendNearby(ServerPlayer caster, HeldCastVisualState state) {
        MinecraftServer server = caster.level().getServer();
        if (server == null) {
            return;
        }
        HeldCastVisualStatePacket packet = new HeldCastVisualStatePacket(
                caster.getUUID(),
                state.skill(),
                state.phase(),
                state.stage(),
                (float) state.charge()
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
        HeldCastVisualStatePacket packet = new HeldCastVisualStatePacket(
                caster.getUUID(),
                null,
                HeldCastVisualPhase.STOPPED,
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
