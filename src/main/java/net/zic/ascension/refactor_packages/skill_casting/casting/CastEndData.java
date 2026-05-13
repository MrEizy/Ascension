package net.zic.ascension.refactor_packages.skill_casting.casting;

import net.minecraft.resources.ResourceLocation;
import net.zic.ascension.refactor_packages.registries.AscensionRegistries;
import net.zic.ascension.refactor_packages.skills.ISkill;
import net.zic.ascension.refactor_packages.skills.castable.ICastData;

//will be used by skills to determine cooldown
public record CastEndData(ResourceLocation skillId, CastEndReason reason, ICastData castData,int ticksElapsed){
    public ISkill getSkill(){
        return AscensionRegistries.Skills.SKILL_REGISTRY.get(skillId);
    }
}
