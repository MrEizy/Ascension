package net.zic.ascension.handler;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.ascension.core.resource.source.ResourceSourceIdentity;
import net.zic.ascension.impl.resource.stamina.StaminaRegenerationPolicy;
import net.zic.ascension.impl.resource.stamina.StaminaService;
import net.zic.ascension.impl.resource.AscensionResourceSources;

@EventBusSubscriber(modid = AscensionCraft.MOD_ID)
public final class StaminaTicker {
    public static final double SPRINTING_COST = 0.10D;
    public static final double SWIMMING_COST = 0.14D;
    public static final double ELYTRA_COST = 0.08D;
    public static final double CLIMBING_COST = 0.10D;
    public static final double CRAWLING_COST = 0.06D;
    public static final double JUMPING_COST = 4.0D;
    public static final double ATTACKING_COST = 6.0D;

    private StaminaTicker() {
    }

    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Pre event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        clampToMaximum(player);

        if (player.isSpectator() || player.getAbilities().instabuild) {
            regenerate(player);
            return;
        }

        MovementCost movementCost = resolveMovementCost(player);

        if (movementCost != null) {
            StaminaService.resetRegenerationDelay(player);
            StaminaService.spendOrDrain(player, movementCost.source(), movementCost.amount());

            if (movementCost.source() == AscensionResourceSources.SPRINTING && StaminaService.getStamina(player) <= 0.0D) {
                player.setSprinting(false);
            }

            return;
        }

        regenerate(player);
    }

    public static void spendJumpStamina(ServerPlayer player) {
        if (player == null || player.isSpectator() || player.getAbilities().instabuild) {
            return;
        }

        StaminaService.resetRegenerationDelay(player);
        StaminaService.spendOrDrain(player, AscensionResourceSources.JUMPING, JUMPING_COST);
    }

    public static void spendAttackStamina(ServerPlayer player) {
        if (player == null || player.isSpectator() || player.getAbilities().instabuild) {
            return;
        }

        StaminaService.resetRegenerationDelay(player);
        StaminaService.spendOrDrain(player, AscensionResourceSources.ATTACKING, ATTACKING_COST);
    }

    private static void clampToMaximum(ServerPlayer player) {
        double maximum = StaminaService.getMaximumStamina(player);

        if (StaminaService.getStoredStamina(player) > maximum) {
            StaminaService.setStamina(player, maximum);
        }
    }

    private static void regenerate(ServerPlayer player) {
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

        double rate = StaminaRegenerationPolicy.getRegenerationRate(player);

        if (rate > 0.0D) {
            StaminaService.restore(player, AscensionResourceSources.NATURAL_REGENERATION, rate);
        }
    }

    private static MovementCost resolveMovementCost(ServerPlayer player) {
        Vec3 movement = player.getDeltaMovement();

        if (movement.lengthSqr() <= 1.0E-4D) {
            return null;
        }

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