package net.zic.ascension.impl.core.progression;

import net.minecraft.core.RegistryAccess;
import net.zic.ascension.api.ascension.core.bloodline.Bloodline;
import net.zic.ascension.api.ascension.core.bloodline.BloodlineData;
import net.zic.ascension.api.ascension.core.bloodline.purity.PurityChangeAction;
import net.zic.ascension.api.ascension.core.path.bonus.PathBonus;
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
 * Gives path bonuses scaled by the bloodline purity (modifier value * purity).
 */
public record GivePathBonusesUsingPurityAction(UUID uuid, Map<PathBonus, ModifierHolder<Double>> bonuses, ModifierConflictAction conflictAction) implements PurityChangeAction {

    public static GivePathBonusesUsingPurityAction from(Map<PathBonus, ModifierHolder<Double>> bonuses, ModifierConflictAction conflictAction) {
        return new GivePathBonusesUsingPurityAction(UUID.randomUUID(), Map.copyOf(bonuses), conflictAction);
    }

    @Override
    public UUID getUniqueId() {
        return uuid;
    }

    @Override
    public void run(UUID holderId, OriginSource source, Bloodline<?> bloodline, BloodlineData data, int purity, ProgressDirection direction, String salt) {
        ProgressionModifiers.applyWithPurity(conflictAction, data, purity, direction,
                (transform, effectiveDirection) -> ProgressionModifiers.applyPathBonuses(source, bonuses, conflictAction, transform, effectiveDirection, salt));
    }

    @Override
    public List<ProgressActionDescription> getDescriptions(RegistryAccess access) {
        return GivePathBonusesAction.describe(bonuses, access);
    }

    @Override
    public ProgressActionType getType() {
        return AscensionProgressActionTypes.GIVE_PATH_BONUSES_USING_PURITY_TYPE.get();
    }
}
