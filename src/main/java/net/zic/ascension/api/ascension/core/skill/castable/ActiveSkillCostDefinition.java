package net.zic.ascension.api.ascension.core.skill.castable;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.ascension.core.resource.ResourceOperation;
import net.zic.ascension.api.ascension.core.resource.ResourceTransactionRequest;
import net.zic.ascension.api.ascension.core.resource.ResourceTransactionService;
import net.zic.ascension.api.ascension.value.ScaledValue;

import java.util.List;
import java.util.Map;
import java.util.Set;
import net.zic.ascension.api.ascension.core.resource.ResourceSourceIdentity;

public record ActiveSkillCostDefinition(
        Identifier resource,
        ResourceOperation operation,
        Identifier source,
        ScaledValue amount
) {
    public static final MapCodec<ActiveSkillCostDefinition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Identifier.CODEC.fieldOf("resource").forGetter(ActiveSkillCostDefinition::resource),
            ResourceOperation.CODEC.optionalFieldOf("operation", ResourceOperation.CONSUME).forGetter(ActiveSkillCostDefinition::operation),
            Identifier.CODEC.optionalFieldOf("source", AscensionCraft.prefix("skill_casting")).forGetter(ActiveSkillCostDefinition::source),
            ScaledValue.COMPACT_CODEC.fieldOf("amount").forGetter(ActiveSkillCostDefinition::amount)
    ).apply(instance, ActiveSkillCostDefinition::new));
    public static final Codec<List<ActiveSkillCostDefinition>> LIST_CODEC = Codec.either(
            CODEC.codec(),
            CODEC.codec().listOf()
    ).xmap(
            value -> value.map(List::of, values -> List.copyOf(values)),
            values -> values.size() == 1 ? Either.left(values.getFirst()) : Either.right(List.copyOf(values))
    );

    public boolean canPay(
            LivingEntity caster,
            Identifier skill,
            LivingEntity target,
            ScaledValue.Context context,
            Map<Identifier, Double> variables
    ) {
        return transact(caster, skill, target, context, variables, true);
    }

    public boolean pay(
            LivingEntity caster,
            Identifier skill,
            LivingEntity target,
            ScaledValue.Context context,
            Map<Identifier, Double> variables
    ) {
        return transact(caster, skill, target, context, variables, false);
    }

    private boolean transact(
            LivingEntity caster,
            Identifier skill,
            LivingEntity target,
            ScaledValue.Context context,
            Map<Identifier, Double> variables,
            boolean simulate
    ) {
        double resolvedAmount = amount.resolve(context);
        if (!Double.isFinite(resolvedAmount) || resolvedAmount <= 1.0E-8D) {
            return true;
        }
        Set<ResourceTransactionRequest.Flag> flags = simulate ? Set.of(ResourceTransactionRequest.Flag.SIMULATE) : Set.of();
        ResourceTransactionService.Result result = ResourceTransactionService.transact(new ResourceTransactionRequest(
                caster,
                resource,
                operation,
                resolvedAmount,
                ResourceSourceIdentity.of(source, ResourceSourceIdentity.Tags.SKILL),
                skill,
                target,
                variables,
                flags
        ));
        return result.succeeded() && result.appliedAmount() + 1.0E-8D >= result.resolvedAmount();
    }
}
