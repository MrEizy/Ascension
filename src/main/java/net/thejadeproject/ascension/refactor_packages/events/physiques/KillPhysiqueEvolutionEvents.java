package net.thejadeproject.ascension.refactor_packages.events.physiques;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.thejadeproject.ascension.AscensionCraft;
import net.thejadeproject.ascension.data_attachments.ModAttachments;
import net.thejadeproject.ascension.refactor_packages.entity_data.IEntityData;
import net.thejadeproject.ascension.refactor_packages.paths.ModPaths;
import net.thejadeproject.ascension.refactor_packages.paths.PathData;
import net.thejadeproject.ascension.refactor_packages.physiques.ModPhysiques;
import net.thejadeproject.ascension.refactor_packages.physiques.custom.helpers.PhysiqueEvolutionHelper;
import net.thejadeproject.ascension.refactor_packages.util.PhysiqueEvolutionEventUtil;

@EventBusSubscriber(modid = AscensionCraft.MOD_ID)
public final class KillPhysiqueEvolutionEvents {
    private KillPhysiqueEvolutionEvents() {}

    private static final String TAG_ROOT = "ascension_blood_physique_evolution";
    private static final String TAG_MORTAL_BLOOD_KILLS = "mortal_blood_kills";
    private static final String TAG_FIEND_BLOOD_KILLS = "fiend_blood_kills";
    private static final String TAG_FIEND_WEAK_SOUL_KILLS = "fiend_weak_soul_kills";

    private static final int BLOOD_FIEND_REQUIRED_KILLS = 20;

    private static final int BLOOD_WRAITH_REQUIRED_SOUL_MAJOR_REALM = 2;
    private static final int BLOOD_WRAITH_REQUIRED_KILLS = 100;

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        LivingEntity victim = event.getEntity();

        if (!(event.getSource().getEntity() instanceof ServerPlayer killer)) return;
        if (victim == killer) return;
        if (killer.level().isClientSide()) return;
        if (!killer.hasData(ModAttachments.ENTITY_DATA)) return;
        if (!isValidBloodKill(victim)) return;

        IEntityData entityData = killer.getData(ModAttachments.ENTITY_DATA);
        CompoundTag tag = getTag(killer);

        if (PhysiqueEvolutionEventUtil.hasPhysique(entityData, ModPhysiques.MORTAL.getId())) {
            int kills = tag.getInt(TAG_MORTAL_BLOOD_KILLS) + 1;
            tag.putInt(TAG_MORTAL_BLOOD_KILLS, kills);

            if (shouldSendKillProgress(kills, BLOOD_FIEND_REQUIRED_KILLS)) {
                sendActionBar(killer, Component.translatable(
                        "ascension.message.physique_evolution.blood_fiend_progress",
                        kills,
                        BLOOD_FIEND_REQUIRED_KILLS
                ));
            }

            if (kills >= BLOOD_FIEND_REQUIRED_KILLS) {
                boolean evolved = PhysiqueEvolutionHelper.tryEvolveInto(
                        killer,
                        entityData,
                        ModPhysiques.BLOOD_FIEND.getId()
                );

                if (evolved) {
                    tag.remove(TAG_MORTAL_BLOOD_KILLS);
                    tag.putInt(TAG_FIEND_BLOOD_KILLS, 0);
                }
            }

            return;
        }

        if (PhysiqueEvolutionEventUtil.hasPhysique(entityData, ModPhysiques.BLOOD_FIEND.getId())) {
            PathData soulData = entityData.getPathData(ModPaths.SOUL.getId());

            if (soulData == null || soulData.getMajorRealm() < BLOOD_WRAITH_REQUIRED_SOUL_MAJOR_REALM) {
                int weakSoulKills = tag.getInt(TAG_FIEND_WEAK_SOUL_KILLS) + 1;
                tag.putInt(TAG_FIEND_WEAK_SOUL_KILLS, weakSoulKills);

                if (weakSoulKills == 1 || weakSoulKills % 10 == 0) {
                    sendActionBar(killer, Component.translatable(
                            "ascension.message.physique_evolution.blood_wraith_soul_too_weak",
                            BLOOD_WRAITH_REQUIRED_SOUL_MAJOR_REALM
                    ));
                }

                return;
            }

            tag.remove(TAG_FIEND_WEAK_SOUL_KILLS);

            int kills = tag.getInt(TAG_FIEND_BLOOD_KILLS) + 1;
            tag.putInt(TAG_FIEND_BLOOD_KILLS, kills);

            if (shouldSendKillProgress(kills, BLOOD_WRAITH_REQUIRED_KILLS)) {
                sendActionBar(killer, Component.translatable(
                        "ascension.message.physique_evolution.blood_wraith_progress",
                        kills,
                        BLOOD_WRAITH_REQUIRED_KILLS
                ));
            }

            if (kills >= BLOOD_WRAITH_REQUIRED_KILLS) {
                boolean evolved = PhysiqueEvolutionHelper.tryEvolveInto(
                        killer,
                        entityData,
                        ModPhysiques.BLOOD_WRAITH.getId()
                );

                if (evolved) {
                    clearTag(killer);
                }
            }

            return;
        }

        clearTag(killer);
    }

    private static boolean isValidBloodKill(LivingEntity victim) {
        return victim instanceof Player || victim instanceof Villager;
    }

    private static CompoundTag getTag(ServerPlayer player) {
        CompoundTag data = player.getPersistentData();

        if (!data.contains(TAG_ROOT, Tag.TAG_COMPOUND)) {
            data.put(TAG_ROOT, new CompoundTag());
        }

        return data.getCompound(TAG_ROOT);
    }

    private static void clearTag(ServerPlayer player) {
        player.getPersistentData().remove(TAG_ROOT);
    }

    private static void sendActionBar(ServerPlayer player, Component message) {
        player.displayClientMessage(message, true);
    }

    private static boolean shouldSendKillProgress(int kills, int requiredKills) {
        return kills == 1
                || kills == requiredKills
                || kills % 10 == 0;
    }

}