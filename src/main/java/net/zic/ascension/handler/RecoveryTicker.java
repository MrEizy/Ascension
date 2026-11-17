package net.zic.ascension.handler;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.ascension.capabilities.CoreCapabilities;
import net.zic.ascension.api.ascension.capabilities.EntityQiProvider;
import net.zic.ascension.api.ascension.core.resource.ResourceSourceIdentity;
import net.zic.ascension.common.util.AscensionAttributes;
import net.zic.ascension.impl.core.skill.body.BodyCultivationSkillService;
import net.zic.ascension.impl.resource.AscensionResourceSources;
import net.zic.ascension.impl.resource.AscensionResourceTypes;
import net.zic.ascension.impl.resource.stamina.StaminaService;
import net.zic.ascension.api.ascension.core.resource.ResourceTransactionService;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@EventBusSubscriber(modid = AscensionCraft.MOD_ID)
public final class RecoveryTicker {
    private static final double TICKS_PER_SECOND = 20.0D;
    private static final float VANILLA_HEALTH_THRESHOLD = 20.0F;
    public static final double SPRINTING_COST = 0.3D;
    public static final double SWIMMING_COST = 0.14D;
    public static final double ELYTRA_COST = 0.08D;
    public static final double CLIMBING_COST = 0.10D;
    public static final double CRAWLING_COST = 0.03D;
    public static final double JUMPING_COST = 1.5D;
    public static final double ATTACKING_COST = 6.0D;
    // 0.01 blocks per tick; sneaking is about 0.065.
    private static final double MIN_MOVEMENT_SQR = 1.0E-4D;
    // 10 blocks in one tick is a teleport, not movement (elytra tops out around 3-4).
    private static final double MAX_MOVEMENT_SQR = 100.0D;
    // Last tick's position per online player; the server has no reliable per-player velocity.
    private static final Map<UUID, Vec3> LAST_POSITIONS = new HashMap<>();

