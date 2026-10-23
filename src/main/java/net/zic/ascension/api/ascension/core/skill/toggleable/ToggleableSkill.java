package net.zic.ascension.api.ascension.core.skill.toggleable;

import net.minecraft.world.entity.LivingEntity;
import net.zic.ascension.api.ascension.core.skill.Skill;
import net.zic.ascension.api.ascension.core.skill.SkillData;
import net.zic.ascension.api.ascension.core.skill.SkillInstance;
import net.zic.ascension.api.rpg_engine.source.OriginSource;


/**
 * A skill whose active state is stored in its {@link SkillData} and controlled
 * by the server.
 */
public interface ToggleableSkill<D extends SkillData> extends Skill<D> {

    boolean isEnabled(D data);

    void setEnabled(D data, boolean enabled);

    default boolean canEnable(
            LivingEntity entity,
            OriginSource source,
            SkillInstance<D> instance
    ) {
        return true;
    }

    void onEnabled(OriginSource source, SkillInstance<D> instance);

    void onDisabled(OriginSource source, SkillInstance<D> instance);

    default void applyEnabledToEntity(LivingEntity entity, SkillInstance<D> instance) {
        applyToEntity(entity, instance);
    }

    default void removeEnabledFromEntity(LivingEntity entity, SkillInstance<D> instance) {
        removeFromEntity(entity, instance);
    }

    /**
     * Runs once per server tick for the source while the skill is enabled.
     * Returning {@code false} disables the skill.
     */
    default boolean tickEnabled(
            LivingEntity entity,
            OriginSource source,
            SkillInstance<D> instance
    ) {
        return true;
    }
}
