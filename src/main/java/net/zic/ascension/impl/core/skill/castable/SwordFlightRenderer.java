package net.zic.ascension.impl.core.skill.castable;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderPlayerEvent;
import net.zic.ascension.AscensionCraft;


@EventBusSubscriber(modid = AscensionCraft.MOD_ID, value = Dist.CLIENT)
public final class SwordFlightRenderer {
    private SwordFlightRenderer() {
    }

    // ── Tuning ──────────────────────────────────────────────
    private static final float SWORD_SCALE = 3.2F;
    private static final double FEET_OFFSET_Y = -0.05D; // tune empirically so feet meet the sword surface
    private static final float BASE_FORWARD_LEAN_DEGREES = 15.0F; // constant lean so feet actually plant on the board
    private static final float FLIGHT_PITCH_SCALE = 0.6F; // how strongly climb/dive tilts the whole stance

    @SubscribeEvent
    public static void onRenderPre(RenderPlayerEvent.Pre<?> event) {
        Player player = resolvePlayer(event.getRenderState());
        if (player == null || !SwordFlightPhysics.isFlying(player.getUUID())) {
            return;
        }
        freezeLimbSwing(event.getRenderState());
        hideHeldItems(event.getRenderState());
        pushBodyTilt(player, event);
    }

    @SubscribeEvent
    public static void onRenderPost(RenderPlayerEvent.Post<?> event) {
        Player player = resolvePlayer(event.getRenderState());
        if (player == null || !SwordFlightPhysics.isFlying(player.getUUID())) {
            return;
        }
        // Render the sword INSIDE the still-active tilt pushed in onRenderPre,
        // so it banks/pitches together with the player, then pop it — must be
        // popped exactly once or the frame's "Pose stack not empty" check throws.
        renderFlatSword(player, event.getPoseStack(), event.getSubmitNodeCollector());
        event.getPoseStack().popPose();
    }


    private static Player resolvePlayer(AvatarRenderState state) {
        var level = Minecraft.getInstance().level;
        if (level == null) {
            return null;
        }
        Entity entity = level.getEntity(state.id);
        return entity instanceof Player player ? player : null;
    }

    private static void freezeLimbSwing(AvatarRenderState state) {
        state.walkAnimationPos = 0.0F;
        state.walkAnimationSpeed = 0.0F;
    }


    private static void hideHeldItems(AvatarRenderState state) {
        // state.mainHandItem = ItemStack.EMPTY;
        // state.offhandItem = ItemStack.EMPTY;
    }

    /**
     * Pushes a rotation matching the sword's plane and the current camera
     * bank plus actual flight pitch (climb/dive velocity, not look direction)
     * onto the shared PoseStack — popped in onRenderPost, after the sword is
     * drawn inside the same tilted frame, so both tilt together.
     */
    private static void pushBodyTilt(Player player, RenderPlayerEvent.Pre<?> event) {
        double bank = SwordFlightPhysics.getBankDegrees(player.getUUID(), event.getPartialTick());
        double flightPitch = SwordFlightPhysics.getVelocityPitchDegrees(player.getUUID());
        float pitchLean = BASE_FORWARD_LEAN_DEGREES + (float) flightPitch * FLIGHT_PITCH_SCALE;

        PoseStack poseStack = event.getPoseStack();
        poseStack.pushPose();
        poseStack.mulPose(Axis.ZP.rotationDegrees((float) bank));
        poseStack.mulPose(Axis.XP.rotationDegrees(pitchLean));
    }

    private static void renderFlatSword(Player player, PoseStack poseStack, SubmitNodeCollector submitNodeCollector) {
        ItemStack sword = SwordFlightPhysics.heldSword(player);
        if (sword == null || sword.isEmpty()) {
            return;
        }

        int packedLight = LevelRenderer.getLightCoords(player.level(), player.blockPosition());
        ItemInHandRenderer itemInHandRenderer = Minecraft.getInstance().gameRenderer.itemInHandRenderer;

        poseStack.pushPose();
        poseStack.translate(0.0D, FEET_OFFSET_Y, 0.0D);
        poseStack.mulPose(Axis.YP.rotationDegrees(-player.yBodyRot));
        poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
        poseStack.scale(SWORD_SCALE, SWORD_SCALE, SWORD_SCALE);

        itemInHandRenderer.renderItem(
                player,
                sword,
                ItemDisplayContext.GROUND,
                poseStack,
                submitNodeCollector,
                packedLight
        );

        poseStack.popPose();
    }
}