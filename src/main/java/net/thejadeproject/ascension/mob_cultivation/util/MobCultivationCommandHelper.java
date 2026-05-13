package net.thejadeproject.ascension.mob_cultivation.util;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.network.PacketDistributor;
import net.thejadeproject.ascension.data_attachments.ModAttachments;
import net.thejadeproject.ascension.mob_cultivation.MobCultivationApplier;
import net.thejadeproject.ascension.mob_cultivation.MobCultivationData;
import net.thejadeproject.ascension.mob_cultivation.MobCultivationList;
import net.thejadeproject.ascension.refactor_packages.network.client_bound.mob_culti.SyncMobCultivation;

public final class MobCultivationCommandHelper {
    private MobCultivationCommandHelper() {}

    public static boolean applyCultivation(LivingEntity entity, String realmId, int stage) {
        if (!MobCultivationList.isValidRealm(realmId)) {
            return false;
        }

        MobCultivationData data = entity.getData(ModAttachments.MOB_RANK);
        if (data == null) return false;

        data.setRealmId(realmId);
        data.setStage(Math.max(1, Math.min(3, stage)));
        data.setInitialized(true);

        MobCultivationApplier.applyFromData(entity, data);
        sync(entity);
        return true;
    }

    public static Component getStatsMessage(LivingEntity entity) {
        MobCultivationData data = entity.getData(ModAttachments.MOB_RANK);

        if (data == null || !data.isInitialized()) {
            return Component.literal(entity.getName().getString() + " has no initialized mob cultivation.");
        }

        return Component.literal(
                entity.getName().getString()
                        + " | Realm: " + data.getRealmId()
                        + " | Stage: " + data.getStage()
                        + " | HP: " + String.format("%.1f", entity.getMaxHealth())
        );
    }

    private static void sync(LivingEntity entity) {
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