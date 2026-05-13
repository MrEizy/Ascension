package net.thejadeproject.ascension.mob_cultivation.events;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.thejadeproject.ascension.AscensionCraft;
import net.thejadeproject.ascension.data_attachments.ModAttachments;
import net.thejadeproject.ascension.mob_cultivation.*;
import net.thejadeproject.ascension.refactor_packages.network.client_bound.mob_culti.SyncMobCultivation;

@EventBusSubscriber(modid = AscensionCraft.MOD_ID)
public class MobCultivationEvents {

    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide()) return;
        if (!(event.getEntity() instanceof LivingEntity living)) return;

        initializeRank(living);
    }

    @SubscribeEvent
    public static void onStartTracking(PlayerEvent.StartTracking event) {
        if (!(event.getTarget() instanceof LivingEntity living)) return;
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        MobCultivationData data = living.getData(ModAttachments.MOB_RANK);
        if (data == null) return;

        PacketDistributor.sendToPlayer(
                player,
                new SyncMobCultivation(
                        living.getId(),
                        data.getRealmId(),
                        data.getStage(),
                        data.isInitialized()
                )
        );
    }

//    @SubscribeEvent
//    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
//        if (event.getLevel().isClientSide()) return;
//        if (event.getHand() != InteractionHand.MAIN_HAND) return;
//
//        Player player = event.getEntity();
//        if (!player.isShiftKeyDown()) return;
//        if (!(event.getTarget() instanceof LivingEntity living)) return;
//        if (living instanceof Player) return;
//
//        if (player.getMainHandItem().isEmpty()) {
//            sendMobRankInfo(player, living, "Current mob stats");
//            return;
//        }
//
//        if (player.getMainHandItem().is(Items.STICK)) {
//            sendMobRankInfo(player, living, "Before applying rank");
//            debugApplyRank(living, "qi_gathering", 3);
//            sendMobRankInfo(player, living, "After applying rank");
//            event.setCanceled(true);
//        }
//
//        if (player.getMainHandItem().is(Items.BONE)) {
//            sendMobRankInfo(player, living, "Before applying rank");
//            debugApplyRank(living, "soul_formation", 3);
//            sendMobRankInfo(player, living, "After applying rank");
//            event.setCanceled(true);
//        }
//
//        if (player.getMainHandItem().is(Items.END_ROD)) {
//            sendMobRankInfo(player, living, "Before applying rank");
//            debugApplyRank(living, "earth_immortal", 3);
//            sendMobRankInfo(player, living, "After applying rank");
//            event.setCanceled(true);
//        }
//    }
//
//    public static void debugApplyRank(LivingEntity entity, String realmId, int stage) {
//        MobRankData data = entity.getData(ModAttachments.MOB_RANK);
//        if (data == null) return;
//
//        data.setRealmId(realmId);
//        data.setStage(stage);
//        data.setInitialized(true);
//
//        MobRankApplier.applyFromData(entity, data);
//        syncMobRanks(entity);
//    }

    public static void initializeRank(LivingEntity entity) {
        if (!MobCultivationRoller.canHaveRank(entity)) return;

        MobCultivationData data = entity.getData(ModAttachments.MOB_RANK);
        if (data == null || data.isInitialized()) return;

        MobCultivationDefinition definition = MobCultivationResolver.resolveAroundNearbyPlayer(entity);
        if (definition == null) {
            definition = MobCultivationRoller.rollRank(entity);
        }

        data.setRealmId(definition.realmId());
        data.setStage(definition.stage());
        data.setInitialized(true);

        MobCultivationApplier.applyRank(entity, definition);
        syncMobRanks(entity);
    }

    public static void reapplyRank(LivingEntity entity) {
        if (!MobCultivationRoller.canHaveRank(entity)) return;

        MobCultivationData data = entity.getData(ModAttachments.MOB_RANK);
        if (data == null || !data.isInitialized()) return;

        MobCultivationApplier.applyFromData(entity, data);
        syncMobRanks(entity);
    }

