package net.zic.ascension.impl.core.skill.toggleable;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.zic.ascension.api.ascension.core.skill.SkillData;
import net.zic.ascension.api.ascension.core.skill.toggleable.ToggleableSkill;
import net.zic.ascension.api.ascension.datapack.skill.SkillType;
import net.zic.ascension.api.rpg_engine.source.OriginSource;

/**
 * Blood Frenzy — Sanguine Abyss Technique toggle.
 * While enabled: bonus outgoing damage + lifesteal, bonus incoming damage taken.
 *
 * NOTE: ToggleableSkill (like Skill) is a pure Java interface with no codec shown
 * in the uploaded sources, unlike CastableSkill's data-driven "ascension:active"
 * JSON path. This is written as a hand-coded skill registered under its own
 * SkillType, following the same shape as Skill/CastableSkill implementations
 * elsewhere in the codebase. If there is already a generic data-driven toggle
 * skill type, prefer that instead and drop this class.
 *
 * TODO: verify SkillType registration pattern (see wherever burning_bone_tempering
 * / other Skill impls are registered) and wire real stat-modifier application in
 * applyToEntity/removeFromEntity + tickEnabled.
 */
public class BloodFrenzySkill implements ToggleableSkill {

    public static final double DAMAGE_DEALT_BONUS = 0.35D;   // +35% outgoing damage
    public static final double DAMAGE_TAKEN_BONUS = 0.25D;   // +25% incoming damage
    public static final double LIFESTEAL_FRACTION = 0.15D;   // 15% of damage dealt returned as HP

    @Override
    public SkillType getType() {
        // TODO: return the registered SkillType for this skill.
        throw new UnsupportedOperationException("wire SkillType registration");
    }

    @Override
    public Component getName() {
        return Component.translatable("ascension.skill.blood_frenzy.name");
    }

    @Override
    public Component getDescription() {
        return Component.translatable("ascension.skill.blood_frenzy.desc");
    }

    @Override
    public boolean isEnabled(SkillData data) {
        return data instanceof BloodFrenzyData frenzyData && frenzyData.enabled();
    }

    @Override
    public void setEnabled(SkillData data, boolean enabled) {
        if (data instanceof BloodFrenzyData frenzyData) {
            frenzyData.setEnabled(enabled);
        }
    }

    @Override
    public boolean canEnable(LivingEntity entity, OriginSource source, SkillData data) {
        // Prevent turning it on if it would kill the caster outright, etc.
        return entity.getHealth() > 2.0F;
    }

    @Override
    public void onEnabled(OriginSource source, SkillData data) {
        // TODO: apply attribute modifiers / hook damage-dealt & damage-taken events
        // for DAMAGE_DEALT_BONUS / DAMAGE_TAKEN_BONUS / LIFESTEAL_FRACTION.
    }

    @Override
    public void onDisabled(OriginSource source, SkillData data) {
        // TODO: remove attribute modifiers applied in onEnabled.
    }

    @Override
    public boolean tickEnabled(LivingEntity entity, OriginSource source, SkillData data) {
        // Auto-disable if the caster drops too low, so Blood Frenzy can't suicide them.
        return entity.getHealth() > 1.0F;
    }

    @Override
    public void onAdded(OriginSource source, SkillData data) {
    }

    @Override
    public void onRemoved(OriginSource source, SkillData data) {
    }

    @Override
    public void applyToEntity(LivingEntity entity, SkillData data) {
    }

    @Override
    public void removeFromEntity(LivingEntity entity, SkillData data) {
    }

    @Override
    public SkillData newData(RegistryAccess access) {
        return new BloodFrenzyData(false);
    }

    @Override
    public SkillData loadData(ValueInput input, RegistryAccess access) {
        return new BloodFrenzyData(input.getBooleanOr("enabled", false));
    }

    @Override
    public SkillData loadData(ByteBuf buf) {
        return new BloodFrenzyData(buf.readBoolean());
    }

    /**
     * Minimal SkillData holding just the toggle state.
     * TODO: move to its own file if other toggle skills need the same shape.
     */
    public static final class BloodFrenzyData implements SkillData {
        private boolean enabled;

        public BloodFrenzyData(boolean enabled) {
            this.enabled = enabled;
        }

        public boolean enabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        @Override
        public SkillType getType() {
            throw new UnsupportedOperationException("wire SkillType registration");
        }

        @Override
        public void write(ValueOutput output, RegistryAccess access) {
            output.putBoolean("enabled", enabled);
        }

        @Override
        public void encode(ByteBuf buf, RegistryAccess access) {
            buf.writeBoolean(enabled);
        }
    }
}
