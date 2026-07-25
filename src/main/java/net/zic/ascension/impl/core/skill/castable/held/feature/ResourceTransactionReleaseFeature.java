package net.zic.ascension.impl.core.skill.castable.held.feature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.zic.ascension.api.core.resource.ResourceOperation;
import net.zic.ascension.api.core.resource.ResourceTransactionRequest;
import net.zic.ascension.api.core.resource.ResourceTransactionService;
import net.zic.ascension.api.core.resource.source.SimpleResourceSourceIdentity;
import net.zic.ascension.api.core.skill.castable.held.feature.HeldCastReleaseContext;
import net.zic.ascension.api.core.skill.castable.held.feature.HeldCastReleaseFeature;
import net.zic.ascension.api.core.skill.castable.held.feature.HeldCastReleaseFeatureType;
import net.zic.ascension.api.value.ScaledValue;
import net.zic.ascension.impl.datapack.skill.castable.held.AscensionHeldCastReleaseFeatureTypes;
import net.zic.ascension.impl.resource.AscensionResourceSourceTags;

import java.util.Locale;
import java.util.Map;
import java.util.Set;

public record ResourceTransactionReleaseFeature(
        Target target,
        Identifier resource,
        ResourceOperation operation,
        Identifier source,
        ScaledValue amount
) implements HeldCastReleaseFeature {
    public enum Target {
        CASTER,
        TARGET;

        public static final Codec<Target> CODEC = Codec.STRING.comapFlatMap(
                value -> {
                    try {
                        return DataResult.success(valueOf(value.toUpperCase(Locale.ROOT)));
                    } catch (IllegalArgumentException exception) {
                        return DataResult.error(() -> "Unknown held-cast resource target: " + value);
                    }
                },
                value -> value.name().toLowerCase(Locale.ROOT)
        );
    }

    public static final MapCodec<ResourceTransactionReleaseFeature> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Target.CODEC.optionalFieldOf("target", Target.TARGET).forGetter(ResourceTransactionReleaseFeature::target),
            Identifier.CODEC.fieldOf("resource").forGetter(ResourceTransactionReleaseFeature::resource),
            ResourceOperation.CODEC.fieldOf("operation").forGetter(ResourceTransactionReleaseFeature::operation),
            Identifier.CODEC.fieldOf("source").forGetter(ResourceTransactionReleaseFeature::source),
            ScaledValue.CODEC.codec().fieldOf("amount").forGetter(ResourceTransactionReleaseFeature::amount)
    ).apply(instance, ResourceTransactionReleaseFeature::new));

    @Override
    public HeldCastReleaseFeatureType getType() {
        return AscensionHeldCastReleaseFeatureTypes.RESOURCE_TRANSACTION.get();
    }

    @Override
    public void apply(HeldCastReleaseContext context) {
        LivingEntity entity = target == Target.CASTER
                ? context.execution().caster()
                : context.target();
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
                context.execution().skill(),
                context.target(),
                Map.of(),
                Set.of()
        ));
    }
}
