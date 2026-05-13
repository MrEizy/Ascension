package net.thejadeproject.ascension.common.effects;

import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.effect.MobEffects;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CleanseEffect extends MobEffect {
    // Tiered lists of effects that should be cleared by this cleanse effect
    private static final List<Holder<MobEffect>> TIER_1_EFFECTS = Arrays.asList(
            MobEffects.POISON,
            MobEffects.WEAKNESS
    );

    private static final List<Holder<MobEffect>> TIER_2_EFFECTS = Arrays.asList(
            MobEffects.POISON,
            MobEffects.WEAKNESS,
            MobEffects.MOVEMENT_SLOWDOWN,
            MobEffects.DIG_SLOWDOWN,
            MobEffects.HUNGER
    );

    private static final List<Holder<MobEffect>> TIER_3_EFFECTS = Arrays.asList(
            MobEffects.POISON,
            MobEffects.WEAKNESS,
            MobEffects.MOVEMENT_SLOWDOWN,
            MobEffects.DIG_SLOWDOWN,
            MobEffects.HUNGER,
            MobEffects.BLINDNESS,
            MobEffects.LEVITATION,
            MobEffects.CONFUSION
    );

    private static final List<Holder<MobEffect>> TIER_4_EFFECTS = Arrays.asList(
            MobEffects.POISON,
            MobEffects.WEAKNESS,
            MobEffects.MOVEMENT_SLOWDOWN,
            MobEffects.DIG_SLOWDOWN,
            MobEffects.HUNGER,
            MobEffects.BLINDNESS,
            MobEffects.LEVITATION,
            MobEffects.CONFUSION,
            MobEffects.WITHER,
            MobEffects.UNLUCK,
            MobEffects.BAD_OMEN
    );

    public CleanseEffect(MobEffectCategory category, int color) {
        super(category, color);
    }

    // Called every tick the effect is active
    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        // Get the appropriate tier list based on amplifier (0-3)
        List<Holder<MobEffect>> effectsToCleanse = getEffectsForTier(amplifier);

        // Remove only the effects in our tier-specific cleanse list
        ArrayList<MobEffectInstance> effectsToRemove = new ArrayList<>();

        for (MobEffectInstance effectInstance : entity.getActiveEffects()) {
            Holder<MobEffect> effectHolder = effectInstance.getEffect();

            // Compare the Holder objects directly
            if (effectsToCleanse.contains(effectHolder)) {
                effectsToRemove.add(effectInstance);
            }
        }

        for (MobEffectInstance effectInstance : effectsToRemove) {
            entity.removeEffect(effectInstance.getEffect());
        }

        return true;
    }

    /**
     * Get the list of effects to cleanse based on the amplifier (tier)
     *
     * @param amplifier The amplifier level (0-3)
     * @return The list of effects for that tier
     */
    private List<Holder<MobEffect>> getEffectsForTier(int amplifier) {
        // Clamp amplifier to valid range 0-3
        int tier = Math.min(Math.max(amplifier, 0), 3);

        switch (tier) {
            case 0: return TIER_1_EFFECTS;
            case 1: return TIER_2_EFFECTS;
            case 2: return TIER_3_EFFECTS;
            case 3: return TIER_4_EFFECTS;
            default: return TIER_1_EFFECTS; // Fallback
        }
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        // Only apply effect every 20 ticks (1 second) to prevent spam
        return duration % 20 == 0;
    }
}