package net.zic.ascension.core.progression;

import net.minecraft.resources.Identifier;
import net.zic.ascension.api.core.RegistryObjectData;
import net.zic.ascension.api.core.progression.ProgressDirection;
import net.zic.ascension.api.core.progression.ProgressAction;
import net.zic.ascension.api.core.source.OriginSource;
import net.zic.ascension.api.datapack.progresison.ProgressActionType;
import net.zic.ascension.datapack.progression.AscensionProgressActionTypes;

import java.util.List;
import java.util.UUID;

public record GiveSkillsAction(UUID uuid,List<Identifier> skills)  implements ProgressAction {

    public static GiveSkillsAction from(List<Identifier> skills){
        return new GiveSkillsAction(UUID.randomUUID(),skills);
    }

    @Override
    public UUID getUniqueId() {
        return uuid;
    }

    @Override
    public void run(UUID holderId, OriginSource source, Identifier contextIdentifier, RegistryObjectData contextData, ProgressDirection direction) {
        for(Identifier skill:skills){
            if(direction.equals(ProgressDirection.UP)) source.addSkill(skill,source.getRegistryAccess());
            else source.removeSkill(skill,source.getRegistryAccess());
        }
    }

    @Override
    public ProgressActionType getType() {
        return AscensionProgressActionTypes.GIVE_SKILLS_TYPE.get();
    }
}