package net.zic.ascension.impl.core.progression;

import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.zic.ascension.api.ascension.core.bloodline.BloodlineData;
import net.zic.ascension.api.ascension.core.progression.ProgressAction;
import net.zic.ascension.api.ascension.core.progression.ProgressActionDescription;
import net.zic.ascension.api.ascension.core.progression.ProgressDirection;
import net.zic.ascension.api.ascension.datapack.progresison.ProgressActionType;
import net.zic.ascension.impl.datapack.progression.AscensionProgressActionTypes;
import net.zic.ascension.api.rpg_engine.source.OriginSource;
import net.zic.zenithlib.stats.Stat;
import net.zic.zenithlib.stats.ZenithStatHelper;
import net.zic.zenithlib.value_containers.typed.ModifierHolder;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record GiveStatsAction(UUID uuid, Map<Identifier,ModifierHolder<Double>> stats, boolean perPurity, ModifierMergeMode mergeMode) implements ProgressAction {

    public static GiveStatsAction from(Map<Identifier,ModifierHolder<Double>> stats, boolean perPurity, ModifierMergeMode mergeMode){
        if (perPurity && mergeMode.resolve(true) != ModifierMergeMode.REPLACE) {
            throw new IllegalArgumentException("Per-purity modifiers require replace mode");
        }
        return new GiveStatsAction(UUID.randomUUID(), Map.copyOf(stats), perPurity, mergeMode);
    }
    @Override
    public UUID getUniqueId() {
        return uuid;
    }

    @Override
    public void run(UUID holderId, OriginSource source, Identifier contextIdentifier, Object contextData, ProgressDirection direction) {
        if (perPurity && !(contextData instanceof BloodlineData)) return;
        int purity = perPurity ? Math.max(0, Math.min(100, ((BloodlineData) contextData).getPurity())) : 1;

        for (Map.Entry<Identifier, ModifierHolder<Double>> modifiers : stats.entrySet()) {
            Stat stat = ZenithStatHelper.stat(modifiers.getKey());
            ModifierActionHelper.apply(
                    modifiers.getValue(),
                    () -> source.getStatInstance(stat),
                    modifier -> source.addFlatStatModifier(stat, modifier),
                    modifier -> source.addMultiplierStatModifier(stat, modifier),
                    id -> source.removeStatModifier(stat, id),
                    direction, mergeMode, perPurity, purity
            );
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
        return AscensionProgressActionTypes.GIVE_STATS_ACTION.get();
    }
}
