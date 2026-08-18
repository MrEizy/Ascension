package net.zic.ascension.impl.datapack.technique;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.Identifier;
import net.zic.ascension.api.ascension.core.progression.ProgressActionHolder;
import net.zic.ascension.api.ascension.core.technique.Technique;
import net.zic.ascension.api.ascension.core.technique.TechniqueSkillDefinition;
import net.zic.ascension.api.ascension.datapack.technique.TechniqueType;
import net.zic.ascension.api.tooltip.AscensionItemTooltipDefinition;
import net.zic.ascension.impl.core.technique.SimpleTechnique;
import net.zic.ascension.impl.core.technique.realm.MajorRealmDefinitionOverride;

import java.util.List;
import java.util.Map;

public class SimpleTechniqueType extends TechniqueType {
    @Override
    public MapCodec<? extends Technique> codec() {
        ProgressActionHolder emptyHandler = ProgressActionHolder.from(List.of());
        return RecordCodecBuilder.<SimpleTechnique>mapCodec(instance -> instance.group(
                ComponentSerialization.CODEC.fieldOf("name").forGetter(technique -> technique.getName(null)),
                ComponentSerialization.CODEC.fieldOf("description").forGetter(technique -> technique.getDescription(null)),
                Identifier.CODEC.fieldOf("path").forGetter(SimpleTechnique::getPath),
                Codec.INT.listOf().optionalFieldOf("milestone_realms", List.of()).forGetter(SimpleTechnique::getMilestoneRealms),
                Codec.STRING.listOf().optionalFieldOf("technique_families", List.of()).forGetter(SimpleTechnique::getTechniqueFamilies),
                Codec.INT.optionalFieldOf("max_realm").forGetter(SimpleTechnique::getHardCodedMaxMajorRealm),
                Codec.INT.optionalFieldOf("max_minor_realm").forGetter(SimpleTechnique::getHardCodedMaxMinorRealm),
                Codec.INT.optionalFieldOf("min_realm").forGetter(SimpleTechnique::getHardCodedMinMajorRealm),
                Codec.unboundedMap(
                                Codec.STRING.xmap(Integer::parseInt, Object::toString),
                                MajorRealmDefinitionOverride.CODEC
                        )
                        .optionalFieldOf("realm_overrides", Map.of())
                        .forGetter(SimpleTechnique::getMajorRealmOverrides),
                Codec.unboundedMap(Identifier.CODEC, TechniqueSkillDefinition.CODEC).optionalFieldOf("skills", Map.of()).forGetter(SimpleTechnique::getSkills),
                ProgressActionHolder.PROGRESS_HOLDER_CODEC.optionalFieldOf("realm_change_handler", emptyHandler).forGetter(SimpleTechnique::getHolder),
                AscensionItemTooltipDefinition.CODEC.optionalFieldOf("item_tooltip").forGetter(Technique::itemTooltip)
        ).apply(instance, (
                name,
                description,
                path,
                milestones,
                families,
                max,
                maxMinor,
                min,
                overrides,
                skills,
                handler,
                itemTooltip
        ) -> new SimpleTechnique(
                name,
                description,
                path,
                milestones,
                families,
                max.orElse(null),
                maxMinor.orElse(null),
                min.orElse(0),
                itemTooltip,
                skills,
                handler,
                overrides
        )));
    }
}
