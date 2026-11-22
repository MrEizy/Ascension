package net.zic.ascension.configuration.mobs;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;

public record MobConfiguration(List<PotentialTrait> traits, List<MobTraitPool> pools) {
    public static final Codec<MobConfiguration> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            PotentialTrait.CODEC.listOf().optionalFieldOf("traits", List.of()).forGetter(MobConfiguration::traits),
            MobTraitPool.CODEC.listOf().optionalFieldOf("pools", List.of()).forGetter(MobConfiguration::pools)
    ).apply(instance, MobConfiguration::new));
}
