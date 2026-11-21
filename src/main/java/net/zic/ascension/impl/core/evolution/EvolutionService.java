package net.zic.ascension.impl.core.evolution;

import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.zic.ascension.api.ascension.core.CoreRegistries;
import net.zic.ascension.api.ascension.core.bloodline.Bloodline;
import net.zic.ascension.api.ascension.core.bloodline.BloodlineInstance;
import net.zic.ascension.api.ascension.core.physique.Physique;
import net.zic.ascension.api.ascension.core.source.AscensionOriginSourceHelper;
import net.zic.ascension.api.rpg_engine.source.OriginSource;
import net.zic.ascension.impl.core.connection.ConnectionRequirements;

import java.util.List;

/**
 * Runs evolutions. The transfer items call this first: using a bloodline / physique item that is the catalyst of one of the
 * player's evolution definitions evolves it (or says what is missing) instead of giving the item's own bloodline / physique.
 */
public final class EvolutionService {
    private EvolutionService() {
    }

    public enum Result {
        /** the item is not a catalyst for anything the player has, handle it as a normal transfer item */
        NOT_APPLICABLE,
        EVOLVED,
        /** it is a catalyst but the evolution did not happen, the player was told why */
        FAILED
    }

    public static Result tryEvolveBloodline(Player player, OriginSource source, Identifier catalystId, RegistryAccess access) {
        Component firstFailure = null;
        for (Identifier heldId : List.copyOf(AscensionOriginSourceHelper.getBloodlines(source))) {
            Bloodline<?> definition = CoreRegistries.safeAccess(CoreRegistries.BLOODLINE_REGISTRY, heldId, access);
            if (!(definition instanceof Evolving evolving) || !evolving.evolution().catalysts().contains(catalystId)) {
                continue;
            }
            EvolutionRequirements evolution = evolving.evolution();
            int purity = AscensionOriginSourceHelper.getBloodlineData(source, heldId).getPurity();
            Component failure = evolution.failureMessage(source, access, purity);
            if (failure != null) {
                if (firstFailure == null) {
                    firstFailure = failure;
                }
                continue;
            }
            BloodlineInstance<?> target = BloodlineInstance.create(evolution.into(), access);
            if (target == null) {
                if (firstFailure == null) {
                    firstFailure = Component.translatable("ascension.evolution.fail.unknown_target");
                }
                continue;
            }
            target.getData().setPurity(evolution.resultPurity());
            // the new bloodline goes on first, so a failed add never costs the player the one they have
            if (!AscensionOriginSourceHelper.addBloodline(source, target, true)) {
                if (firstFailure == null) {
                    firstFailure = Component.translatable("ascension.evolution.fail.cannot_add");
                }
                continue;
            }
            AscensionOriginSourceHelper.removeBloodline(source, heldId);
            player.sendOverlayMessage(Component.translatable("ascension.evolution.success",
                    ConnectionRequirements.bloodlineName(evolution.into(), access)));
            return Result.EVOLVED;
        }
        if (firstFailure != null) {
            player.sendOverlayMessage(firstFailure);
            return Result.FAILED;
        }
        return Result.NOT_APPLICABLE;
    }

    public static Result tryEvolvePhysique(Player player, OriginSource source, Identifier catalystId, RegistryAccess access) {
        Identifier heldId = AscensionOriginSourceHelper.getPhysiqueId(source);
        if (heldId == null) {
            return Result.NOT_APPLICABLE;
        }
        Physique<?> definition = CoreRegistries.safeAccess(CoreRegistries.PHYSIQUE_REGISTRY, heldId, access);
        if (!(definition instanceof Evolving evolving) || !evolving.evolution().catalysts().contains(catalystId)) {
            return Result.NOT_APPLICABLE;
        }
        EvolutionRequirements evolution = evolving.evolution();
        Component failure = evolution.failureMessage(source, access, 0);
        if (failure != null) {
            player.sendOverlayMessage(failure);
            return Result.FAILED;
        }
        if (CoreRegistries.safeAccess(CoreRegistries.PHYSIQUE_REGISTRY, evolution.into(), access) == null) {
            player.sendOverlayMessage(Component.translatable("ascension.evolution.fail.unknown_target"));
            return Result.FAILED;
        }
        if (!AscensionOriginSourceHelper.setPhysique(source, evolution.into())) {
            player.sendOverlayMessage(Component.translatable("ascension.evolution.fail.cannot_add"));
            return Result.FAILED;
        }
        player.sendOverlayMessage(Component.translatable("ascension.evolution.success",
                ConnectionRequirements.physiqueName(evolution.into(), access)));
        return Result.EVOLVED;
    }
}