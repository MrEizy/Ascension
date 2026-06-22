package net.zic.ascension.impl.core.progression;

import net.minecraft.resources.Identifier;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.core.RegistryObjectData;
import net.zic.ascension.api.core.progression.ProgressDirection;
import net.zic.ascension.api.core.progression.ProgressAction;
import net.zic.ascension.api.core.source.OriginSource;
import net.zic.ascension.api.datapack.progresison.ProgressActionType;
import net.zic.ascension.impl.datapack.progression.AscensionProgressActionTypes;

import java.util.List;
import java.util.UUID;

public record GiveSkillsAction(UUID uuid,List<Identifier> skills)  implements ProgressAction {

    public static GiveSkillsAction from(List<Identifier> skills){
        return new GiveSkillsAction(UUID.randomUUID(),skills);
    }

    /**
     *
     * @return the owner ID passed when adding or removing skills
     */
    public Identifier getOwnerId(){
       return Identifier.fromNamespaceAndPath(AscensionCraft.MOD_ID,"give_skills_"+getUniqueId());
    }

    @Override
    public UUID getUniqueId() {
        return uuid;
    }

    @Override
    public void run(UUID holderId, OriginSource source, Identifier contextIdentifier, RegistryObjectData contextData, ProgressDirection direction) {
        for(Identifier skill:skills){
            if(direction.equals(ProgressDirection.UP)) source.addSkill(skill,getOwnerId());
            else source.removeSkill(skill,getOwnerId());
        }
    }

    @Override
    public ProgressActionType getType() {
        return AscensionProgressActionTypes.GIVE_SKILLS_TYPE.get();
    }
}