package net.zic.ascension.impl.datapack.technique;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.zic.ascension.api.ascension.core.technique.Technique;
import net.zic.ascension.api.ascension.datapack.technique.TechniqueType;
import net.zic.ascension.impl.core.connection.ConnectionRequirements;
import net.zic.ascension.impl.core.technique.ConnectedTechnique;

import java.util.List;

/**
 * ascension:connected_technique. Every field of simple_technique plus physique_connection, bloodline_connection and
 * path_connection, at least one of which is required.
 */
public class ConnectedTechniqueType extends TechniqueType {
    @Override
    public MapCodec<? extends Technique<?>> codec() {
        return ConnectionRequirements.requireAtLeastOne(RecordCodecBuilder.<ConnectedTechnique>mapCodec(instance -> instance.group(
                SimpleTechniqueCodecs.simple().forGetter((ConnectedTechnique technique) -> technique),
                ConnectionRequirements.IDS.optionalFieldOf("physique_connection", List.of()).forGetter(technique -> technique.connections().physiques()),
                ConnectionRequirements.IDS.optionalFieldOf("bloodline_connection", List.of()).forGetter(technique -> technique.connections().bloodlines()),
                ConnectionRequirements.IDS.optionalFieldOf("path_connection", List.of()).forGetter(technique -> technique.connections().paths())
        ).apply(instance, (base, physiques, bloodlines, paths) ->
                new ConnectedTechnique(base, new ConnectionRequirements(physiques, List.of(), bloodlines, paths)))));
    }
}