package net.zic.ascension.handler;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.event.level.SleepFinishedTimeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.ascension.capabilities.CoreCapabilities;
import net.zic.ascension.api.ascension.capabilities.EntityQiProvider;
import net.zic.ascension.api.ascension.core.resource.ResourceSourceIdentity;
import net.zic.ascension.common.util.AscensionAttributes;
import net.zic.ascension.api.ascension.core.attribute.RecoveryScaling;
import net.zic.ascension.impl.core.attribute.ScaledAttributes;
import net.zic.zenithlib.common.ZenithAttachments;
import net.zic.zenithlib.value_containers.typed.Modifier;
import net.minecraft.resources.Identifier;
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
    private static final Identifier HEALTH_RECOVERY_MODIFIER = AscensionCraft.prefix("capacity_health_recovery");
    private static final String RECOVERY_PROCESS = "ascension_capacity_health_recovery";
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
        if (event.getEntity() instanceof ServerPlayer player) {
            tickHealth(player);
            tickQi(player);
            tickStamina(player);
        } else if (event.getEntity() instanceof Mob mob && !mob.level().isClientSide() && mob.isAlive()) {
            tickQi(mob);
        }
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
        refreshHealthRecovery(player, maximum);
        if (current >= maximum || (maximum <= VANILLA_HEALTH_THRESHOLD && current < maximum)) {
            return;
        }
        double rate = Math.max(0.0D, player.getAttributeValue(AscensionAttributes.HEALTH_REGEN_RATE));
        if (Double.isFinite(rate) && rate > 0.0D) {
            player.heal((float) Math.min(rate / TICKS_PER_SECOND, maximum - current));
        }
    }

    private static RecoveryScaling recoveryProfile(ServerPlayer player) {
        var profile = ScaledAttributes.profile(player);
        return profile == null ? RecoveryScaling.DEFAULT : profile.recovery();
    }

    private static void refreshHealthRecovery(ServerPlayer player, float maximum) {
        var holder = player.getData(ZenithAttachments.ATTRIBUTE_HOLDER);
        if (!holder.hasAttribute(AscensionAttributes.HEALTH_REGEN_RATE)) return;
        var attribute = holder.getAttribute(AscensionAttributes.HEALTH_REGEN_RATE);
        if (attribute == null) return;
        double bonus = maximum > VANILLA_HEALTH_THRESHOLD && Float.isFinite(maximum)
                ? recoveryProfile(player).healthPerSecond(maximum) - AscensionAttributes.BASE_HEALTH_REGEN_RATE
                : 0.0D;
        var existing = attribute.getFlatModifier(HEALTH_RECOVERY_MODIFIER);
        if (existing == null ? bonus == 0 : Double.compare(existing.value(), bonus) == 0) return;
        holder.startProcess(RECOVERY_PROCESS);
        try {
            attribute.startProcess(RECOVERY_PROCESS);
            try {
                attribute.removeModifier(HEALTH_RECOVERY_MODIFIER);
                if (bonus != 0) attribute.addFlatModifier(Modifier.flat(HEALTH_RECOVERY_MODIFIER, 0, bonus));
            } finally {
                attribute.resolveProcess(RECOVERY_PROCESS);
            }
        } finally {
            holder.resolveProcess(RECOVERY_PROCESS);
        }
        player.syncData(ZenithAttachments.ATTRIBUTE_HOLDER);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onSleepFinished(SleepFinishedTimeEvent event) {
        if (event.isCanceled() || !(event.getLevel() instanceof ServerLevel level)) return;
        for (ServerPlayer player : level.players()) {
            if (!player.isAlive() || player.isSpectator() || !player.isSleepingLongEnough()) continue;
            float maximum = player.getMaxHealth();
            if (maximum <= VANILLA_HEALTH_THRESHOLD) continue;
            double healing = recoveryProfile(player).sleepHealing(maximum, player.getHealth());
            if (healing > 0) player.heal((float) healing);
        }
    }

    private static void tickQi(LivingEntity entity) {
        EntityQiProvider provider = entity.getCapability(CoreCapabilities.ASCENSION_ENTITY_QI_PROVIDER);
        if (provider == null || !entity.getAttributes().hasAttribute(AscensionAttributes.QI_REGEN_RATE)) {
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
        double rate = Math.max(0.0D, entity.getAttributeValue(AscensionAttributes.QI_REGEN_RATE));
        if (rate > 0.0D) {
            ResourceTransactionService.restore(
                    entity,
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
        return distanceSqr > MIN_MOVEMENT_SQR && distanceSqr < MAX_MOVEMENT_SQR;
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
