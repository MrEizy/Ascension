package net.zic.ascension.api.core.effect;

import com.mojang.serialization.Codec;
import net.minecraft.world.entity.LivingEntity;
import net.zic.ascension.api.datapack.TypeRegistries;

public interface SkillEffectModule {
    Codec<SkillEffectModule> CODEC = TypeRegistries.SKILL_EFFECT_MODULE_TYPE_REGISTRY.byNameCodec().dispatch(
            SkillEffectModule::getType,
            SkillEffectModuleType::codec
    );

    SkillEffectModuleType getType();

    default void onApply(LivingEntity entity, SkillEffectInstance instance) {
    }

    default void tick(LivingEntity entity, SkillEffectInstance instance) {
    }

    default void onRemove(LivingEntity entity, SkillEffectInstance instance) {
    }
}
