package net.zic.ascension.configuration.mobs;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.RandomSource;
import net.zic.ascension.configuration.mob_traits.MobTraitRoll;
import net.zic.ascension.configuration.mob_traits.traits.CultivationTraitDefinition;

import java.util.List;

public record MobCultivationRules(List<CultivationTraitDefinition.WeightedPath> foundationPaths,
                                  List<CultivationTraitDefinition.WeightedPath> subpaths,
                                  CultivationTraitDefinition.RollRange subpathCount,
                                  List<CultivationTraitDefinition.SkillPool> skillPools) {
    public static final MobCultivationRules EMPTY = new MobCultivationRules(
            List.of(), List.of(), new CultivationTraitDefinition.RollRange(0, 0), List.of());

    public static final Codec<MobCultivationRules> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            CultivationTraitDefinition.WeightedPath.CODEC.listOf().optionalFieldOf("foundation_paths", List.of()).forGetter(MobCultivationRules::foundationPaths),
            CultivationTraitDefinition.WeightedPath.CODEC.listOf().optionalFieldOf("subpaths", List.of()).forGetter(MobCultivationRules::subpaths),
            CultivationTraitDefinition.RollRange.CODEC.optionalFieldOf("subpath_count", new CultivationTraitDefinition.RollRange(0, 0)).forGetter(MobCultivationRules::subpathCount),
            CultivationTraitDefinition.SkillPool.CODEC.listOf().optionalFieldOf("skill_pools", List.of()).forGetter(MobCultivationRules::skillPools)
    ).apply(instance, MobCultivationRules::new));

    public MobTraitRoll roll(RandomSource random) {
        return CultivationTraitDefinition.roll(random, foundationPaths, subpaths, subpathCount, skillPools);
    }
}
