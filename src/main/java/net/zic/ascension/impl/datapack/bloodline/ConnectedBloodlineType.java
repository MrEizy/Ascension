package net.zic.ascension.impl.datapack.bloodline;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.Identifier;
import net.zic.ascension.api.ascension.core.bloodline.Bloodline;
import net.zic.ascension.api.ascension.core.progression.ProgressActionHolder;
import net.zic.ascension.api.ascension.core.requirement.RequirementHolder;
import net.zic.ascension.api.ascension.datapack.bloodline.BloodlineType;
import net.zic.ascension.api.tooltip.AscensionItemTooltipDefinition;
import net.zic.ascension.impl.core.bloodline.ConnectedBloodline;
import net.zic.ascension.impl.core.bloodline.SimpleBloodline;
import net.zic.ascension.impl.core.connection.ConnectionRequirements;

import java.util.List;

/**
 * ascension:connected_bloodline. Same fields as simple bloodline plus physique_connection, technique_connection and path_connection,
 * at least one of which is required.
 */
public class ConnectedBloodlineType extends BloodlineType {
    @Override
    public MapCodec<? extends Bloodline<?>> codec() {
        return ConnectionRequirements.requireAtLeastOne(RecordCodecBuilder.<ConnectedBloodline>mapCodec(instance -> instance.group(
                ComponentSerialization.CODEC.fieldOf("name").forGetter(bloodline -> bloodline.getName(null)),
                ComponentSerialization.CODEC.fieldOf("description").forGetter(bloodline -> bloodline.getDescription(null)),
                Identifier.CODEC.listOf().optionalFieldOf("paths", List.of()).forGetter(SimpleBloodline::unlockedPaths),
                ProgressActionHolder.PROGRESS_HOLDER_CODEC.fieldOf("purity_handler").forGetter(SimpleBloodline::getHolder),
                AscensionItemTooltipDefinition.CODEC.optionalFieldOf("item_tooltip").forGetter(SimpleBloodline::itemTooltip),
                RequirementHolder.CODEC.optionalFieldOf("requirements", RequirementHolder.EMPTY).forGetter(SimpleBloodline::requirements),
                ConnectionRequirements.IDS.optionalFieldOf("physique_connection", List.of()).forGetter(bloodline -> bloodline.connections().physiques()),
                ConnectionRequirements.IDS.optionalFieldOf("technique_connection", List.of()).forGetter(bloodline -> bloodline.connections().techniques()),
                ConnectionRequirements.IDS.optionalFieldOf("path_connection", List.of()).forGetter(bloodline -> bloodline.connections().paths())
        ).apply(instance, (name, description, paths, purityHandler, tooltip, requirements, physiques, techniques, requiredPaths) ->
                new ConnectedBloodline(name, description, paths, purityHandler, tooltip, requirements,
                        new ConnectionRequirements(physiques, techniques, List.of(), requiredPaths)))));
    }
}