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
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Runs evolutions. There are two ways to trigger one:
 * <ul>
 *   <li>catalyst: the transfer items call tryEvolve... when a bloodline / physique item is used</li>
 *   <li>absorb_catalyst: an absorption_cultivation skill finishes absorbing an item or block, see checkAbsorb / applyAbsorb</li>
 * </ul>
 */
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

    /**
     * @param result  EVOLVED means an evolution is possible right now, FAILED means something matched but its requirements are not met
     * @param failure the first unmet requirement when result is FAILED
     */
    public record AbsorbCheck(Result result, @Nullable Component failure) {
        public boolean possible() {
            return result == Result.EVOLVED;
        }
    }

    // ── catalyst (bloodline / physique items) ──────────────────────────────────────────

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
            if (failure == null) {
                failure = evolveBloodline(player, source, heldId, evolution, access);
            }
            if (failure == null) {
                return Result.EVOLVED;
            }
            if (firstFailure == null) {
                firstFailure = failure;
            }
        }
        if (firstFailure != null) {
            player.sendOverlayMessage(firstFailure);
            return Result.FAILED;
        }
        return Result.NOT_APPLICABLE;
    }

    /**
     * If the physique the player has is an evolution_physique and this item is its catalyst and every requirement is met, the
     * player evolves into its "into" physique. In every other case nothing happens here (NOT_APPLICABLE), and the transfer item
     * carries on and swaps to the physique of the item as usual.
     */
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
        if (evolution.failureMessage(source, access, 0) != null || !evolvePhysique(player, source, heldId, evolution, access)) {
            return Result.NOT_APPLICABLE;
        }
        RECENT_PHYSIQUE.put(player.getUUID(), new RecentEvolution(catalystId, player.level().getGameTime()));
        return Result.EVOLVED;
    }

    // ── absorb_catalyst (items and blocks absorbed by an absorption_cultivation skill) ───────

    /**
     * Whether absorbing this item / block id would evolve anything the player has right now. Changes nothing.
     */
    public static AbsorbCheck checkAbsorb(OriginSource source, Identifier absorbedId, RegistryAccess access) {
        Component firstFailure = null;
        boolean possible = false;

        Identifier physiqueId = AscensionOriginSourceHelper.getPhysiqueId(source);
        if (physiqueId != null
                && CoreRegistries.safeAccess(CoreRegistries.PHYSIQUE_REGISTRY, physiqueId, access) instanceof Evolving evolving
                && evolving.evolution().absorbCatalysts().contains(absorbedId)) {
            Component failure = evolving.evolution().failureMessage(source, access, 0);
            if (failure == null) {
                possible = true;
            } else {
                firstFailure = failure;
            }
        }
        for (Identifier heldId : List.copyOf(AscensionOriginSourceHelper.getBloodlines(source))) {
            if (!(CoreRegistries.safeAccess(CoreRegistries.BLOODLINE_REGISTRY, heldId, access) instanceof Evolving evolving)
                    || !evolving.evolution().absorbCatalysts().contains(absorbedId)) {
                continue;
            }
            int purity = AscensionOriginSourceHelper.getBloodlineData(source, heldId).getPurity();
            Component failure = evolving.evolution().failureMessage(source, access, purity);
            if (failure == null) {
                possible = true;
            } else if (firstFailure == null) {
                firstFailure = failure;
            }
        }
        if (possible) {
            return new AbsorbCheck(Result.EVOLVED, null);
        }
        return firstFailure != null ? new AbsorbCheck(Result.FAILED, firstFailure) : new AbsorbCheck(Result.NOT_APPLICABLE, null);
    }

    /**
     * Evolves everything the player has that takes this item / block id as an absorb_catalyst and has its requirements met.
     *
     * @return whether anything evolved
     */
    public static boolean applyAbsorb(Player player, OriginSource source, Identifier absorbedId, RegistryAccess access) {
        boolean evolved = false;

        Identifier physiqueId = AscensionOriginSourceHelper.getPhysiqueId(source);
        if (physiqueId != null
                && CoreRegistries.safeAccess(CoreRegistries.PHYSIQUE_REGISTRY, physiqueId, access) instanceof Evolving evolving
                && evolving.evolution().absorbCatalysts().contains(absorbedId)
                && evolving.evolution().failureMessage(source, access, 0) == null
                && evolvePhysique(player, source, physiqueId, evolving.evolution(), access)) {
            evolved = true;
        }
        for (Identifier heldId : List.copyOf(AscensionOriginSourceHelper.getBloodlines(source))) {
            if (!(CoreRegistries.safeAccess(CoreRegistries.BLOODLINE_REGISTRY, heldId, access) instanceof Evolving evolving)
                    || !evolving.evolution().absorbCatalysts().contains(absorbedId)) {
                continue;
            }
            int purity = AscensionOriginSourceHelper.getBloodlineData(source, heldId).getPurity();
            if (evolving.evolution().failureMessage(source, access, purity) == null
                    && evolveBloodline(player, source, heldId, evolving.evolution(), access) == null) {
                evolved = true;
            }
        }
        return evolved;
    }

    // ── shared ────────────────────────────────────────────────────────────────────────

    /** @return null if it evolved, otherwise why it did not */
    private static @Nullable Component evolveBloodline(Player player, OriginSource source, Identifier heldId,
                                                       EvolutionRequirements evolution, RegistryAccess access) {
        BloodlineInstance<?> target = BloodlineInstance.create(evolution.into(), access);
        if (target == null) {
            return Component.translatable("ascension.evolution.fail.unknown_target");
        }
        target.getData().setPurity(evolution.resultPurity());
        // the new bloodline goes on first, so a failed add never costs the player the one they have
        if (!AscensionOriginSourceHelper.addBloodline(source, target, true)) {
            return Component.translatable("ascension.evolution.fail.cannot_add");
        }
        AscensionOriginSourceHelper.removeBloodline(source, heldId);
        player.sendOverlayMessage(Component.translatable("ascension.evolution.success",
                ConnectionRequirements.bloodlineName(evolution.into(), access)));
        return null;
    }

    private static boolean evolvePhysique(Player player, OriginSource source, Identifier heldId,
                                          EvolutionRequirements evolution, RegistryAccess access) {
        if (CoreRegistries.safeAccess(CoreRegistries.PHYSIQUE_REGISTRY, evolution.into(), access) == null) {
            AscensionCraft.LOGGER.warn("[Evolution] {} evolves into {}, which is not registered", heldId, evolution.into());
            return false;
        }
        if (!AscensionOriginSourceHelper.setPhysique(source, evolution.into())) {
            return false;
        }
        player.sendOverlayMessage(Component.translatable("ascension.evolution.success",
                ConnectionRequirements.physiqueName(evolution.into(), access)));
        return true;
    }
}