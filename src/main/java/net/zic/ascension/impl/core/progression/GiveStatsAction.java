package net.zic.ascension.impl.core.progression;

import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.zic.ascension.api.ascension.core.progression.ProgressAction;
import net.zic.ascension.api.ascension.core.progression.ProgressActionDescription;
import net.zic.ascension.api.ascension.core.progression.ProgressDirection;
import net.zic.ascension.api.ascension.datapack.progresison.ProgressActionType;
import net.zic.ascension.api.rpg_engine.source.OriginSource;
import net.zic.ascension.impl.core.progression.modifier.ModifierConflictAction;
import net.zic.ascension.impl.datapack.progression.AscensionProgressActionTypes;
import net.zic.zenithlib.value_containers.typed.ModifierHolder;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.UnaryOperator;

public record GiveStatsAction(UUID uuid, Map<Identifier, ModifierHolder<Double>> stats, ModifierConflictAction conflictAction) implements ProgressAction {

    public static GiveStatsAction from(Map<Identifier, ModifierHolder<Double>> stats, ModifierConflictAction conflictAction) {
        return new GiveStatsAction(UUID.randomUUID(), Map.copyOf(stats), conflictAction);
    }

    @Override
    public UUID getUniqueId() {
        return uuid;
    }

    @Override
    public void run(UUID holderId, OriginSource source, Identifier contextIdentifier, Object contextData, ProgressDirection direction, String salt) {
        ProgressionModifiers.applyStats(source, stats, conflictAction, UnaryOperator.identity(), direction.value, salt);
    }

    @Override
    public List<ProgressActionDescription> getDescriptions(RegistryAccess access) {
        return describe(stats);
    }

    static List<ProgressActionDescription> describe(Map<Identifier, ModifierHolder<Double>> stats) {
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
        return AscensionProgressActionTypes.GIVE_STATS_ACTION.get();
    }
}
