package net.zic.ascension.api.ascension.core.skill.toggleable;

import net.minecraft.world.entity.LivingEntity;
import net.zic.ascension.api.ascension.core.skill.SkillData;
import net.zic.ascension.api.ascension.core.skill.SkillInstance;
import net.zic.ascension.api.rpg_engine.source.OriginSource;

/**
 * typed helpers for toggling a {@link SkillInstance} whose skill is a {@link ToggleableSkill}.
 * none of these mark the skill dirty, callers are expected to do so.
 */
public final class ToggleableSkills {
    private ToggleableSkills() {
    }

    public static boolean isToggleable(SkillInstance<?> instance) {
        return instance != null && instance.getSkill() instanceof ToggleableSkill<?>;
    }

    public static boolean isEnabled(SkillInstance<?> instance) {
        return instance != null && isEnabledTyped(instance);
    }

    /**
     * enables the skill if it can be enabled
     * @param entity the entity enabling it, it has the enabled state applied to it
     * @return true if the skill was enabled
     */
    public static boolean enable(LivingEntity entity, OriginSource source, SkillInstance<?> instance) {
        return instance != null && enableTyped(entity, source, instance);
    }

    /**
     * disables the skill
     * @param entities the entities to remove the enabled state from
     * @return true if the skill was disabled
     */
    public static boolean disable(OriginSource source, SkillInstance<?> instance, Iterable<? extends LivingEntity> entities) {
        return instance != null && disableTyped(source, instance, entities);
    }

    /**
     * ticks an enabled skill
     * @return false if the skill is toggleable, enabled and asked to be disabled
     */
    public static boolean tick(LivingEntity entity, OriginSource source, SkillInstance<?> instance) {
        return instance == null || tickTyped(entity, source, instance);
    }

    //──Typed────────────────────────────────────────────────────────

    private static <D extends SkillData> boolean isEnabledTyped(SkillInstance<D> instance) {
        return instance.getSkill() instanceof ToggleableSkill<D> toggleable && toggleable.isEnabled(instance.getData());
    }

    private static <D extends SkillData> boolean enableTyped(LivingEntity entity, OriginSource source, SkillInstance<D> instance) {
        if (!(instance.getSkill() instanceof ToggleableSkill<D> toggleable) || toggleable.isEnabled(instance.getData())) return false;
        if (!toggleable.canEnable(entity, source, instance)) return false;
        toggleable.setEnabled(instance.getData(), true);
        toggleable.onEnabled(source, instance);
        toggleable.applyEnabledToEntity(entity, instance);
        return true;
    }

    private static <D extends SkillData> boolean disableTyped(OriginSource source, SkillInstance<D> instance, Iterable<? extends LivingEntity> entities) {
        if (!(instance.getSkill() instanceof ToggleableSkill<D> toggleable) || !toggleable.isEnabled(instance.getData())) return false;
        toggleable.onDisabled(source, instance);
        for (LivingEntity entity : entities) {
            toggleable.removeEnabledFromEntity(entity, instance);
        }
        toggleable.setEnabled(instance.getData(), false);
        return true;
    }

    private static <D extends SkillData> boolean tickTyped(LivingEntity entity, OriginSource source, SkillInstance<D> instance) {
        if (!(instance.getSkill() instanceof ToggleableSkill<D> toggleable) || !toggleable.isEnabled(instance.getData())) return true;
        return toggleable.tickEnabled(entity, source, instance);
    }
}
