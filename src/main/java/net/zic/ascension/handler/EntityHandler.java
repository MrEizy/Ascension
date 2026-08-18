package net.zic.ascension.handler;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.zic.ascension.api.ascension.capabilities.AscensionEntityDataProvider;
import net.zic.ascension.api.ascension.capabilities.CoreCapabilities;
import net.zic.ascension.api.ascension.datapack.path.realm.PathRealmChangeEvent;
import net.zic.ascension.api.ascension.event.path.PathAddedEvent;
import net.zic.ascension.api.ascension.event.path.PathRemovedEvent;
import net.zic.ascension.api.rpg_engine.source.OriginSource;
import net.zic.ascension.common.starter.StarterSelectionManager;
import net.zic.ascension.impl.core.entity.SimpleAscensionEntityData;

@EventBusSubscriber
public class EntityHandler {



    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event){
        AscensionEntityDataProvider holder = event.getEntity().getCapability(CoreCapabilities.ASCENSION_ENTITY_DATA_PROVIDER_CAPABILITY);
        if(holder == null) return;

        holder.getData().initialize();
        if (event.getEntity() instanceof ServerPlayer player) {
            StarterSelectionManager.openIfIncomplete(player);
        }

    }
    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event){
        AscensionEntityDataProvider holder = event.getEntity().getCapability(CoreCapabilities.ASCENSION_ENTITY_DATA_PROVIDER_CAPABILITY);
        if(holder == null) return;

        if(holder.getData() == null) return;

        if(holder.getData().getSource() == null) return;

        holder.getData().getSource().detachFromEntity(event.getEntity());
    }
    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event){
        AscensionEntityDataProvider holder = event.getEntity().getCapability(CoreCapabilities.ASCENSION_ENTITY_DATA_PROVIDER_CAPABILITY);
        if(holder == null) return;

        holder.getData().initializeAfterRespawn();
        if (event.getEntity() instanceof ServerPlayer player) {
            StarterSelectionManager.openIfIncomplete(player);
        }

    }
    @SubscribeEvent
    public static void onPathRealmUp(PathRealmChangeEvent.PathRealmUpEvent event) {
        onPathRealmChange(event);
    }

    @SubscribeEvent
    public static void onPathRealmDown(PathRealmChangeEvent.PathRealmDownEvent event) {
        onPathRealmChange(event);
    }

    private static void onPathRealmChange(PathRealmChangeEvent event) {
        if (event.getRealm().majorRealm() != event.getOldRealm().majorRealm()) {
            refreshRealmEffectiveness(event.getSource());
        }
    }

    @SubscribeEvent
    public static void onPathAdded(PathAddedEvent.Post event) {
        refreshRealmEffectiveness(event.getSource());
    }

    @SubscribeEvent
    public static void onPathRemoved(PathRemovedEvent.Post event) {
        refreshRealmEffectiveness(event.getSource());
    }

    private static void refreshRealmEffectiveness(OriginSource source) {
        if (source == null) {
            return;
        }

        for (LivingEntity entity : source.getAttachedEntities()) {
            AscensionEntityDataProvider provider = entity.getCapability(CoreCapabilities.ASCENSION_ENTITY_DATA_PROVIDER_CAPABILITY);
            if (provider == null || !(provider.getData() instanceof SimpleAscensionEntityData data)) {
                continue;
            }
            data.refreshRealmEffectiveness();
        }
    }

}
