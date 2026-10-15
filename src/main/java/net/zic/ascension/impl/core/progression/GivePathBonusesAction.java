package net.zic.ascension.impl.core.progression;

import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.zic.ascension.api.ascension.core.path.bonus.PathBonus;
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

/**
 * Adds or removes path bonuses as progression moves up or down.
 */
public record GivePathBonusesAction(UUID uuid, Map<PathBonus, ModifierHolder<Double>> bonuses, ModifierConflictAction conflictAction) implements ProgressAction {

    public static GivePathBonusesAction from(Map<PathBonus, ModifierHolder<Double>> bonuses, ModifierConflictAction conflictAction) {
        return new GivePathBonusesAction(UUID.randomUUID(), Map.copyOf(bonuses), conflictAction);
    }

    @Override
    public UUID getUniqueId() {
        return uuid;
    }

    @Override
    public void run(
            UUID holderId,
            OriginSource source,
            Identifier contextIdentifier,
            Object contextData,
            ProgressDirection direction,
            String salt
    ) {
        ProgressionModifiers.applyPathBonuses(source, bonuses, conflictAction, UnaryOperator.identity(), direction.value, salt);
    }

    @Override
    public List<ProgressActionDescription> getDescriptions(RegistryAccess access) {
        return describe(bonuses, access);
    }

    static List<ProgressActionDescription> describe(Map<PathBonus, ModifierHolder<Double>> bonuses, RegistryAccess access) {
        return bonuses.entrySet().stream()
                .map(bonus ->
                    bonus.getValue().flat().stream().map( modifier ->  ProgressActionDescription.numeric(
                                    ProgressionDescriptionUtil.pathBonusName(
                                            bonus.getKey().category(),
                                            bonus.getKey().path(),
                                            access),
                                    Component.literal(ProgressionDescriptionUtil.signedNumber( modifier.value())),
                                    modifier.value()
                            )).toList()).flatMap(Collection::stream).toList();
    }

    @Override
    public ProgressActionType getType() {
        return AscensionProgressActionTypes.GIVE_PATH_BONUSES_TYPE.get();
    }
}
