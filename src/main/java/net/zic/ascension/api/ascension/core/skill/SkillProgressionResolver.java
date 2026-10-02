package net.zic.ascension.api.ascension.core.skill;

import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.common.NeoForge;
import net.zic.ascension.api.ascension.core.CoreRegistries;
import net.zic.ascension.api.ascension.core.source.AscensionOriginSourceHelper;
import net.zic.ascension.api.ascension.event.skill.SkillEvent;
import net.zic.ascension.api.rpg_engine.source.OriginSource;

public final class SkillProgressionResolver {
    private SkillProgressionResolver() {
    }

    public static SkillProgressionSnapshot resolve(OriginSource source, Identifier skillId) {
        if (source == null || skillId == null || source.getRegistryAccess() == null) {
            return emptySnapshot();
        }

        Skill skill = CoreRegistries.safeAccess(
                CoreRegistries.SKILL_REGISTRY,
                skillId,
                source.getRegistryAccess()
        );
        SkillData data = AscensionOriginSourceHelper.getSkillData(source, skillId);
        if (!(skill instanceof ProgressingSkill progressingSkill)
                || !(data instanceof ProgressingSkillData progressingData)) {
            return emptySnapshot();
        }

        return resolve(source, skillId, progressingSkill, progressingData);
    }

    public static SkillProgressionSnapshot resolve(
            OriginSource source,
            Identifier skillId,
            ProgressingSkill skill,
            ProgressingSkillData data
    ) {
        SkillProgressionData progression = data.getSkillProgression();
        int maximum = Math.max(1, skill.getMaximumProgression());
        int trained = clamp(progression.getTrainedProgression(), 1, maximum);
        int accessibleCap = progression.getAccessibleCap(skill.getDefaultProgressionCap(), maximum);
        int permanent = Math.min(trained, accessibleCap);

        SkillEvent.ProgressionResolve event = new SkillEvent.ProgressionResolve(
                source,
                skillId,
                data,
                permanent,
                maximum
        );
        NeoForge.EVENT_BUS.post(event);

        return new SkillProgressionSnapshot(
                trained,
                progression.getExperience(),
                accessibleCap,
                permanent,
                event.resolve()
        );
    }

    private static SkillProgressionSnapshot emptySnapshot() {
        return new SkillProgressionSnapshot(0, 0.0D, 0, 0, 0);
    }

    private static int clamp(int value, int minimum, int maximum) {
        return Math.max(minimum, Math.min(maximum, value));
    }
}
