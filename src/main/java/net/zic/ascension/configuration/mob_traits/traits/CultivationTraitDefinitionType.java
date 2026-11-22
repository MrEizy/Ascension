package net.zic.ascension.configuration.mob_traits.traits;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.ComponentSerialization;
import net.zic.ascension.configuration.mob_traits.MobTraitDefinition;
import net.zic.ascension.configuration.mob_traits.MobTraitDefinitionType;

import java.util.List;

public class CultivationTraitDefinitionType extends MobTraitDefinitionType {
    @Override
    public MapCodec<? extends MobTraitDefinition> codec() {
        return RecordCodecBuilder.<CultivationTraitDefinition>mapCodec(instance -> instance.group(
                ComponentSerialization.CODEC.fieldOf("name").forGetter(CultivationTraitDefinition::name),
                CultivationTraitDefinition.WeightedPath.CODEC.listOf().optionalFieldOf("foundation_paths", List.of()).forGetter(CultivationTraitDefinition::foundationPaths),
                CultivationTraitDefinition.WeightedPath.CODEC.listOf().optionalFieldOf("subpaths", List.of()).forGetter(CultivationTraitDefinition::subpaths),
                CultivationTraitDefinition.RollRange.CODEC.optionalFieldOf("subpath_count", new CultivationTraitDefinition.RollRange(0, 0)).forGetter(CultivationTraitDefinition::subpathCount),
                CultivationTraitDefinition.SkillPool.CODEC.listOf().optionalFieldOf("skill_pools", List.of()).forGetter(CultivationTraitDefinition::skillPools)
        ).apply(instance, CultivationTraitDefinition::new));
    }
}
