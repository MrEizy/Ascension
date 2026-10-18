package net.zic.ascension.impl.core.skill.passive;

import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.Identifier;
import net.zic.ascension.api.ascension.core.skill.SkillInstance;
import net.zic.ascension.api.ascension.core.skill.passive.PassiveModifier;
import net.zic.ascension.api.ascension.core.skill.toggleable.ToggleableSkills;
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
            SkillInstance<?> instance = AscensionOriginSourceHelper.getSkill(source, skillId);
            if (instance == null
                    || !(instance.getSkill() instanceof PassiveSkill passive)
                    || !group.equals(passive.stateGroup().orElse(null))) {
                continue;
            }
            if (ToggleableSkills.disable(source, instance, source.getAttachedEntities())) {
                AscensionOriginSourceHelper.markSkillDirty(source, skillId);
            }
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
            SkillInstance<PassiveSkill.Data> instance = passiveInstance(source, skillId);
            if (instance == null) {
                continue;
            }
            PassiveSkill passive = (PassiveSkill) instance.getSkill();
            PassiveSkill.Data passiveData = instance.getData();
            if (!passive.isActive(passiveData)) {
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

    //the passive skill instance held for this skill, null if it is not a passive skill
    public static SkillInstance<PassiveSkill.Data> passiveInstance(OriginSource source, Identifier skillId) {
        SkillInstance<?> instance = AscensionOriginSourceHelper.getSkill(source, skillId);
        return instance == null ? null : instance.as(PassiveSkill.class);
    }

    public record Entry<T extends PassiveModifier>(
            Identifier skillId,
            PassiveSkill skill,
            PassiveSkill.Data data,
            T modifier
    ) {
    }
}
