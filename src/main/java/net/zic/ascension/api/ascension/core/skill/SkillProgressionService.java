package net.zic.ascension.api.ascension.core.skill;

import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.common.NeoForge;
import net.zic.ascension.api.ascension.core.CoreRegistries;
import net.zic.ascension.api.ascension.core.source.AscensionOriginSourceHelper;
import net.zic.ascension.api.ascension.event.skill.SkillEvent;
import net.zic.ascension.api.rpg_engine.source.OriginSource;

public final class SkillProgressionService {
    private SkillProgressionService() {
    }

    public static boolean setTrainedProgression(OriginSource source, Identifier skillId, int progression) {
        return mutate(
                source,
                skillId,
                SkillEvent.ProgressionChanged.Reason.TRAINED_PROGRESSION,
                (skill, data) -> {
                    int resolved = clamp(progression, 1, skill.getMaximumProgression());
                    if (data.getTrainedProgression() == resolved && data.getExperience() == 0.0D) {
                        return false;
                    }
                    data.setTrainedProgression(resolved);
                    data.setExperience(0.0D);
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
                SkillEvent.ProgressionChanged.Reason.EXPERIENCE,
                (skill, data) -> {
                    int maximum = Math.max(1, skill.getMaximumProgression());
                    int cap = data.getAccessibleCap(skill.getDefaultProgressionCap(), maximum);
                    int progression = clamp(data.getTrainedProgression(), 1, maximum);

                    if (progression >= cap) {
                        if (data.getExperience() == 0.0D) {
                            return false;
                        }
                        data.setExperience(0.0D);
                        return true;
                    }

                    double experience = data.getExperience() + amount;
                    while (progression < cap) {
                        double required = skill.getExperienceRequiredForNextProgression(progression);
                        if (!Double.isFinite(required) || required <= 0.0D || experience < required) {
                            break;
                        }
                        experience -= required;
                        progression++;
                    }

                    data.setTrainedProgression(progression);
                    data.setExperience(progression >= cap ? 0.0D : experience);
                    return true;
                }
        );
    }

    public static boolean setCap(
            OriginSource source,
            Identifier skillId,
            Identifier contributionId,
            int progression
    ) {
        return mutate(
                source,
                skillId,
                SkillEvent.ProgressionChanged.Reason.CAP,
                (skill, data) -> data.setCap(
                        contributionId,
                        clamp(progression, 1, skill.getMaximumProgression())
                )
        );
    }

    public static boolean removeCap(
            OriginSource source,
            Identifier skillId,
            Identifier contributionId
    ) {
        return mutate(
                source,
                skillId,
                SkillEvent.ProgressionChanged.Reason.CAP_REMOVED,
                (skill, data) -> data.removeCap(contributionId)
        );
    }

    private static boolean mutate(
            OriginSource source,
            Identifier skillId,
            SkillEvent.ProgressionChanged.Reason reason,
            ProgressionMutation mutation
    ) {
        ResolvedSkill resolved = resolve(source, skillId);
        if (resolved == null) {
            return false;
        }

        SkillProgressionSnapshot previous = SkillProgressionResolver.resolve(
                source,
                skillId,
                resolved.skill,
                resolved.data
        );
        if (!mutation.apply(resolved.skill, resolved.data.getSkillProgression())) {
            return false;
        }

        SkillProgressionSnapshot current = SkillProgressionResolver.resolve(
                source,
                skillId,
                resolved.skill,
                resolved.data
        );
        if (previous.effectiveProgression() != current.effectiveProgression()) {
            resolved.skill.onProgressionChanged(
                    source,
                    resolved.data,
                    previous.effectiveProgression(),
                    current.effectiveProgression()
            );
        }

        AscensionOriginSourceHelper.markSkillDirty(source, skillId);
        NeoForge.EVENT_BUS.post(new SkillEvent.ProgressionChanged(
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
        if (!(skill instanceof ProgressingSkill progressingSkill)
                || !(data instanceof ProgressingSkillData progressingData)) {
            return null;
        }
        return new ResolvedSkill(progressingSkill, progressingData);
    }

    private static int clamp(int value, int minimum, int maximum) {
        return Math.max(minimum, Math.min(maximum, value));
    }

    @FunctionalInterface
    private interface ProgressionMutation {
        boolean apply(ProgressingSkill skill, SkillProgressionData progression);
    }

    private record ResolvedSkill(ProgressingSkill skill, ProgressingSkillData data) {
    }
}
