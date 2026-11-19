package net.zic.ascension.client.combat;

import net.minecraft.util.Mth;

public final class DualWieldClientState {
    private DualWieldClientState() {
    }

    public static final int SWING_DURATION = 6;

    private static boolean active;
    private static int offhandCooldown;
    private static int offhandCooldownTotal;
    private static int offhandSwing;

    public static boolean active() {
        return active;
    }

    public static void set(boolean value) {
        if (active == value) {
            return;
        }
        active = value;
        if (!value) {
            offhandCooldown = 0;
            offhandCooldownTotal = 0;
            offhandSwing = 0;
        }
    }


    public static int offhandCooldown() {
        return offhandCooldown;
    }

    public static int offhandCooldownTotal() {
        return offhandCooldownTotal;
    }

    public static void setCooldown(int ticks) {
        offhandCooldown = Math.max(0, ticks);
        offhandCooldownTotal = offhandCooldown;
    }


    public static void startOffhandSwing() {
        offhandSwing = SWING_DURATION;
    }

    public static float offhandSwingProgress(float partialTick) {
        if (offhandSwing <= 0) {
            return 0.0F;
        }
        return Mth.clamp(1.0F - (offhandSwing - partialTick) / SWING_DURATION, 0.0F, 1.0F);
    }


    public static void tick() {
        if (offhandCooldown > 0) {
            --offhandCooldown;
        }
        if (offhandSwing > 0) {
            --offhandSwing;
        }
    }

    public static void reset() {
        active = false;
        offhandCooldown = 0;
        offhandCooldownTotal = 0;
        offhandSwing = 0;
    }
}