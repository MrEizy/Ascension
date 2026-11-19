package net.zic.ascension.client.combat;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.network.OffhandAttackPayload;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(modid = AscensionCraft.MOD_ID, value = Dist.CLIENT)
public final class DualWieldClientEvents {
    private DualWieldClientEvents() {
    }

    private static boolean usePressed;

    private static boolean isUseKey(InputConstants.Type type, int code) {
        InputConstants.Key bound = Minecraft.getInstance().options.keyUse.getKey();
        return bound.getType() == type && bound.getValue() == code;
    }

    @SubscribeEvent
    public static void onMouseButton(InputEvent.MouseButton.Pre event) {
        if (event.getAction() == GLFW.GLFW_PRESS && isUseKey(InputConstants.Type.MOUSE, event.getButton())) {
            usePressed = true;
        }
    }

    @SubscribeEvent
    public static void onKey(InputEvent.Key event) {
        if (event.getAction() == GLFW.GLFW_PRESS && isUseKey(InputConstants.Type.KEYSYM, event.getKey())) {
            usePressed = true;
        }
    }

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
            return;
        }

        event.setCanceled(true);
        event.setSwingHand(false);

        boolean fresh = usePressed;
        usePressed = false;
        if (!fresh) {
            return;
        }
        if (DualWieldClientState.offhandCooldown() > 0) {
            return;
        }

        DualWieldClientState.startOffhandSwing();
        minecraft.player.swing(InteractionHand.OFF_HAND);
        ClientPacketDistributor.sendToServer(new OffhandAttackPayload(
                hit instanceof EntityHitResult entityHit ? entityHit.getEntity().getId() : -1));
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        DualWieldClientState.tick();
        if (!Minecraft.getInstance().options.keyUse.isDown()) {
            usePressed = false;
        }
    }

    @SubscribeEvent
    public static void onLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        DualWieldClientState.reset();
        usePressed = false;
    }
}