package net.zic.ascension.handler;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.server.level.ServerPlayer;
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
import net.zic.ascension.common.data_attachements.AscensionAttachments;
import net.zic.ascension.common.starter.StarterSelectionManager;
import net.zic.ascension.impl.core.entity.SimpleAscensionEntityData;

@EventBusSubscriber
public class EntityHandler {

    private static final String FOUNDATION_PATH_PREFIX = "foundation/";

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
        if (event.getOldRealm().majorRealm() == event.getRealm().majorRealm()) {
            return;
        }
        refreshRealmEffectiveness(event.getSource(), event.getPathId());
    }

    @SubscribeEvent
    public static void onPathRealmDown(PathRealmChangeEvent.PathRealmDownEvent event) {
        if (event.getOldRealm().majorRealm() == event.getRealm().majorRealm()) {
            return;
        }
        refreshRealmEffectiveness(event.getSource(), event.getPathId());
    }

    @SubscribeEvent
    public static void onPathAdded(PathAddedEvent.Post event) {
        refreshRealmEffectiveness(event.getSource(), event.getPathIdentifier());
    }

    @SubscribeEvent
    public static void onPathRemoved(PathRemovedEvent.Post event) {
        refreshRealmEffectiveness(event.getSource(), event.getPathIdentifier());
    }

    private static void refreshRealmEffectiveness(OriginSource source, Identifier changedPath) {
        if (source == null || changedPath == null || !changedPath.getPath().startsWith(FOUNDATION_PATH_PREFIX)) {
            return;
        }

        for (LivingEntity entity : source.getAttachedEntities()) {
            SimpleAscensionEntityData data = entity.getData(AscensionAttachments.SIMPLE_ENTITY_DATA);
            if (data != null) {
                data.refreshRealmEffectiveness();
            }
        }
    }
}
