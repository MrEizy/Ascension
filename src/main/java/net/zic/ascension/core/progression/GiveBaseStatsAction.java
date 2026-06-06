package net.zic.ascension.core.progression;

import net.minecraft.resources.Identifier;
import net.zic.ascension.api.core.RegistryObjectData;
import net.zic.ascension.api.core.progression.ProgressDirection;
import net.zic.ascension.api.core.progression.ProgressAction;
import net.zic.ascension.api.core.source.OriginSource;
import net.zic.ascension.api.datapack.progresison.ProgressActionType;
import net.zic.ascension.datapack.progression.AscensionProgressActionTypes;
import net.zic.zenithlib.common.ZenithRegistries;
import net.zic.zenithlib.value_containers.ValueContainer;

import java.util.List;
import java.util.UUID;

public record GiveBaseStatsAction(UUID uuid,List<ValueContainer.BaseModifier> baseStats) implements ProgressAction {
    public static GiveBaseStatsAction from(List<ValueContainer.BaseModifier> baseStats){
        return new GiveBaseStatsAction(UUID.randomUUID(),baseStats);
    }


    @Override
    public UUID getUniqueId() {
        return uuid;
    }

    @Override
    public void run(UUID holderId, OriginSource source, Identifier contextIdentifier, RegistryObjectData contextData, ProgressDirection direction) {
        for(ValueContainer.BaseModifier modifier : baseStats){
            System.out.println("trying to give stats");
            if(direction == ProgressDirection.UP) source.addStat(ZenithRegistries.STAT_REGISTRY.getValue(modifier.container()), modifier.val());
            else source.removeStat(ZenithRegistries.STAT_REGISTRY.getValue(modifier.container()),modifier.val());
        }
    }

    @Override
    public ProgressActionType getType() {
        return AscensionProgressActionTypes.GIVE_BASE_STATS_TYPE.get();
    }
}
