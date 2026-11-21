package net.zic.ascension.impl.core.evolution;

import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.ascension.core.CoreRegistries;
import net.zic.ascension.api.ascension.core.bloodline.Bloodline;
import net.zic.ascension.api.ascension.core.bloodline.BloodlineInstance;
import net.zic.ascension.api.ascension.core.physique.Physique;
import net.zic.ascension.api.ascension.core.source.AscensionOriginSourceHelper;
import net.zic.ascension.api.rpg_engine.source.OriginSource;
import net.zic.ascension.impl.core.connection.ConnectionRequirements;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;


public final class EvolutionService {
    // The catalyst used for each player's last physique evolution, and when. Holding right click on a stack repeats the use,
    // and the repeat would otherwise swap the freshly evolved physique for the catalyst's own.
    private static final Map<UUID, RecentEvolution> RECENT_PHYSIQUE = new HashMap<>();
    private static final long REPEAT_GUARD_TICKS = 100L;

    private record RecentEvolution(Identifier catalyst, long gameTime) {
    }

    private EvolutionService() {
    }

    /** true if this player evolved their physique with this catalyst a moment ago, so this use is a repeat of the same click */
    public static boolean isRepeatOfPhysiqueEvolution(Player player, Identifier catalystId) {
        RecentEvolution recent = RECENT_PHYSIQUE.get(player.getUUID());
        return recent != null && recent.catalyst().equals(catalystId)
                && player.level().getGameTime() - recent.gameTime() < REPEAT_GUARD_TICKS;
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
        if (evolution.failureMessage(source, access, 0) != null) {
            return Result.NOT_APPLICABLE;
        }
        if (CoreRegistries.safeAccess(CoreRegistries.PHYSIQUE_REGISTRY, evolution.into(), access) == null) {
            AscensionCraft.LOGGER.warn("[Evolution] {} evolves into {}, which is not registered", heldId, evolution.into());
            return Result.NOT_APPLICABLE;
        }
        if (!AscensionOriginSourceHelper.setPhysique(source, evolution.into())) {
            return Result.NOT_APPLICABLE;
        }
        RECENT_PHYSIQUE.put(player.getUUID(), new RecentEvolution(catalystId, player.level().getGameTime()));
        player.sendOverlayMessage(Component.translatable("ascension.evolution.success",
                ConnectionRequirements.physiqueName(evolution.into(), access)));
        return Result.EVOLVED;
    }
}