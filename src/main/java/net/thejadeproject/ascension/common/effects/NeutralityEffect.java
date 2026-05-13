package net.thejadeproject.ascension.common.effects;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;

public class NeutralityEffect extends MobEffect {
    private static final TargetingConditions NEUTRALITY_CONDITIONS = TargetingConditions.forCombat().ignoreLineOfSight();

    public NeutralityEffect(MobEffectCategory category, int color) {
        super(category, color);
    }

    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        if (entity instanceof Player player && !player.level().isClientSide()) {
            var nearbyMobs = player.level().getNearbyEntities(Mob.class,
                    NEUTRALITY_CONDITIONS.range(16.0),
                    player,
                    player.getBoundingBox().inflate(16.0)
            );

            for (Mob mob : nearbyMobs) {
                if (mob.getTarget() == player) {
                    mob.setTarget(null);
                }
                if (NEUTRALITY_CONDITIONS.test(mob, player)) {
                    mob.setTarget(null);
                    mob.setAggressive(false);
                }
            }
        }
        return true;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return true;
    }
}