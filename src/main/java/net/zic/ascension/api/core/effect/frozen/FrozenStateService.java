package net.zic.ascension.api.core.effect.frozen;

import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.zic.ascension.common.data_attachements.AscensionAttachments;
import net.zic.ascension.common.util.ModTags;

public final class FrozenStateService {
    public static final TagKey<EntityType<?>> IMMUNE = ModTags.EntityTypes.FROZEN_IMMUNE;

    public static final TagKey<EntityType<?>> RESISTANT = ModTags.EntityTypes.FROZEN_RESISTANT;

    public static final TagKey<EntityType<?>> BOSS_PROFILE = ModTags.EntityTypes.FROZEN_BOSS_PROFILE;

    private FrozenStateService() {
    }

    public static double apply(LivingEntity entity, double amount, int decayDelay) {
        if (is(entity, IMMUNE) || amount <= 0.0D) {
            return get(entity);
        }

        double multiplier;

        if (is(entity, BOSS_PROFILE)) {
            multiplier = 0.25D;
        } else if (is(entity, RESISTANT)) {
            multiplier = 0.5D;
        } else {
            multiplier = 1.0D;
        }

        FrozenStateData data = entity.getData(AscensionAttachments.FROZEN_STATE);

        data.setBuildup(data.buildup() + amount * multiplier);

        data.setDecayDelay(Math.max(data.decayDelay(), decayDelay));

        syncVanillaVisual(entity, data.buildup());

        return data.buildup();
    }

    public static void maintainMinimum(LivingEntity entity, double minimum) {
        FrozenStateData data = entity.getData(AscensionAttachments.FROZEN_STATE);

        if (data.buildup() < minimum) {
            data.setBuildup(minimum);
            syncVanillaVisual(entity, data.buildup());
        }
    }

    public static double get(LivingEntity entity) {
        return entity.getData(AscensionAttachments.FROZEN_STATE).buildup();
    }

    public static void tick(LivingEntity entity) {
        FrozenStateData data = entity.getData(AscensionAttachments.FROZEN_STATE);

        if (entity.isOnFire()) {
            data.setBuildup(data.buildup() - 0.04D);
            data.setDecayDelay(0);
        } else if (data.decayDelay() > 0) {
            data.setDecayDelay(data.decayDelay() - 1);
        } else if (data.buildup() > 0.0D) {
            data.setBuildup(data.buildup() - 0.005D);
        }

        syncVanillaVisual(entity, data.buildup());
    }

    private static boolean is(LivingEntity entity, TagKey<EntityType<?>> tag) {
        return entity.getType().builtInRegistryHolder().is(tag);
    }

    private static void syncVanillaVisual(LivingEntity entity, double buildup) {
        entity.setTicksFrozen((int) Math.round(entity.getTicksRequiredToFreeze() * Math.clamp(buildup, 0.0D, 1.0D)));
    }
}