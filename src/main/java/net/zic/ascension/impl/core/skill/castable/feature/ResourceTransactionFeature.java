package net.zic.ascension.impl.core.skill.castable.feature;

import net.zic.ascension.api.ascension.datapack.CodecType;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.zic.ascension.api.ascension.core.resource.ResourceOperation;
import net.zic.ascension.api.ascension.core.resource.ResourceTransactionRequest;
import net.zic.ascension.api.ascension.core.resource.ResourceTransactionService;
import net.zic.ascension.api.ascension.core.resource.source.AscensionResourceSourceTags;
import net.zic.ascension.api.ascension.core.resource.source.SimpleResourceSourceIdentity;
import net.zic.ascension.api.ascension.core.skill.castable.feature.SkillExecutionContext;
import net.zic.ascension.api.ascension.core.skill.castable.feature.SkillExecutionFeature;
import net.zic.ascension.api.ascension.value.ScaledValue;
import net.zic.ascension.impl.datapack.skill.AscensionSkillExecutionFeatureTypes;

import java.util.Locale;
import java.util.Set;

public record ResourceTransactionFeature(
        Target target,
        Identifier resource,
        ResourceOperation operation,
        Identifier source,
        ScaledValue amount
) implements SkillExecutionFeature {
    public enum Target {
        CASTER,
        TARGET;

        public static final Codec<Target> CODEC = Codec.STRING.comapFlatMap(
                value -> {
                    try {
                        return DataResult.success(valueOf(value.toUpperCase(Locale.ROOT)));
                    } catch (IllegalArgumentException exception) {
                        return DataResult.error(() -> "Unknown skill resource target: " + value);
                    }
                },
                value -> value.name().toLowerCase(Locale.ROOT)
        );
    }

    public static final MapCodec<ResourceTransactionFeature> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Target.CODEC.optionalFieldOf("target", Target.TARGET).forGetter(ResourceTransactionFeature::target),
            Identifier.CODEC.fieldOf("resource").forGetter(ResourceTransactionFeature::resource),
            ResourceOperation.CODEC.fieldOf("operation").forGetter(ResourceTransactionFeature::operation),
            Identifier.CODEC.fieldOf("source").forGetter(ResourceTransactionFeature::source),
            ScaledValue.CODEC.codec().fieldOf("amount").forGetter(ResourceTransactionFeature::amount)
    ).apply(instance, ResourceTransactionFeature::new));

    @Override
    public CodecType<SkillExecutionFeature> getType() {
        return AscensionSkillExecutionFeatureTypes.RESOURCE_TRANSACTION.get();
    }

    @Override
    public void apply(SkillExecutionContext context) {
        LivingEntity entity = target == Target.CASTER ? context.caster() : context.target();
        if (entity == null) {
            return;
        }
        double resolvedAmount = amount.resolve(context.scaledValueContext());
        if (!Double.isFinite(resolvedAmount) || resolvedAmount <= 0.0D) {
            return;
        }
        ResourceTransactionService.transact(new ResourceTransactionRequest(
                entity,
                resource,
                operation,
                resolvedAmount,
                SimpleResourceSourceIdentity.of(source, AscensionResourceSourceTags.SKILL),
                context.skill(),
                context.target(),
                context.variables(),
                Set.of()
        ));
    }
}
