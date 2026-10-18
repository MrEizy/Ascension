package net.zic.ascension.api.ascension.core.skill;

import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.common.NeoForge;
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
        ResolvedSkill<?> resolved = resolve(source, skillId);
        return resolved != null && mutateTyped(source, resolved, reason, mutation);
    }

    private static <D extends ProgressingSkillData> boolean mutateTyped(
            OriginSource source,
            ResolvedSkill<D> resolved,
            SkillEvent.ProgressionChanged.Reason reason,
            ProgressionMutation mutation
    ) {
        SkillInstance<D> instance = resolved.instance();
        ProgressingSkill<D> skill = resolved.skill();
        D data = instance.getData();

        SkillProgressionSnapshot previous = SkillProgressionResolver.resolve(source, instance, skill, data);
        if (!mutation.apply(skill, data.getSkillProgression())) {
            return false;
        }

        SkillProgressionSnapshot current = SkillProgressionResolver.resolve(source, instance, skill, data);
        if (previous.effectiveProgression() != current.effectiveProgression()) {
            skill.onProgressionChanged(
                    source,
                    instance,
                    previous.effectiveProgression(),
                    current.effectiveProgression()
            );
        }

        AscensionOriginSourceHelper.markSkillDirty(source, instance.getSkillId());
        NeoForge.EVENT_BUS.post(new SkillEvent.ProgressionChanged(
                source,
                instance,
                previous,
                current,
                reason
        ));
        return true;
    }

    //the data is always created by the skill, so a progressing skill always holds progressing data
    @SuppressWarnings({"unchecked", "rawtypes"})
    private static ResolvedSkill<?> resolve(OriginSource source, Identifier skillId) {
        if (source == null || skillId == null || source.getRegistryAccess() == null) {
            return null;
        }

        SkillInstance<?> instance = AscensionOriginSourceHelper.getSkill(source, skillId);
        if (instance == null || !(instance.getSkill() instanceof ProgressingSkill progressingSkill)
                || !(instance.getData() instanceof ProgressingSkillData)) {
            return null;
        }
        return new ResolvedSkill((SkillInstance) instance, progressingSkill);
    }

    private static int clamp(int value, int minimum, int maximum) {
        return Math.max(minimum, Math.min(maximum, value));
    }

    @FunctionalInterface
    private interface ProgressionMutation {
        boolean apply(ProgressingSkill<?> skill, SkillProgressionData progression);
    }

    private record ResolvedSkill<D extends ProgressingSkillData>(SkillInstance<D> instance, ProgressingSkill<D> skill) {
    }
}
