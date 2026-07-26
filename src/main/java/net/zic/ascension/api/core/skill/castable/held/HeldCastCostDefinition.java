package net.zic.ascension.api.core.skill.castable.held;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.core.resource.ResourceOperation;
import net.zic.ascension.api.core.resource.ResourceTransactionFlag;
import net.zic.ascension.api.core.resource.ResourceTransactionRequest;
import net.zic.ascension.api.core.resource.ResourceTransactionResult;
import net.zic.ascension.api.core.resource.ResourceTransactionService;
import net.zic.ascension.api.core.resource.source.SimpleResourceSourceIdentity;
import net.zic.ascension.api.value.ScaledValue;
import net.zic.ascension.api.value.ScaledValueContext;
import net.zic.ascension.api.core.resource.source.AscensionResourceSourceTags;

import java.util.Map;
import java.util.Set;

public record HeldCastCostDefinition(
        Identifier resource,
        Identifier source,
        ScaledValue cumulativeCost,
        boolean releaseOnFailure
) {
    public static final Identifier CHARGE_VALUE = AscensionCraft.prefix("cast/charge");
    public static final Identifier CHARGE_TICKS_VALUE = AscensionCraft.prefix("cast/charge_ticks");
    public static final Identifier MAXIMUM_CHARGE_TICKS_VALUE = AscensionCraft.prefix("cast/maximum_charge_ticks");

    public static final MapCodec<HeldCastCostDefinition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Identifier.CODEC.fieldOf("resource").forGetter(HeldCastCostDefinition::resource),
            Identifier.CODEC.optionalFieldOf("source", AscensionCraft.prefix("skill_casting"))
                    .forGetter(HeldCastCostDefinition::source),
            ScaledValue.CODEC.codec().fieldOf("cumulative_cost").forGetter(HeldCastCostDefinition::cumulativeCost),
            Codec.BOOL.optionalFieldOf("release_on_failure", true).forGetter(HeldCastCostDefinition::releaseOnFailure)
    ).apply(instance, HeldCastCostDefinition::new));

    public double resolveCumulativeCost(ScaledValueContext context) {
        double value = cumulativeCost.resolve(context);
        return Double.isFinite(value) ? Math.max(0.0D, value) : 0.0D;
    }

    public ResourceTransactionResult payIncrement(
            LivingEntity caster,
            Identifier skill,
            HeldCastData data,
            ScaledValueContext context,
            int maximumChargeTicks
    ) {
        double target = resolveCumulativeCost(context);
        double delta = Math.max(0.0D, target - data.getCumulativeCostTarget());
        if (delta <= 1.0E-8D) {
            data.setCumulativeCostTarget(target);
            return null;
        }

        ResourceTransactionRequest request = new ResourceTransactionRequest(
                caster,
                resource,
                ResourceOperation.CONSUME,
                delta,
                SimpleResourceSourceIdentity.of(source, AscensionResourceSourceTags.SKILL),
                skill,
                null,
                Map.of(
                        CHARGE_VALUE, context.charge(),
                        CHARGE_TICKS_VALUE, (double) data.getChargeTicks(),
                        MAXIMUM_CHARGE_TICKS_VALUE, (double) maximumChargeTicks
                ),
                Set.of()
        );
        ResourceTransactionResult result = ResourceTransactionService.transact(request);
        if (result.succeeded() && result.appliedAmount() + 1.0E-8D >= result.resolvedAmount()) {
            data.setCumulativeCostTarget(target);
        }
        return result;
    }

    public boolean canStart(LivingEntity caster, Identifier skill, ScaledValueContext context, int maximumChargeTicks) {
        double target = resolveCumulativeCost(context);
        if (target <= 1.0E-8D) {
            return true;
        }
        ResourceTransactionRequest request = new ResourceTransactionRequest(
                caster,
                resource,
                ResourceOperation.CONSUME,
                target,
                SimpleResourceSourceIdentity.of(source, AscensionResourceSourceTags.SKILL),
                skill,
                null,
                Map.of(
                        CHARGE_VALUE, context.charge(),
                        CHARGE_TICKS_VALUE, 1.0D,
                        MAXIMUM_CHARGE_TICKS_VALUE, (double) maximumChargeTicks
                ),
                Set.of(ResourceTransactionFlag.SIMULATE)
        );
        ResourceTransactionResult result = ResourceTransactionService.transact(request);
        return result.succeeded() && result.appliedAmount() + 1.0E-8D >= result.resolvedAmount();
    }
}
