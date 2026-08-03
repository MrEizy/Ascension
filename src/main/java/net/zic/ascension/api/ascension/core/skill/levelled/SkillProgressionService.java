package net.zic.ascension.api.ascension.core.skill.levelled;

import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.common.NeoForge;
import net.zic.ascension.api.ascension.core.CoreRegistries;
import net.zic.ascension.api.ascension.core.skill.Skill;
import net.zic.ascension.api.ascension.core.skill.SkillData;
import net.zic.ascension.api.ascension.core.source.AscensionOriginSourceHelper;
import net.zic.ascension.api.rpg_engine.source.OriginSource;
import net.zic.ascension.api.ascension.event.skill.SkillLevelChangedEvent;

public final class SkillProgressionService {
    private SkillProgressionService() {
    }

    public static boolean setTrainedLevel(OriginSource source, Identifier skillId, int level) {
        return mutate(
                source,
                skillId,
                SkillLevelChangedEvent.Reason.TRAINED_LEVEL,
                (skill, progression) -> {
                    int clampedLevel = clamp(level, 0, skill.getMaximumLevel());
                    if (progression.getTrainedLevel() == clampedLevel) {
                        return false;
                    }
                    progression.setTrainedLevel(clampedLevel);
                    progression.setExperience(0.0D);
                    return true;
                }
        );
    }

    public static boolean addExperience(OriginSource source, Identifier skillId, double amount) {
        if (!Double.isFinite(amount) || amount <= 0.0D) {
            return false;
        }

        return mutate(
                source,
                skillId,
                SkillLevelChangedEvent.Reason.EXPERIENCE,
                (skill, progression) -> {
                    int level = clamp(progression.getTrainedLevel(), 0, skill.getMaximumLevel());
                    if (level >= skill.getMaximumLevel()) {
                        if (progression.getExperience() == 0.0D) {
                            return false;
                        }
                        progression.setExperience(0.0D);
                        return true;
                    }

                    double experience = progression.getExperience() + amount;
                    while (level < skill.getMaximumLevel()) {
                        double required = skill.getExperienceRequiredForNextLevel(level);
                        if (!Double.isFinite(required) || required <= 0.0D || experience < required) {
                            break;
                        }
                        experience -= required;
                        level++;
                    }

                    progression.setTrainedLevel(level);
                    progression.setExperience(level >= skill.getMaximumLevel() ? 0.0D : experience);
                    return true;
                }
        );
    }

    public static boolean setLevelFloor(
            OriginSource source,
            Identifier skillId,
            Identifier contributionId,
            int level
    ) {
        return mutate(
                source,
                skillId,
                SkillLevelChangedEvent.Reason.LEVEL_FLOOR,
                (skill, progression) -> progression.setLevelFloor(
                        contributionId,
                        clamp(level, 0, skill.getMaximumLevel())
                )
        );
    }

    public static boolean removeLevelFloor(
            OriginSource source,
            Identifier skillId,
            Identifier contributionId
    ) {
        return mutate(
                source,
                skillId,
                SkillLevelChangedEvent.Reason.CONTRIBUTION_REMOVED,
                (skill, progression) -> progression.removeLevelFloor(contributionId)
        );
    }

    public static boolean setLevelCap(
            OriginSource source,
            Identifier skillId,
            Identifier contributionId,
            int level
    ) {
        return mutate(
                source,
                skillId,
                SkillLevelChangedEvent.Reason.LEVEL_CAP,
                (skill, progression) -> progression.setLevelCap(
                        contributionId,
                        clamp(level, 0, skill.getMaximumLevel())
                )
        );
    }

    public static boolean removeLevelCap(
            OriginSource source,
            Identifier skillId,
            Identifier contributionId
    ) {
        return mutate(
                source,
                skillId,
                SkillLevelChangedEvent.Reason.CONTRIBUTION_REMOVED,
                (skill, progression) -> progression.removeLevelCap(contributionId)
        );
    }

