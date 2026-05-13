package net.thejadeproject.ascension.refactor_packages.events.physiques;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
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
import net.thejadeproject.ascension.refactor_packages.techniques.ModTechniques;
import net.thejadeproject.ascension.refactor_packages.util.PhysiqueEvolutionEventUtil;

@EventBusSubscriber(modid = AscensionCraft.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public final class YinYangEyesEvolutionEvents {

    private YinYangEyesEvolutionEvents() {}

    private static final String TAG_ROOT = "ascension_yin_yang_evolution";
    private static final String TAG_TIMER = "evolution_timer";

    private static final long TICKS_PER_MINUTE = 1200L;
    private static final long TICKS_REQUIRED = 20L * TICKS_PER_MINUTE; // 20 minutes

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (player.level().isClientSide()) return;
        if (player.tickCount % TICKS_PER_MINUTE != 0) return;
        if (!player.hasData(ModAttachments.ENTITY_DATA)) return;

        IEntityData entityData = player.getData(ModAttachments.ENTITY_DATA);

        boolean hasYinEyes = PhysiqueEvolutionEventUtil.hasPhysique(entityData, ModPhysiques.YIN_EYES.getId());
        boolean hasYangEyes = PhysiqueEvolutionEventUtil.hasPhysique(entityData, ModPhysiques.YANG_EYES.getId());

        if (!hasYinEyes && !hasYangEyes) return;

        PathData soulData = entityData.getPathData(ModPaths.SOUL.getId());
        if (soulData == null) return;

        ResourceLocation currentTechnique = entityData.getTechnique(ModPaths.SOUL.getId());
        boolean hasRightTechnique = hasYinEyes
                ? isSunTechnique(currentTechnique)
                : isMoonTechnique(currentTechnique);
        boolean hasRealm = soulData.getMajorRealm() >= 1;

        if (hasRightTechnique && hasRealm) {
            CompoundTag tag = getTag(player);
            long timer = tag.getLong(TAG_TIMER);
            timer += TICKS_PER_MINUTE;
            tag.putLong(TAG_TIMER, timer);

            if (timer >= TICKS_REQUIRED) {
                PhysiqueEvolutionHelper.tryEvolveInto(player, entityData, ModPhysiques.YIN_YANG_EYES.getId());
                clearTag(player);
            }
        } else {
            clearTag(player);
        }
    }

    @SubscribeEvent
    public static void onPhysiqueChange(PhysiqueChangeEvent.Post event) {
        if (!(event.entityData.getAttachedEntity() instanceof ServerPlayer player)) return;
        if (!player.getPersistentData().contains(TAG_ROOT)) return;
        ResourceLocation newPhysique = event.newPhysique;
        if (!ModPhysiques.YIN_EYES.getId().equals(newPhysique)
                && !ModPhysiques.YANG_EYES.getId().equals(newPhysique)) {
            clearTag(player);
        }
    }

    private static boolean isSunTechnique(ResourceLocation technique) {
        if (technique == null) return false;
        return ModTechniques.DAWNING_SUN_SCRIPTURE.getId().equals(technique)
                || ModTechniques.ZENITH_SUN_SCRIPTURE.getId().equals(technique);
    }

    private static boolean isMoonTechnique(ResourceLocation technique) {
        if (technique == null) return false;
        return ModTechniques.PALE_MOON_SCRIPTURE.getId().equals(technique)
                || ModTechniques.GIBBOUS_MOON_SCRIPTURE.getId().equals(technique);
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
}
