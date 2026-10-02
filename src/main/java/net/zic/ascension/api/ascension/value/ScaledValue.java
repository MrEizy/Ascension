package net.zic.ascension.api.ascension.value;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.zic.ascension.api.ascension.datapack.CodecType;
import net.zic.ascension.api.ascension.datapack.TypeRegistries;
import net.zic.ascension.api.rpg_engine.source.OriginSource;
import net.zic.ascension.impl.value.source.ScaledValueSources;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/** Resolves reusable numeric values from a base, common skill shorthands, and optional advanced terms. */
public record ScaledValue(
        double base,
        List<Term> terms,
        Optional<Double> minimum,
        Optional<Double> maximum
) {
    public static final MapCodec<ScaledValue> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.DOUBLE.optionalFieldOf("base", 0.0D).forGetter(ScaledValue::base),
            Term.CODEC.codec().listOf().optionalFieldOf("terms", List.of()).forGetter(ScaledValue::explicitTerms),
            Codec.DOUBLE.optionalFieldOf("mastery", 0.0D).forGetter(value -> value.shorthand(Shorthand.MASTERY)),
            Codec.DOUBLE.optionalFieldOf("per_mastery", 0.0D).forGetter(value -> value.shorthand(Shorthand.PER_MASTERY)),
            Codec.DOUBLE.optionalFieldOf("level", 0.0D).forGetter(value -> value.shorthand(Shorthand.LEVEL)),
            Codec.DOUBLE.optionalFieldOf("per_level", 0.0D).forGetter(value -> value.shorthand(Shorthand.PER_LEVEL)),
            Codec.DOUBLE.optionalFieldOf("charge", 0.0D).forGetter(value -> value.shorthand(Shorthand.CHARGE)),
            Codec.DOUBLE.optionalFieldOf("minimum").forGetter(ScaledValue::minimum),
            Codec.DOUBLE.optionalFieldOf("maximum").forGetter(ScaledValue::maximum)
    ).apply(instance, ScaledValue::decode));
    public static final Codec<ScaledValue> COMPACT_CODEC = Codec.either(Codec.DOUBLE, CODEC.codec()).xmap(
            value -> value.map(ScaledValue::constant, entry -> entry),
            value -> value.terms().isEmpty() && value.minimum().isEmpty() && value.maximum().isEmpty()
                    ? com.mojang.datafixers.util.Either.left(value.base())
                    : com.mojang.datafixers.util.Either.right(value)
    );

    public ScaledValue {
        terms = terms == null ? List.of() : List.copyOf(terms);
        minimum = minimum == null ? Optional.empty() : minimum;
        maximum = maximum == null ? Optional.empty() : maximum;
    }

    public static ScaledValue constant(double value) {
        return new ScaledValue(value, List.of(), Optional.empty(), Optional.empty());
    }

    private static ScaledValue decode(
            double base,
            List<Term> terms,
            double mastery,
            double perMastery,
            double level,
            double perLevel,
            double charge,
            Optional<Double> minimum,
            Optional<Double> maximum
    ) {
        List<Term> resolved = new java.util.ArrayList<>(terms == null ? List.of() : terms);
        addShorthand(resolved, Shorthand.MASTERY, mastery);
        addShorthand(resolved, Shorthand.PER_MASTERY, perMastery);
        addShorthand(resolved, Shorthand.LEVEL, level);
        addShorthand(resolved, Shorthand.PER_LEVEL, perLevel);
        addShorthand(resolved, Shorthand.CHARGE, charge);
        return new ScaledValue(base, resolved, minimum, maximum);
    }

    private static void addShorthand(List<Term> terms, Shorthand shorthand, double value) {
        if (!Double.isFinite(value) || Math.abs(value) <= 1.0E-12D) {
            return;
        }
        terms.add(shorthand.term(value));
    }

    private List<Term> explicitTerms() {
        if (!supportsShorthandEncoding()) {
            return terms;
        }
        return terms.stream().filter(term -> Shorthand.match(term) == null).toList();
    }

    private double shorthand(Shorthand shorthand) {
        if (!supportsShorthandEncoding()) {
            return 0.0D;
        }
        double value = 0.0D;
        for (Term term : terms) {
            if (Shorthand.match(term) == shorthand) {
                value += term.scale();
            }
        }
        return value;
    }

    private boolean supportsShorthandEncoding() {
        return terms.stream().allMatch(term -> term.operation() == Operation.ADD);
    }

    private enum Shorthand {
        MASTERY,
        PER_MASTERY,
        LEVEL,
        PER_LEVEL,
        CHARGE;

        private Term term(double value) {
            return switch (this) {
                case MASTERY -> new Term(new ScaledValueSources.Mastery(Optional.empty()), Operation.ADD, 1.0D, value, 0.0D);
                case PER_MASTERY -> new Term(new ScaledValueSources.Mastery(Optional.empty()), Operation.ADD, 1.0D, value, -value);
                case LEVEL -> new Term(new ScaledValueSources.Level(Optional.empty()), Operation.ADD, 1.0D, value, 0.0D);
                case PER_LEVEL -> new Term(new ScaledValueSources.Level(Optional.empty()), Operation.ADD, 1.0D, value, -value);
                case CHARGE -> new Term(new ScaledValueSources.Charge(), Operation.ADD, 1.0D, value, 0.0D);
            };
        }

        private static Shorthand match(Term term) {
            if (term == null || term.operation() != Operation.ADD || term.power() != 1.0D) {
                return null;
            }
            if (term.source() instanceof ScaledValueSources.Mastery mastery && mastery.skill().isEmpty()) {
                if (Math.abs(term.offset()) <= 1.0E-12D) {
                    return MASTERY;
                }
                if (Math.abs(term.offset() + term.scale()) <= 1.0E-12D) {
                    return PER_MASTERY;
                }
            }
            if (term.source() instanceof ScaledValueSources.Level level && level.skill().isEmpty()) {
                if (Math.abs(term.offset()) <= 1.0E-12D) {
                    return LEVEL;
                }
                if (Math.abs(term.offset() + term.scale()) <= 1.0E-12D) {
                    return PER_LEVEL;
                }
            }
            if (term.source() instanceof ScaledValueSources.Charge && Math.abs(term.offset()) <= 1.0E-12D) {
                return CHARGE;
            }
            return null;
        }
    }

    public double resolve(Context context) {
        double value = base;
        for (Term term : terms) {
            value = term.apply(value, context);
        }
        if (!Double.isFinite(value)) {
            value = 0.0D;
        }
        double lowerBound = minimum.orElse(Double.NEGATIVE_INFINITY);
        double upperBound = maximum.orElse(Double.POSITIVE_INFINITY);
        if (lowerBound > upperBound) {
            upperBound = lowerBound;
        }
        return Math.max(lowerBound, Math.min(upperBound, value));
    }

    public interface Source {
        Codec<Source> CODEC = TypeRegistries.SCALED_VALUE_SOURCE_TYPE_REGISTRY.byNameCodec()
                .dispatch(Source::getType, CodecType<Source>::codec);

        CodecType<Source> getType();

        double resolve(Context context);
    }

    public enum Operation {
        ADD,
        MULTIPLY,
        SET;

        public static final Codec<Operation> CODEC = Codec.STRING.comapFlatMap(
                value -> {
                    try {
                        return DataResult.success(valueOf(value.toUpperCase(Locale.ROOT)));
                    } catch (IllegalArgumentException exception) {
                        return DataResult.error(() -> "Unknown scaled value operation: " + value);
                    }
                },
                value -> value.name().toLowerCase(Locale.ROOT)
        );

        public double apply(double current, double value) {
            return switch (this) {
                case ADD -> current + value;
                case MULTIPLY -> current * value;
                case SET -> value;
            };
        }
    }

    public record Term(
            Source source,
            Operation operation,
            double power,
            double scale,
            double offset
    ) {
        public static final MapCodec<Term> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Source.CODEC.fieldOf("source").forGetter(Term::source),
                Operation.CODEC.optionalFieldOf("operation", Operation.ADD).forGetter(Term::operation),
                Codec.DOUBLE.optionalFieldOf("power", 1.0D).forGetter(Term::power),
                Codec.DOUBLE.optionalFieldOf("scale", 1.0D).forGetter(Term::scale),
                Codec.DOUBLE.optionalFieldOf("offset", 0.0D).forGetter(Term::offset)
        ).apply(instance, Term::new));

        public double apply(double current, Context context) {
            double sourceValue = source.resolve(context);
            if (!Double.isFinite(sourceValue)) {
                sourceValue = 0.0D;
            }
            double value = Math.pow(sourceValue, power) * scale + offset;
            return Double.isFinite(value) ? operation.apply(current, value) : current;
        }
    }

    public record Context(
            OriginSource source,
            Identifier skill,
            LivingEntity sourceEntity,
            LivingEntity targetEntity,
            double charge,
            Map<Identifier, Double> variables
    ) {
        public Context {
            variables = variables == null ? Map.of() : Map.copyOf(variables);
        }

        public static Context of(OriginSource source, Identifier skill) {
            return new Context(source, skill, null, null, 0.0D, Map.of());
        }

        public Context withCharge(double charge) {
            return new Context(source, skill, sourceEntity, targetEntity, charge, variables);
        }

        public Context withEntities(LivingEntity sourceEntity, LivingEntity targetEntity) {
            return new Context(source, skill, sourceEntity, targetEntity, charge, variables);
        }

        public Context withVariable(Identifier key, double value) {
            if (key == null) {
                return this;
            }
            Map<Identifier, Double> updated = new HashMap<>(variables);
            updated.put(key, value);
            return new Context(source, skill, sourceEntity, targetEntity, charge, updated);
        }

        public double getVariable(Identifier key, double fallback) {
            return key == null ? fallback : variables.getOrDefault(key, fallback);
        }
    }
}
