package net.zic.ascension.configuration.mobs;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Map;

public record MobProgressionRules(double rateMultiplier, double potentialMultiplier, double qiSensitivity,
                                  double affinitySensitivity, double foundationWeight, double subpathWeight,
                                  double maxGainPerUpdate, int maxMajorRealm, Map<String, Double> rarityMultipliers) {
    public static final MobProgressionRules DEFAULT = new MobProgressionRules(
            1.0D, 1.0D, 1.0D, 0.25D, 1.0D, 0.3D, 100000.0D, -1,
            Map.of("normal", 1.0D, "elite", 1.25D, "ancient", 1.6D));

    public static final Codec<MobProgressionRules> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.DOUBLE.optionalFieldOf("rate_multiplier", DEFAULT.rateMultiplier()).forGetter(MobProgressionRules::rateMultiplier),
            Codec.DOUBLE.optionalFieldOf("potential_multiplier", DEFAULT.potentialMultiplier()).forGetter(MobProgressionRules::potentialMultiplier),
            Codec.DOUBLE.optionalFieldOf("qi_sensitivity", DEFAULT.qiSensitivity()).forGetter(MobProgressionRules::qiSensitivity),
            Codec.DOUBLE.optionalFieldOf("affinity_sensitivity", DEFAULT.affinitySensitivity()).forGetter(MobProgressionRules::affinitySensitivity),
            Codec.DOUBLE.optionalFieldOf("foundation_weight", DEFAULT.foundationWeight()).forGetter(MobProgressionRules::foundationWeight),
            Codec.DOUBLE.optionalFieldOf("subpath_weight", DEFAULT.subpathWeight()).forGetter(MobProgressionRules::subpathWeight),
            Codec.DOUBLE.optionalFieldOf("max_gain_per_update", DEFAULT.maxGainPerUpdate()).forGetter(MobProgressionRules::maxGainPerUpdate),
            Codec.INT.optionalFieldOf("max_major_realm", DEFAULT.maxMajorRealm()).forGetter(MobProgressionRules::maxMajorRealm),
            Codec.unboundedMap(Codec.STRING, Codec.DOUBLE).optionalFieldOf("rarity_multipliers", DEFAULT.rarityMultipliers()).forGetter(MobProgressionRules::rarityMultipliers)
    ).apply(instance, MobProgressionRules::new));

    public MobProgressionRules {
        rarityMultipliers = Map.copyOf(rarityMultipliers);
    }
}
