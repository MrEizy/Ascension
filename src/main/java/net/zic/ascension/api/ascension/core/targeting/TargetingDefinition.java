package net.zic.ascension.api.ascension.core.targeting;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.ascension.datapack.CodecType;
import net.zic.ascension.api.ascension.core.source.AscensionOriginSourceHelper;
import net.zic.ascension.api.ascension.datapack.TypeRegistries;
import net.zic.ascension.api.ascension.value.ScaledValue;
import net.zic.ascension.api.rpg_engine.source.OriginSource;
import net.zic.ascension.impl.core.targeting.TargetingDefinitions;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public interface TargetingDefinition {
    Codec<TargetingDefinition> DISPATCH_CODEC = TypeRegistries.TARGETING_TYPE_REGISTRY.byNameCodec().dispatch(
            TargetingDefinition::getType,
            CodecType<TargetingDefinition>::codec
    );
    Codec<TargetingDefinition> CODEC = Codec.either(Codec.STRING, DISPATCH_CODEC).comapFlatMap(
            value -> value.map(TargetingDefinition::shorthand, DataResult::success),
            value -> value instanceof TargetingDefinitions.Self
                    ? Either.left("self")
                    : Either.right(value)
    );

    CodecType<TargetingDefinition> getType();

    Result resolve(Context context);

    private static DataResult<TargetingDefinition> shorthand(String value) {
        return switch (value) {
            case "self", "ascension:self" -> DataResult.success(new TargetingDefinitions.Self());
            default -> DataResult.error(() -> "Unknown targeting shorthand: " + value);
        };
    }

    record Target(LivingEntity entity, Vec3 position) {
        public Target {
            if (position == null && entity != null) {
                position = entity.position().add(0.0D, entity.getBbHeight() * 0.5D, 0.0D);
            }
        }

        public static Target entity(LivingEntity entity) {
            return new Target(entity, null);
        }

        public static Target position(Vec3 position) {
            return new Target(null, position);
        }
    }

    enum Sort {
        NEAREST,
        FURTHEST,
        LOWEST_HEALTH,
        HIGHEST_HEALTH,
        CLOSEST_TO_VIEW;

        public static final Codec<Sort> CODEC = Codec.STRING.comapFlatMap(
                value -> {
                    try {
                        return DataResult.success(valueOf(value.toUpperCase(Locale.ROOT)));
                    } catch (IllegalArgumentException exception) {
                        return DataResult.error(() -> "Unknown target sort: " + value);
                    }
                },
                value -> value.name().toLowerCase(Locale.ROOT)
        );
    }

    record Result(List<Target> targets, Component failureMessage) {
        public Result {
            targets = targets == null ? List.of() : List.copyOf(targets);
        }

        public static Result success(List<Target> targets) {
            return new Result(targets, null);
        }

        public static Result success(Target target) {
            return new Result(target == null ? List.of() : List.of(target), null);
        }

        public static Result failure(Component message) {
            return new Result(List.of(), message);
        }

        public boolean succeeded() {
            return failureMessage == null;
        }

        public Target primaryTarget() {
            return targets.isEmpty() ? null : targets.getFirst();
        }
    }

    record Context(
            ServerLevel level,
            LivingEntity caster,
            Identifier skill,
            int effectiveProgression,
            double charge,
            Map<Identifier, Double> variables
    ) {
        public static final Identifier EFFECTIVE_PROGRESSION = AscensionCraft.prefix("skill/effective_progression");

        public Context {
            effectiveProgression = Math.max(0, effectiveProgression);
            charge = Double.isFinite(charge) ? Math.clamp(charge, 0.0D, 1.0D) : 0.0D;
            variables = variables == null ? Map.of() : Map.copyOf(variables);
        }

        public ScaledValue.Context scaledValueContext(LivingEntity target) {
            Map<Identifier, Double> resolved = new HashMap<>(variables);
            resolved.put(EFFECTIVE_PROGRESSION, (double) effectiveProgression);
            return new ScaledValue.Context(originSource(), skill, caster, target, charge, resolved);
        }

        public OriginSource originSource() {
            return AscensionOriginSourceHelper.getEntitySource(caster);
        }
    }

    record Filter(Set<Relation> relations, boolean includePlayers, boolean requireLineOfSight) {
        private static final Codec<Set<Relation>> RELATIONS_CODEC = Relation.CODEC.listOf().xmap(Set::copyOf, List::copyOf);
        public static final MapCodec<Filter> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                RELATIONS_CODEC.optionalFieldOf("relations", Set.of(Relation.NEUTRAL, Relation.HOSTILE)).forGetter(Filter::relations),
                Codec.BOOL.optionalFieldOf("include_players", true).forGetter(Filter::includePlayers),
                Codec.BOOL.optionalFieldOf("line_of_sight", true).forGetter(Filter::requireLineOfSight)
        ).apply(instance, Filter::new));

        public Filter {
            relations = relations == null ? Set.of() : Set.copyOf(relations);
        }

        public static Filter hostile() {
            return new Filter(Set.of(Relation.HOSTILE), true, true);
        }

        public boolean matches(LivingEntity caster, LivingEntity target) {
            return target != null
                    && !target.isRemoved()
                    && target.isAlive()
                    && (!(target instanceof Player) || includePlayers)
                    && relations.contains(relation(caster, target));
        }

        public Relation relation(LivingEntity caster, LivingEntity target) {
            if (target == caster) {
                return Relation.SELF;
            }
            if (caster.isAlliedTo(target)) {
                return Relation.ALLY;
            }
            boolean hostile = caster instanceof Mob casterMob
                    ? casterMob.getTarget() == target
                            || target instanceof Mob targetMob && targetMob.getTarget() == caster
                            || caster.getLastHurtMob() == target
                            || caster.getLastHurtByMob() == target
                    : target instanceof Enemy
                            || target instanceof Mob targetMob && targetMob.getTarget() == caster
                            || caster.getLastHurtMob() == target
                            || caster.getLastHurtByMob() == target;
            return hostile ? Relation.HOSTILE : Relation.NEUTRAL;
        }

        public enum Relation implements StringRepresentable {
            SELF("self"),
            ALLY("ally"),
            NEUTRAL("neutral"),
            HOSTILE("hostile");

            public static final Codec<Relation> CODEC = StringRepresentable.fromEnum(Relation::values);
            private final String name;

            Relation(String name) {
                this.name = name;
            }

            @Override
            public String getSerializedName() {
                return name;
            }
        }
    }
}
