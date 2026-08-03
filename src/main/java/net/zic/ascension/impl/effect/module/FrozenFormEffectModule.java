package net.zic.ascension.impl.effect.module;

import net.zic.ascension.api.ascension.datapack.CodecType;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.core.effect.SkillEffectContext;
import net.zic.ascension.api.core.effect.SkillEffectModule;
import net.zic.ascension.api.value.ScaledValue;
import net.zic.ascension.api.value.ScaledValueContext;
import net.zic.ascension.common.effect.frozen.FrozenStateService;
import net.zic.ascension.impl.datapack.effect.AscensionSkillEffectModuleTypes;

import java.util.Map;

public record FrozenFormEffectModule(
        ScaledValue frozenFloor,
        ScaledValue movementReduction,
        double playerMultiplier,
        double resistantMultiplier,
        double bossMultiplier,
        boolean removeWhenBurning
) implements SkillEffectModule {
    private static final net.minecraft.resources.Identifier EFFECT_POTENCY = AscensionCraft.prefix("effect_potency");

    public static final MapCodec<FrozenFormEffectModule> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ScaledValue.CODEC.codec().fieldOf("frozen_floor").forGetter(FrozenFormEffectModule::frozenFloor),
            ScaledValue.CODEC.codec().fieldOf("movement_reduction").forGetter(FrozenFormEffectModule::movementReduction),
            Codec.DOUBLE.optionalFieldOf("player_multiplier", 0.6D).forGetter(FrozenFormEffectModule::playerMultiplier),
            Codec.DOUBLE.optionalFieldOf("resistant_multiplier", 0.5D).forGetter(FrozenFormEffectModule::resistantMultiplier),
            Codec.DOUBLE.optionalFieldOf("boss_multiplier", 0.3D).forGetter(FrozenFormEffectModule::bossMultiplier),
            Codec.BOOL.optionalFieldOf("remove_when_burning", true).forGetter(FrozenFormEffectModule::removeWhenBurning)
    ).apply(instance, FrozenFormEffectModule::new));

    public FrozenFormEffectModule {
        playerMultiplier = sanitizeMultiplier(playerMultiplier);
        resistantMultiplier = sanitizeMultiplier(resistantMultiplier);
        bossMultiplier = sanitizeMultiplier(bossMultiplier);
    }

    @Override
    public CodecType<SkillEffectModule> getType() {
        return AscensionSkillEffectModuleTypes.FROZEN_FORM.get();
    }

    @Override
    public void tick(LivingEntity entity, SkillEffectContext effect) {
        double profile = profileMultiplier(entity);
        ScaledValueContext context = new ScaledValueContext(
                null,
                effect.sourceSkill(),
                entity,
                entity,
                effect.potency(),
                Map.of(EFFECT_POTENCY, effect.potency())
        );
        FrozenStateService.maintainMinimum(
                entity,
                Math.clamp(frozenFloor.resolve(context) * profile, 0.0D, 1.0D)
        );
        double reduction = Math.clamp(movementReduction.resolve(context) * profile, 0.0D, 0.9D);
        Vec3 velocity = entity.getDeltaMovement();
        entity.setDeltaMovement(
                velocity.x * (1.0D - reduction),
                velocity.y,
                velocity.z * (1.0D - reduction)
        );
    }

    @Override
    public boolean shouldRemove(LivingEntity entity, SkillEffectContext context) {
        return removeWhenBurning && entity.isOnFire();
    }

    private double profileMultiplier(LivingEntity entity) {
        if (FrozenStateService.isBossProfile(entity)) {
            return bossMultiplier;
        }
        if (FrozenStateService.isResistant(entity)) {
            return resistantMultiplier;
        }
        if (entity instanceof Player) {
            return playerMultiplier;
        }
        return 1.0D;
    }

    private static double sanitizeMultiplier(double value) {
        return Double.isFinite(value) ? Math.clamp(value, 0.0D, 1.0D) : 1.0D;
    }
}
