package net.zic.ascension.impl.core.progression;

import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.zic.ascension.api.ascension.core.progression.ProgressAction;
import net.zic.ascension.api.ascension.core.progression.ProgressActionDescription;
import net.zic.ascension.api.ascension.core.progression.ProgressDirection;
import net.zic.ascension.api.ascension.core.source.AscensionOriginSourceHelper;
import net.zic.ascension.api.ascension.datapack.path.PathBonusBase;
import net.zic.ascension.api.ascension.datapack.progresison.ProgressActionType;
import net.zic.ascension.api.rpg_engine.source.OriginSource;
import net.zic.ascension.impl.datapack.progression.AscensionProgressActionTypes;

import java.util.List;
import java.util.UUID;

/**
 * Adds or removes flat path bonuses as progression moves up or down.
 */
public record GivePathBonusesAction(UUID uuid, List<PathBonusBase> bonuses) implements ProgressAction {

    public static GivePathBonusesAction from(List<PathBonusBase> bonuses) {
        return new GivePathBonusesAction(UUID.randomUUID(), List.copyOf(bonuses));
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
        for (PathBonusBase bonus : bonuses) {
            if (direction == ProgressDirection.UP) {
                AscensionOriginSourceHelper.addBonus(source, bonus.category(), bonus.path(), bonus.value());
            } else {
                AscensionOriginSourceHelper.removeBonus(source, bonus.category(), bonus.path(), bonus.value());
            }
        }
    }

    @Override
    public List<ProgressActionDescription> getDescriptions(RegistryAccess access) {
        return bonuses.stream()
                .map(bonus -> ProgressActionDescription.numeric(
                        ProgressionDescriptionUtil.pathBonusName(
                                bonus.category(),
                                bonus.path(),
                                access),
                        Component.literal(ProgressionDescriptionUtil.signedNumber(bonus.value())),
                        bonus.value()
                ))
                .toList();
    }

    @Override
    public ProgressActionType getType() {
        return AscensionProgressActionTypes.GIVE_PATH_BONUSES_TYPE.get();
    }
}
