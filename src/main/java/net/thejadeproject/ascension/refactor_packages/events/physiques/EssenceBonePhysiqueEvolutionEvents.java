package net.thejadeproject.ascension.refactor_packages.events.physiques;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.thejadeproject.ascension.AscensionCraft;
import net.thejadeproject.ascension.data_attachments.ModAttachments;
import net.thejadeproject.ascension.refactor_packages.entity_data.IEntityData;
import net.thejadeproject.ascension.refactor_packages.events.PhysiqueChangeEvent;
import net.thejadeproject.ascension.refactor_packages.paths.ModPaths;
import net.thejadeproject.ascension.refactor_packages.paths.PathData;
import net.thejadeproject.ascension.refactor_packages.physiques.ModPhysiques;
import net.thejadeproject.ascension.refactor_packages.physiques.custom.helpers.PhysiqueEvolutionHelper;
import net.thejadeproject.ascension.refactor_packages.util.PhysiqueEvolutionEventUtil;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.event.entity.EntityStruckByLightningEvent;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;

import net.thejadeproject.ascension.common.blocks.ModBlocks;
import net.thejadeproject.ascension.common.items.ModItems;

@EventBusSubscriber(modid = AscensionCraft.MOD_ID)
public final class EssenceBonePhysiqueEvolutionEvents {
    private EssenceBonePhysiqueEvolutionEvents() {}

    private enum EssenceBoneRitual {
        MORTAL_TO_SPECIAL,
        SPECIAL_TO_HEAVENLY,
        HEAVENLY_TO_DIVINE
    }

    private static final String TAG_ROOT = "ascension_essence_bone_evolution";
    private static final String TAG_PURIFICATION_TIMER = "purification_timer";
    private static final String TAG_DIVINE_LIGHTNING_MARKED = "divine_lightning_marked";
    private static final String TAG_DIVINE_PILL_FUEL = "divine_pill_fuel";
    private static final String TAG_LAST_NO_FUEL_MESSAGE_TICK = "last_no_fuel_message_tick";


    private static final long TICKS_PER_SECOND = 20L;
    private static final long TICKS_PER_MINUTE = 1200L;

    private static final long SPECIAL_REQUIRED_TIME = 15L * TICKS_PER_MINUTE;
    private static final long HEAVENLY_REQUIRED_TIME = 35L * TICKS_PER_MINUTE;
    private static final long DIVINE_REQUIRED_TIME = 60L * TICKS_PER_MINUTE;

    private static final long DIVINE_FUEL_PER_PILL = 10L * TICKS_PER_MINUTE;
    private static final long MAX_DIVINE_PILL_FUEL = 30L * TICKS_PER_MINUTE;
    private static final long NO_FUEL_MESSAGE_COOLDOWN = 10L * TICKS_PER_SECOND;

    // update these to be more spread our as more realms are added
    private static final int SPECIAL_REQUIRED_ESSENCE_MAJOR_REALM = 1;
    private static final int HEAVENLY_REQUIRED_ESSENCE_MAJOR_REALM = 3;
    private static final int DIVINE_REQUIRED_ESSENCE_MAJOR_REALM = 4;

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (player.level().isClientSide()) return;
        if (player.tickCount % TICKS_PER_SECOND != 0) return;
        if (!player.hasData(ModAttachments.ENTITY_DATA)) return;

        IEntityData entityData = player.getData(ModAttachments.ENTITY_DATA);

        if (PhysiqueEvolutionEventUtil.hasPhysique(entityData, ModPhysiques.ESSENCE_BONE_MORTAL.getId())) {
            tickPurification(
                    player,
                    entityData,
                    SPECIAL_REQUIRED_TIME,
                    SPECIAL_REQUIRED_ESSENCE_MAJOR_REALM,
                    ModPhysiques.ESSENCE_BONE_SPECIAL.getId(),
                    EssenceBoneRitual.MORTAL_TO_SPECIAL
            );
            return;
        }

        if (PhysiqueEvolutionEventUtil.hasPhysique(entityData, ModPhysiques.ESSENCE_BONE_SPECIAL.getId())) {
            tickPurification(
                    player,
                    entityData,
                    HEAVENLY_REQUIRED_TIME,
                    HEAVENLY_REQUIRED_ESSENCE_MAJOR_REALM,
                    ModPhysiques.ESSENCE_BONE_HEAVENLY.getId(),
                    EssenceBoneRitual.SPECIAL_TO_HEAVENLY
            );
            return;
        }

