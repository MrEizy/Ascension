package net.zic.ascension.api.ascension.core.skill.castable.held;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.ascension.core.resource.ResourceOperation;
import net.zic.ascension.api.ascension.core.resource.ResourceSourceIdentity;
import net.zic.ascension.api.ascension.core.resource.ResourceTransactionRequest;
import net.zic.ascension.api.ascension.core.resource.ResourceTransactionService;
import net.zic.ascension.api.ascension.core.skill.castable.CastSoundDefinition;
import net.zic.ascension.api.ascension.value.ScaledValue;
import net.zic.ascension.api.ascension.core.skill.particle_field.ParticleFieldDefinition;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public record HeldCastSpec(
        int minimumCharge,
        int maximumCharge,
        int cooldown,
        Optional<Cost> cost,
        Movement movement,
        Optional<Interruption> interruption,
        List<Stage> stages
) {
    public static final Codec<HeldCastSpec> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.intRange(0, 72000).optionalFieldOf("minimum_charge", 0).forGetter(HeldCastSpec::minimumCharge),
            Codec.intRange(1, 72000).optionalFieldOf("maximum_charge", 40).forGetter(HeldCastSpec::maximumCharge),
            Codec.intRange(0, 72000).optionalFieldOf("cooldown", 0).forGetter(HeldCastSpec::cooldown),
            Cost.CODEC.codec().optionalFieldOf("cost").forGetter(HeldCastSpec::cost),
            Movement.CODEC.codec().optionalFieldOf("movement", Movement.defaults()).forGetter(HeldCastSpec::movement),
            Interruption.CODEC.codec().optionalFieldOf("interruption").forGetter(HeldCastSpec::interruption),
            Stage.CODEC.listOf().optionalFieldOf("stages", List.of()).forGetter(HeldCastSpec::stages)
    ).apply(instance, HeldCastSpec::new));

    public HeldCastSpec {
        maximumCharge = Math.max(1, maximumCharge);
        minimumCharge = Math.clamp(minimumCharge, 0, maximumCharge);
        cooldown = Math.max(0, cooldown);
        cost = cost == null ? Optional.empty() : cost;
        movement = movement == null ? Movement.defaults() : movement;
        interruption = interruption == null ? Optional.empty() : interruption;
        List<Stage> sorted = new ArrayList<>(stages == null ? List.of() : stages);
        sorted.sort(Comparator.comparingDouble(Stage::threshold));
        Map<Double, Stage> unique = new LinkedHashMap<>();
        for (Stage stage : sorted) {
            unique.put(stage.threshold(), stage);
        }
        sorted = new ArrayList<>(unique.values());
        if (sorted.isEmpty() || sorted.getFirst().threshold() > 0.0D) {
            sorted.addFirst(new Stage(0.0D, Optional.empty(), List.of()));
        }
        stages = List.copyOf(sorted);
    }

    public double charge(int ticks) {
        return Math.clamp(ticks / (double) maximumCharge, 0.0D, 1.0D);
    }

    public int stage(double charge) {
        int stage = 0;
        for (int index = 0; index < stages.size(); index++) {
            if (charge + 1.0E-8D < stages.get(index).threshold()) {
                break;
            }
            stage = index;
        }
        return stage;
    }

    public boolean minimumReached(int ticks) {
        return ticks >= minimumCharge;
    }

    public record Stage(
            double threshold,
            Optional<ParticleFieldDefinition> particleField,
            List<CastSoundDefinition> sounds
    ) {
        public static final Codec<Stage> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.DOUBLE.optionalFieldOf("threshold", 0.0D).forGetter(Stage::threshold),
                ParticleFieldDefinition.CODEC.optionalFieldOf("particle_field").forGetter(Stage::particleField),
                CastSoundDefinition.CODEC.listOf().optionalFieldOf("sounds", List.of()).forGetter(Stage::sounds)
        ).apply(instance, Stage::new));

        public Stage {
            threshold = Double.isFinite(threshold) ? Math.clamp(threshold, 0.0D, 1.0D) : 0.0D;
            particleField = particleField == null ? Optional.empty() : particleField;
            sounds = sounds == null ? List.of() : List.copyOf(sounds);
        }
    }

    public record Movement(ScaledValue horizontalDrag, ScaledValue verticalDrag, boolean disableSprinting) {
        public static final MapCodec<Movement> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                ScaledValue.COMPACT_CODEC.optionalFieldOf("horizontal_drag", ScaledValue.constant(1.0D)).forGetter(Movement::horizontalDrag),
                ScaledValue.COMPACT_CODEC.optionalFieldOf("vertical_drag", ScaledValue.constant(1.0D)).forGetter(Movement::verticalDrag),
                Codec.BOOL.optionalFieldOf("disable_sprinting", false).forGetter(Movement::disableSprinting)
        ).apply(instance, Movement::new));

        public static Movement defaults() {
            return new Movement(ScaledValue.constant(1.0D), ScaledValue.constant(1.0D), false);
        }

        public void apply(LivingEntity caster, ScaledValue.Context context) {
            if (caster == null) {
                return;
            }
            double horizontal = multiplier(horizontalDrag.resolve(context));
            double vertical = multiplier(verticalDrag.resolve(context));
            Vec3 current = caster.getDeltaMovement();
            caster.setDeltaMovement(current.x * horizontal, current.y * vertical, current.z * horizontal);
            if (disableSprinting) {
                caster.setSprinting(false);
            }
        }

        private static double multiplier(double value) {
            return Double.isFinite(value) ? Math.clamp(value, 0.0D, 1.0D) : 1.0D;
        }
    }

    public record Interruption(ScaledValue damageThreshold, int accumulationWindow, boolean releaseAfterMinimum) {
        public static final MapCodec<Interruption> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                ScaledValue.COMPACT_CODEC.fieldOf("damage_threshold").forGetter(Interruption::damageThreshold),
                Codec.intRange(1, 1200).optionalFieldOf("accumulation_window", 20).forGetter(Interruption::accumulationWindow),
                Codec.BOOL.optionalFieldOf("release_after_minimum", false).forGetter(Interruption::releaseAfterMinimum)
        ).apply(instance, Interruption::new));

        public boolean shouldInterrupt(HeldCastData data, int ticksElapsed, double damage, ScaledValue.Context context) {
            double threshold = damageThreshold.resolve(context);
            return Double.isFinite(threshold) && threshold > 0.0D
                    && data.addInterruptionDamage(damage, ticksElapsed, accumulationWindow) >= threshold;
        }
    }

    public record Cost(
            Identifier resource,
            ResourceOperation operation,
            Identifier source,
            ScaledValue cumulativeCost,
            boolean releaseOnFailure
    ) {
        public static final Identifier CHARGE_VALUE = AscensionCraft.prefix("cast/charge");
        public static final Identifier CHARGE_TICKS_VALUE = AscensionCraft.prefix("cast/charge_ticks");
        public static final Identifier MAXIMUM_CHARGE_TICKS_VALUE = AscensionCraft.prefix("cast/maximum_charge_ticks");
        public static final MapCodec<Cost> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Identifier.CODEC.fieldOf("resource").forGetter(Cost::resource),
                ResourceOperation.CODEC.optionalFieldOf("operation", ResourceOperation.CONSUME).forGetter(Cost::operation),
                Identifier.CODEC.optionalFieldOf("source", AscensionCraft.prefix("skill_casting")).forGetter(Cost::source),
                ScaledValue.COMPACT_CODEC.fieldOf("cumulative_cost").forGetter(Cost::cumulativeCost),
                Codec.BOOL.optionalFieldOf("release_on_failure", true).forGetter(Cost::releaseOnFailure)
        ).apply(instance, Cost::new));

        public double resolveCumulativeCost(ScaledValue.Context context) {
            double value = cumulativeCost.resolve(context);
            return Double.isFinite(value) ? Math.max(0.0D, value) : 0.0D;
        }

        public ResourceTransactionService.Result payIncrement(
                LivingEntity caster,
                Identifier skill,
                HeldCastData data,
                ScaledValue.Context context,
                int maximumChargeTicks
        ) {
            double target = resolveCumulativeCost(context);
            double delta = Math.max(0.0D, target - data.getCumulativeCostTarget());
            if (delta <= 1.0E-8D) {
                data.setCumulativeCostTarget(target);
                return null;
            }
            ResourceTransactionService.Result result = ResourceTransactionService.transact(request(
                    caster, skill, context, data.getChargeTicks(), maximumChargeTicks, delta, Set.of()
            ));
            if (result.succeeded() && result.appliedAmount() + 1.0E-8D >= result.resolvedAmount()) {
                data.setCumulativeCostTarget(target);
            }
            return result;
        }

        public boolean canStart(LivingEntity caster, Identifier skill, ScaledValue.Context context, int maximumChargeTicks) {
            double target = resolveCumulativeCost(context);
            if (target <= 1.0E-8D) {
                return true;
            }
            ResourceTransactionService.Result result = ResourceTransactionService.transact(request(
                    caster, skill, context, 1, maximumChargeTicks, target, Set.of(ResourceTransactionRequest.Flag.SIMULATE)
            ));
            return result.succeeded() && result.appliedAmount() + 1.0E-8D >= result.resolvedAmount();
        }

        private ResourceTransactionRequest request(
                LivingEntity caster,
                Identifier skill,
                ScaledValue.Context context,
                int chargeTicks,
                int maximumChargeTicks,
                double amount,
                Set<ResourceTransactionRequest.Flag> flags
        ) {
            return new ResourceTransactionRequest(
                    caster,
                    resource,
                    operation,
                    amount,
                    ResourceSourceIdentity.of(source, ResourceSourceIdentity.Tags.SKILL),
                    skill,
                    null,
                    Map.of(
                            CHARGE_VALUE, context.charge(),
                            CHARGE_TICKS_VALUE, (double) chargeTicks,
                            MAXIMUM_CHARGE_TICKS_VALUE, (double) maximumChargeTicks
                    ),
                    flags
            );
        }
    }
}
