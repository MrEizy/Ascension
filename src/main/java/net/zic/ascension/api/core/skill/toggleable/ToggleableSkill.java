package net.zic.ascension.api.core.skill.toggleable;

import net.minecraft.world.entity.LivingEntity;
import net.zic.ascension.api.core.skill.Skill;
import net.zic.ascension.api.core.skill.SkillData;
import net.zic.ascension.api.core.source.OriginSource;

/**
 * A skill whose active state is stored in its {@link SkillData} and controlled
 * by the server.
 */
public interface ToggleableSkill extends Skill {

    boolean isEnabled(SkillData data);

    void setEnabled(SkillData data, boolean enabled);

    default boolean canEnable(
            LivingEntity entity,
            OriginSource source,
            SkillData data
    ) {
        return true;
    }

    void onEnabled(OriginSource source, SkillData data);

    void onDisabled(OriginSource source, SkillData data);

    default void applyEnabledToEntity(LivingEntity entity, SkillData data) {
        applyToEntity(entity, data);
    }

    default void removeEnabledFromEntity(LivingEntity entity, SkillData data) {
        removeFromEntity(entity, data);
    }

    /**
     * Runs once per server tick for the source while the skill is enabled.
     * Returning {@code false} disables the skill.
     */
    default boolean tickEnabled(
            LivingEntity entity,
            OriginSource source,
            SkillData data
    ) {
        return true;
    }
}
