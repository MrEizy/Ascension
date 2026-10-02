package net.zic.ascension.impl.core.innerworld;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Relative;

import java.util.Set;

final class InnerWorldTeleport {
    private InnerWorldTeleport() {
    }

    static void to(ServerPlayer player, ServerLevel level, double x, double y, double z, float yaw, float pitch) {
        player.teleportTo(level, x, y, z, Set.of(), yaw, pitch, true);
    }
}