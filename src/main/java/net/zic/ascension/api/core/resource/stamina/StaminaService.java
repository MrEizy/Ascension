package net.zic.ascension.api.core.resource.stamina;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.core.resource.ResourceTransactionResult;
import net.zic.ascension.api.core.resource.ResourceTransactionStatus;
import net.zic.ascension.api.core.resource.ResourceTransactions;
import net.zic.ascension.api.core.resource.source.ResourceSourceIdentity;
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
        if (entity == null || !entity.getAttributes().hasAttribute(AscensionAttributes.MAX_STAMINA)) {
            return 0.0D;
        }
        return Math.max(0.0D, entity.getAttributeValue(AscensionAttributes.MAX_STAMINA));
    }

    public static double getRegenerationRate(LivingEntity entity) {
        if (entity == null || !entity.getAttributes().hasAttribute(AscensionAttributes.STAMINA_REGEN_RATE)) {
            return 0.0D;
        }
        return Math.max(0.0D, entity.getAttributeValue(AscensionAttributes.STAMINA_REGEN_RATE));
    }

    public static int getRegenerationDelay(LivingEntity entity) {
        if (entity == null || !entity.getAttributes().hasAttribute(AscensionAttributes.STAMINA_REGEN_DELAY)) {
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

    public static ResourceTransactionResult spend(
            LivingEntity entity,
            ResourceSourceIdentity source,
            double amount
    ) {
        return ResourceTransactions.consume(entity, RESOURCE_ID, source, amount);
    }

    public static ResourceTransactionResult spendOrDrain(
            LivingEntity entity,
            ResourceSourceIdentity source,
            double amount
    ) {
        ResourceTransactionResult result = spend(entity, source, amount);
        if (result.status() != ResourceTransactionStatus.REJECTED || getStamina(entity) <= 0.0D) {
            return result;
        }
        return ResourceTransactions.drain(entity, RESOURCE_ID, source, amount);
    }

    public static ResourceTransactionResult restore(
            LivingEntity entity,
            ResourceSourceIdentity source,
            double amount
    ) {
        return ResourceTransactions.restore(entity, RESOURCE_ID, source, amount);
    }
}
