package net.zic.ascension.impl.core.progression;

import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.Identifier;
import net.zic.ascension.api.ascension.core.bloodline.Bloodline;
import net.zic.ascension.api.ascension.core.bloodline.BloodlineData;
import net.zic.ascension.api.ascension.core.bloodline.purity.PurityChangeAction;
import net.zic.ascension.api.ascension.core.progression.ProgressActionDescription;
import net.zic.ascension.api.ascension.core.progression.ProgressDirection;
import net.zic.ascension.api.ascension.datapack.progresison.ProgressActionType;
import net.zic.ascension.api.rpg_engine.source.OriginSource;
import net.zic.ascension.impl.core.progression.modifier.ModifierConflictAction;
import net.zic.ascension.impl.datapack.progression.AscensionProgressActionTypes;
import net.zic.zenithlib.value_containers.typed.ModifierHolder;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Gives stats scaled by the bloodline purity (modifier value * purity).
 */
public record GiveStatsUsingPurityAction(UUID uuid, Map<Identifier, ModifierHolder<Double>> stats, ModifierConflictAction conflictAction) implements PurityChangeAction {

    public static GiveStatsUsingPurityAction from(Map<Identifier, ModifierHolder<Double>> stats, ModifierConflictAction conflictAction) {
        return new GiveStatsUsingPurityAction(UUID.randomUUID(), Map.copyOf(stats), conflictAction);
    }

    @Override
    public UUID getUniqueId() {
        return uuid;
    }

    @Override
    public void run(UUID holderId, OriginSource source, Bloodline<?> bloodline, BloodlineData data, int purity, ProgressDirection direction, String salt) {
        ProgressionModifiers.applyWithPurity(conflictAction, data, purity, direction,
                (transform, effectiveDirection) -> ProgressionModifiers.applyStats(source, stats, conflictAction, transform, effectiveDirection, salt));
    }

    @Override
    public List<ProgressActionDescription> getDescriptions(RegistryAccess access) {
        return GiveStatsAction.describe(stats);
    }

    @Override
    public ProgressActionType getType() {
        return AscensionProgressActionTypes.GIVE_STATS_USING_PURITY_TYPE.get();
    }
}
