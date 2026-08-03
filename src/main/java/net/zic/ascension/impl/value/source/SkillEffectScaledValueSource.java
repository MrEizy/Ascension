package net.zic.ascension.impl.value.source;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.util.StringRepresentable;
import net.zic.ascension.api.ascension.core.effect.SkillEffectContext;
import net.zic.ascension.api.ascension.datapack.CodecType;
import net.zic.ascension.api.ascension.value.ScaledValueContext;
import net.zic.ascension.api.ascension.value.ScaledValueSource;
import net.zic.ascension.impl.core.effect.SkillEffectService;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public record SkillEffectScaledValueSource(
        Identifier effect,
        Optional<Identifier> sourceSkill,
        boolean ownerScoped,
        Metric metric,
        Aggregation aggregation
) implements ScaledValueSource {
    public static final MapCodec<SkillEffectScaledValueSource> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Identifier.CODEC.fieldOf("effect").forGetter(SkillEffectScaledValueSource::effect),
            Identifier.CODEC.optionalFieldOf("source_skill").forGetter(SkillEffectScaledValueSource::sourceSkill),
            Codec.BOOL.optionalFieldOf("owner_scoped", true).forGetter(SkillEffectScaledValueSource::ownerScoped),
            Metric.CODEC.optionalFieldOf("metric", Metric.STACKS).forGetter(SkillEffectScaledValueSource::metric),
            Aggregation.CODEC.optionalFieldOf("aggregation", Aggregation.SUM)
                    .forGetter(SkillEffectScaledValueSource::aggregation)
    ).apply(instance, SkillEffectScaledValueSource::new));

    public SkillEffectScaledValueSource {
        sourceSkill = sourceSkill == null ? Optional.empty() : sourceSkill;
        metric = metric == null ? Metric.STACKS : metric;
        aggregation = aggregation == null ? Aggregation.SUM : aggregation;
    }

    @Override
    public CodecType<ScaledValueSource> getType() {
        return AscensionScaledValueSourceTypes.SKILL_EFFECT.get();
    }

    @Override
    public double resolve(ScaledValueContext context) {
        if (context.targetEntity() == null) {
            return 0.0D;
        }

        UUID sourceEntity = ownerScoped && context.sourceEntity() != null ? context.sourceEntity().getUUID() : null;
        Identifier sourceSkillId = sourceSkill.orElse(null);
        List<SkillEffectContext> matches = SkillEffectService.find(
                context.targetEntity(),
                effect,
                sourceEntity,
                sourceSkillId
        );
        if (matches.isEmpty()) {
            return 0.0D;
        }

        double result = aggregation == Aggregation.MIN ? Double.POSITIVE_INFINITY : aggregation == Aggregation.MAX ? Double.NEGATIVE_INFINITY : 0.0D;
        for (SkillEffectContext active : matches) {
            double value = metric.resolve(active);
            result = switch (aggregation) {
                case SUM -> result + value;
                case MAX -> Math.max(result, value);
                case MIN -> Math.min(result, value);
            };
        }
        return Double.isFinite(result) ? result : 0.0D;
    }

    public enum Metric implements StringRepresentable {
        POTENCY("potency"),
        STACKS("stacks"),
        DURATION("duration"),
        COUNT("count");

        public static final Codec<Metric> CODEC = StringRepresentable.fromEnum(Metric::values);
        private final String name;

        Metric(String name) {
            this.name = name;
        }

        private double resolve(SkillEffectContext context) {
            return switch (this) {
                case POTENCY -> context.potency();
                case STACKS -> context.stacks();
                case DURATION -> context.remainingDuration();
                case COUNT -> 1.0D;
            };
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }

    public enum Aggregation implements StringRepresentable {
        SUM("sum"),
        MAX("max"),
        MIN("min");

        public static final Codec<Aggregation> CODEC = StringRepresentable.fromEnum(Aggregation::values);
        private final String name;

        Aggregation(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }
}
