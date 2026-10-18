package net.zic.ascension.api.ascension.core.skill.castable;

import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.zic.ascension.api.ascension.core.CoreRegistries;
import net.zic.ascension.api.ascension.core.skill.Skill;
import net.zic.ascension.api.ascension.core.skill.SkillData;
import net.zic.ascension.api.ascension.core.skill.SkillInstance;
import net.zic.ascension.api.ascension.core.source.AscensionOriginSourceHelper;
import net.zic.ascension.api.rpg_engine.source.OriginSource;
import org.jspecify.annotations.Nullable;

//shared lookups for the cast wrappers
final class CastableSkillLookup {
    private CastableSkillLookup() {
    }

    static @Nullable CastableSkill<?, ?, ?> castable(Identifier skillId, RegistryAccess access) {
        return CoreRegistries.safeAccess(CoreRegistries.SKILL_REGISTRY, skillId, access) instanceof CastableSkill<?, ?, ?> castable
                ? castable
                : null;
    }

    /**
     * the skill data the owner's origin holds for this skill. looked up every time, since the slot and cast
     * can outlive the skill instance (the hot bar loads before the origin, and skills can be removed and added again)
     */
    @SuppressWarnings("unchecked")
    static <D extends SkillData> @Nullable D skillData(LivingEntity owner, Identifier skillId, Skill<D> skill) {
        OriginSource source = AscensionOriginSourceHelper.getEntitySource(owner);
        if (source == null) return null;
        SkillInstance<?> instance = AscensionOriginSourceHelper.getSkill(source, skillId);
        //the data is always created by the skill, so a matching skill means matching data
        return instance != null && instance.getSkill() == skill ? (D) instance.getData() : null;
    }

    static void markSkillDataDirty(LivingEntity owner, Identifier skillId) {
        OriginSource source = AscensionOriginSourceHelper.getEntitySource(owner);
        if (source != null && AscensionOriginSourceHelper.hasSkill(source, skillId)) {
            AscensionOriginSourceHelper.markSkillDirty(source, skillId);
        }
    }
}
