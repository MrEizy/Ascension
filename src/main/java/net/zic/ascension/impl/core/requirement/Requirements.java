package net.zic.ascension.impl.core.requirement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.util.StringRepresentable;
import net.zic.ascension.api.ascension.core.bloodline.BloodlineData;
import net.zic.ascension.api.ascension.core.path.PathInstance;
import net.zic.ascension.api.ascension.core.requirement.Requirement;
import net.zic.ascension.api.ascension.core.requirement.RequirementHolder;
import net.zic.ascension.api.ascension.core.source.AscensionOriginSourceHelper;
import net.zic.ascension.api.ascension.core.skill.SkillMasteryRank;
import net.zic.ascension.api.ascension.core.skill.SkillProgressionResolver;
import net.zic.ascension.api.ascension.datapack.requirement.RequirementType;
import net.zic.ascension.api.rpg_engine.source.OriginSource;
import net.zic.ascension.impl.datapack.requirement.AscensionRequirementTypes;
import net.zic.zenithlib.common.ZenithRegistries;
import net.zic.zenithlib.stats.Stat;

import java.util.List;

public final class Requirements {
    private Requirements() {
    }

    public enum Comparison implements StringRepresentable {
        AT_LEAST("at_least"),
        AT_MOST("at_most"),
        GREATER_THAN("greater_than"),
        LESS_THAN("less_than"),
        EQUAL("equal");

        public static final Codec<Comparison> CODEC = StringRepresentable.fromEnum(Comparison::values);

        private final String serializedName;

        Comparison(String serializedName) {
            this.serializedName = serializedName;
        }

        @Override
        public String getSerializedName() {
            return serializedName;
        }

        public boolean test(double actual, double expected) {
            return switch (this) {
                case AT_LEAST -> actual >= expected;
                case AT_MOST -> actual <= expected;
                case GREATER_THAN -> actual > expected;
                case LESS_THAN -> actual < expected;
                case EQUAL -> Double.compare(actual, expected) == 0;
            };
        }
    }

