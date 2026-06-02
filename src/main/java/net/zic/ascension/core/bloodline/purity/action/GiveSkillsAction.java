package net.zic.ascension.core.bloodline.purity.action;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import net.zic.ascension.api.core.ProgressDirection;
import net.zic.ascension.api.core.bloodline.Bloodline;
import net.zic.ascension.api.core.bloodline.BloodlineData;
import net.zic.ascension.api.core.bloodline.purity.PurityChangeAction;
import net.zic.ascension.api.core.source.OriginSource;
import net.zic.ascension.api.datapack.bloodline.purity.PurityChangeActionType;
import net.zic.ascension.datapack.bloodline.purity.action.AscensionPurityChangeActionTypes;
import net.zic.zenithlib.common.ZenithRegistries;
import net.zic.zenithlib.value_containers.ValueContainer;

import java.util.List;
import java.util.UUID;

public record GiveSkillsAction(List<Identifier> skills)  implements PurityChangeAction {

    @Override
    public void run(UUID handlerId, OriginSource source, Bloodline bloodline, BloodlineData bloodlineData, int purity, ProgressDirection direction) {
        for(Identifier skill:skills){
            if(direction.equals(ProgressDirection.UP)) source.addSkill(skill,source.getRegistryAccess());
            else source.removeSkill(skill,source.getRegistryAccess());
        }
    }

    @Override
    public PurityChangeActionType getType() {
        return AscensionPurityChangeActionTypes.GIVE_SKILLS_TYPE.get();
    }
}