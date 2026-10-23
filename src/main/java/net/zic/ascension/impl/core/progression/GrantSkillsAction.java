package net.zic.ascension.impl.core.progression;

import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.zic.ascension.api.ascension.core.CoreRegistries;
import net.zic.ascension.api.ascension.core.progression.ProgressAction;
import net.zic.ascension.api.ascension.core.progression.ProgressActionDescription;
import net.zic.ascension.api.ascension.core.progression.ProgressDirection;
import net.zic.ascension.api.ascension.core.skill.Skill;
import net.zic.ascension.api.ascension.core.source.AscensionOriginSourceHelper;
import net.zic.ascension.api.ascension.datapack.progresison.ProgressActionType;
import net.zic.ascension.api.rpg_engine.source.OriginSource;
import net.zic.ascension.impl.datapack.progression.AscensionProgressActionTypes;

import java.util.List;
import java.util.UUID;

public record GrantSkillsAction(UUID uuid, List<Identifier> skills) implements ProgressAction {
    public static GrantSkillsAction from(List<Identifier> skills) {
        return new GrantSkillsAction(UUID.randomUUID(), skills == null ? List.of() : List.copyOf(skills));
    }

    @Override
    public UUID getUniqueId() {
        return uuid;
    }

    @Override
    public void run(UUID holderId, OriginSource source, Identifier contextIdentifier, Object contextData, ProgressDirection direction,String salt) {
        for (Identifier skill : skills) {
            if (direction == ProgressDirection.UP) {
                AscensionOriginSourceHelper.addSkill(source, skill, contextIdentifier);
            } else {
                AscensionOriginSourceHelper.removeSkill(source, skill, contextIdentifier);
            }
        }
    }

    @Override
    public List<ProgressActionDescription> getDescriptions(RegistryAccess access) {
        return skills.stream()
                .map(skillId -> {
                    Skill<?> skill = CoreRegistries.safeAccess(CoreRegistries.SKILL_REGISTRY, skillId, access);
                    return new ProgressActionDescription(
                            skill == null ? Component.literal(skillId.getPath()) : skill.getName(),
                            Component.translatable("ascension.tooltip.progression.unlock"),
                            ProgressActionDescription.Tone.SPECIAL
                    );
                })
                .toList();
    }

    @Override
    public ProgressActionType getType() {
        return AscensionProgressActionTypes.GRANT_SKILLS_TYPE.get();
    }
}
