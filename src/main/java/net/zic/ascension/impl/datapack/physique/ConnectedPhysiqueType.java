package net.zic.ascension.impl.datapack.physique;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.Identifier;
import net.zic.ascension.api.ascension.core.path.bonus.PathBonusHolder;
import net.zic.ascension.api.ascension.core.physique.Physique;
import net.zic.ascension.api.ascension.core.requirement.RequirementHolder;
import net.zic.ascension.api.ascension.datapack.physique.PhysiqueType;
import net.zic.ascension.api.tooltip.AscensionItemTooltipDefinition;
import net.zic.ascension.impl.core.connection.ConnectionRequirements;
import net.zic.ascension.impl.core.physique.ConnectedPhysique;
import net.zic.zenithlib.value_containers.typed.ValueContainerCodecHelper;

import java.util.List;
import java.util.Map;

/**
 * ascension:connected_physique. Same fields as simple physique plus technique_connection, bloodline_connection and path_connection,
 * at least one of which is required.
 */
public class ConnectedPhysiqueType extends PhysiqueType {
    @Override
    public MapCodec<? extends Physique<?>> codec() {
        return ConnectionRequirements.requireAtLeastOne(RecordCodecBuilder.<ConnectedPhysique>mapCodec(instance -> instance.group(
                ComponentSerialization.CODEC.fieldOf("name").forGetter(ConnectedPhysique::name),
                ComponentSerialization.CODEC.fieldOf("description").forGetter(ConnectedPhysique::description),
                Identifier.CODEC.listOf().fieldOf("paths").forGetter(ConnectedPhysique::unlockedPaths),
                Identifier.CODEC.listOf().optionalFieldOf("skills", List.of()).forGetter(ConnectedPhysique::skills),
                ValueContainerCodecHelper.containersCodec(Codec.DOUBLE).optionalFieldOf("stats", Map.of()).forGetter(ConnectedPhysique::statModifiers),
                PathBonusHolder.MODIFIER_CODEC.optionalFieldOf("path_bonus", Map.of()).forGetter(ConnectedPhysique::pathBonusModifiers),
                AscensionItemTooltipDefinition.CODEC.optionalFieldOf("item_tooltip").forGetter(ConnectedPhysique::itemTooltip),
                RequirementHolder.CODEC.optionalFieldOf("requirements", RequirementHolder.EMPTY).forGetter(ConnectedPhysique::requirements),
                ConnectionRequirements.IDS.optionalFieldOf("technique_connection", List.of()).forGetter(physique -> physique.connections().techniques()),
                ConnectionRequirements.IDS.optionalFieldOf("bloodline_connection", List.of()).forGetter(physique -> physique.connections().bloodlines()),
                ConnectionRequirements.IDS.optionalFieldOf("path_connection", List.of()).forGetter(physique -> physique.connections().paths())
        ).apply(instance, (name, description, paths, skills, stats, pathBonus, tooltip, requirements, techniques, bloodlines, requiredPaths) ->
                new ConnectedPhysique(name, description, paths, skills, stats, pathBonus, tooltip, requirements,
                        new ConnectionRequirements(List.of(), techniques, bloodlines, requiredPaths)))));
    }
}