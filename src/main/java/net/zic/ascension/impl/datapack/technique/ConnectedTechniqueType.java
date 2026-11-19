package net.zic.ascension.impl.datapack.technique;

import com.mojang.serialization.MapCodec;
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
    @Override
    public MapCodec<? extends Technique<?>> codec() {
        return ConnectionRequirements.requireAtLeastOne(RecordCodecBuilder.<ConnectedTechnique>mapCodec(instance -> instance.group(
                ComponentSerialization.CODEC.fieldOf("name").forGetter(technique -> technique.getName(null)),
                ComponentSerialization.CODEC.fieldOf("description").forGetter(technique -> technique.getDescription(null)),
                ProgressActionList.CODEC.optionalFieldOf("on_added", ProgressActionList.EMPTY).forGetter(ConnectedTechnique::getOnAddedActions),
                TechniqueRealmStatBonus.CODEC.listOf().optionalFieldOf("realm_stat_growth", List.of()).forGetter(ConnectedTechnique::getRealmStatGrowth),
                AscensionItemTooltipDefinition.CODEC.optionalFieldOf("item_tooltip").forGetter(Technique::itemTooltip),
                RequirementHolder.CODEC.optionalFieldOf("requirements", RequirementHolder.EMPTY).forGetter(Technique::requirements),
                ConnectionRequirements.IDS.optionalFieldOf("physique_connection", List.of()).forGetter(technique -> technique.connections().physiques()),
                ConnectionRequirements.IDS.optionalFieldOf("bloodline_connection", List.of()).forGetter(technique -> technique.connections().bloodlines()),
                ConnectionRequirements.IDS.optionalFieldOf("path_connection", List.of()).forGetter(technique -> technique.connections().paths())
        ).apply(instance, (name, description, actions, growth, tooltip, requirements, physiques, bloodlines, requiredPaths) ->
                new ConnectedTechnique(name, description, actions, growth, tooltip, requirements,
                        new ConnectionRequirements(physiques, List.of(), bloodlines, requiredPaths)))));
    }
}