    public static boolean setLevelContribution(
            OriginSource source,
            Identifier skillId,
            Identifier contributionId,
            int level,
            boolean setFloor,
            boolean setCap
    ) {
        if (!setFloor && !setCap) {
            return false;
        }
        return mutate(
                source,
                skillId,
                SkillLevelChangedEvent.Reason.LEVEL_CONTRIBUTION,
                (skill, progression) -> {
                    int clampedLevel = clamp(level, 0, skill.getMaximumLevel());
                    boolean changed = false;
                    if (setFloor) {
                        changed |= progression.setLevelFloor(contributionId, clampedLevel);
                    }
                    if (setCap) {
                        changed |= progression.setLevelCap(contributionId, clampedLevel);
                    }
                    return changed;
                }
        );
    }

    public static boolean removeLevelContribution(
            OriginSource source,
            Identifier skillId,
            Identifier contributionId,
            boolean removeFloor,
            boolean removeCap
    ) {
        if (!removeFloor && !removeCap) {
            return false;
        }
        return mutate(
                source,
                skillId,
                SkillLevelChangedEvent.Reason.CONTRIBUTION_REMOVED,
                (skill, progression) -> {
                    boolean changed = false;
                    if (removeFloor) {
                        changed |= progression.removeLevelFloor(contributionId);
                    }
                    if (removeCap) {
                        changed |= progression.removeLevelCap(contributionId);
                    }
                    return changed;
                }
        );
    }

    public static boolean removeContribution(
            OriginSource source,
            Identifier skillId,
            Identifier contributionId
    ) {
        return mutate(
                source,
                skillId,
                SkillLevelChangedEvent.Reason.CONTRIBUTION_REMOVED,
                (skill, progression) -> progression.removeContribution(contributionId)
        );
    }

    private static boolean mutate(
            OriginSource source,
            Identifier skillId,
            SkillLevelChangedEvent.Reason reason,
            ProgressionMutation mutation
    ) {
        ResolvedSkill resolved = resolve(source, skillId);
        if (resolved == null) {
            return false;
        }

        SkillLevelSnapshot previous = SkillLevelResolver.resolve(
                source,
                skillId,
                resolved.skill,
                resolved.data
        );
        if (!mutation.apply(resolved.skill, resolved.data.getSkillProgression())) {
            return false;
        }

        SkillLevelSnapshot current = SkillLevelResolver.resolve(
                source,
                skillId,
                resolved.skill,
                resolved.data
        );
        if (previous.effectiveLevel() != current.effectiveLevel()) {
            resolved.skill.onLevelChanged(
                    source,
                    resolved.data,
                    previous.effectiveLevel(),
                    current.effectiveLevel()
            );
        }

        AscensionOriginSourceHelper.markSkillDirty(source, skillId);
        NeoForge.EVENT_BUS.post(new SkillLevelChangedEvent(
                source,
                skillId,
                resolved.data,
                previous,
                current,
                reason
        ));
        return true;
    }

    private static ResolvedSkill resolve(OriginSource source, Identifier skillId) {
        if (source == null || skillId == null || source.getRegistryAccess() == null) {
            return null;
        }

        Skill skill = CoreRegistries.safeAccess(
                CoreRegistries.SKILL_REGISTRY,
                skillId,
                source.getRegistryAccess()
        );
        SkillData data = AscensionOriginSourceHelper.getSkillData(source, skillId);
        if (!(skill instanceof LevelledSkill levelledSkill)
                || !(data instanceof LevelledSkillData levelledData)) {
            return null;
        }
        return new ResolvedSkill(levelledSkill, levelledData);
    }

    private static int clamp(int value, int minimum, int maximum) {
        return Math.max(minimum, Math.min(maximum, value));
    }

    @FunctionalInterface
    private interface ProgressionMutation {
        boolean apply(LevelledSkill skill, SkillProgressionData progression);
    }

    private record ResolvedSkill(LevelledSkill skill, LevelledSkillData data) {
    }
}
