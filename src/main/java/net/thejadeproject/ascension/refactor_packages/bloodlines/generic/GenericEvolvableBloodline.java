package net.thejadeproject.ascension.refactor_packages.bloodlines.generic;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.network.PacketDistributor;
import net.thejadeproject.ascension.AscensionCraft;
import net.thejadeproject.ascension.refactor_packages.bloodlines.IBloodline;
import net.thejadeproject.ascension.refactor_packages.bloodlines.IBloodlineData;
import net.thejadeproject.ascension.refactor_packages.entity_data.IEntityData;
import net.thejadeproject.ascension.refactor_packages.network.client_bound.toast.ShowAscensionToast;
import net.thejadeproject.ascension.refactor_packages.registries.AscensionRegistries;

import static net.thejadeproject.ascension.clients.toast.AscensionToastInterface.DEFAULT_BACKGROUND;

/**
 * Base class for any bloodline that can evolve into a higher tier.
 *
 * Evolution is NOT fixed to kills — subclasses decide what triggers it.
 * The purity system (0-100 tracked in {@link PurityBloodlineData}) is the built-in
 * progression model, but subclasses can completely ignore it and drive evolution
 * via any other mechanism by overriding {@link  #onBloodlineAdded(IEntityData, ResourceLocation)} or adding
 * their own event hooks.
 *
 * ── Built-in kill-based purity ──────────────────────────────────────────────
 * Call {@link #onKill} from a LivingDeathEvent listener. The amount of purity
 * gained per kill is determined by a {@link KillQualityWeights} table which you
 * configure via {@link #setKillWeights(KillQualityWeights)}.
 *
 * If no KillQualityWeights is set, {@link #getDefaultPurityPerKill} is used
 * (override it in your subclass to set a blanket default without building a table).
 *
 * ── Custom evolution triggers ────────────────────────────────────────────────
 * Override {@link #checkEvolution} to define your own condition. It is called:
 *   - automatically after every {@link #onKill}
 *   - can be called manually from any other context
 *
 * ── Wiring kills ─────────────────────────────────────────────────────────────
 * In your event handler:
 *
 * @SubscribeEvent
 * public static void onLivingDeath(LivingDeathEvent event) {
 *     if (!(event.getSource().getEntity() instanceof ServerPlayer player)) return;
 *     if (!player.hasData(ModAttachments.ENTITY_DATA)) return;
 *     IEntityData data = player.getData(ModAttachments.ENTITY_DATA);
 *     if (data.getBloodline() instanceof GenericEvolvableBloodline b) {
 *         b.onKill(player, data, event.getEntity());
 *     }
 * }
 */
public abstract class GenericEvolvableBloodline extends GenericBloodline {

    /** The bloodline this evolves into. Null = final tier, no evolution. */
    private final ResourceLocation evolvesInto;

    /**
     * Optional kill quality weight table.
     * If null, {@link #getDefaultPurityPerKill} is used for all kills.
     */
    private KillQualityWeights killWeights;

    public GenericEvolvableBloodline(Component title, ResourceLocation evolvesInto) {
        super(title);
        this.evolvesInto = evolvesInto;
    }

    // ─────────────────────────────── builder ──────────────────────────────────

    /**
     * Sets the kill quality weight table for purity gain.
     * Rules are evaluated in order — first match wins.
     *
     * Example:
     * .setKillWeights(KillQualityWeights.builder()
     *     .defaultGain(0.01)
     *     .ifBoss(0.5)
     *     .ifPassive(0.0)
     *     .ifRealmAtLeast("golden_core", 1, 0.05)
     *     .ifHealthAtLeast(200, 0.08)
     *     .build())
     */
    public GenericEvolvableBloodline setKillWeights(KillQualityWeights weights) {
        this.killWeights = weights;
        return this;
    }

    // ─────────────────────────────── data ─────────────────────────────────────

    @Override
    public IBloodlineData freshBloodlineData(IEntityData heldEntity) {
        return new PurityBloodlineData(0);
    }

    @Override
    public IBloodlineData fromCompound(CompoundTag tag, IEntityData heldEntity) {
        return PurityBloodlineData.read(tag);
    }

    @Override
    public IBloodlineData fromNetwork(RegistryFriendlyByteBuf buf) {
        return PurityBloodlineData.decode(buf);
    }

    // ─────────────────────────────── kill hook ────────────────────────────────

