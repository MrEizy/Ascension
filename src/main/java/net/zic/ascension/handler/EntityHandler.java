package net.zic.ascension.handler;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.zic.ascension.api.ascension.capabilities.AscensionEntityDataProvider;
import net.zic.ascension.api.ascension.capabilities.CoreCapabilities;
import net.zic.ascension.common.starter.StarterSelectionManager;

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
}
