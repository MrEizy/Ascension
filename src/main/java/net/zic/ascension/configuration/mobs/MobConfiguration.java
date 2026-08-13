package net.zic.ascension.configuration.mobs;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.zic.ascension.mob_cultivation.generation.MobCultivationEliteTier;

import java.util.List;
import java.util.Map;

/**
 * Holds the configuration data for an entity type
 */
public class MobConfiguration {
    private final List<MobTierDefinition> tierDefinitions;
    private final Map<MobCultivationEliteTier, List<MobTraitDefinition.PotentialTrait>> traits;

    public MobConfiguration(List<MobTierDefinition> tierDefinitions, Map<MobCultivationEliteTier, List<MobTraitDefinition.PotentialTrait>> traits) {
        this.tierDefinitions = tierDefinitions;
        this.traits = traits;
    }
}
