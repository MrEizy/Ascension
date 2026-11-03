package net.zic.ascension.impl.core.skill.passive;

import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.ascension.core.resource.ResourceOperation;
import net.zic.ascension.api.ascension.core.resource.ResourceSourceIdentity;
import net.zic.ascension.api.ascension.core.resource.ResourceTransactionRequest;
import net.zic.ascension.api.ascension.core.resource.ResourceTransactionService;
import net.zic.ascension.api.ascension.core.skill.castable.ActiveSkillCostDefinition;
import net.zic.ascension.api.ascension.core.source.AscensionOriginSourceHelper;
import net.zic.ascension.api.ascension.value.ScaledValue;
import net.zic.ascension.api.rpg_engine.source.OriginSource;
import net.zic.ascension.impl.resource.stamina.StaminaService;
import net.zic.ascension.mixins.accessor.ServerGamePacketListenerImplAccessor;
import net.zic.ascension.network.QiStepPlatePacket;
import net.zic.ascension.network.QiStepStatePacket;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Server side of Qi Steps (PassiveModifiers.QiStep). The player's own client decides when a plate forms under them, since
 * clients move their own players, and reports each one (QiStepActionPacket). Here each plate is paid for in qi and counted
 * toward the trip's strain; when either runs out the client is told the trip is spent. Strain resets on landing.
 */
@EventBusSubscriber(modid = AscensionCraft.MOD_ID)
public final class QiStepService {
    // Standing still on a plate is charged once every HOVER_CHARGE_TICKS.
    public static final int HOVER_CHARGE_TICKS = 5;
    // How far from the player a reported plate may be, in blocks, before it's ignored.
    private static final double MAX_PLATE_DISTANCE = 3.0D;

    private static final Map<UUID, PassiveModifiers.QiStep> ACTIVE = new ConcurrentHashMap<>();
    private static final Map<UUID, Double> STRAIN = new ConcurrentHashMap<>();
    // The skill each player's Qi Steps comes from, which its costs are charged under.
    private static final Map<UUID, Identifier> SKILLS = new ConcurrentHashMap<>();

    private QiStepService() {
    }

    public enum Action {
        STEP, JUMP, HOVER
    }

    /**
     * Strain is only cleared by landing (onPlayerTick), not by switching Qi Steps off and on, so toggling it in mid-air
     * can't buy a fresh trip.
     */
    public static void enable(LivingEntity entity, PassiveModifiers.QiStep settings, Identifier skillId) {
        if (entity instanceof ServerPlayer player) {
            ACTIVE.put(player.getUUID(), settings);
            SKILLS.put(player.getUUID(), skillId);
            // Strain cap off for now.
            boolean spent = false; // STRAIN.getOrDefault(player.getUUID(), 0.0D) >= settings.maxStrain();
            PacketDistributor.sendToPlayer(player, new QiStepStatePacket(settings, spent));
        }
    }

    public static void disable(LivingEntity entity) {
        if (entity instanceof ServerPlayer player && ACTIVE.remove(player.getUUID()) != null) {
            SKILLS.remove(player.getUUID());
            PacketDistributor.sendToPlayer(player, new QiStepStatePacket(null, false));
        }
    }

    /**
     * A plate the player's client formed: charges it, then keeps the server from treating the player as flying and shows the
     * plate to everyone else nearby.
     */
    public static void handleAction(ServerPlayer player, Action action, Vec3 plate) {
        PassiveModifiers.QiStep settings = ACTIVE.get(player.getUUID());
        if (settings == null || player.position().distanceTo(plate) > MAX_PLATE_DISTANCE) {
            return;
        }
        double cost = switch (action) {
            case STEP -> settings.stepCost();
            case JUMP -> settings.jumpCost();
            case HOVER -> settings.hoverCost();
        };
        List<ActiveSkillCostDefinition> costs = switch (action) {
            case STEP -> settings.stepCosts();
            case JUMP -> settings.jumpCosts();
            case HOVER -> settings.hoverCosts();
        };
        double strain = switch (action) {
            case STEP -> 1.0D;
            case JUMP -> settings.jumpStrain();
            case HOVER -> settings.hoverStrain();
        };
        // A little slack over the cap, for a plate the client formed before it heard the trip was spent.
        STRAIN.merge(player.getUUID(), strain, Double::sum);
        // Strain cap off for now: Qi Steps lasts as long as your qi does.
        if (/* STRAIN.get(player.getUUID()) > settings.maxStrain() + 1.0D || */ !(costs.isEmpty() ? payQi(player, cost) : pay(player, costs))) {
            PacketDistributor.sendToPlayer(player, new QiStepStatePacket(settings, true));
            return;
        }
        ((ServerGamePacketListenerImplAccessor) player.connection).ascension$setAboveGroundTickCount(0);
        player.resetFallDistance();
        PacketDistributor.sendToPlayersTrackingEntity(player, new QiStepPlatePacket(plate, player.yBodyRot, action != Action.STEP));
    }

    /**
     * Pays a list of skill costs (step_costs and the like), all or nothing: each is checked before any is taken.
     */
    private static boolean pay(ServerPlayer player, List<ActiveSkillCostDefinition> costs) {
        OriginSource source = AscensionOriginSourceHelper.getEntitySource(player);
        Identifier skillId = SKILLS.get(player.getUUID());
        if (source == null || skillId == null) {
            return false;
        }
        Map<Identifier, Double> variables = Map.of();
        ScaledValue.Context context = new ScaledValue.Context(source, skillId, player, null, 0.0D, variables);
        for (ActiveSkillCostDefinition cost : costs) {
            if (!cost.canPay(player, skillId, null, context, variables)) {
                return false;
            }
        }
        for (ActiveSkillCostDefinition cost : costs) {
            if (!cost.pay(player, skillId, null, context, variables)) {
                return false;
            }
        }
        return true;
    }

    private static boolean payQi(ServerPlayer player, double amount) {
        if (amount <= 0.0D) {
            return true;
        }
        ResourceTransactionRequest request = ResourceTransactionRequest.of(player, AscensionCraft.prefix("qi"), ResourceOperation.CONSUME,
                amount, new ResourceSourceIdentity.Simple(AscensionCraft.prefix("skill_casting"), Set.of(ResourceSourceIdentity.Tags.SKILL)));
        return ResourceTransactionService.transact(request).succeeded();
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.level().isClientSide() || !(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        // Same "back on solid footing" checks as QiStepClient, so both sides end the trip together.
        boolean grounded = player.onGround() || player.isInWater() || player.isInLava() || player.isPassenger() || player.onClimbable();
        if (!grounded) {
            // Standing still on a plate costs nothing, so without this stamina would start coming back mid-air. Holding the
            // regen delay at full keeps it paused for the whole trip, and the usual delay then runs from landing.
            if (STRAIN.containsKey(player.getUUID())) {
                PassiveModifiers.QiStep settings = ACTIVE.get(player.getUUID());
                if (settings != null && settings.pausesStaminaRegen()) {
                    StaminaService.resetRegenerationDelay(serverPlayer);
                }
            }
            return;
        }
        if (STRAIN.remove(player.getUUID()) != null) {
            PassiveModifiers.QiStep settings = ACTIVE.get(player.getUUID());
            if (settings != null) {
                // Landing refreshes the trip, in case the client was told it was spent.
                PacketDistributor.sendToPlayer(serverPlayer, new QiStepStatePacket(settings, false));
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        ACTIVE.remove(event.getEntity().getUUID());
        STRAIN.remove(event.getEntity().getUUID());
    }
}
