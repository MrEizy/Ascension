package net.thejadeproject.ascension.common.effects;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.thejadeproject.ascension.data_attachments.ModAttachments;
import net.thejadeproject.ascension.refactor_packages.entity_data.IEntityData;

public class QiEnhancedRegeneration extends MobEffect {
    public QiEnhancedRegeneration(MobEffectCategory category, int color) {
        super(category, color);
    }


    @Override
    public boolean applyEffectTick(LivingEntity livingEntity, int amplifier) {
        if (livingEntity.getHealth() < livingEntity.getMaxHealth()) {
            float healAmount = 5.f * (amplifier + 1);
            if (livingEntity instanceof ServerPlayer player) {
                IEntityData entityData = player.getData(ModAttachments.ENTITY_DATA);
                if (entityData != null && entityData.getQiContainer().tryConsumeQi(healAmount)) {
                    player.heal(healAmount);
                }
            } else livingEntity.heal(healAmount);
        }

        return super.applyEffectTick(livingEntity, amplifier);
    }


    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        int interval = 10;
        return duration % interval == 0;
    }
}