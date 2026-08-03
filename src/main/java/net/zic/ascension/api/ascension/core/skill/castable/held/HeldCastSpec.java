package net.zic.ascension.api.ascension.core.skill.castable.held;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public record HeldCastSpec(
        int minimumCharge,
        int maximumCharge,
        int cooldown,
        Optional<HeldCastCostDefinition> cost,
        HeldCastMovementDefinition movement,
        Optional<HeldCastInterruptionDefinition> interruption,
        List<HeldCastChargeStage> stages
) {
    public static final Codec<HeldCastSpec> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.intRange(0, 72000).optionalFieldOf("minimum_charge", 0).forGetter(HeldCastSpec::minimumCharge),
            Codec.intRange(1, 72000).optionalFieldOf("maximum_charge", 40).forGetter(HeldCastSpec::maximumCharge),
            Codec.intRange(0, 72000).optionalFieldOf("cooldown", 0).forGetter(HeldCastSpec::cooldown),
            HeldCastCostDefinition.CODEC.codec().optionalFieldOf("cost").forGetter(HeldCastSpec::cost),
            HeldCastMovementDefinition.CODEC.codec().optionalFieldOf("movement", HeldCastMovementDefinition.defaults())
                    .forGetter(HeldCastSpec::movement),
            HeldCastInterruptionDefinition.CODEC.codec().optionalFieldOf("interruption")
                    .forGetter(HeldCastSpec::interruption),
            HeldCastChargeStage.CODEC.listOf().optionalFieldOf("stages", List.of())
                    .forGetter(HeldCastSpec::stages)
    ).apply(instance, HeldCastSpec::new));

    public HeldCastSpec {
        maximumCharge = Math.max(1, maximumCharge);
        minimumCharge = Math.clamp(minimumCharge, 0, maximumCharge);
        cooldown = Math.max(0, cooldown);
        cost = cost == null ? Optional.empty() : cost;
        movement = movement == null ? HeldCastMovementDefinition.defaults() : movement;
        interruption = interruption == null ? Optional.empty() : interruption;

        List<HeldCastChargeStage> sorted = new ArrayList<>(stages == null ? List.of() : stages);
        sorted.sort(Comparator.comparingDouble(HeldCastChargeStage::threshold));
        Map<Double, HeldCastChargeStage> unique = new LinkedHashMap<>();
        for (HeldCastChargeStage stage : sorted) {
            unique.put(stage.threshold(), stage);
        }
        sorted = new ArrayList<>(unique.values());
        if (sorted.isEmpty() || sorted.getFirst().threshold() > 0.0D) {
            sorted.addFirst(new HeldCastChargeStage(0.0D, Optional.empty(), List.of()));
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
}
