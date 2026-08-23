package net.zic.ascension.configuration.mobs;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.entity.Mob;
import net.zic.ascension.configuration.mobs.condition.TierCondition;
import net.zic.ascension.mob_cultivation.generation.MobCultivationEliteTier;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Holds the configuration data for an entity type
 */
public record MobConfiguration(List<PotentialTier> potentialTiers,
                               Map<MobCultivationEliteTier, List<PotentialTrait>> traits) {
    public static final Codec<MobConfiguration> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    PotentialTier.CODEC.fieldOf("tiers").forGetter(MobConfiguration::potentialTiers),
                    Codec.unboundedMap(
                            MobTierDefinition.TIER_CODEC,
                            PotentialTrait.CODEC.listOf()
                    ).fieldOf("traits").forGetter(MobConfiguration::traits)
            ).apply(instance, MobConfiguration::new)
    );
    public MobConfiguration(List<PotentialTier> potentialTiers,
                            Map<MobCultivationEliteTier, List<PotentialTrait>> traits){
        this.potentialTiers = potentialTiers;
        this.traits = traits;
        System.out.println("CREATED DATA MAP ENTRY");
    }
    public List<PotentialTrait> getPotentialTraits(MobCultivationEliteTier tier) {
        return traits.getOrDefault(tier, List.of());
    }


}
