package net.zic.ascension.impl.core.skill.passive;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.zic.ascension.AscensionCraft;

import java.lang.reflect.Field;


final class DualWieldAccess {
    private DualWieldAccess() {
    }

    private static final Field FIELD = find();

    private static Field find() {
        try {
            Field field = LivingEntity.class.getDeclaredField("attackStrengthTicker");
            field.setAccessible(true);
            return field;
        } catch (ReflectiveOperationException | RuntimeException e) {
            AscensionCraft.LOGGER.error("Dual wielding is DISABLED: cannot access LivingEntity.attackStrengthTicker", e);
            return null;
        }
    }

    static boolean available() {
        return FIELD != null;
    }

    static int get(Player player) {
        try {
            return FIELD.getInt(player);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }

    static void set(Player player, int ticks) {
        try {
            FIELD.setInt(player, ticks);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }
}