    private RecoveryTicker() {
    }

    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Pre event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        tickHealth(player);
        tickQi(player);
        tickStamina(player);
    }

    public static void spendJumpStamina(ServerPlayer player) {
        if (!canSpend(player)) {
            return;
        }
        BodyCultivationSkillService.stimulate(player, AscensionResourceSources.JUMPING, 1.0D);
        spend(player, AscensionResourceSources.JUMPING, JUMPING_COST);
    }

    public static void spendAttackStamina(ServerPlayer player) {
        if (!canSpend(player)) {
            return;
        }
        BodyCultivationSkillService.stimulate(player, AscensionResourceSources.ATTACKING, 1.0D);
        spend(player, AscensionResourceSources.ATTACKING, ATTACKING_COST);
    }

    private static void tickHealth(ServerPlayer player) {
        if (player.isSpectator() || !player.isAlive()
                || !player.getAttributes().hasAttribute(AscensionAttributes.HEALTH_REGEN_RATE)) {
            return;
        }
        float maximum = player.getMaxHealth();
        float current = player.getHealth();
        if (current >= maximum || current < Math.min(VANILLA_HEALTH_THRESHOLD, maximum)) {
            return;
        }
        double rate = Math.max(0.0D, player.getAttributeValue(AscensionAttributes.HEALTH_REGEN_RATE));
        if (rate > 0.0D) {
            player.heal((float) Math.min(rate / TICKS_PER_SECOND, maximum - current));
        }
    }

    private static void tickQi(ServerPlayer player) {
        EntityQiProvider provider = player.getCapability(CoreCapabilities.ASCENSION_ENTITY_QI_PROVIDER);
        if (provider == null || !player.getAttributes().hasAttribute(AscensionAttributes.QI_REGEN_RATE)) {
            return;
        }
        double maximum = Math.max(0.0D, provider.getMaxQi());
        double current = Math.max(0.0D, provider.getQi());
        if (current > maximum) {
            provider.setQi(maximum);
            current = maximum;
        }
        if (current >= maximum) {
            return;
        }
        double rate = Math.max(0.0D, player.getAttributeValue(AscensionAttributes.QI_REGEN_RATE));
        if (rate > 0.0D) {
            ResourceTransactionService.restore(
                    player,
                    AscensionResourceTypes.QI.getId(),
                    AscensionResourceSources.NATURAL_REGENERATION,
                    rate / TICKS_PER_SECOND
            );
        }
    }

    private static void tickStamina(ServerPlayer player) {
        clampStamina(player);
        if (player.isSpectator() || player.getAbilities().instabuild) {
            regenerateStamina(player);
            return;
        }
        boolean moved = movedSinceLastTick(player);
        if (moved) {
            BodyCultivationSkillService.stimulate(
                    player,
                    AscensionResourceSources.movementSource(player),
                    1.0D
            );
        }
        MovementCost movement = moved ? movementCost(player) : null;
        if (movement == null) {
            regenerateStamina(player);
            return;
        }
        StaminaService.resetRegenerationDelay(player);
        StaminaService.spendOrDrain(player, movement.source(), movement.amount());
        if (movement.source() == AscensionResourceSources.SPRINTING && StaminaService.getStamina(player) <= 0.0D) {
            player.setSprinting(false);
        }
    }

    private static void spend(ServerPlayer player, ResourceSourceIdentity source, double amount) {
        if (!canSpend(player)) {
            return;
        }
        StaminaService.resetRegenerationDelay(player);
        StaminaService.spendOrDrain(player, source, amount);
    }

    private static boolean canSpend(ServerPlayer player) {
        return player != null && !player.isSpectator() && !player.getAbilities().instabuild;
    }

    private static void clampStamina(ServerPlayer player) {
        double maximum = StaminaService.getMaximumStamina(player);
        if (StaminaService.getStoredStamina(player) > maximum) {
            StaminaService.setStamina(player, maximum);
        }
    }

    private static void regenerateStamina(ServerPlayer player) {
        int delay = StaminaService.getRemainingRegenerationDelay(player);
        if (delay > 0) {
            StaminaService.setRemainingRegenerationDelay(player, delay - 1);
            return;
        }
        double current = StaminaService.getStamina(player);
        double maximum = StaminaService.getMaximumStamina(player);
        if (current >= maximum) {
            return;
        }
        double rate = StaminaService.getEffectiveRegenerationRate(player);
        if (rate > 0.0D) {
            StaminaService.restore(player, AscensionResourceSources.NATURAL_REGENERATION, rate / TICKS_PER_SECOND);
        }
    }

    /**
     * Whether the player really moved since the previous tick. Server-side delta movement can't be used: it never holds
     * walking or sprinting (the client moves the player) and always holds gravity, so standing still looked like moving.
     */
    private static boolean movedSinceLastTick(ServerPlayer player) {
        Vec3 current = player.position();
        Vec3 previous = LAST_POSITIONS.put(player.getUUID(), current);
        if (previous == null || player.isPassenger()) {
            return false;
        }
        double distanceSqr = current.distanceToSqr(previous);
        // Teleports, pearls and dimension changes aren't movement the player made.
        boolean moved = distanceSqr > MIN_MOVEMENT_SQR && distanceSqr < MAX_MOVEMENT_SQR;
        // TEMP DEBUG: checks the position-based movement check. Remove once confirmed.
        if (player.tickCount % 20 == 0) {
            MovementCost cost = moved ? movementCost(player) : null;
            AscensionCraft.LOGGER.info(
                    "[MovementDebug] {} distance={} moved={} source={} cost={} stamina={}",
                    player.getName().getString(),
                    String.format("%.4f", Math.sqrt(distanceSqr)),
                    moved,
                    AscensionResourceSources.movementSource(player).id(),
                    cost == null ? "none" : cost.source().id() + "@" + cost.amount(),
                    String.format("%.2f", StaminaService.getStamina(player))
            );
        }
        return moved;
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        LAST_POSITIONS.remove(event.getEntity().getUUID());
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        LAST_POSITIONS.clear();
    }

    private static MovementCost movementCost(ServerPlayer player) {
        if (player.isFallFlying()) {
            return new MovementCost(AscensionResourceSources.ELYTRA, ELYTRA_COST);
        }
        if (player.isSwimming()) {
            return new MovementCost(AscensionResourceSources.SWIMMING, SWIMMING_COST);
        }
        if (player.onClimbable()) {
            return new MovementCost(AscensionResourceSources.CLIMBING, CLIMBING_COST);
        }
        if (player.isVisuallyCrawling()) {
            return new MovementCost(AscensionResourceSources.CRAWLING, CRAWLING_COST);
        }
        if (player.isSprinting()) {
            return new MovementCost(AscensionResourceSources.SPRINTING, SPRINTING_COST);
        }
        return null;
    }

    private record MovementCost(ResourceSourceIdentity source, double amount) {
    }
}
