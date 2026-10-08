package net.zic.ascension.impl.core.progression;

import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.zic.ascension.api.ascension.core.bloodline.BloodlineData;
import net.zic.ascension.api.ascension.core.path.bonus.PathBonus;
import net.zic.ascension.api.ascension.core.progression.ProgressAction;
import net.zic.ascension.api.ascension.core.progression.ProgressActionDescription;
import net.zic.ascension.api.ascension.core.progression.ProgressDirection;
import net.zic.ascension.api.ascension.core.source.AscensionOriginSourceHelper;
import net.zic.ascension.api.ascension.datapack.progresison.ProgressActionType;
import net.zic.ascension.api.rpg_engine.source.OriginSource;
import net.zic.ascension.impl.datapack.progression.AscensionProgressActionTypes;
import net.zic.zenithlib.value_containers.typed.ModifierHolder;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Adds or removes flat path bonuses as progression moves up or down.
 */
public record GivePathBonusesAction(UUID uuid, Map<PathBonus, ModifierHolder<Double>> bonuses, boolean perPurity, ModifierMergeMode mergeMode) implements ProgressAction {

    public static GivePathBonusesAction from(Map<PathBonus, ModifierHolder<Double>> bonuses, boolean perPurity, ModifierMergeMode mergeMode) {
        if (perPurity && mergeMode.resolve(true) != ModifierMergeMode.REPLACE) {
            throw new IllegalArgumentException("Per-purity modifiers require replace mode");
        }
        return new GivePathBonusesAction(UUID.randomUUID(), Map.copyOf(bonuses), perPurity, mergeMode);
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
            ProgressDirection direction
    ) {
        if (perPurity && !(contextData instanceof BloodlineData)) return;
        int purity = perPurity ? Math.max(0, Math.min(100, ((BloodlineData) contextData).getPurity())) : 1;

        for (Map.Entry<PathBonus, ModifierHolder<Double>> modifiers : bonuses.entrySet()) {
            PathBonus bonus = modifiers.getKey();
            ModifierActionHelper.apply(
                    modifiers.getValue(),
                    () -> AscensionOriginSourceHelper.getPathBonusContainer(source, bonus.category(), bonus.path()),
                    modifier -> AscensionOriginSourceHelper.addBonusFlatModifier(source, bonus.category(), bonus.path(), modifier),
                    modifier -> AscensionOriginSourceHelper.addBonusMultiplierModifier(source, bonus.category(), bonus.path(), modifier),
                    id -> AscensionOriginSourceHelper.removeBonusModifier(source, bonus.category(), bonus.path(), id),
                    direction, mergeMode, perPurity, purity
            );
        }
    }

    @Override
    public List<ProgressActionDescription> getDescriptions(RegistryAccess access) {
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