    public record AllOf(RequirementHolder requirements) implements Requirement {
        public static final MapCodec<AllOf> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                RequirementHolder.CODEC.fieldOf("requirements").forGetter(AllOf::requirements)
        ).apply(instance, AllOf::new));

        public AllOf {
            requirements = requirements == null ? RequirementHolder.EMPTY : requirements;
        }

        @Override
        public RequirementType getType() {
            return AscensionRequirementTypes.ALL_OF.get();
        }

        @Override
        public boolean test(OriginSource source) {
            return requirements.test(source);
        }
    }

    public record AnyOf(List<Requirement> requirements) implements Requirement {
        public static final MapCodec<AnyOf> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                RequirementType.REQUIREMENT_CODEC.listOf().fieldOf("requirements").forGetter(AnyOf::requirements)
        ).apply(instance, AnyOf::new));

        public AnyOf {
            requirements = requirements == null ? List.of() : List.copyOf(requirements);
        }

        @Override
        public RequirementType getType() {
            return AscensionRequirementTypes.ANY_OF.get();
        }

        @Override
        public boolean test(OriginSource source) {
            if (source == null) return false;
            for (Requirement requirement : requirements) {
                if (requirement != null && requirement.test(source)) return true;
            }
            return false;
        }
    }

    public record Not(Requirement requirement) implements Requirement {
        public static final MapCodec<Not> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                RequirementType.REQUIREMENT_CODEC.fieldOf("requirement").forGetter(Not::requirement)
        ).apply(instance, Not::new));

        @Override
        public RequirementType getType() {
            return AscensionRequirementTypes.NOT.get();
        }

        @Override
        public boolean test(OriginSource source) {
            return source != null && requirement != null && !requirement.test(source);
        }
    }

    public record HasPath(Identifier path) implements Requirement {
        public static final MapCodec<HasPath> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Identifier.CODEC.fieldOf("path").forGetter(HasPath::path)
        ).apply(instance, HasPath::new));

        @Override
        public RequirementType getType() {
            return AscensionRequirementTypes.HAS_PATH.get();
        }

        @Override
        public boolean test(OriginSource source) {
            return source != null && path != null && AscensionOriginSourceHelper.hasPath(source, path);
        }
    }

    public record PathRealm(Identifier path, int minimumMajorRealm, int minimumMinorRealm) implements Requirement {
        public static final MapCodec<PathRealm> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Identifier.CODEC.fieldOf("path").forGetter(PathRealm::path),
                Codec.INT.fieldOf("minimum_major_realm").forGetter(PathRealm::minimumMajorRealm),
                Codec.INT.optionalFieldOf("minimum_minor_realm", 0).forGetter(PathRealm::minimumMinorRealm)
        ).apply(instance, PathRealm::new));

        @Override
        public RequirementType getType() {
            return AscensionRequirementTypes.PATH_REALM.get();
        }

        @Override
        public boolean test(OriginSource source) {
            if (source == null || path == null) return false;
            PathInstance instance = AscensionOriginSourceHelper.getPathInstance(source, path);
            if (instance == null) return false;
            int major = instance.getCurrentMajorRealm();
            if (major != minimumMajorRealm) return major > minimumMajorRealm;
            return instance.getCurrentMinorRealm() >= minimumMinorRealm;
        }
    }

    public record HasTechnique(Identifier technique) implements Requirement {
        public static final MapCodec<HasTechnique> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Identifier.CODEC.fieldOf("technique").forGetter(HasTechnique::technique)
        ).apply(instance, HasTechnique::new));

        @Override
        public RequirementType getType() {
            return AscensionRequirementTypes.HAS_TECHNIQUE.get();
        }

        @Override
        public boolean test(OriginSource source) {
            return source != null && technique != null && AscensionOriginSourceHelper.hasTechnique(source, technique);
        }
    }

    public record HasBloodline(Identifier bloodline) implements Requirement {
        public static final MapCodec<HasBloodline> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Identifier.CODEC.fieldOf("bloodline").forGetter(HasBloodline::bloodline)
        ).apply(instance, HasBloodline::new));

        @Override
        public RequirementType getType() {
            return AscensionRequirementTypes.HAS_BLOODLINE.get();
        }

        @Override
        public boolean test(OriginSource source) {
            return source != null && bloodline != null && AscensionOriginSourceHelper.hasBloodline(source, bloodline);
        }
    }

    public record BloodlinePurity(Identifier bloodline, Comparison comparison, double value) implements Requirement {
        public static final MapCodec<BloodlinePurity> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Identifier.CODEC.fieldOf("bloodline").forGetter(BloodlinePurity::bloodline),
                Comparison.CODEC.optionalFieldOf("comparison", Comparison.AT_LEAST).forGetter(BloodlinePurity::comparison),
                Codec.DOUBLE.fieldOf("value").forGetter(BloodlinePurity::value)
        ).apply(instance, BloodlinePurity::new));

        public BloodlinePurity {
            comparison = comparison == null ? Comparison.AT_LEAST : comparison;
        }

        @Override
        public RequirementType getType() {
            return AscensionRequirementTypes.BLOODLINE_PURITY.get();
        }

        @Override
        public boolean test(OriginSource source) {
            if (source == null || bloodline == null || !AscensionOriginSourceHelper.hasBloodline(source, bloodline)) return false;
            BloodlineData data = AscensionOriginSourceHelper.getBloodlineData(source, bloodline);
            return data != null && comparison.test(data.getPurity(), value);
        }
    }

    public record HasPhysique(Identifier physique) implements Requirement {
        public static final MapCodec<HasPhysique> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Identifier.CODEC.fieldOf("physique").forGetter(HasPhysique::physique)
        ).apply(instance, HasPhysique::new));

        @Override
        public RequirementType getType() {
            return AscensionRequirementTypes.HAS_PHYSIQUE.get();
        }

        @Override
        public boolean test(OriginSource source) {
            return source != null && physique != null && physique.equals(AscensionOriginSourceHelper.getPhysiqueId(source));
        }
    }

    public record HasSkill(Identifier skill) implements Requirement {
        public static final MapCodec<HasSkill> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Identifier.CODEC.fieldOf("skill").forGetter(HasSkill::skill)
        ).apply(instance, HasSkill::new));

        @Override
        public RequirementType getType() {
            return AscensionRequirementTypes.HAS_SKILL.get();
        }

        @Override
        public boolean test(OriginSource source) {
            return source != null && skill != null && AscensionOriginSourceHelper.hasSkill(source, skill);
        }
    }

    public record SkillMastery(Identifier skill, SkillMasteryRank mastery) implements Requirement {
        public static final MapCodec<SkillMastery> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Identifier.CODEC.fieldOf("skill").forGetter(SkillMastery::skill),
                SkillMasteryRank.CODEC.fieldOf("mastery").forGetter(SkillMastery::mastery)
        ).apply(instance, SkillMastery::new));

        @Override
        public RequirementType getType() {
            return AscensionRequirementTypes.SKILL_MASTERY.get();
        }

        @Override
        public boolean test(OriginSource source) {
            if (source == null || skill == null || mastery == null || !AscensionOriginSourceHelper.hasSkill(source, skill)) return false;
            return SkillProgressionResolver.resolve(source, skill).effectiveProgression() >= mastery.progression();
        }
    }

    public record Affinity(Identifier path, Comparison comparison, double value) implements Requirement {
        public static final MapCodec<Affinity> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Identifier.CODEC.fieldOf("path").forGetter(Affinity::path),
                Comparison.CODEC.optionalFieldOf("comparison", Comparison.AT_LEAST).forGetter(Affinity::comparison),
                Codec.DOUBLE.fieldOf("value").forGetter(Affinity::value)
        ).apply(instance, Affinity::new));

        public Affinity {
            comparison = comparison == null ? Comparison.AT_LEAST : comparison;
        }

        @Override
        public RequirementType getType() {
            return AscensionRequirementTypes.AFFINITY.get();
        }

        @Override
        public boolean test(OriginSource source) {
            return source != null && path != null && comparison.test(AscensionOriginSourceHelper.getAffinity(source, path), value);
        }
    }

    public record StatRequirement(Identifier stat, boolean base, Comparison comparison, double value) implements Requirement {
        public static final MapCodec<StatRequirement> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Identifier.CODEC.fieldOf("stat").forGetter(StatRequirement::stat),
                Codec.BOOL.optionalFieldOf("base", false).forGetter(StatRequirement::base),
                Comparison.CODEC.optionalFieldOf("comparison", Comparison.AT_LEAST).forGetter(StatRequirement::comparison),
                Codec.DOUBLE.fieldOf("value").forGetter(StatRequirement::value)
        ).apply(instance, StatRequirement::new));

        public StatRequirement {
            comparison = comparison == null ? Comparison.AT_LEAST : comparison;
        }

        @Override
        public RequirementType getType() {
            return AscensionRequirementTypes.STAT.get();
        }

        @Override
        public boolean test(OriginSource source) {
            if (source == null || stat == null) return false;
            Stat definition = ZenithRegistries.STAT_REGISTRY.getValue(stat);
            if (definition == null) return false;
            double actual = base ? source.getBaseStat(definition) : source.getStat(definition);
            return comparison.test(actual, value);
        }
    }
}
