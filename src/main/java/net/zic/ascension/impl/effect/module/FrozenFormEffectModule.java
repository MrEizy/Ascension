package net.zic.ascension.impl.effect.module;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.zic.ascension.api.core.effect.SkillEffectInstance;
import net.zic.ascension.api.core.effect.SkillEffectModule;
import net.zic.ascension.api.core.effect.SkillEffectModuleType;
import net.zic.ascension.api.core.effect.frozen.FrozenStateService;
import net.zic.ascension.api.value.ScaledValue;
import net.zic.ascension.api.value.ScaledValueContext;
import net.zic.ascension.impl.datapack.effect.AscensionSkillEffectModuleTypes;

public record FrozenFormEffectModule(
        ScaledValue frozenFloor,
        ScaledValue movementReduction
) implements SkillEffectModule {
    public static final MapCodec<FrozenFormEffectModule> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ScaledValue.CODEC.codec().fieldOf("frozen_floor").forGetter(FrozenFormEffectModule::frozenFloor),
            ScaledValue.CODEC.codec().fieldOf("movement_reduction").forGetter(FrozenFormEffectModule::movementReduction)
    ).apply(instance, FrozenFormEffectModule::new));

    @Override
    public SkillEffectModuleType getType() {
        return AscensionSkillEffectModuleTypes.FROZEN_FORM.get();
    }

    @Override
    public void tick(LivingEntity entity, SkillEffectInstance effect) {
        ScaledValueContext context = new ScaledValueContext(null, effect.sourceSkill(), entity, entity, effect.potency(), java.util.Map.of(net.zic.ascension.AscensionCraft.prefix("effect_potency"), effect.potency()));
        FrozenStateService.maintainMinimum(entity, Math.clamp(frozenFloor.resolve(context), 0.0D, 1.0D));
        double reduction = Math.clamp(movementReduction.resolve(context), 0.0D, 0.9D);
        Vec3 velocity = entity.getDeltaMovement();
        entity.setDeltaMovement(velocity.x * (1.0D - reduction), velocity.y, velocity.z * (1.0D - reduction));
    }
}
