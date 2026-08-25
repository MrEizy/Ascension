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
import net.zic.ascension.impl.core.technique.KillProgressionTechnique;
import net.zic.ascension.impl.core.technique.realm.MajorRealmDefinitionOverride;

import java.util.List;
import java.util.Map;

public final class KillProgressionTechniqueType extends TechniqueType {
    @Override
    public MapCodec<? extends Technique> codec() {
        ProgressActionHolder emptyHandler = ProgressActionHolder.from(List.of());
        return RecordCodecBuilder.<KillProgressionTechnique>mapCodec(instance -> instance.group(
                ComponentSerialization.CODEC.fieldOf("name").forGetter(technique -> technique.getName(null)),
                ComponentSerialization.CODEC.fieldOf("description").forGetter(technique -> technique.getDescription(null)),
                Identifier.CODEC.fieldOf("path").forGetter(KillProgressionTechnique::getPath),
                Codec.INT.listOf().optionalFieldOf("milestone_realms", List.of()).forGetter(KillProgressionTechnique::getMilestoneRealms),
                Codec.STRING.listOf().optionalFieldOf("technique_families", List.of()).forGetter(KillProgressionTechnique::getTechniqueFamilies),
                Codec.INT.optionalFieldOf("max_realm").forGetter(KillProgressionTechnique::getHardCodedMaxMajorRealm),
                Codec.INT.optionalFieldOf("max_minor_realm").forGetter(KillProgressionTechnique::getHardCodedMaxMinorRealm),
                Codec.INT.optionalFieldOf("min_realm").forGetter(KillProgressionTechnique::getHardCodedMinMajorRealm),
                Codec.unboundedMap(
                                Codec.STRING.xmap(Integer::parseInt, Object::toString),
                                MajorRealmDefinitionOverride.CODEC
                        )
                        .optionalFieldOf("realm_overrides", Map.of())
                        .forGetter(KillProgressionTechnique::getMajorRealmOverrides),
                Codec.unboundedMap(Identifier.CODEC, TechniqueSkillDefinition.CODEC).optionalFieldOf("skills", Map.of()).forGetter(KillProgressionTechnique::getSkills),
                ProgressActionHolder.PROGRESS_HOLDER_CODEC.optionalFieldOf("realm_change_handler", emptyHandler).forGetter(KillProgressionTechnique::getHolder),
                AscensionItemTooltipDefinition.CODEC.optionalFieldOf("item_tooltip").forGetter(Technique::itemTooltip),
                Codec.DOUBLE.optionalFieldOf("base_progress", 0.0D).forGetter(KillProgressionTechnique::getBaseProgress),
                Codec.DOUBLE.optionalFieldOf("max_health_multiplier", 1.0D).forGetter(KillProgressionTechnique::getMaxHealthMultiplier),
                Codec.DOUBLE.optionalFieldOf("player_multiplier", 1.0D).forGetter(KillProgressionTechnique::getPlayerMultiplier)
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
                itemTooltip,
                baseProgress,
                maxHealthMultiplier,
                playerMultiplier
        ) -> new KillProgressionTechnique(
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
                overrides,
                baseProgress,
                maxHealthMultiplier,
                playerMultiplier
        )));
    }
}
