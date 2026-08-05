package net.zic.ascension.impl.resource.stamina;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.ascension.core.resource.ResourceTransactionService;
import net.zic.ascension.api.ascension.core.resource.ResourceSourceIdentity;
import net.zic.ascension.common.data_attachements.AscensionAttachments;
import net.zic.ascension.common.util.AscensionAttributes;

public final class StaminaService {
    public static final Identifier RESOURCE_ID = AscensionCraft.prefix("stamina");

    private StaminaService() {
    }

    public static double getStamina(LivingEntity entity) {
        if (entity == null) {
            return 0.0D;
        }
        return Math.clamp(getStoredStamina(entity), 0.0D, getMaximumStamina(entity));
    }

    public static double getStoredStamina(LivingEntity entity) {
        if (entity == null) {
            return 0.0D;
        }
        return Math.max(0.0D, entity.getData(AscensionAttachments.ENTITY_STAMINA));
    }

    public static double getMaximumStamina(LivingEntity entity) {
        if (entity == null) {
            return 0.0D;
        }

        return Math.max(0.0D, entity.getAttributeValue(AscensionAttributes.MAX_STAMINA));
    }


    public static double getRegenerationRate(LivingEntity entity) {
        if (entity == null) {
            return 0.0D;
        }
        return Math.max(0.0D, entity.getAttributeValue(AscensionAttributes.STAMINA_REGEN_RATE));
    }


    public static double getEffectiveRegenerationRate(LivingEntity entity) {
        if (!(entity instanceof Player player)) {
            return 0.0D;
        }
        FoodData food = player.getFoodData();
        double multiplier;
        if (food.getSaturationLevel() > 0.0F) {
            multiplier = 1.10D;
        } else if (food.getFoodLevel() >= 12) {
            multiplier = 1.00D;
        } else if (food.getFoodLevel() >= 6) {
            multiplier = 0.70D;
        } else if (food.getFoodLevel() > 0) {
            multiplier = 0.35D;
        } else {
            multiplier = 0.0D;
        }
        return getRegenerationRate(entity) * multiplier;
    }

    public static int getRegenerationDelay(LivingEntity entity) {
        if (entity == null) {
            return 0;
        }
        return Math.max(0, (int) Math.round(entity.getAttributeValue(AscensionAttributes.STAMINA_REGEN_DELAY)));
    }

    public static int getRemainingRegenerationDelay(LivingEntity entity) {
        if (entity == null) {
            return 0;
        }
        return Math.max(0, entity.getData(AscensionAttachments.ENTITY_STAMINA_REGEN_DELAY));
    }

    public static void setRemainingRegenerationDelay(LivingEntity entity, int ticks) {
        if (entity == null || entity.level().isClientSide()) {
            return;
        }
        entity.setData(AscensionAttachments.ENTITY_STAMINA_REGEN_DELAY, Math.max(0, ticks));
    }

    public static void resetRegenerationDelay(LivingEntity entity) {
        setRemainingRegenerationDelay(entity, getRegenerationDelay(entity));
    }

    public static void setStamina(LivingEntity entity, double amount) {
        if (entity == null || entity.level().isClientSide()) {
            return;
        }
        entity.setData(
                AscensionAttachments.ENTITY_STAMINA,
                Math.clamp(amount, 0.0D, getMaximumStamina(entity))
        );
    }

    public static ResourceTransactionService.Result spend(
            LivingEntity entity,
            ResourceSourceIdentity source,
            double amount
    ) {
        return ResourceTransactionService.consume(entity, RESOURCE_ID, source, amount);
    }

    public static ResourceTransactionService.Result spendOrDrain(
            LivingEntity entity,
            ResourceSourceIdentity source,
            double amount
    ) {
        ResourceTransactionService.Result result = spend(entity, source, amount);
        if (result.status() != ResourceTransactionService.Status.REJECTED || getStamina(entity) <= 0.0D) {
            return result;
        }
        return ResourceTransactionService.drain(entity, RESOURCE_ID, source, amount);
    }

    public static ResourceTransactionService.Result restore(
            LivingEntity entity,
            ResourceSourceIdentity source,
            double amount
    ) {
        return ResourceTransactionService.restore(entity, RESOURCE_ID, source, amount);
    }
}
