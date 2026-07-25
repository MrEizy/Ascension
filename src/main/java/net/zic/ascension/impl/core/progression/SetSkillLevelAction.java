package net.zic.ascension.impl.core.progression;

import net.minecraft.resources.Identifier;
import net.zic.ascension.api.core.RegistryObjectData;
import net.zic.ascension.api.core.progression.ProgressAction;
import net.zic.ascension.api.core.progression.ProgressDirection;
import net.zic.ascension.api.core.skill.levelled.SkillProgressionService;
import net.zic.ascension.api.core.source.OriginSource;
import net.zic.ascension.api.datapack.progresison.ProgressActionType;
import net.zic.ascension.impl.datapack.progression.AscensionProgressActionTypes;

import java.nio.charset.StandardCharsets;
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
    public void run(
            UUID holderId,
            OriginSource source,
            Identifier contextIdentifier,
            RegistryObjectData contextData,
            ProgressDirection direction
    ) {
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
    public ProgressActionType getType() {
        return AscensionProgressActionTypes.SET_SKILL_LEVEL_TYPE.get();
    }
}
