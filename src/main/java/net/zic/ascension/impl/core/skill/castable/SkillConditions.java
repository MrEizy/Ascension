package net.zic.ascension.impl.core.skill.castable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.LivingEntity;
import net.zic.ascension.api.ascension.core.requirement.RequirementHolder;
import net.zic.ascension.api.ascension.core.resource.ResourceTransactionService;
import net.zic.ascension.api.ascension.core.skill.castable.SkillCondition;
import net.zic.ascension.api.ascension.core.skill.castable.action.ActionSubject;
import net.zic.ascension.api.ascension.core.skill.castable.action.SkillActionContext;
import net.zic.ascension.api.ascension.datapack.CodecType;
import net.zic.ascension.api.ascension.value.ScaledValue;
import net.zic.ascension.impl.core.requirement.Requirements;
import net.zic.ascension.impl.datapack.skill.AscensionSkillConditionTypes;

import java.util.List;

public final class SkillConditions {
    private SkillConditions() {
    }

    public record AllOf(List<SkillCondition> conditions) implements SkillCondition {
        public static final MapCodec<AllOf> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                SkillCondition.CODEC.listOf().fieldOf("conditions").forGetter(AllOf::conditions)
        ).apply(instance, AllOf::new));

        public AllOf {
            conditions = conditions == null ? List.of() : List.copyOf(conditions);
        }

        @Override
        public CodecType<SkillCondition> getType() {
            return AscensionSkillConditionTypes.ALL_OF.get();
        }

        @Override
        public boolean test(SkillActionContext context) {
            return context != null && conditions.stream().allMatch(condition -> condition != null && condition.test(context));
        }
    }

    public record AnyOf(List<SkillCondition> conditions) implements SkillCondition {
        public static final MapCodec<AnyOf> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                SkillCondition.CODEC.listOf().fieldOf("conditions").forGetter(AnyOf::conditions)
        ).apply(instance, AnyOf::new));

        public AnyOf {
            conditions = conditions == null ? List.of() : List.copyOf(conditions);
        }

        @Override
        public CodecType<SkillCondition> getType() {
            return AscensionSkillConditionTypes.ANY_OF.get();
        }

        @Override
        public boolean test(SkillActionContext context) {
            return context != null && conditions.stream().anyMatch(condition -> condition != null && condition.test(context));
        }
    }

    public record Not(SkillCondition condition) implements SkillCondition {
        public static final MapCodec<Not> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                SkillCondition.CODEC.fieldOf("condition").forGetter(Not::condition)
        ).apply(instance, Not::new));

        @Override
        public CodecType<SkillCondition> getType() {
            return AscensionSkillConditionTypes.NOT.get();
        }

        @Override
        public boolean test(SkillActionContext context) {
            return condition != null && !condition.test(context);
        }
    }

    public record Requirement(RequirementHolder requirements) implements SkillCondition {
        public static final MapCodec<Requirement> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                RequirementHolder.CODEC.fieldOf("requirements").forGetter(Requirement::requirements)
        ).apply(instance, Requirement::new));

        public Requirement {
            requirements = requirements == null ? RequirementHolder.EMPTY : requirements;
        }

        @Override
        public CodecType<SkillCondition> getType() {
            return AscensionSkillConditionTypes.REQUIREMENT.get();
        }

        @Override
        public boolean test(SkillActionContext context) {
            return context != null && requirements.test(context.originSource());
        }
    }

    public record Health(ActionSubject subject, Requirements.Comparison comparison, double value, boolean percentage) implements SkillCondition {
        public static final MapCodec<Health> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                ActionSubject.CODEC.optionalFieldOf("subject", ActionSubject.CASTER).forGetter(Health::subject),
                Requirements.Comparison.CODEC.optionalFieldOf("comparison", Requirements.Comparison.AT_LEAST).forGetter(Health::comparison),
                Codec.DOUBLE.fieldOf("value").forGetter(Health::value),
                Codec.BOOL.optionalFieldOf("percentage", true).forGetter(Health::percentage)
        ).apply(instance, Health::new));

        public Health {
            subject = subject == null ? ActionSubject.CASTER : subject;
            comparison = comparison == null ? Requirements.Comparison.AT_LEAST : comparison;
        }

        @Override
        public CodecType<SkillCondition> getType() {
            return AscensionSkillConditionTypes.HEALTH.get();
        }

        @Override
        public boolean test(SkillActionContext context) {
            LivingEntity entity = context == null ? null : context.entity(subject);
            if (entity == null) {
                return false;
            }
            double actual = percentage && entity.getMaxHealth() > 0.0F
                    ? entity.getHealth() / entity.getMaxHealth()
                    : entity.getHealth();
            return comparison.test(actual, value);
        }
    }

    public record Resource(ActionSubject subject, Identifier resource, Requirements.Comparison comparison, ScaledValue value, boolean percentage) implements SkillCondition {
        public static final MapCodec<Resource> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                ActionSubject.CODEC.optionalFieldOf("subject", ActionSubject.CASTER).forGetter(Resource::subject),
                Identifier.CODEC.fieldOf("resource").forGetter(Resource::resource),
                Requirements.Comparison.CODEC.optionalFieldOf("comparison", Requirements.Comparison.AT_LEAST).forGetter(Resource::comparison),
                ScaledValue.COMPACT_CODEC.fieldOf("value").forGetter(Resource::value),
                Codec.BOOL.optionalFieldOf("percentage", false).forGetter(Resource::percentage)
        ).apply(instance, Resource::new));

        public Resource {
            subject = subject == null ? ActionSubject.CASTER : subject;
            comparison = comparison == null ? Requirements.Comparison.AT_LEAST : comparison;
            value = value == null ? ScaledValue.constant(0.0D) : value;
        }

        @Override
        public CodecType<SkillCondition> getType() {
            return AscensionSkillConditionTypes.RESOURCE.get();
        }

        @Override
        public boolean test(SkillActionContext context) {
            LivingEntity entity = context == null ? null : context.entity(subject);
            if (entity == null || resource == null) {
                return false;
            }
            double amount = ResourceTransactionService.getAmount(entity, resource);
            double maximum = ResourceTransactionService.getMaximum(entity, resource);
            if (!Double.isFinite(amount) || !Double.isFinite(maximum) || maximum < 0.0D) {
                return false;
            }
            double actual = percentage ? maximum <= 0.0D ? 0.0D : amount / maximum : amount;
            return comparison.test(actual, value.resolve(context.scaledValueContext()));
        }
    }

    public record EntityState(ActionSubject subject, State state, boolean value) implements SkillCondition {
        public static final MapCodec<EntityState> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                ActionSubject.CODEC.optionalFieldOf("subject", ActionSubject.CASTER).forGetter(EntityState::subject),
                State.CODEC.fieldOf("state").forGetter(EntityState::state),
                Codec.BOOL.optionalFieldOf("value", true).forGetter(EntityState::value)
        ).apply(instance, EntityState::new));

        public EntityState {
            subject = subject == null ? ActionSubject.CASTER : subject;
            state = state == null ? State.SNEAKING : state;
        }

        @Override
        public CodecType<SkillCondition> getType() {
            return AscensionSkillConditionTypes.ENTITY_STATE.get();
        }

        @Override
        public boolean test(SkillActionContext context) {
            LivingEntity entity = context == null ? null : context.entity(subject);
            return entity != null && state.test(entity) == value;
        }
    }

    public record Distance(Requirements.Comparison comparison, ScaledValue value) implements SkillCondition {
        public static final MapCodec<Distance> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Requirements.Comparison.CODEC.optionalFieldOf("comparison", Requirements.Comparison.AT_MOST).forGetter(Distance::comparison),
                ScaledValue.COMPACT_CODEC.fieldOf("value").forGetter(Distance::value)
        ).apply(instance, Distance::new));

        public Distance {
            comparison = comparison == null ? Requirements.Comparison.AT_MOST : comparison;
            value = value == null ? ScaledValue.constant(0.0D) : value;
        }

        @Override
        public CodecType<SkillCondition> getType() {
            return AscensionSkillConditionTypes.DISTANCE.get();
        }

        @Override
        public boolean test(SkillActionContext context) {
            return context != null
                    && context.target() != null
                    && comparison.test(context.caster().distanceTo(context.target()), value.resolve(context.scaledValueContext()));
        }
    }

    public record Variable(Identifier variable, Requirements.Comparison comparison, ScaledValue value) implements SkillCondition {
        public static final MapCodec<Variable> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Identifier.CODEC.fieldOf("variable").forGetter(Variable::variable),
                Requirements.Comparison.CODEC.optionalFieldOf("comparison", Requirements.Comparison.AT_LEAST).forGetter(Variable::comparison),
                ScaledValue.COMPACT_CODEC.fieldOf("value").forGetter(Variable::value)
        ).apply(instance, Variable::new));

        public Variable {
            comparison = comparison == null ? Requirements.Comparison.AT_LEAST : comparison;
            value = value == null ? ScaledValue.constant(0.0D) : value;
        }

        @Override
        public CodecType<SkillCondition> getType() {
            return AscensionSkillConditionTypes.VARIABLE.get();
        }

        @Override
        public boolean test(SkillActionContext context) {
            if (context == null || variable == null || !context.variables().containsKey(variable)) {
                return false;
            }
            return comparison.test(context.variables().get(variable), value.resolve(context.scaledValueContext()));
        }
    }

    public record Random(double chance) implements SkillCondition {
        public static final MapCodec<Random> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.doubleRange(0.0D, 1.0D).fieldOf("chance").forGetter(Random::chance)
        ).apply(instance, Random::new));

        @Override
        public CodecType<SkillCondition> getType() {
            return AscensionSkillConditionTypes.RANDOM.get();
        }

        @Override
        public boolean test(SkillActionContext context) {
            return context != null && context.level().getRandom().nextDouble() < chance;
        }
    }

    public enum State implements StringRepresentable {
        SNEAKING("sneaking") {
            @Override
            boolean test(LivingEntity entity) {
                return entity.isShiftKeyDown();
            }
        },
        SPRINTING("sprinting") {
            @Override
            boolean test(LivingEntity entity) {
                return entity.isSprinting();
            }
        },
        AIRBORNE("airborne") {
            @Override
            boolean test(LivingEntity entity) {
                return !entity.onGround();
            }
        },
        ON_FIRE("on_fire") {
            @Override
            boolean test(LivingEntity entity) {
                return entity.isOnFire();
            }
        },
        IN_WATER("in_water") {
            @Override
            boolean test(LivingEntity entity) {
                return entity.isInWater();
            }
        };

        public static final Codec<State> CODEC = StringRepresentable.fromEnum(State::values);
        private final String name;

        State(String name) {
            this.name = name;
        }

        abstract boolean test(LivingEntity entity);

        @Override
        public String getSerializedName() {
            return name;
        }
    }
}
