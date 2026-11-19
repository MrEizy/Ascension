package net.zic.ascension.impl.core.skill.passive;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.zic.ascension.AscensionCraft;

import java.lang.reflect.Field;

/**
 * Reflection handles for the three vanilla fields dual wielding has to drive:
 * the attack cooldown ticker, and the two hurt-resistance fields that would otherwise
 * delete every second hit when both hands land inside the same i-frame window.
 */
final class DualWieldAccess {
    private DualWieldAccess() {
    }

    private static final Field ATTACK_TICKER = find(LivingEntity.class, "attackStrengthTicker");
    private static final Field INVULNERABLE_TIME = find(Entity.class, "invulnerableTime");
    private static final Field LAST_HURT = find(LivingEntity.class, "lastHurt");

    static {
        if (ATTACK_TICKER == null) {
            AscensionCraft.LOGGER.error("Dual wielding is DISABLED: cannot access LivingEntity.attackStrengthTicker");
        }
        if (INVULNERABLE_TIME == null || LAST_HURT == null) {
            AscensionCraft.LOGGER.warn("Dual wielding: i-frame fields unavailable, alternating hands will be eaten by vanilla hurt resistance");
        }
    }

    private static Field find(Class<?> owner, String name) {
        try {
            Field field = owner.getDeclaredField(name);
            field.setAccessible(true);
            return field;
        } catch (ReflectiveOperationException | RuntimeException e) {
            return null;
        }
    }

    static boolean available() {
        return ATTACK_TICKER != null;
    }

    static int get(Player player) {
        try {
            return ATTACK_TICKER.getInt(player);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }

    static void set(Player player, int ticks) {
        try {
            ATTACK_TICKER.setInt(player, ticks);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }

    // ── i-frames ────────────────────────────────────────────────────────────────


    static void clearIFrames(Entity target) {
        writeInvulnerableTime(target, 0);
        writeLastHurt(target, 0.0F);
    }

    static IFrames bypassIFrames(Entity target) {
        return new IFrames(target);
    }

    static final class IFrames implements AutoCloseable {
        private final Entity target;
        private final int invulnerableTime;
        private final float lastHurt;

        private IFrames(Entity target) {
            this.target = target;
            this.invulnerableTime = readInvulnerableTime(target);
            this.lastHurt = readLastHurt(target);
            writeInvulnerableTime(target, 0);
            writeLastHurt(target, 0.0F);
        }

        @Override
        public void close() {
            writeInvulnerableTime(target, Math.max(invulnerableTime, readInvulnerableTime(target)));
            writeLastHurt(target, Math.max(lastHurt, readLastHurt(target)));
        }
    }

    private static int readInvulnerableTime(Entity target) {
        if (INVULNERABLE_TIME == null) {
            return 0;
        }
        try {
            return INVULNERABLE_TIME.getInt(target);
        } catch (IllegalAccessException e) {
            return 0;
        }
    }

    private static void writeInvulnerableTime(Entity target, int value) {
        if (INVULNERABLE_TIME == null) {
            return;
        }
        try {
            INVULNERABLE_TIME.setInt(target, value);
        } catch (IllegalAccessException ignored) {
        }
    }

    private static float readLastHurt(Entity target) {
        if (LAST_HURT == null || !(target instanceof LivingEntity)) {
            return 0.0F;
        }
        try {
            return LAST_HURT.getFloat(target);
        } catch (IllegalAccessException e) {
            return 0.0F;
        }
    }

    private static void writeLastHurt(Entity target, float value) {
        if (LAST_HURT == null || !(target instanceof LivingEntity)) {
            return;
        }
        try {
            LAST_HURT.setFloat(target, value);
        } catch (IllegalAccessException ignored) {
        }
    }
}