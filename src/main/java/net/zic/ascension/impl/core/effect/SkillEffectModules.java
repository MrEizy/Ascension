package net.zic.ascension.impl.core.effect;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.ascension.core.effect.SkillEffectContext;
import net.zic.ascension.api.ascension.core.effect.SkillEffectModule;
import net.zic.ascension.api.ascension.core.resource.ResourceTransactionService;
import net.zic.ascension.api.ascension.core.resource.ResourceModifiers;
import net.zic.ascension.api.ascension.datapack.CodecType;
import net.zic.ascension.api.ascension.value.ScaledValue;
import net.zic.ascension.impl.datapack.effect.AscensionSkillEffectModuleTypes;

import java.util.Map;
import net.zic.ascension.api.ascension.core.resource.ResourceTransactionRequest;

public final class SkillEffectModules {
    private static final Identifier EFFECT_POTENCY = AscensionCraft.prefix("effect_potency");
    private static final Identifier EFFECT_STACKS = AscensionCraft.prefix("effect_stacks");

    private SkillEffectModules() {
    }

    public record FrozenForm(
            ScaledValue frozenFloor,
            ScaledValue movementReduction,
            double playerMultiplier,
            double resistantMultiplier,
            double bossMultiplier,
            boolean removeWhenBurning
    ) implements SkillEffectModule {
        public static final MapCodec<FrozenForm> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                ScaledValue.COMPACT_CODEC.fieldOf("frozen_floor").forGetter(FrozenForm::frozenFloor),
                ScaledValue.COMPACT_CODEC.fieldOf("movement_reduction").forGetter(FrozenForm::movementReduction),
                Codec.DOUBLE.optionalFieldOf("player_multiplier", 0.6D).forGetter(FrozenForm::playerMultiplier),
                Codec.DOUBLE.optionalFieldOf("resistant_multiplier", 0.5D).forGetter(FrozenForm::resistantMultiplier),
                Codec.DOUBLE.optionalFieldOf("boss_multiplier", 0.3D).forGetter(FrozenForm::bossMultiplier),
                Codec.BOOL.optionalFieldOf("remove_when_burning", true).forGetter(FrozenForm::removeWhenBurning)
        ).apply(instance, FrozenForm::new));

        public FrozenForm {
            playerMultiplier = multiplier(playerMultiplier);
            resistantMultiplier = multiplier(resistantMultiplier);
            bossMultiplier = multiplier(bossMultiplier);
        }

        @Override
        public CodecType<SkillEffectModule> getType() {
            return AscensionSkillEffectModuleTypes.FROZEN_FORM.get();
        }

        @Override
        public void tick(LivingEntity entity, SkillEffectContext effect) {
            double profile = profileMultiplier(entity);
            ScaledValue.Context context = new ScaledValue.Context(
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
            return entity instanceof Player ? playerMultiplier : 1.0D;
        }

        private static double multiplier(double value) {
            return Double.isFinite(value) ? Math.clamp(value, 0.0D, 1.0D) : 1.0D;
        }
    }

    public record ResourceModifierModule(
            Identifier id,
            ResourceTransactionRequest.Selector selector,
            ResourceModifiers.Operation operation,
            ScaledValue value,
            int priority
    ) implements SkillEffectModule {
        public static final MapCodec<ResourceModifierModule> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Identifier.CODEC.fieldOf("id").forGetter(ResourceModifierModule::id),
                ResourceTransactionRequest.Selector.CODEC.fieldOf("selector").forGetter(ResourceModifierModule::selector),
                ResourceModifiers.Operation.CODEC.fieldOf("operation").forGetter(ResourceModifierModule::operation),
                ScaledValue.COMPACT_CODEC.optionalFieldOf("value", ScaledValue.constant(0.0D)).forGetter(ResourceModifierModule::value),
                Codec.INT.optionalFieldOf("priority", 0).forGetter(ResourceModifierModule::priority)
        ).apply(instance, ResourceModifierModule::new));

        @Override
        public CodecType<SkillEffectModule> getType() {
            return AscensionSkillEffectModuleTypes.RESOURCE_MODIFIER.get();
        }

        public boolean matches(ResourceTransactionService.Context context) {
            return selector.matches(context);
        }

        public ResourceModifiers.Entry resolve(ResourceTransactionService.Context context, SkillEffectContext effect) {
            ScaledValue.Context scaledContext = new ScaledValue.Context(
                    null,
                    effect.sourceSkill(),
                    context.request().entity(),
                    context.request().target(),
                    effect.potency(),
                    Map.of(EFFECT_POTENCY, effect.potency(), EFFECT_STACKS, (double) effect.stacks())
            );
            Identifier resolvedId = Identifier.fromNamespaceAndPath(
                    id.getNamespace(),
                    "skill_effect/" + effect.definition().getNamespace() + "/" + effect.definition().getPath() + "/" + id.getPath()
            );
            return new ResourceModifiers.Entry(resolvedId, operation, value.resolve(scaledContext), priority);
        }
    }
}
