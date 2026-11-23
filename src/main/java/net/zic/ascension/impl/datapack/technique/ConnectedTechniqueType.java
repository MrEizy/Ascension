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
import net.zic.ascension.impl.core.connection.ConnectionRequirements;
import net.zic.ascension.impl.core.technique.ConnectedTechnique;
import net.zic.ascension.impl.core.technique.TechniqueRealmStatBonus;

import java.util.List;

/**
 * ascension:connected_technique. Same fields as simple_technique plus physique_connection, bloodline_connection and path_connection,
 * at least one of which is required.
 */
public class ConnectedTechniqueType extends TechniqueType {
    private static final Codec<List<Identifier>> CULTIVATION_PATHS = Codec.either(Identifier.CODEC, Identifier.CODEC.listOf())
            .xmap(value -> value.map(List::of, List::copyOf),
                    paths -> paths.size() == 1 ? Either.left(paths.getFirst()) : Either.right(paths));
    @Override
    public MapCodec<? extends Technique<?>> codec() {
        return ConnectionRequirements.requireAtLeastOne(RecordCodecBuilder.<ConnectedTechnique>mapCodec(instance -> instance.group(
                ComponentSerialization.CODEC.fieldOf("name").forGetter(technique -> technique.getName(null)),
                ComponentSerialization.CODEC.fieldOf("description").forGetter(technique -> technique.getDescription(null)),
                ProgressActionList.CODEC.optionalFieldOf("on_added", ProgressActionList.EMPTY).forGetter(ConnectedTechnique::getOnAddedActions),
                TechniqueRealmStatBonus.CODEC.listOf().optionalFieldOf("realm_stat_growth", List.of()).forGetter(ConnectedTechnique::getRealmStatGrowth),
                Codec.unboundedMap(Identifier.CODEC, TechniqueRealmStatRate.CODEC).optionalFieldOf("stats_per_realm", Map.of()).forGetter(ConnectedTechnique::getStatsPerRealm),
                CULTIVATION_PATHS.optionalFieldOf("path", List.of()).forGetter(ConnectedTechnique::getCultivationPaths),
                Codec.intRange(0, 1000).optionalFieldOf("max_realm").forGetter(ConnectedTechnique::getMaxRealm),
                Codec.intRange(0, 1000).optionalFieldOf("max_minor_realm").forGetter(ConnectedTechnique::getMaxMinorRealm),
                AscensionItemTooltipDefinition.CODEC.optionalFieldOf("item_tooltip").forGetter(Technique::itemTooltip),
                RequirementHolder.CODEC.optionalFieldOf("requirements", RequirementHolder.EMPTY).forGetter(Technique::requirements),
                ConnectionRequirements.IDS.optionalFieldOf("physique_connection", List.of()).forGetter(technique -> technique.connections().physiques()),
                ConnectionRequirements.IDS.optionalFieldOf("bloodline_connection", List.of()).forGetter(technique -> technique.connections().bloodlines()),
                ConnectionRequirements.IDS.optionalFieldOf("path_connection", List.of()).forGetter(technique -> technique.connections().paths())
        ).apply(instance, (name, description, actions, growth, perRealm, paths, maxRealm, maxMinor, tooltip, requirements, physiques, bloodlines, requiredPaths) ->
                new ConnectedTechnique(name, description, actions, growth, perRealm, paths, maxRealm, maxMinor, tooltip, requirements,
                        new ConnectionRequirements(physiques, List.of(), bloodlines, requiredPaths)))));
    }
}