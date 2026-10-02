package net.zic.ascension.impl.core.skill.passive;

import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.Identifier;
import net.zic.ascension.api.ascension.core.CoreRegistries;
import net.zic.ascension.api.ascension.core.skill.Skill;
import net.zic.ascension.api.ascension.core.skill.SkillData;
import net.zic.ascension.api.ascension.core.skill.passive.PassiveModifier;
import net.zic.ascension.api.ascension.core.skill.toggleable.ToggleableSkill;
import net.zic.ascension.api.ascension.core.source.AscensionOriginSourceHelper;
import net.zic.ascension.api.rpg_engine.source.OriginSource;

import java.util.ArrayList;
import java.util.List;

public final class PassiveSkillService {
    private PassiveSkillService() {
    }

    public static void disableStateGroup(OriginSource source, Identifier group, Identifier except) {
        if (source == null || group == null) {
            return;
        }
        for (Identifier skillId : List.copyOf(AscensionOriginSourceHelper.getSkills(source))) {
            if (skillId.equals(except)) {
                continue;
            }
            Skill skill = CoreRegistries.safeAccess(CoreRegistries.SKILL_REGISTRY, skillId, source.getRegistryAccess());
            SkillData data = AscensionOriginSourceHelper.getSkillData(source, skillId);
            if (!(skill instanceof PassiveSkill passive)
                    || !(skill instanceof ToggleableSkill toggleable)
                    || data == null
                    || !group.equals(passive.stateGroup().orElse(null))
                    || !toggleable.isEnabled(data)) {
                continue;
            }
            toggleable.onDisabled(source, data);
            for (var entity : source.getAttachedEntities()) {
                toggleable.removeEnabledFromEntity(entity, data);
            }
            toggleable.setEnabled(data, false);
            AscensionOriginSourceHelper.markSkillDirty(source, skillId);
        }
    }

    public static <T extends PassiveModifier> List<Entry<T>> modifiers(
            OriginSource source,
            RegistryAccess access,
            Class<T> type
    ) {
        if (source == null || access == null || type == null) {
            return List.of();
        }
        List<Entry<T>> values = new ArrayList<>();
        for (Identifier skillId : AscensionOriginSourceHelper.getSkills(source)) {
            Skill skill = CoreRegistries.safeAccess(CoreRegistries.SKILL_REGISTRY, skillId, access);
            SkillData data = AscensionOriginSourceHelper.getSkillData(source, skillId);
            if (!(skill instanceof PassiveSkill passive) || !(data instanceof PassiveSkill.Data passiveData) || !passive.isActive(passiveData)) {
                continue;
            }
            for (PassiveModifier modifier : passive.modifiers()) {
                if (type.isInstance(modifier)) {
                    values.add(new Entry<>(skillId, passive, passiveData, type.cast(modifier)));
                }
            }
        }
        return List.copyOf(values);
    }

    public record Entry<T extends PassiveModifier>(
            Identifier skillId,
            PassiveSkill skill,
            PassiveSkill.Data data,
            T modifier
    ) {
    }
}