//    private static void sendMobRankInfo(Player player, LivingEntity entity, String header) {
//        MobRankData data = entity.getData(ModAttachments.MOB_RANK);
//
//        String realm = data != null ? data.getRealmId() : "null";
//        int stage = data != null ? data.getStage() : -1;
//
//        MobRankDefinition def = data != null && data.isInitialized()
//                ? MobRankResolver.resolveDefinition(data)
//                : null;
//
//        MobRankStatProfile base = def != null ? def.baseStats() : new MobRankStatProfile(0, 0, 0);
//        MobBodyStatBias bias = def != null ? MobRankResolver.resolveBodyBias(entity) : MobBodyStatBias.NONE;
//        MobRankStatProfile finalStats = base.add(bias.asProfile());
//
//        MobRankCategory category = MobRankResolver.resolveCategory(entity);
//
//        double maxHealth = entity.getMaxHealth();
//        double attackDamage = entity.getAttribute(Attributes.ATTACK_DAMAGE) != null
//                ? entity.getAttributeValue(Attributes.ATTACK_DAMAGE) : -1;
//        double movementSpeed = entity.getAttribute(Attributes.MOVEMENT_SPEED) != null
//                ? entity.getAttributeValue(Attributes.MOVEMENT_SPEED) : -1;
//
//        double armor = entity.getAttribute(Attributes.ARMOR) != null
//                ? entity.getAttributeValue(Attributes.ARMOR) : -1;
//        double armorToughness = entity.getAttribute(Attributes.ARMOR_TOUGHNESS) != null
//                ? entity.getAttributeValue(Attributes.ARMOR_TOUGHNESS) : -1;
//
//        double waterMove = entity.getAttribute(Attributes.WATER_MOVEMENT_EFFICIENCY) != null
//                ? entity.getAttributeValue(Attributes.WATER_MOVEMENT_EFFICIENCY) : -1;
//        double safeFall = entity.getAttribute(Attributes.SAFE_FALL_DISTANCE) != null
//                ? entity.getAttributeValue(Attributes.SAFE_FALL_DISTANCE) : -1;
//
//
//        player.sendSystemMessage(Component.literal(
//                header + " | " +
//                        entity.getName().getString() + " | " +
//                        "[" + category + "] " +
//                        realm + " " + stage +
//                        " || Base[V:" + fmt(base.vitality()) + " S:" + fmt(base.strength()) + " A:" + fmt(base.agility()) + "]" +
//                        " + Bias[V:" + fmt(bias.vitality()) + " S:" + fmt(bias.strength()) + " A:" + fmt(bias.agility()) + "]" +
//                        " = Final[V:" + fmt(finalStats.vitality()) + " S:" + fmt(finalStats.strength()) + " A:" + fmt(finalStats.agility()) + "]"
//        ));
//
//        player.sendSystemMessage(Component.literal(
//                " → HP:" + fmt(maxHealth) +
//                        " DMG:" + (attackDamage >= 0 ? fmt(attackDamage) : "N/A") +
//                        " SPD:" + (movementSpeed >= 0 ? fmt(movementSpeed) : "N/A") +
//                        " ARM:" + (armor >= 0 ? fmt(armor) : "N/A") +
//                        " TGH:" + (armorToughness >= 0 ? fmt(armorToughness) : "N/A") +
//                        " WTR:" + (waterMove >= 0 ? fmt(waterMove) : "N/A") +
//                        " FALL:" + (safeFall >= 0 ? fmt(safeFall) : "N/A")
//        ));
//
//    }

    private static String fmt(double value) {
        return String.format("%.1f", value);
    }

    private static void syncMobRanks(LivingEntity entity) {
        if (entity.level().isClientSide()) return;

        MobCultivationData data = entity.getData(ModAttachments.MOB_RANK);
        if (data == null) return;

        PacketDistributor.sendToPlayersTrackingEntity(
                entity,
                new SyncMobCultivation(
                        entity.getId(),
                        data.getRealmId(),
                        data.getStage(),
                        data.isInitialized()
                )
        );
    }

}