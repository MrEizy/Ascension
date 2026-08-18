package net.zic.ascension.api.ascension.core.runtime;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import net.zic.ascension.api.ascension.core.skill.castable.action.SkillAction;
import net.zic.ascension.api.ascension.core.skill.DefinitionRef;
import net.zic.ascension.api.ascension.datapack.CodecHelpers;
import net.zic.ascension.api.ascension.value.ScaledValue;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import net.zic.ascension.api.ascension.core.projectile.NormalProjectileDefinition;

public record OwnerBoundConstructDefinition(
        ScaledValue duration,
        ScaledValue stability,
        Vec3 offset,
        boolean rotateWithOwner,
        Optional<DefinitionRef<RuntimeVisualDefinition>> visual,
        List<VisualStage> visualStages,
        Optional<Interception> interception,
        List<SkillAction> onBreak,
        List<SkillAction> onExpire
) {
    public static final Codec<OwnerBoundConstructDefinition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ScaledValue.COMPACT_CODEC.fieldOf("duration").forGetter(OwnerBoundConstructDefinition::duration),
            ScaledValue.COMPACT_CODEC.fieldOf("stability").forGetter(OwnerBoundConstructDefinition::stability),
            CodecHelpers.VEC3.optionalFieldOf("offset", Vec3.ZERO).forGetter(OwnerBoundConstructDefinition::offset),
            Codec.BOOL.optionalFieldOf("rotate_with_owner", true).forGetter(OwnerBoundConstructDefinition::rotateWithOwner),
            DefinitionRef.codec(RuntimeVisualDefinition.CODEC).optionalFieldOf("visual").forGetter(OwnerBoundConstructDefinition::visual),
            VisualStage.CODEC.listOf().optionalFieldOf("visual_stages", List.of()).forGetter(OwnerBoundConstructDefinition::visualStages),
            Interception.CODEC.optionalFieldOf("interception").forGetter(OwnerBoundConstructDefinition::interception),
            SkillAction.CODEC.listOf().optionalFieldOf("on_break", List.of()).forGetter(OwnerBoundConstructDefinition::onBreak),
            SkillAction.CODEC.listOf().optionalFieldOf("on_expire", List.of()).forGetter(OwnerBoundConstructDefinition::onExpire)
    ).apply(instance, OwnerBoundConstructDefinition::new));

    public OwnerBoundConstructDefinition {
        visual = visual == null ? Optional.empty() : visual;
        visualStages = visualStages == null
                ? List.of()
                : visualStages.stream()
                .sorted(Comparator.comparingDouble(VisualStage::maximumStabilityFraction))
                .toList();
        interception = interception == null ? Optional.empty() : interception;
        onBreak = onBreak == null ? List.of() : List.copyOf(onBreak);
        onExpire = onExpire == null ? List.of() : List.copyOf(onExpire);
    }

    public record VisualStage(double maximumStabilityFraction, DefinitionRef<RuntimeVisualDefinition> visual) {
        public static final Codec<VisualStage> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.doubleRange(0.0D, 1.0D).fieldOf("maximum_stability_fraction").forGetter(VisualStage::maximumStabilityFraction),
                DefinitionRef.codec(RuntimeVisualDefinition.CODEC).fieldOf("visual").forGetter(VisualStage::visual)
        ).apply(instance, VisualStage::new));
    }

    public record Interception(
            ScaledValue absorption,
            ScaledValue stabilityCost,
            boolean overflow,
            int priority,
            BarrierDefinition.DamageFilter filter,
            NormalProjectileDefinition.ImpactResponse projectileResponse,
            List<SkillAction> onIntercept
    ) {
        public static final Codec<Interception> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ScaledValue.COMPACT_CODEC.optionalFieldOf("absorption", ScaledValue.constant(1.0D)).forGetter(Interception::absorption),
                ScaledValue.COMPACT_CODEC.optionalFieldOf("stability_cost", ScaledValue.constant(1.0D)).forGetter(Interception::stabilityCost),
                Codec.BOOL.optionalFieldOf("overflow", true).forGetter(Interception::overflow),
                Codec.INT.optionalFieldOf("priority", 0).forGetter(Interception::priority),
                BarrierDefinition.DamageFilter.CODEC.optionalFieldOf("filter", BarrierDefinition.DamageFilter.EMPTY).forGetter(Interception::filter),
                NormalProjectileDefinition.ImpactResponse.CODEC.optionalFieldOf("projectile_response", NormalProjectileDefinition.ImpactResponse.DEFLECT).forGetter(Interception::projectileResponse),
                SkillAction.CODEC.listOf().optionalFieldOf("on_intercept", List.of()).forGetter(Interception::onIntercept)
        ).apply(instance, Interception::new));

        public Interception {
            filter = filter == null ? BarrierDefinition.DamageFilter.EMPTY : filter;
            projectileResponse = projectileResponse == null ? NormalProjectileDefinition.ImpactResponse.DEFLECT : projectileResponse;
            onIntercept = onIntercept == null ? List.of() : List.copyOf(onIntercept);
        }
    }

    public interface View {
        UUID runtimeId();

        UUID ownerId();

        Identifier definitionId();

        double stability();

        double maximumStability();

        Vec3 position();

        long expiresAt();
    }
}
