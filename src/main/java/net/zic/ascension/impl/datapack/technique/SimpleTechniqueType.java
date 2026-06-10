package net.zic.ascension.impl.datapack.technique;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.Identifier;
import net.zic.ascension.api.core.progression.ProgressActionHolder;
import net.zic.ascension.api.core.technique.Technique;
import net.zic.ascension.api.datapack.technique.TechniqueType;
import net.zic.ascension.impl.core.technique.SimpleTechnique;

import java.util.List;
import java.util.Map;

public class SimpleTechniqueType extends TechniqueType {
    @Override
    public MapCodec<? extends Technique> codec() {
        return RecordCodecBuilder.<SimpleTechnique>mapCodec(instance ->
                instance.group(
                        ComponentSerialization.CODEC.fieldOf("name").forGetter(technique->technique.getName(null)),
                        ComponentSerialization.CODEC.fieldOf("description").forGetter(technique->technique.getDescription(null)),
                        Identifier.CODEC.fieldOf("path").forGetter(SimpleTechnique::getPath),
                        Codec.INT.listOf().optionalFieldOf("milestone_realms",List.of()).forGetter(SimpleTechnique::getMilestoneRealms),
                        Codec.STRING.listOf().optionalFieldOf("technique_families",List.of()).forGetter(SimpleTechnique::getTechniqueFamilies),
                        Codec.INT.optionalFieldOf("max_realm").forGetter(SimpleTechnique::getHardCodedMaxMajorRealm),
                        Codec.INT.optionalFieldOf("max_minor_realm").forGetter(SimpleTechnique::getHardCodedMaxMinorRealm),
                        Codec.INT.optionalFieldOf("min_realm").forGetter(SimpleTechnique::getHardCodedMinMajorRealm),
                        Codec.unboundedMap(Codec.INT,SimpleTechnique.MajorRealmNames.CODEC).optionalFieldOf("realm_names",Map.of()).forGetter(SimpleTechnique::getMajorRealmOverrides),
                        ProgressActionHolder.PROGRESS_HOLDER_CODEC.fieldOf("realm_change_handler").forGetter(SimpleTechnique::getHolder)

                ).apply(instance,
                        (name,
                         description,
                         path,
                         milestones,
                         families,
                         max,
                         max_minor,
                         min,
                         overrides,
                         handler)->
                                new SimpleTechnique(
                                        name,
                                        description,
                                        path,
                                        milestones,
                                        families,
                                        max.orElse(null),
                                        max_minor.orElse(null),
                                        min.orElse(0),
                                        handler,
                                        overrides
                                )
                        )
        );
    }
}
