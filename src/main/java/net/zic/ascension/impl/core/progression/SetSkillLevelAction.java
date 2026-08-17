package net.zic.ascension.impl.core.progression;

import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.zic.ascension.api.ascension.core.progression.ProgressAction;
import net.zic.ascension.api.ascension.core.progression.ProgressActionDescription;
import net.zic.ascension.api.ascension.core.progression.ProgressDirection;
import net.zic.ascension.api.ascension.core.skill.SkillProgressionService;
import net.zic.ascension.api.ascension.datapack.progresison.ProgressActionType;
import net.zic.ascension.api.rpg_engine.source.OriginSource;
import net.zic.ascension.impl.datapack.progression.AscensionProgressActionTypes;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

public record SetSkillLevelAction(
        Identifier skill,
        Identifier contribution,
        int level,
        boolean setFloor,
        boolean setCap
) implements ProgressAction {

    @Override
    public UUID getUniqueId() {
        String key = skill + "|" + contribution + "|" + level + "|" + setFloor + "|" + setCap;
        return UUID.nameUUIDFromBytes(key.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public void run(UUID holderId, OriginSource source, Identifier contextIdentifier, Object contextData, ProgressDirection direction) {
        if (direction == ProgressDirection.UP) {
            SkillProgressionService.setLevelContribution(
                    source,
                    skill,
                    contribution,
                    level,
                    setFloor,
                    setCap
            );
            return;
        }

        SkillProgressionService.removeLevelContribution(
                source,
                skill,
                contribution,
                setFloor,
                setCap
        );
    }

    @Override
    public List<ProgressActionDescription> getDescriptions(RegistryAccess access) {
        if (level <= 1) {
            return List.of();
        }

        return List.of(new ProgressActionDescription(
                ProgressionDescriptionUtil.skillName(skill, access),
                Component.translatable(
                        "ascension.tooltip.progression.level",
                        level
                ),
                ProgressActionDescription.Tone.SPECIAL
        ));
    }

    @Override
    public ProgressActionType getType() {
        return AscensionProgressActionTypes.SET_SKILL_LEVEL_TYPE.get();
    }
}
