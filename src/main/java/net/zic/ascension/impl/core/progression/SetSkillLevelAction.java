package net.zic.ascension.impl.core.progression;

import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.zic.ascension.api.ascension.core.CoreRegistries;
import net.zic.ascension.api.ascension.core.progression.ProgressAction;
import net.zic.ascension.api.ascension.core.progression.ProgressActionDescription;
import net.zic.ascension.api.ascension.core.progression.ProgressDirection;
import net.zic.ascension.api.ascension.core.skill.Skill;
import net.zic.ascension.api.ascension.core.skill.SkillMasteryRank;
import net.zic.ascension.api.ascension.core.skill.SkillProgressionService;
import net.zic.ascension.api.ascension.datapack.progresison.ProgressActionType;
import net.zic.ascension.api.rpg_engine.source.OriginSource;
import net.zic.ascension.impl.core.skill.castable.ActiveSkill;
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
    public void run(
            UUID holderId,
            OriginSource source,
            Identifier contextIdentifier,
            Object contextData,
            ProgressDirection direction
    ) {
        Skill definition = CoreRegistries.safeAccess(
                CoreRegistries.SKILL_REGISTRY,
                skill,
                source.getRegistryAccess()
        );
        if (definition == null) {
            return;
        }

        if (direction == ProgressDirection.DOWN) {
            if (setCap) {
                SkillProgressionService.removeCap(source, skill, contribution);
            }
            return;
        }

        if (!(definition instanceof ActiveSkill) && setFloor) {
            SkillProgressionService.setTrainedProgression(source, skill, level);
        }
        if (setCap) {
            SkillProgressionService.setCap(source, skill, contribution, level);
        }
    }

    @Override
    public List<ProgressActionDescription> getDescriptions(RegistryAccess access) {
        if (level <= 1) {
            return List.of();
        }

        Skill definition = CoreRegistries.safeAccess(CoreRegistries.SKILL_REGISTRY, skill, access);
        Component value = definition instanceof ActiveSkill
                ? Component.literal("Mastery cap: " + SkillMasteryRank.fromProgression(level).displayName())
                : Component.translatable("ascension.tooltip.progression.level", level);

        return List.of(new ProgressActionDescription(
                ProgressionDescriptionUtil.skillName(skill, access),
                value,
                ProgressActionDescription.Tone.SPECIAL
        ));
    }

    @Override
    public ProgressActionType getType() {
        return AscensionProgressActionTypes.SET_SKILL_LEVEL_TYPE.get();
    }
}
