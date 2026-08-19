package net.zic.ascension.impl.value.source;

import net.zic.ascension.api.ascension.value.ScaledValue;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.util.StringRepresentable;
import net.zic.ascension.api.ascension.core.effect.SkillEffectContext;

import net.zic.ascension.api.ascension.core.path.PathInstance;
import net.zic.ascension.api.ascension.core.skill.SkillProgressionResolver;
import net.zic.ascension.api.ascension.core.source.AscensionOriginSourceHelper;
import net.zic.ascension.api.ascension.datapack.CodecType;
import net.zic.ascension.impl.core.effect.SkillEffectService;
import net.zic.zenithlib.common.ZenithRegistries;
import net.zic.zenithlib.stats.Stat;
import net.zic.zenithlib.value_containers.ValueContainer;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Provides reusable numeric sources for skill and passive scaling. */
public final class ScaledValueSources {
    private ScaledValueSources() {
    }

    public record Constant(double value) implements ScaledValue.Source {
        public static final MapCodec<Constant> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.DOUBLE.fieldOf("value").forGetter(Constant::value)
        ).apply(instance, Constant::new));

        @Override
        public CodecType<ScaledValue.Source> getType() {
            return AscensionScaledValueSourceTypes.CONSTANT.get();
        }

        @Override
        public double resolve(ScaledValue.Context context) {
            return value;
        }
    }

    public static final class Charge implements ScaledValue.Source {
        public static final MapCodec<Charge> CODEC = MapCodec.unit(Charge::new);

        @Override
        public CodecType<ScaledValue.Source> getType() {
            return AscensionScaledValueSourceTypes.CHARGE.get();
        }

        @Override
        public double resolve(ScaledValue.Context context) {
            return Math.clamp(context.charge(), 0.0D, 1.0D);
        }
    }

    public record Level(Optional<Identifier> skill) implements ScaledValue.Source {
        public static final MapCodec<Level> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Identifier.CODEC.optionalFieldOf("skill").forGetter(Level::skill)
        ).apply(instance, Level::new));

        public Level {
            skill = skill == null ? Optional.empty() : skill;
        }

        @Override
        public CodecType<ScaledValue.Source> getType() {
            return AscensionScaledValueSourceTypes.LEVEL.get();
        }

        @Override
        public double resolve(ScaledValue.Context context) {
            Identifier skillId = skill.orElse(context.skill());
            return context.source() == null || skillId == null
                    ? 0.0D
                    : SkillProgressionResolver.resolve(context.source(), skillId).effectiveProgression();
        }
    }

    public record PathRealmMultiplier(
            Identifier path,
            double baseBonus,
            double bonusPerMajorRealm,
            double bonusPerMinorRealm,
            double maximumBonus
    ) implements ScaledValue.Source {
        public static final MapCodec<PathRealmMultiplier> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Identifier.CODEC.fieldOf("path").forGetter(PathRealmMultiplier::path),
                Codec.DOUBLE.optionalFieldOf("base_bonus", 0.0D).forGetter(PathRealmMultiplier::baseBonus),
                Codec.DOUBLE.optionalFieldOf("bonus_per_major_realm", 0.0D).forGetter(PathRealmMultiplier::bonusPerMajorRealm),
                Codec.DOUBLE.optionalFieldOf("bonus_per_minor_realm", 0.0D).forGetter(PathRealmMultiplier::bonusPerMinorRealm),
                Codec.DOUBLE.optionalFieldOf("maximum_bonus", Double.MAX_VALUE).forGetter(PathRealmMultiplier::maximumBonus)
        ).apply(instance, PathRealmMultiplier::new));

        @Override
        public CodecType<ScaledValue.Source> getType() {
            return AscensionScaledValueSourceTypes.PATH_REALM_MULTIPLIER.get();
        }

        @Override
        public double resolve(ScaledValue.Context context) {
            if (context.source() == null) {
                return 1.0D;
            }
            PathInstance pathInstance = AscensionOriginSourceHelper.getPathInstance(context.source(), path);
            if (pathInstance == null) {
                return 1.0D;
            }
            double bonus = baseBonus
                    + pathInstance.getCurrentMajorRealm() * bonusPerMajorRealm
                    + pathInstance.getCurrentMinorRealm() * bonusPerMinorRealm;
            double maximum = Double.isFinite(maximumBonus) ? Math.max(0.0D, maximumBonus) : Double.MAX_VALUE;
            return 1.0D + Math.clamp(Double.isFinite(bonus) ? bonus : 0.0D, 0.0D, maximum);
        }
    }

    public record Mastery(Optional<Identifier> skill) implements ScaledValue.Source {
        public static final MapCodec<Mastery> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Identifier.CODEC.optionalFieldOf("skill").forGetter(Mastery::skill)
        ).apply(instance, Mastery::new));

        public Mastery {
            skill = skill == null ? Optional.empty() : skill;
        }

        @Override
        public CodecType<ScaledValue.Source> getType() {
            return AscensionScaledValueSourceTypes.MASTERY.get();
        }

        @Override
        public double resolve(ScaledValue.Context context) {
            Identifier skillId = skill.orElse(context.skill());
            return context.source() == null || skillId == null
                    ? 0.0D
                    : SkillProgressionResolver.resolve(context.source(), skillId).effectiveProgression();
        }
    }

    public record StatValue(Identifier stat, boolean base) implements ScaledValue.Source {
        public static final MapCodec<StatValue> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Identifier.CODEC.fieldOf("stat").forGetter(StatValue::stat),
                Codec.BOOL.optionalFieldOf("base", false).forGetter(StatValue::base)
        ).apply(instance, StatValue::new));

        @Override
        public CodecType<ScaledValue.Source> getType() {
            return AscensionScaledValueSourceTypes.STAT.get();
        }

        @Override
        public double resolve(ScaledValue.Context context) {
            if (context.source() == null) {
                return 0.0D;
            }
            Stat definition = ZenithRegistries.STAT_REGISTRY.getValue(stat);
            if (definition == null) {
                return 0.0D;
            }
            return base ? context.source().getBaseStat(definition) : context.source().getStat(definition);
        }
    }

    public record Affinity(Identifier path, Optional<Identifier> category, boolean base) implements ScaledValue.Source {
        public static final MapCodec<Affinity> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Identifier.CODEC.fieldOf("path").forGetter(Affinity::path),
                Identifier.CODEC.optionalFieldOf("category").forGetter(Affinity::category),
                Codec.BOOL.optionalFieldOf("base", false).forGetter(Affinity::base)
        ).apply(instance, Affinity::new));

        public Affinity {
            category = category == null ? Optional.empty() : category;
        }

        @Override
        public CodecType<ScaledValue.Source> getType() {
            return AscensionScaledValueSourceTypes.AFFINITY.get();
        }

        @Override
        public double resolve(ScaledValue.Context context) {
            if (context.source() == null) {
                return 0.0D;
            }
            Identifier resolvedCategory = category
                    .orElse(AscensionOriginSourceHelper.AFFINITY_CATEGORY);
            ValueContainer container = AscensionOriginSourceHelper.getPathBonusContainer(
                    context.source(),
                    resolvedCategory,
                    path
            );
            return container == null ? 0.0D : base ? container.getBaseValue() : container.getValue();
        }
    }

    public record ContextValue(Identifier key, double fallback) implements ScaledValue.Source {
        public static final MapCodec<ContextValue> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Identifier.CODEC.fieldOf("key").forGetter(ContextValue::key),
                Codec.DOUBLE.optionalFieldOf("fallback", 0.0D).forGetter(ContextValue::fallback)
        ).apply(instance, ContextValue::new));

        @Override
        public CodecType<ScaledValue.Source> getType() {
            return AscensionScaledValueSourceTypes.CONTEXT.get();
        }

        @Override
        public double resolve(ScaledValue.Context context) {
            return context.getVariable(key, fallback);
        }
    }

    public record EffectValue(
            Identifier effect,
            Optional<Identifier> sourceSkill,
            boolean ownerScoped,
            Metric metric,
            Aggregation aggregation
    ) implements ScaledValue.Source {
        public static final MapCodec<EffectValue> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Identifier.CODEC.fieldOf("effect").forGetter(EffectValue::effect),
                Identifier.CODEC.optionalFieldOf("source_skill").forGetter(EffectValue::sourceSkill),
                Codec.BOOL.optionalFieldOf("owner_scoped", true).forGetter(EffectValue::ownerScoped),
                Metric.CODEC.optionalFieldOf("metric", Metric.STACKS).forGetter(EffectValue::metric),
                Aggregation.CODEC.optionalFieldOf("aggregation", Aggregation.SUM).forGetter(EffectValue::aggregation)
        ).apply(instance, EffectValue::new));

        public EffectValue {
            sourceSkill = sourceSkill == null ? Optional.empty() : sourceSkill;
            metric = metric == null ? Metric.STACKS : metric;
            aggregation = aggregation == null ? Aggregation.SUM : aggregation;
        }

        @Override
        public CodecType<ScaledValue.Source> getType() {
            return AscensionScaledValueSourceTypes.SKILL_EFFECT.get();
        }

        @Override
        public double resolve(ScaledValue.Context context) {
            if (context.targetEntity() == null) {
                return 0.0D;
            }
            UUID sourceEntity = ownerScoped && context.sourceEntity() != null ? context.sourceEntity().getUUID() : null;
            List<SkillEffectContext> matches = SkillEffectService.find(
                    context.targetEntity(),
                    effect,
                    sourceEntity,
                    sourceSkill.orElse(null)
            );
            if (matches.isEmpty()) {
                return 0.0D;
            }
            double result = aggregation == Aggregation.MIN
                    ? Double.POSITIVE_INFINITY
                    : aggregation == Aggregation.MAX ? Double.NEGATIVE_INFINITY : 0.0D;
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
