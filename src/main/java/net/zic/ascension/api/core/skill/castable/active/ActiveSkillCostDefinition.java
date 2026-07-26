package net.zic.ascension.api.core.skill.castable.active;

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
import net.zic.ascension.api.core.resource.source.AscensionResourceSourceTags;
import net.zic.ascension.api.core.resource.source.SimpleResourceSourceIdentity;
import net.zic.ascension.api.value.ScaledValue;
import net.zic.ascension.api.value.ScaledValueContext;

import java.util.Map;
import java.util.Set;

public record ActiveSkillCostDefinition(
        Identifier resource,
        Identifier source,
        ScaledValue amount
) {
    public static final MapCodec<ActiveSkillCostDefinition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Identifier.CODEC.fieldOf("resource").forGetter(ActiveSkillCostDefinition::resource),
            Identifier.CODEC.optionalFieldOf("source", AscensionCraft.prefix("skill_casting"))
                    .forGetter(ActiveSkillCostDefinition::source),
            ScaledValue.CODEC.codec().fieldOf("amount").forGetter(ActiveSkillCostDefinition::amount)
    ).apply(instance, ActiveSkillCostDefinition::new));

    public boolean canPay(
            LivingEntity caster,
            Identifier skill,
            LivingEntity target,
            ScaledValueContext context,
            Map<Identifier, Double> variables
    ) {
        return transact(caster, skill, target, context, variables, true);
    }

    public boolean pay(
            LivingEntity caster,
            Identifier skill,
            LivingEntity target,
            ScaledValueContext context,
            Map<Identifier, Double> variables
    ) {
        return transact(caster, skill, target, context, variables, false);
    }

    private boolean transact(
            LivingEntity caster,
            Identifier skill,
            LivingEntity target,
            ScaledValueContext context,
            Map<Identifier, Double> variables,
            boolean simulate
    ) {
        double resolvedAmount = amount.resolve(context);
        if (!Double.isFinite(resolvedAmount) || resolvedAmount <= 1.0E-8D) {
            return true;
        }
        Set<ResourceTransactionFlag> flags = simulate
                ? Set.of(ResourceTransactionFlag.SIMULATE)
                : Set.of();
        ResourceTransactionResult result = ResourceTransactionService.transact(new ResourceTransactionRequest(
                caster,
                resource,
                ResourceOperation.CONSUME,
                resolvedAmount,
                SimpleResourceSourceIdentity.of(source, AscensionResourceSourceTags.SKILL),
                skill,
                target,
                variables,
                flags
        ));
        return result.succeeded() && result.appliedAmount() + 1.0E-8D >= result.resolvedAmount();
    }
}
