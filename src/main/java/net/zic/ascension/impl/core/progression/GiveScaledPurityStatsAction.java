package net.zic.ascension.impl.core.progression;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.zic.ascension.api.ascension.core.bloodline.BloodlineData;
import net.zic.ascension.api.ascension.core.progression.*;
import net.zic.ascension.api.ascension.datapack.progresison.ProgressActionType;
import net.zic.ascension.api.rpg_engine.source.OriginSource;
import net.zic.ascension.configuration.ConfigurationRegistries;
import net.zic.ascension.configuration.cultivation.PurityStatScaling;
import net.zic.ascension.impl.datapack.progression.AscensionProgressActionTypes;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record GiveScaledPurityStatsAction(UUID uuid, Identifier id, Either<Identifier, PurityStatScaling> scaling,
                                          Map<Identifier, Reward> stats) implements ProgressAction {
    private static final Identifier GROUP = Identifier.parse("ascension:bloodline_cultivation_growth");

    public record Reward(double continuous, Map<String, Double> milestones) {
        public static final Codec<Reward> CODEC = RecordCodecBuilder.<Reward>create(instance -> instance.group(
                Codec.DOUBLE.optionalFieldOf("continuous", 0.0D).forGetter(Reward::continuous),
                Codec.unboundedMap(Codec.STRING, Codec.DOUBLE).optionalFieldOf("milestones", Map.of()).forGetter(Reward::milestones)
        ).apply(instance, Reward::new)).validate(value -> value.valid() ? DataResult.success(value)
                : DataResult.error(() -> "Purity rewards must be finite, non-negative, with integer milestone keys 1-100"));

        public Reward { milestones = Map.copyOf(milestones); }
        private boolean valid() {
            if (!StatScalingMath.nonNegativeFinite(continuous) || !Double.isFinite(total())) return false;
            for (var entry : milestones.entrySet()) {
                try {
                    int p = Integer.parseInt(entry.getKey());
                    if (p < 1 || p > 100 || !Integer.toString(p).equals(entry.getKey()) || !StatScalingMath.nonNegativeFinite(entry.getValue())) return false;
                } catch (NumberFormatException ex) { return false; }
            }
            return true;
        }
        double total() { return continuous + milestones.values().stream().mapToDouble(Double::doubleValue).sum(); }
        double fraction(int purity) {
            double reached = milestones.entrySet().stream().filter(e -> Integer.parseInt(e.getKey()) <= purity)
                    .mapToDouble(Map.Entry::getValue).sum();
            return StatScalingMath.purityFraction(continuous, reached, total(), purity);
        }
    }

    public static final MapCodec<GiveScaledPurityStatsAction> CODEC = RecordCodecBuilder.<GiveScaledPurityStatsAction>mapCodec(instance -> instance.group(
            Identifier.CODEC.optionalFieldOf("id", Identifier.parse("ascension:default")).forGetter(GiveScaledPurityStatsAction::id),
            Codec.either(Identifier.CODEC, PurityStatScaling.CODEC).fieldOf("scaling").forGetter(GiveScaledPurityStatsAction::scaling),
            Codec.unboundedMap(Identifier.CODEC, Reward.CODEC).fieldOf("stats").forGetter(GiveScaledPurityStatsAction::stats)
    ).apply(instance, (id, scaling, stats) -> new GiveScaledPurityStatsAction(UUID.randomUUID(), id, scaling, Map.copyOf(stats))));

    @Override public UUID getUniqueId() { return uuid; }
    @Override public ProgressActionType getType() { return AscensionProgressActionTypes.GIVE_SCALED_PURITY_STATS.get(); }

    @Override
    public void run(UUID holderId, OriginSource source, Identifier contextIdentifier, Object contextData,
                    ProgressDirection direction, String salt) {
        if (!(contextData instanceof BloodlineData bloodline)) return;
        int purity = bloodline.getPurity();
        PurityStatScaling profile = ScalingProfiles.resolve(source.getRegistryAccess(), ConfigurationRegistries.PURITY_STAT_SCALING_REGISTRY, scaling);
        stats.forEach((stat, reward) -> {
            double fraction = reward.fraction(purity);
            ScaledStatModifiers.setFlat(source, stat, ScaledStatModifiers.id("purity", contextIdentifier, id, stat, "flat"),
                    profile.flat(reward.total()) * fraction);
            ScaledStatModifiers.setMultiplier(source, stat, ScaledStatModifiers.id("purity", contextIdentifier, id, stat, "multiplier"),
                    GROUP, profile.multiplier(reward.total()) * fraction);
        });
        source.updateEntityStatHolder();
    }

    @Override
    public List<ProgressActionDescription> getDescriptions(RegistryAccess access) {
        PurityStatScaling profile = ScalingProfiles.resolve(access, ConfigurationRegistries.PURITY_STAT_SCALING_REGISTRY, scaling);
        List<ProgressActionDescription> descriptions = new ArrayList<>();
        stats.forEach((stat, reward) -> {
            Component label = ProgressionDescriptionUtil.statName(stat).copy().append(" (100% purity total)");
            double flat = profile.flat(reward.total()), multiplier = profile.multiplier(reward.total());
            if (flat != 0) descriptions.add(ProgressActionDescription.numeric(label, Component.literal(ProgressionDescriptionUtil.signedNumber(flat)), flat));
            if (multiplier != 0) descriptions.add(ProgressActionDescription.numeric(label, Component.literal(ProgressionDescriptionUtil.signedPercent(multiplier)), multiplier));
        });
        return descriptions;
    }
}
