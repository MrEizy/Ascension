package net.zic.ascension.common.effect.frozen;

import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.zic.ascension.common.data_attachements.AscensionAttachments;
import net.zic.ascension.common.util.ModTags;

public final class FrozenStateService {
    public static final TagKey<EntityType<?>> IMMUNE = ModTags.EntityTypes.FROZEN_IMMUNE;
    public static final TagKey<EntityType<?>> RESISTANT = ModTags.EntityTypes.FROZEN_RESISTANT;
    public static final TagKey<EntityType<?>> BOSS_PROFILE = ModTags.EntityTypes.FROZEN_BOSS_PROFILE;

    private static final double NORMAL_MULTIPLIER = 1.0D;
    private static final double RESISTANT_MULTIPLIER = 0.5D;
    private static final double BOSS_MULTIPLIER = 0.25D;
    private static final double NORMAL_DECAY = 0.005D;
    private static final double FIRE_DECAY = 0.04D;

    private FrozenStateService() {
    }

    public static double apply(LivingEntity entity, double amount, int decayDelay) {
        if (entity == null
                || entity.level().isClientSide()
                || !Double.isFinite(amount)
                || amount <= 0.0D
                || is(entity, IMMUNE)) {
            return get(entity);
        }

        FrozenStateData data = data(entity);
        data.setBuildup(data.buildup() + amount * resistanceMultiplier(entity));
        data.setDecayDelay(Math.max(data.decayDelay(), decayDelay));
        syncVanillaVisual(entity, data);
        return data.buildup();
    }

    public static void maintainMinimum(LivingEntity entity, double minimum) {
        if (entity == null || entity.level().isClientSide() || !Double.isFinite(minimum)) {
            return;
        }

        FrozenStateData data = data(entity);
        if (data.buildup() < minimum) {
            data.setBuildup(minimum);
            syncVanillaVisual(entity, data);
        }
    }

    public static double get(LivingEntity entity) {
        return entity == null ? 0.0D : data(entity).buildup();
    }

    public static boolean isActive(LivingEntity entity) {
        return entity != null && data(entity).isActive();
    }

    public static boolean isResistant(LivingEntity entity) {
        return entity != null && is(entity, RESISTANT);
    }

    public static boolean isBossProfile(LivingEntity entity) {
        return entity != null && is(entity, BOSS_PROFILE);
    }

    public static void tick(LivingEntity entity) {
        if (entity == null || entity.level().isClientSide()) {
            return;
        }

        FrozenStateData data = data(entity);
        if (!data.isActive()) {
            return;
        }

        if (entity.isOnFire()) {
            data.setBuildup(data.buildup() - FIRE_DECAY);
            data.setDecayDelay(0);
        } else if (data.decayDelay() > 0) {
            data.setDecayDelay(data.decayDelay() - 1);
        } else if (data.buildup() > 0.0D) {
            data.setBuildup(data.buildup() - NORMAL_DECAY);
        }

        syncVanillaVisual(entity, data);
    }

    private static FrozenStateData data(LivingEntity entity) {
        return entity.getData(AscensionAttachments.FROZEN_STATE);
    }

    private static double resistanceMultiplier(LivingEntity entity) {
        if (is(entity, BOSS_PROFILE)) {
            return BOSS_MULTIPLIER;
        }
        if (is(entity, RESISTANT)) {
            return RESISTANT_MULTIPLIER;
        }
        return NORMAL_MULTIPLIER;
    }

    private static boolean is(LivingEntity entity, TagKey<EntityType<?>> tag) {
        return entity.getType().builtInRegistryHolder().is(tag);
    }

    private static void syncVanillaVisual(LivingEntity entity, FrozenStateData data) {
        int desired = (int) Math.round(
                entity.getTicksRequiredToFreeze() * Math.clamp(data.buildup(), 0.0D, 1.0D)
        );
        int current = entity.getTicksFrozen();
        int previousCustom = data.visualTicks();

        if (current <= previousCustom || desired > current) {
            entity.setTicksFrozen(desired);
        }
        data.setVisualTicks(desired);
    }
}
