package net.zic.ascension.impl.core.progression;

import net.minecraft.resources.Identifier;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.ascension.core.progression.ProgressDirection;
import net.zic.ascension.api.ascension.core.progression.ProgressAction;
import net.zic.ascension.api.ascension.core.source.AscensionOriginSourceHelper;
import net.zic.ascension.api.ascension.datapack.progresison.ProgressActionType;
import net.zic.ascension.api.rpg_engine.source.OriginSource;
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
    public void run(UUID holderId, OriginSource source, Identifier contextIdentifier, Object contextData, ProgressDirection direction) {
        for(Identifier skill:skills){
            if(direction.equals(ProgressDirection.UP)) AscensionOriginSourceHelper.addSkill(source,skill,getOwnerId());
            else AscensionOriginSourceHelper.removeSkill(source,skill,getOwnerId());
        }
    }

    @Override
    public ProgressActionType getType() {
        return AscensionProgressActionTypes.GIVE_SKILLS_TYPE.get();
    }
}