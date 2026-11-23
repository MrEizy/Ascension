package net.zic.ascension.impl.datapack.technique;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.Codec;
import com.mojang.datafixers.util.Either;
import net.minecraft.resources.Identifier;
import net.zic.ascension.impl.core.technique.TechniqueRealmStatRate;
import java.util.Map;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.ComponentSerialization;
import net.zic.ascension.api.ascension.core.progression.ProgressActionList;
import net.zic.ascension.api.ascension.core.requirement.RequirementHolder;
import net.zic.ascension.api.ascension.core.technique.Technique;
import net.zic.ascension.api.ascension.datapack.technique.TechniqueType;
import net.zic.ascension.api.tooltip.AscensionItemTooltipDefinition;
import net.zic.ascension.impl.core.technique.SimpleTechnique;
import net.zic.ascension.impl.core.technique.TechniqueRealmStatBonus;

import java.util.List;

public class SimpleTechniqueType extends TechniqueType {
    private static final Codec<List<Identifier>> CULTIVATION_PATHS = Codec.either(Identifier.CODEC, Identifier.CODEC.listOf())
            .xmap(value -> value.map(List::of, List::copyOf),
                    paths -> paths.size() == 1 ? Either.left(paths.getFirst()) : Either.right(paths));
    @Override
    public MapCodec<? extends Technique<?>> codec() {
        return RecordCodecBuilder.<SimpleTechnique>mapCodec(instance -> instance.group(
                ComponentSerialization.CODEC.fieldOf("name").forGetter(technique -> technique.getName(null)),
                ComponentSerialization.CODEC.fieldOf("description").forGetter(technique -> technique.getDescription(null)),
                ProgressActionList.CODEC.optionalFieldOf("on_added", ProgressActionList.EMPTY).forGetter(SimpleTechnique::getOnAddedActions),
                TechniqueRealmStatBonus.CODEC.listOf().optionalFieldOf("realm_stat_growth", List.of()).forGetter(SimpleTechnique::getRealmStatGrowth),
                Codec.unboundedMap(Identifier.CODEC, TechniqueRealmStatRate.CODEC).optionalFieldOf("stats_per_realm", Map.of()).forGetter(SimpleTechnique::getStatsPerRealm),
                CULTIVATION_PATHS.optionalFieldOf("path", List.of()).forGetter(SimpleTechnique::getCultivationPaths),
                Codec.intRange(0, 1000).optionalFieldOf("max_realm").forGetter(SimpleTechnique::getMaxRealm),
                Codec.intRange(0, 1000).optionalFieldOf("max_minor_realm").forGetter(SimpleTechnique::getMaxMinorRealm),
                AscensionItemTooltipDefinition.CODEC.optionalFieldOf("item_tooltip").forGetter(Technique::itemTooltip),
                RequirementHolder.CODEC.optionalFieldOf("requirements", RequirementHolder.EMPTY).forGetter(Technique::requirements)
        ).apply(instance, SimpleTechnique::new));
    }
}