        if (PhysiqueEvolutionEventUtil.hasPhysique(entityData, ModPhysiques.ESSENCE_BONE_HEAVENLY.getId())) {
            tickPurification(
                    player,
                    entityData,
                    DIVINE_REQUIRED_TIME,
                    DIVINE_REQUIRED_ESSENCE_MAJOR_REALM,
                    ModPhysiques.ESSENCE_BONE_DIVINE.getId(),
                    EssenceBoneRitual.HEAVENLY_TO_DIVINE
            );
            return;
        }

        clearTag(player);
    }

    @SubscribeEvent
    public static void onPhysiqueChange(PhysiqueChangeEvent.Post event) {
        if (!(event.entityData.getAttachedEntity() instanceof ServerPlayer player)) return;

        if (!ModPhysiques.ESSENCE_BONE_MORTAL.getId().equals(event.newPhysique)
                && !ModPhysiques.ESSENCE_BONE_SPECIAL.getId().equals(event.newPhysique)
                && !ModPhysiques.ESSENCE_BONE_HEAVENLY.getId().equals(event.newPhysique)) {
            clearTag(player);
        }
    }

    private static void tickPurification(
            ServerPlayer player,
            IEntityData entityData,
            long requiredTime,
            int requiredEssenceMajorRealm,
            ResourceLocation targetPhysique,
            EssenceBoneRitual ritual
    ) {
        PathData essenceData = entityData.getPathData(ModPaths.ESSENCE.getId());

        if (essenceData == null || essenceData.getMajorRealm() < requiredEssenceMajorRealm) {
            clearTimer(player);
            return;
        }

        if (!canProgressRitual(player, ritual)) {
            clearTimer(player);
            return;
        }

        CompoundTag tag = getTag(player);

        if (ritual == EssenceBoneRitual.HEAVENLY_TO_DIVINE) {
            long fuel = tag.getLong(TAG_DIVINE_PILL_FUEL);

            if (fuel <= 0L) {
                long lastMessageTick = tag.getLong(TAG_LAST_NO_FUEL_MESSAGE_TICK);

                if (player.tickCount - lastMessageTick >= NO_FUEL_MESSAGE_COOLDOWN) {
                    tag.putLong(TAG_LAST_NO_FUEL_MESSAGE_TICK, player.tickCount);

                    sendActionBar(player, Component.translatable(
                            "ascension.message.physique_evolution.essence_bone_needs_pill_fuel"
                    ));
                }

                return;
            }

            tag.putLong(TAG_DIVINE_PILL_FUEL, Math.max(0L, fuel - TICKS_PER_SECOND));
        }

        long timer = tag.getLong(TAG_PURIFICATION_TIMER) + TICKS_PER_SECOND;
        tag.putLong(TAG_PURIFICATION_TIMER, timer);

        if (timer % TICKS_PER_MINUTE == 0 && shouldSendPurificationProgress(timer, requiredTime)) {
            sendActionBar(player, Component.translatable(
                    "ascension.message.physique_evolution.essence_bone_purifying",
                    timer / TICKS_PER_MINUTE,
                    requiredTime / TICKS_PER_MINUTE
            ));
        }

        if (timer < requiredTime) return;

        boolean evolved = PhysiqueEvolutionHelper.tryEvolveInto(
                player,
                entityData,
                targetPhysique
        );

        if (evolved) {
            clearTag(player);
        }
    }

    private static boolean canProgressRitual(ServerPlayer player, EssenceBoneRitual ritual) {
        return switch (ritual) {
            case MORTAL_TO_SPECIAL -> isSkyPurifying(player);
            case SPECIAL_TO_HEAVENLY -> isCrimsonLotusTempering(player);
            case HEAVENLY_TO_DIVINE -> isDivineRefining(player);
        };
    }

    private static boolean isSkyPurifying(ServerPlayer player) {
        Level level = player.level();

        boolean underOpenSky = level.canSeeSky(player.blockPosition());
        boolean dayTime = level.isDay();
        boolean clearWeather = !level.isRaining() && !level.isThundering();

        return underOpenSky && dayTime && clearWeather && hasNoImpurityEffects(player);
    }

    private static boolean isCrimsonLotusTempering(ServerPlayer player) {
        return isInCrimsonLotusFlame(player) && hasNoImpurityEffects(player);
    }

    private static boolean isDivineRefining(ServerPlayer player) {
        CompoundTag tag = getTag(player);

        return tag.getBoolean(TAG_DIVINE_LIGHTNING_MARKED)
                && isInCrimsonLotusFlame(player)
                && hasNoImpurityEffects(player);
    }

    private static boolean hasNoImpurityEffects(ServerPlayer player) {
        return !player.hasEffect(MobEffects.POISON)
                && !player.hasEffect(MobEffects.WITHER)
                && !player.hasEffect(MobEffects.HUNGER)
                && !player.hasEffect(MobEffects.WEAKNESS);
    }

    private static boolean isInCrimsonLotusFlame(ServerPlayer player) {
        Block feetBlock = player.level().getBlockState(player.blockPosition()).getBlock();
        Block belowBlock = player.level().getBlockState(player.blockPosition().below()).getBlock();

        return feetBlock == ModBlocks.CRIMSON_LOTUS_FIRE.get()
                || belowBlock == ModBlocks.CRIMSON_LOTUS_FIRE.get();
    }

    private static CompoundTag getTag(ServerPlayer player) {
        CompoundTag data = player.getPersistentData();

        if (!data.contains(TAG_ROOT, Tag.TAG_COMPOUND)) {
            data.put(TAG_ROOT, new CompoundTag());
        }

        return data.getCompound(TAG_ROOT);
    }

    @SubscribeEvent
    public static void onStruckByLightning(EntityStruckByLightningEvent event) {
        Entity entity = event.getEntity();

        if (!(entity instanceof ServerPlayer player)) return;
        if (player.level().isClientSide()) return;
        if (!player.hasData(ModAttachments.ENTITY_DATA)) return;

        IEntityData entityData = player.getData(ModAttachments.ENTITY_DATA);

        if (!PhysiqueEvolutionEventUtil.hasPhysique(entityData, ModPhysiques.ESSENCE_BONE_HEAVENLY.getId())) {
            return;
        }

        PathData essenceData = entityData.getPathData(ModPaths.ESSENCE.getId());

        if (essenceData == null || essenceData.getMajorRealm() < DIVINE_REQUIRED_ESSENCE_MAJOR_REALM) {
            return;
        }

        CompoundTag tag = getTag(player);
        tag.putBoolean(TAG_DIVINE_LIGHTNING_MARKED, true);
        tag.putLong(TAG_PURIFICATION_TIMER, 0L);
        tag.putLong(TAG_DIVINE_PILL_FUEL, 0L);

        sendActionBar(player, Component.translatable(
                "ascension.message.physique_evolution.essence_bone_divine_marked"
        ));
    }

    @SubscribeEvent
    public static void onFinishUsingItem(LivingEntityUseItemEvent.Finish event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (player.level().isClientSide()) return;
        if (!player.hasData(ModAttachments.ENTITY_DATA)) return;

        IEntityData entityData = player.getData(ModAttachments.ENTITY_DATA);

        if (!PhysiqueEvolutionEventUtil.hasPhysique(entityData, ModPhysiques.ESSENCE_BONE_HEAVENLY.getId())) {
            return;
        }

        CompoundTag tag = getTag(player);

        if (!tag.getBoolean(TAG_DIVINE_LIGHTNING_MARKED)) {
            return;
        }

        ItemStack stack = event.getItem();

        if (!isDivineRefinementPill(stack)) {
            return;
        }

        long fuel = tag.getLong(TAG_DIVINE_PILL_FUEL);
        long newFuel = Math.min(MAX_DIVINE_PILL_FUEL, fuel + DIVINE_FUEL_PER_PILL);

        tag.putLong(TAG_DIVINE_PILL_FUEL, newFuel);

        sendActionBar(player, Component.translatable(
                "ascension.message.physique_evolution.essence_bone_pill_fuel",
                newFuel / TICKS_PER_MINUTE,
                MAX_DIVINE_PILL_FUEL / TICKS_PER_MINUTE
        ));
    }

    private static boolean isDivineRefinementPill(ItemStack stack) {
        return stack.is(ModItems.CRIMSON_LOTUS_BONE_PILL.get());
    }

    private static void clearTimer(ServerPlayer player) {
        getTag(player).remove(TAG_PURIFICATION_TIMER);
    }

    private static void clearTag(ServerPlayer player) {
        player.getPersistentData().remove(TAG_ROOT);
    }

    private static void sendActionBar(ServerPlayer player, Component message) {
        player.displayClientMessage(message, true);
    }

    private static boolean shouldSendPurificationProgress(long timer, long requiredTime) {
        long minute = timer / TICKS_PER_MINUTE;
        long requiredMinutes = requiredTime / TICKS_PER_MINUTE;

        return minute == 1
                || timer >= requiredTime
                || minute % 10 == 0
                || requiredMinutes - minute <= 5;
    }

}