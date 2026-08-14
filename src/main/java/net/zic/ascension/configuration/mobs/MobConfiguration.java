package net.zic.ascension.configuration.mobs;

import net.zic.ascension.configuration.mob_traits.MobTraitDefinitionV1;
import net.zic.ascension.configuration.mobs.cultivation.PotentialPathDefinition;
import net.zic.ascension.mob_cultivation.generation.MobCultivationEliteTier;

import java.util.List;
import java.util.Map;

/**
 * Holds the configuration data for an entity type
 */
public class MobConfiguration {
    private final List<MobTierDefinition> tierDefinitions;
    private final Map<MobCultivationEliteTier, List<MobTraitDefinitionV1.PotentialTrait>> traits;
    private final Map<MobCultivationEliteTier,List<PotentialPathDefinition>> paths;
    public MobConfiguration(List<MobTierDefinition> tierDefinitions, Map<MobCultivationEliteTier, List<MobTraitDefinitionV1.PotentialTrait>> traits, Map<MobCultivationEliteTier, List<PotentialPathDefinition>> paths) {
        this.tierDefinitions = tierDefinitions;
        this.traits = traits;
        this.paths = paths;
    }
}
