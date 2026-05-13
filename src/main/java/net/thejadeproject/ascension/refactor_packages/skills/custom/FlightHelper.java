package net.thejadeproject.ascension.refactor_packages.skills.custom;

import net.minecraft.server.level.ServerPlayer;

public final class FlightHelper {

    private FlightHelper() {}

    public static void enableFlight(ServerPlayer player, boolean forceFlying) {
        boolean changed = false;

        if (!player.getAbilities().mayfly) {
            player.getAbilities().mayfly = true;
            changed = true;
        }

        if (forceFlying && !player.getAbilities().flying) {
            player.getAbilities().flying = true;
            changed = true;
        }

        if (changed) {
            player.onUpdateAbilities();
        }
    }

    public static void disableFlightIfNotVanillaAllowed(ServerPlayer player) {
        if (player.isCreative() || player.isSpectator()) return;

        boolean changed = false;

        if (player.getAbilities().flying) {
            player.getAbilities().flying = false;
            changed = true;
        }

        if (player.getAbilities().mayfly) {
            player.getAbilities().mayfly = false;
            changed = true;
        }

        if (changed) {
            player.onUpdateAbilities();
        }
    }
}
