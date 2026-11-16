package net.zic.ascension.client.combat;

import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.network.OffhandAttackPayload;

/**
 * Right-click while dual wielding. Per your rule: if the crosshair is on a BLOCK, vanilla keeps the click (doors, chests, placing);
 * on an entity or empty air, the click becomes an offhand attack. The server re-checks everything, so this only decides whether
 * to send. Clicks are throttled to one per 3 ticks, and the server enforces the real per-hand cooldown.
 *
 * The empty-offhand arm in first person is drawn by DualWieldHandRenderer.
 */
@EventBusSubscriber(modid = AscensionCraft.MOD_ID, value = Dist.CLIENT)
public final class DualWieldClientEvents {
    private DualWieldClientEvents() {
    }

    private static long lastSend = Long.MIN_VALUE;

    @SubscribeEvent
    public static void onInteraction(InputEvent.InteractionKeyMappingTriggered event) {
        if (!event.isUseItem() || !DualWieldClientState.active()) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null || minecraft.screen != null) {
            return;
        }
        HitResult hit = minecraft.hitResult;
        if (hit != null && hit.getType() == HitResult.Type.BLOCK) {
            return; // clicking a block: normal Minecraft
        }

        event.setCanceled(true);
        event.setSwingHand(false);
        long now = minecraft.level.getGameTime();
        if (now - lastSend < 3) {
            return;
        }
        lastSend = now;
        minecraft.player.swing(InteractionHand.OFF_HAND);
        ClientPacketDistributor.sendToServer(new OffhandAttackPayload(hit instanceof EntityHitResult entityHit ? entityHit.getEntity().getId() : -1));
    }

    @SubscribeEvent
    public static void onLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        DualWieldClientState.set(false);
    }
}