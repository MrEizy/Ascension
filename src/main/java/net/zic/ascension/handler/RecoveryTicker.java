package net.zic.ascension.handler;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.ascension.capabilities.CoreCapabilities;
import net.zic.ascension.api.ascension.capabilities.EntityQiProvider;
import net.zic.ascension.api.ascension.core.resource.ResourceSourceIdentity;
import net.zic.ascension.common.util.AscensionAttributes;
import net.zic.ascension.impl.resource.AscensionResourceSources;
import net.zic.ascension.impl.resource.AscensionResourceTypes;
import net.zic.ascension.impl.resource.stamina.StaminaService;
import net.zic.ascension.api.ascension.core.resource.ResourceTransactionService;

@EventBusSubscriber(modid = AscensionCraft.MOD_ID)
public final class RecoveryTicker {
    private static final double TICKS_PER_SECOND = 20.0D;
    private static final float VANILLA_HEALTH_THRESHOLD = 20.0F;
    public static final double SPRINTING_COST = 0.10D;
    public static final double SWIMMING_COST = 0.14D;
    public static final double ELYTRA_COST = 0.08D;
    public static final double CLIMBING_COST = 0.10D;
    public static final double CRAWLING_COST = 0.06D;
    public static final double JUMPING_COST = 4.0D;
    public static final double ATTACKING_COST = 6.0D;

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
        spend(player, AscensionResourceSources.JUMPING, JUMPING_COST);
    }

    public static void spendAttackStamina(ServerPlayer player) {
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
        MovementCost movement = movementCost(player);
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
        if (player == null || player.isSpectator() || player.getAbilities().instabuild) {
            return;
        }
        StaminaService.resetRegenerationDelay(player);
        StaminaService.spendOrDrain(player, source, amount);
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

    private static MovementCost movementCost(ServerPlayer player) {
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
