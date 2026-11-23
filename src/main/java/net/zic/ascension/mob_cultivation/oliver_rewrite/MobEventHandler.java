package net.zic.ascension.mob_cultivation.oliver_rewrite;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.FinalizeSpawnEvent;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.ascension.capabilities.AscensionEntityDataProvider;
import net.zic.ascension.api.ascension.capabilities.CoreCapabilities;
import net.zic.ascension.api.rpg_engine.source.OriginSourceEvent;
import net.zic.ascension.common.data_attachements.AscensionAttachments;

@EventBusSubscriber(modid = AscensionCraft.MOD_ID)
public class MobEventHandler {
    private static final ThreadLocal<Mob> INITIALIZING_MOB = new ThreadLocal<>();

    @SubscribeEvent
    public static void onEntitySpawn(FinalizeSpawnEvent event) {
        if (event.getEntity().level().isClientSide()) return;
        MobSpawnHelper.buildConfigurationInstance(event.getEntity());
    }

    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide() || !(event.getEntity() instanceof Mob mob)) return;
        if (!MobSpawnHelper.hasConfigHolder(mob)) {
            MobSpawnHelper.buildConfigurationInstance(mob);
        }
        if (!MobSpawnHelper.hasConfigHolder(mob)) return;

        MobConfigurationHolder configuration = MobSpawnHelper.getConfigHolder(mob);
        configuration.applyConfigurationToEntity();

        AscensionEntityDataProvider provider = mob.getCapability(CoreCapabilities.ASCENSION_ENTITY_DATA_PROVIDER_CAPABILITY);
        if (provider != null) {
            var data = provider.getData();
            if (data != null && data.getSource() != null) {
                if (data.getSource().isAttachedTo(mob)) {
                    configuration.applyConfigurationToSource();
                } else {
                    Mob previous = INITIALIZING_MOB.get();
                    INITIALIZING_MOB.set(mob);
                    try {
                        data.initialize();
                    } finally {
                        if (previous == null) INITIALIZING_MOB.remove();
                        else INITIALIZING_MOB.set(previous);
                    }
                }
            }
        }
        configuration.finishSpawnInitialization();
    }

    @SubscribeEvent
    public static void onSourceLoad(OriginSourceEvent.OriginSourceFinishedLoadingEvent event) {
        Mob initializing = INITIALIZING_MOB.get();
        if (initializing != null && MobSpawnHelper.hasConfigHolder(initializing)) {
            AscensionEntityDataProvider provider = initializing.getCapability(CoreCapabilities.ASCENSION_ENTITY_DATA_PROVIDER_CAPABILITY);
            if (provider != null && provider.getData() != null && provider.getData().getSource() == event.getSource()) {
                MobSpawnHelper.getConfigHolder(initializing).applyConfigurationToSource();
            }
        }
        for (LivingEntity entity : event.getSource().getAttachedEntities()) {
            if (!(entity instanceof Mob mob) || !mob.hasData(AscensionAttachments.MOB_CONFIG_HOLDER)) continue;
            MobConfigurationHolder configuration = mob.getData(AscensionAttachments.MOB_CONFIG_HOLDER);
            if (configuration.getSource() == event.getSource()) configuration.applyConfigurationToSource();
        }
    }
}
