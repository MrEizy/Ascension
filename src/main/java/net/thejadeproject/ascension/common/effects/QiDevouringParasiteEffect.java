package net.thejadeproject.ascension.common.effects;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.thejadeproject.ascension.data_attachments.ModAttachments;
import net.thejadeproject.ascension.refactor_packages.entity_data.IEntityData;
import net.thejadeproject.ascension.refactor_packages.qi.EntityQiContainer;

public class QiDevouringParasiteEffect extends MobEffect {

    public QiDevouringParasiteEffect(MobEffectCategory category, int color) {
        super(category, color);
    }

    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        if (entity instanceof Player player && !player.level().isClientSide()) {
            int drainAmount = (amplifier + 1) * 25;
            IEntityData entityData = player.getData(ModAttachments.ENTITY_DATA);
            if (entityData != null) {
                EntityQiContainer qi = entityData.getQiContainer();
                double currentQi = qi.getCurrentQi();
                if (currentQi >= drainAmount) {
                    qi.tryConsumeQi(drainAmount);
                } else {
                    double qiDrained = currentQi;
                    qi.tryConsumeQi(qiDrained);
                    float healthDamage = (float)(drainAmount - qiDrained);
                    player.hurt(player.damageSources().magic(), healthDamage);
                }
            } else {
                player.hurt(player.damageSources().magic(), drainAmount);
            }
        }
        return true;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        int interval = Math.max(10, 20 - (amplifier * 3));
        return duration % interval == 0;
    }
}