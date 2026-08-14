package net.zic.ascension.configuration.mobs;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.zic.ascension.mob_cultivation.generation.MobCultivationEliteTier;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Holds the configuration data for an entity type
 */
public record MobConfiguration(List<MobTierDefinition> tierDefinitions,
                               Map<MobCultivationEliteTier, List<PotentialTrait>> traits) {
    public static final Codec<MobConfiguration> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    MobTierDefinition.CODEC.fieldOf("tiers").forGetter(MobConfiguration::tierDefinitions),
                    Codec.unboundedMap(
                            MobTierDefinition.TIER_CODEC,
                            PotentialTrait.CODEC.listOf()
                    ).fieldOf("traits").forGetter(MobConfiguration::traits)
            ).apply(instance, MobConfiguration::new)
    );
    public MobConfiguration(List<MobTierDefinition> tierDefinitions,
                            Map<MobCultivationEliteTier, List<PotentialTrait>> traits){
        this.tierDefinitions = tierDefinitions;
        this.traits = traits;
        System.out.println("CREATED DATA MAP ENTRY");
    }
    public List<PotentialTrait> getPotentialTraits(MobCultivationEliteTier tier) {
        return traits.getOrDefault(tier, List.of());
    }

    public MobCultivationEliteTier rollTier() {
        int totalWeight = 0;

        for (MobTierDefinition definition : tierDefinitions) totalWeight += definition.weight();

        int roll = ThreadLocalRandom.current().nextInt(totalWeight);
        totalWeight = 0;
        for (MobTierDefinition definition : tierDefinitions) {
            totalWeight += definition.weight();
            if (roll <= totalWeight) {
                return definition.tier();
            }
        }
        return MobCultivationEliteTier.NORMAL;

    }
}
