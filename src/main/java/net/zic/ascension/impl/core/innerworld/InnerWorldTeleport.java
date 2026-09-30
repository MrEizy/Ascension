package net.zic.ascension.impl.core.innerworld;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Relative;

import java.util.Set;

/**
 * Isolated wrapper around ServerPlayer's cross-dimension teleport.
 *
 * Confirmed against your real decompiled ServerPlayer.java:
 *   public boolean teleportTo(ServerLevel level, double x, double y, double z,
 *                              Set<Relative> relatives, float newYRot, float newXRot, boolean resetCamera)
 * — note it's net.minecraft.world.entity.Relative, not RelativeMovement (my earlier guess), and it
 * takes a trailing resetCamera boolean. Passed true here so nothing lingers from wherever they were.
 */
final class InnerWorldTeleport {
    private InnerWorldTeleport() {
    }

    static void to(ServerPlayer player, ServerLevel level, double x, double y, double z, float yaw, float pitch) {
        player.teleportTo(level, x, y, z, Set.of(), yaw, pitch, true);
    }
}