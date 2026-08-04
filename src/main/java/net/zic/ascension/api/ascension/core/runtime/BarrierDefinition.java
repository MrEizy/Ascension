package net.zic.ascension.api.ascension.core.runtime;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import net.zic.ascension.api.ascension.core.damage.AscensionDamageTypeHolders;
import net.zic.ascension.api.ascension.core.projectile.ProjectileImpactResponse;
import net.zic.ascension.api.ascension.core.skill.castable.feature.SkillExecutionFeature;
import net.zic.ascension.api.ascension.value.ScaledValue;
import net.zic.ascension.api.rpg_engine.damage.RPGEngineDamageSource;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public record BarrierDefinition(
        ScaledValue duration,
        ScaledValue durability,
        ScaledValue absorption,
        boolean overflow,
        int priority,
        boolean replaceExisting,
        DamageFilter filter,
        ProjectileImpactResponse projectileResponse,
        List<SkillExecutionFeature> onAbsorb,
        List<SkillExecutionFeature> onBreak,
        List<SkillExecutionFeature> onExpire,
        Optional<Visual> visual
) {
    public static final Codec<BarrierDefinition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ScaledValue.CODEC.codec().fieldOf("duration").forGetter(BarrierDefinition::duration),
            ScaledValue.CODEC.codec().fieldOf("durability").forGetter(BarrierDefinition::durability),
            ScaledValue.CODEC.codec().optionalFieldOf("absorption", ScaledValue.constant(1.0D))
                    .forGetter(BarrierDefinition::absorption),
            Codec.BOOL.optionalFieldOf("overflow", true).forGetter(BarrierDefinition::overflow),
            Codec.INT.optionalFieldOf("priority", 0).forGetter(BarrierDefinition::priority),
            Codec.BOOL.optionalFieldOf("replace_existing", true).forGetter(BarrierDefinition::replaceExisting),
            DamageFilter.CODEC.optionalFieldOf("filter", DamageFilter.EMPTY).forGetter(BarrierDefinition::filter),
            ProjectileImpactResponse.CODEC.optionalFieldOf("projectile_response", ProjectileImpactResponse.DISCARD)
                    .forGetter(BarrierDefinition::projectileResponse),
            SkillExecutionFeature.CODEC.listOf().optionalFieldOf("on_absorb", List.of())
                    .forGetter(BarrierDefinition::onAbsorb),
            SkillExecutionFeature.CODEC.listOf().optionalFieldOf("on_break", List.of())
                    .forGetter(BarrierDefinition::onBreak),
            SkillExecutionFeature.CODEC.listOf().optionalFieldOf("on_expire", List.of())
                    .forGetter(BarrierDefinition::onExpire),
            Visual.CODEC.optionalFieldOf("visual").forGetter(BarrierDefinition::visual)
    ).apply(instance, BarrierDefinition::new));

    public BarrierDefinition {
        filter = filter == null ? DamageFilter.EMPTY : filter;
        projectileResponse = projectileResponse == null ? ProjectileImpactResponse.DISCARD : projectileResponse;
        onAbsorb = onAbsorb == null ? List.of() : List.copyOf(onAbsorb);
        onBreak = onBreak == null ? List.of() : List.copyOf(onBreak);
        onExpire = onExpire == null ? List.of() : List.copyOf(onExpire);
        visual = visual == null ? Optional.empty() : visual;
    }

    public record DamageFilter(
            List<Identifier> includeClassifications,
            List<Identifier> excludeClassifications,
            List<Identifier> includeDamageTypes,
            List<Identifier> excludeDamageTypes
    ) {
        public static final DamageFilter EMPTY = new DamageFilter(List.of(), List.of(), List.of(), List.of());
        public static final Codec<DamageFilter> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Identifier.CODEC.listOf().optionalFieldOf("include_classifications", List.of())
                        .forGetter(DamageFilter::includeClassifications),
                Identifier.CODEC.listOf().optionalFieldOf("exclude_classifications", List.of())
                        .forGetter(DamageFilter::excludeClassifications),
                Identifier.CODEC.listOf().optionalFieldOf("include_damage_types", List.of())
                        .forGetter(DamageFilter::includeDamageTypes),
                Identifier.CODEC.listOf().optionalFieldOf("exclude_damage_types", List.of())
                        .forGetter(DamageFilter::excludeDamageTypes)
        ).apply(instance, DamageFilter::new));

        public DamageFilter {
            includeClassifications = includeClassifications == null ? List.of() : List.copyOf(includeClassifications);
            excludeClassifications = excludeClassifications == null ? List.of() : List.copyOf(excludeClassifications);
            includeDamageTypes = includeDamageTypes == null ? List.of() : List.copyOf(includeDamageTypes);
            excludeDamageTypes = excludeDamageTypes == null ? List.of() : List.copyOf(excludeDamageTypes);
        }

        public boolean accepts(RPGEngineDamageSource source) {
            Identifier damageType = source.typeHolder().unwrapKey().map(key -> key.identifier()).orElse(null);
            if (!includeDamageTypes.isEmpty() && !includeDamageTypes.contains(damageType)) {
                return false;
            }
            if (excludeDamageTypes.contains(damageType)) {
                return false;
            }

            AscensionDamageTypeHolders.Classifications classifications =
                    source.getDamageTypeHolder(AscensionDamageTypeHolders.CLASSIFICATIONS)
                            instanceof AscensionDamageTypeHolders.Classifications values ? values : null;
            if (!includeClassifications.isEmpty()
                    && (classifications == null || includeClassifications.stream().noneMatch(classifications::contains))) {
                return false;
            }
            return classifications == null
                    || excludeClassifications.stream().noneMatch(classifications::contains);
        }
    }

    public record Visual(
            Identifier id,
            ScaledValue radius,
            ScaledValue height,
            List<VisualStage> stages
    ) {
        public static final Codec<Visual> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Identifier.CODEC.fieldOf("id").forGetter(Visual::id),
                ScaledValue.CODEC.codec().optionalFieldOf("radius", ScaledValue.constant(1.15D))
                        .forGetter(Visual::radius),
                ScaledValue.CODEC.codec().optionalFieldOf("height", ScaledValue.constant(2.3D))
                        .forGetter(Visual::height),
                VisualStage.CODEC.listOf().optionalFieldOf("stages", List.of()).forGetter(Visual::stages)
        ).apply(instance, Visual::new));

        public Visual {
            stages = stages == null
                    ? List.of()
                    : stages.stream().sorted(Comparator.comparingDouble(VisualStage::maximumDurabilityFraction)).toList();
        }
    }

    public record VisualStage(double maximumDurabilityFraction, Identifier visual) {
        public static final Codec<VisualStage> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.doubleRange(0.0D, 1.0D).fieldOf("maximum_durability_fraction")
                        .forGetter(VisualStage::maximumDurabilityFraction),
                Identifier.CODEC.fieldOf("visual").forGetter(VisualStage::visual)
        ).apply(instance, VisualStage::new));
    }
}
