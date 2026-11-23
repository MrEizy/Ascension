package net.zic.ascension.configuration.mobs;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;

import java.util.List;
import java.util.Map;

public record MobConfiguration(List<PotentialTrait> traits, List<MobTraitPool> pools, MobCultivationRules cultivation, Map<Identifier, Double> statBiases, MobProgressionRules progression) {
    public static final Codec<MobConfiguration> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            PotentialTrait.CODEC.listOf().optionalFieldOf("traits", List.of()).forGetter(MobConfiguration::traits),
            MobTraitPool.CODEC.listOf().optionalFieldOf("pools", List.of()).forGetter(MobConfiguration::pools),
            MobCultivationRules.CODEC.optionalFieldOf("cultivation", MobCultivationRules.EMPTY).forGetter(MobConfiguration::cultivation),
            Codec.unboundedMap(Identifier.CODEC, Codec.DOUBLE).optionalFieldOf("stat_biases", Map.of()).forGetter(MobConfiguration::statBiases),
            MobProgressionRules.CODEC.optionalFieldOf("progression", MobProgressionRules.DEFAULT).forGetter(MobConfiguration::progression)
    ).apply(instance, MobConfiguration::new));
}
