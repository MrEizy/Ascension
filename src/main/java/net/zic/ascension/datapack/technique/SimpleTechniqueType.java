package net.zic.ascension.datapack.technique;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.Identifier;
import net.zic.ascension.api.core.progression.ProgressActionHolder;
import net.zic.ascension.api.core.technique.Technique;
import net.zic.ascension.api.datapack.technique.TechniqueType;
import net.zic.ascension.core.bloodline.SimpleBloodline;
import net.zic.ascension.core.skill.SimplePassiveSkill;
import net.zic.ascension.core.technique.SimpleTechnique;
import net.zic.zenithlib.value_containers.ValueContainer;
import net.zic.zenithlib.value_containers.ValueContainerModifier;

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
                        Codec.INT.optionalFieldOf("max_realm").forGetter(SimpleTechnique::getHardCodedMaxMinorRealm),
                        Codec.INT.optionalFieldOf("min_realm").forGetter(SimpleTechnique::getHardCodedMinMajorRealm),
                        Codec.unboundedMap(Codec.INT,SimpleTechnique.MajorRealmNames.CODEC).optionalFieldOf("realm_names",Map.of()).forGetter(SimpleTechnique::getMajorRealmOverrides),
                        ProgressActionHolder.CODEC.fieldOf("realm_change_handler").forGetter(SimpleTechnique::getListeners)

                ).apply(instance,
                        (name,
                         description,
                         path,
                         milestones,
                         families,
                         max,
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
                                        min.orElse(0),
                                        handler,
                                        overrides
                                )
                        )
        );
    }
}
