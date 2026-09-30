package net.zic.ascension.impl.core.innerworld;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.zic.ascension.AscensionCraft;

@EventBusSubscriber(modid = AscensionCraft.MOD_ID)
public final class InnerWorldEvents {
    private InnerWorldEvents() {
    }

    @SubscribeEvent
    public static void onServerStarting(ServerStartingEvent event) {
        InnerWorldPlots.load(event.getServer());
        InnerWorldGrowth.rebuild(event.getServer());
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        InnerWorldGrowth.clear();
        InnerWorldPlots.unload();
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Pre event) {
        if (InnerWorldPlots.isLoaded()) {
            InnerWorldGrowth.tick(event.getServer());
        }
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            InnerWorldSessions.onPlayerLogin(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (event.getEntity() instanceof ServerPlayer player && InnerWorldSessions.isInside(player)) {
            InnerWorldSessions.enforceBorder(player);
        }
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof InnerWorldGhost ghost) {
            InnerWorldSessions.onGhostDeath(ghost);
        }
    }
}