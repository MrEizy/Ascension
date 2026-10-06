package net.zic.ascension.impl.core.progression;

import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.zic.ascension.api.ascension.core.progression.ProgressAction;
import net.zic.ascension.api.ascension.core.progression.ProgressActionDescription;
import net.zic.ascension.api.ascension.core.progression.ProgressDirection;
import net.zic.ascension.api.ascension.datapack.progresison.ProgressActionType;
import net.zic.ascension.api.rpg_engine.source.OriginSource;
import net.zic.zenithlib.stats.Stat;
import net.zic.zenithlib.stats.ZenithStatHelper;
import net.zic.zenithlib.value_containers.ValueContainer;
import net.zic.zenithlib.value_containers.typed.Modifier;
import net.zic.zenithlib.value_containers.typed.ModifierHolder;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record GiveStatsAction(UUID uuid, Map<Identifier,ModifierHolder<Double>> stats) implements ProgressAction {

    public static GiveStatsAction from(Map<Identifier,ModifierHolder<Double>> stats){
        return new GiveStatsAction(UUID.randomUUID(),stats);
    }
    @Override
    public UUID getUniqueId() {
        return uuid;
    }

    @Override
    public void run(UUID holderId, OriginSource source, Identifier contextIdentifier, Object contextData, ProgressDirection direction) {

        for(Map.Entry<Identifier,ModifierHolder<Double>> modifiers : stats.entrySet()){
            Stat stat = ZenithStatHelper.stat(modifiers.getKey());
            for(Modifier<Double> modifier : modifiers.getValue().flat()){
                if(direction == ProgressDirection.UP) source.addFlatStatModifier(stat,modifier);
                else source.removeStatModifier(stat,modifier.id());
            }
            for(Modifier<Double> modifier : modifiers.getValue().multiplier()){
                if(direction == ProgressDirection.UP) source.addMultiplierStatModifier(stat,modifier);
                else source.removeStatModifier(stat,modifier.id());
            }
        }
    }

    @Override
    public List<ProgressActionDescription> getDescriptions(RegistryAccess access) {
        return stats.entrySet().stream()
                .map(bonus ->
                        bonus.getValue().flat().stream().map( modifier ->  ProgressActionDescription.numeric(
                                ProgressionDescriptionUtil.statName(
                                        bonus.getKey()),
                                Component.literal(ProgressionDescriptionUtil.signedNumber( modifier.value())),
                                modifier.value()
                        )).toList()).flatMap(Collection::stream).toList();

    }

    @Override
    public ProgressActionType getType() {
        return null;
    }
}
