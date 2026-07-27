package net.zic.ascension.api.core.effect;

import net.zic.ascension.api.datapack.CodecType;
import com.mojang.serialization.Codec;
import net.minecraft.world.entity.LivingEntity;
import net.zic.ascension.api.datapack.TypeRegistries;

public interface SkillEffectModule {
    Codec<SkillEffectModule> CODEC = TypeRegistries.SKILL_EFFECT_MODULE_TYPE_REGISTRY.byNameCodec().dispatch(
            SkillEffectModule::getType,
            CodecType<SkillEffectModule>::codec
    );

    CodecType<SkillEffectModule> getType();

    default void onApply(LivingEntity entity, SkillEffectContext context) {
    }

    default void onUpdate(LivingEntity entity, SkillEffectContext context) {
    }

    default void tick(LivingEntity entity, SkillEffectContext context) {
    }

    default boolean shouldRemove(LivingEntity entity, SkillEffectContext context) {
        return false;
    }

    default void onRemove(LivingEntity entity, SkillEffectContext context) {
    }
}