    /**
     * Call this from a LivingDeathEvent listener when the killer holds this bloodline.
     * Computes purity gain, updates data, fires milestones, and triggers evolution check.
     */
    public void onKill(ServerPlayer player, IEntityData entityData, LivingEntity killed) {
        if (!(entityData.getBloodlineData() instanceof PurityBloodlineData data)) return;
        if (data.isMaxPurity()) {
            // Already at max — still run evolution check in case it was missed
            checkEvolution(player, entityData, data);
            return;
        }

        double gain = computePurityGain(player, entityData, killed);
        boolean justMaxed = data.addPurity(gain);

        onPurityChanged(player, entityData, data);

        if (justMaxed) {
            onPurityMaxed(player, entityData, data);
        }

        checkEvolution(player, entityData, data);
    }

    // ─────────────────────────────── evolution ────────────────────────────────

    /**
     * Override to define when this bloodline should evolve.
     * Default: evolves when purity reaches max (100).
     *
     * Called automatically after every kill. Can also be called manually.
     */
    protected void checkEvolution(ServerPlayer player, IEntityData entityData, PurityBloodlineData data) {
        if (data.isMaxPurity()) {
            tryEvolve(player, entityData);
        }
    }

    /**
     * Triggers the actual bloodline swap. Safe to call externally for non-kill-based evolution.
     * Returns true if the evolution succeeded.
     */
    public boolean tryEvolve(ServerPlayer player, IEntityData entityData) {
        if (evolvesInto == null) return false;

        IBloodline newBloodlineObj = AscensionRegistries.Bloodlines.BLOODLINE_REGISTRY.get(evolvesInto);
        if (newBloodlineObj == null) {
            AscensionCraft.LOGGER.error("GenericEvolvableBloodline could not find evolution target: " + evolvesInto);
            return false;
        }

        entityData.setBloodline(evolvesInto);

        String evolvedName = newBloodlineObj instanceof GenericBloodline gb
                ? gb.getDisplayTitle().getString()
                : evolvesInto.getPath();

        if (player.connection != null) {
            PacketDistributor.sendToPlayer(player, new ShowAscensionToast(
                    evolvedName,
                    "Bloodline Awakened",
                    null,
                    DEFAULT_BACKGROUND
            ));
        }

        broadcastRareAcquired(entityData, "ascension.bloodline.awakening_broadcast");
        onEvolved(player, entityData, evolvesInto);
        return true;
    }

    // ─────────────────────────────── overrideable ─────────────────────────────

    /**
     * Default purity gain per kill when no {@link KillQualityWeights} table is set
     * and no rule matches. Override in your subclass to set a blanket default.
     */
    protected double getDefaultPurityPerKill() {
        return 0.01;
    }

    /**
     * Called every time purity changes (after every qualifying kill).
     * Override to add milestone toasts, sounds, particles, etc.
     */
    protected void onPurityChanged(ServerPlayer player, IEntityData entityData, PurityBloodlineData data) {
        double purity = data.getPurity();
        double prev   = purity - computePurityGain(player, entityData, null); // approximate prev

        sendMilestoneIfCrossed(player, purity, prev, 25);
        sendMilestoneIfCrossed(player, purity, prev, 50);
        sendMilestoneIfCrossed(player, purity, prev, 75);
    }

    /**
     * Called once when purity first reaches 100. Override for special effects at max purity.
     */
    protected void onPurityMaxed(ServerPlayer player, IEntityData entityData, PurityBloodlineData data) {}

    /**
     * Called after a successful bloodline evolution.
     * Override to fire extra effects, grant items, etc.
     */
    protected void onEvolved(ServerPlayer player, IEntityData entityData, ResourceLocation evolvedInto) {}

    // ─────────────────────────────── helpers ──────────────────────────────────

    private double computePurityGain(ServerPlayer player, IEntityData entityData, LivingEntity killed) {
        if (killed == null) return getDefaultPurityPerKill();
        if (killWeights != null) return killWeights.evaluate(killed);
        return getDefaultPurityPerKill();
    }

    private void sendMilestoneIfCrossed(ServerPlayer player, double current, double previous, double milestone) {
        if (current >= milestone && previous < milestone) {
            sendMilestoneToast(player, String.valueOf((int) milestone));
        }
    }

    private void sendMilestoneToast(ServerPlayer player, String percent) {
        if (player.connection == null) return;
        PacketDistributor.sendToPlayer(player, new ShowAscensionToast(
                getDisplayTitle().getString(),
                "Bloodline Purity " + percent + "%",
                null,
                DEFAULT_BACKGROUND
        ));
    }

    public ResourceLocation getEvolvesInto() {
        return evolvesInto;
    }

    public KillQualityWeights getKillWeights() {
        return killWeights;
    }
}
