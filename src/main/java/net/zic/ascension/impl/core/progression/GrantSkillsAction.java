package net.zic.ascension.impl.core.progression;

import net.minecraft.resources.Identifier;
import net.zic.ascension.api.ascension.core.progression.ProgressAction;
import net.zic.ascension.api.ascension.core.progression.ProgressDirection;
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
    public void run(UUID holderId, OriginSource source, Identifier contextIdentifier, Object contextData, ProgressDirection direction) {
        for (Identifier skill : skills) {
            if (direction == ProgressDirection.UP) {
                AscensionOriginSourceHelper.addSkill(source, skill, contextIdentifier);
            } else {
                AscensionOriginSourceHelper.removeSkill(source, skill, contextIdentifier);
            }
        }
    }

    @Override
    public ProgressActionType getType() {
        return AscensionProgressActionTypes.GRANT_SKILLS_TYPE.get();
    }
}
