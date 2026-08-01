package net.zic.ascension.skill_casting;

import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.zic.ascension.api.ascension.core.CoreRegistries;
import net.zic.ascension.impl.core.skill.castable.cultivation.SimpleCultivationSkill;
import net.zic.ascension.network.ParticleFieldStatePacket;

public final class ParticleFieldSyncManager {
    private static final int HEARTBEAT_INTERVAL = 20;
    private static final double SYNC_DISTANCE_SQR = 64.0D * 64.0D;

    private ParticleFieldSyncManager() {
    }

    public static void syncNow(ServerPlayer caster, Identifier skillId) {
        Identifier visibleSkill = visibleSkill(caster, skillId);
        if (visibleSkill == null) {
            sendStop(caster);
            return;
        }
        sendNearby(caster, visibleSkill);
    }

    public static void heartbeat(ServerPlayer caster, Identifier skillId) {
        long gameTime = caster.level().getGameTime();
        if (Math.floorMod(gameTime, HEARTBEAT_INTERVAL) != Math.floorMod(caster.getId(), HEARTBEAT_INTERVAL)) {
            return;
        }

        Identifier visibleSkill = visibleSkill(caster, skillId);
        if (visibleSkill != null) {
            sendNearby(caster, visibleSkill);
        }
    }

    private static Identifier visibleSkill(ServerPlayer caster, Identifier skillId) {
        if (skillId == null) {
            return null;
        }

        if (!(CoreRegistries.safeAccess(
                CoreRegistries.SKILL_REGISTRY,
                skillId,
                caster.registryAccess()
        ) instanceof SimpleCultivationSkill cultivationSkill)) {
            return null;
        }

        return cultivationSkill.particleField().isPresent() ? skillId : null;
    }

    private static void sendNearby(ServerPlayer caster, Identifier skillId) {
        MinecraftServer server = caster.level().getServer();
        if (server == null) {
            return;
        }

        ParticleFieldStatePacket packet = new ParticleFieldStatePacket(caster.getUUID(), skillId);
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

        ParticleFieldStatePacket packet = new ParticleFieldStatePacket(caster.getUUID(), null);
        for (ServerPlayer viewer : server.getPlayerList().getPlayers()) {
            if (viewer != caster) {
                PacketDistributor.sendToPlayer(viewer, packet);
            }
        }
    }
}
