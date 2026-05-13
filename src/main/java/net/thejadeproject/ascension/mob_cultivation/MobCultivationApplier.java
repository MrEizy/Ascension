package net.thejadeproject.ascension.mob_cultivation;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.thejadeproject.ascension.AscensionCraft;

public final class MobCultivationApplier {

    // TODO: Fix Skeleton arrow damage not being boosted

    // Base Stats
    private static final ResourceLocation HEALTH_ID =
            ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "mob_rank_health");
    private static final ResourceLocation DAMAGE_ID =
            ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "mob_rank_damage");
    private static final ResourceLocation SPEED_ID =
            ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "mob_rank_speed");
    private static final ResourceLocation SAFE_FALL_ID =
            ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "mob_rank_safe_fall");

    // Hostile and Boss Stats
    private static final ResourceLocation ARMOR_ID =
            ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "mob_rank_armor");
    private static final ResourceLocation ARMOR_TOUGHNESS_ID =
            ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "mob_rank_armor_toughness");
    private static final ResourceLocation WATER_MOVEMENT_ID =
            ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "mob_rank_water_movement_efficiency");


    private MobCultivationApplier() {
    }

    public static void applyFromData(LivingEntity entity, MobCultivationData data) {
        MobCultivationDefinition definition = MobCultivationResolver.resolveDefinition(data);
        applyRank(entity, definition);
    }

    public static void applyRank(LivingEntity entity, MobCultivationDefinition definition) {
        MobCultivationStatProfile finalStats = MobCultivationResolver.resolveFinalStats(entity, definition);
        MobCultivationCategory category = MobCultivationResolver.resolveCategory(entity);

        double healthBonus = AscensionStatConversions.maxHealthBonus(finalStats.vitality());
        double damageBonus = AscensionStatConversions.attackDamageBonus(finalStats.strength());
        double speedBonus = AscensionStatConversions.movementSpeedBonus(finalStats.strength(), finalStats.agility());
        double safeFall = AscensionStatConversions.safeFallBonus(finalStats);

        applyBaseStats(entity, healthBonus, damageBonus, speedBonus, safeFall);

        switch (category) {
            case PASSIVE -> applyPassiveStats(entity, definition, finalStats);
            case HOSTILE -> applyHostileStats(entity, definition, finalStats);
            case BOSS -> applyBossStats(entity, definition, finalStats);
        }

        entity.setHealth(entity.getMaxHealth());
    }


    private static void applyBaseStats(LivingEntity entity, double healthBonus, double damageBonus, double speedBonus, double safeFall) {
        applyAddValue(entity, Attributes.MAX_HEALTH, HEALTH_ID, healthBonus);
        applyAddValue(entity, Attributes.ATTACK_DAMAGE, DAMAGE_ID, damageBonus);
        applyAddValue(entity, Attributes.MOVEMENT_SPEED, SPEED_ID, speedBonus);
        applyAddValue(entity, Attributes.SAFE_FALL_DISTANCE, SAFE_FALL_ID, safeFall);
    }

    private static void applyPassiveStats (LivingEntity entity, MobCultivationDefinition definition, MobCultivationStatProfile finalStats) {
        clearHostileOnlyStats(entity);
    }

    private static void applyHostileStats (LivingEntity entity, MobCultivationDefinition definition, MobCultivationStatProfile finalStats) {
        double armorBonus = AscensionStatConversions.hostileArmorBonus(finalStats);
        double armorToughnessBonus = AscensionStatConversions.hostileArmorToughnessBonus(finalStats);
        double waterMovementBonus = AscensionStatConversions.hostileWaterMovementBonus(finalStats);

        applyAddValue(entity, Attributes.ARMOR, ARMOR_ID, armorBonus);
        applyAddValue(entity, Attributes.ARMOR_TOUGHNESS, ARMOR_TOUGHNESS_ID, armorToughnessBonus);
        applyAddValue(entity, Attributes.WATER_MOVEMENT_EFFICIENCY, WATER_MOVEMENT_ID, waterMovementBonus);

    }

    private static void applyBossStats (LivingEntity entity, MobCultivationDefinition definition, MobCultivationStatProfile finalStats) {
        // Currently same stats as Hostile but 100% higher
        double armorBonus = AscensionStatConversions.hostileArmorBonus(finalStats);
        double armorToughnessBonus = AscensionStatConversions.hostileArmorToughnessBonus(finalStats);
        double waterMovementBonus = AscensionStatConversions.hostileWaterMovementBonus(finalStats);

        applyAddValue(entity, Attributes.ARMOR, ARMOR_ID, armorBonus*2);
        applyAddValue(entity, Attributes.ARMOR_TOUGHNESS, ARMOR_TOUGHNESS_ID, armorToughnessBonus*2);
        applyAddValue(entity, Attributes.WATER_MOVEMENT_EFFICIENCY, WATER_MOVEMENT_ID, waterMovementBonus*2);
    }


    private static void applyAddValue(LivingEntity entity,
                                      Holder<Attribute> attribute,
                                      ResourceLocation id,
                                      double amount) {
        AttributeInstance instance = entity.getAttribute(attribute);
        if (instance == null) return;

        instance.removeModifier(id);
        if (amount != 0) {
            instance.addPermanentModifier(new AttributeModifier(id, amount, AttributeModifier.Operation.ADD_VALUE));
        }
    }

    private static void removeModifier(LivingEntity entity,
                                       Holder<Attribute> attribute,
                                       ResourceLocation id) {
        AttributeInstance instance = entity.getAttribute(attribute);
        if (instance == null) return;

        instance.removeModifier(id);
    }

    private static void clearHostileOnlyStats(LivingEntity entity) {
        removeModifier(entity, Attributes.ARMOR, ARMOR_ID);
        removeModifier(entity, Attributes.ARMOR_TOUGHNESS, ARMOR_TOUGHNESS_ID);
        removeModifier(entity, Attributes.WATER_MOVEMENT_EFFICIENCY, WATER_MOVEMENT_ID);
    }

}