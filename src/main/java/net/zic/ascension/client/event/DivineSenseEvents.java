package net.zic.ascension.client.event;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.client.renderer.DivineSenseRenderer;
import org.joml.Matrix4f;

/**
 * RenderLevelStageEvent is no longer one class with a Stage enum — confirmed
 * from the real 26.1.2 source, it's an abstract base with a concrete
 * subclass per stage, and it no longer exposes a projection matrix or
 * partial tick at all (only getModelViewMatrix()/getPoseStack()). Both are
 * rebuilt here instead of pulled off the event.
 *
 * AfterTranslucentBlocks is the direct match for the old
 * AFTER_TRANSLUCENT_BLOCKS stage. There's no AFTER_ENTITIES equivalent
 * anymore; AfterTranslucentFeatures (entities/block entities in the
 * translucent pass) is the closest fit for billboard entity markers.
 */
@EventBusSubscriber(modid = AscensionCraft.MOD_ID, value = Dist.CLIENT)
public final class DivineSenseEvents {

    private DivineSenseEvents() {
    }

    @SubscribeEvent
    public static void onAfterTranslucentBlocks(RenderLevelStageEvent.AfterTranslucentBlocks event) {
        Matrix4f projection = buildProjectionMatrix();
        DivineSenseRenderer.renderWave(new Matrix4f(event.getModelViewMatrix()), projection);
    }

    @SubscribeEvent
    public static void onAfterTranslucentFeatures(RenderLevelStageEvent.AfterTranslucentFeatures event) {
        Matrix4f projection = buildProjectionMatrix();
        float partialTick = Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(true);
        DivineSenseRenderer.renderMarkers(event.getPoseStack(), projection, partialTick);
    }

    /**
     * Ported directly from Camera#createProjectionMatrixForCulling (private
     * in vanilla, but its body is fully visible in the decompiled source) —
     * same fov/aspect/near-far/zZeroToOne inputs, same JOML call shape.
     *
     * The far plane is the one approximation here: Camera's real depthFar
     * field (set in Camera#update, factoring in cloud render distance too)
     * isn't publicly exposed, so this rebuilds it from just the render
     * distance option. Close enough for a cosmetic VFX depth reconstruction;
     * worst case the wave's far edge doesn't perfectly match the world's
     * actual render distance.
     */
    private static Matrix4f buildProjectionMatrix() {
        Minecraft minecraft = Minecraft.getInstance();
        Camera camera = minecraft.gameRenderer.getMainCamera();
        float fov = camera.getFov();
        float aspectRatio = (float) minecraft.getWindow().getWidth() / (float) minecraft.getWindow().getHeight();
        float farPlane = Math.max((float) (minecraft.options.getEffectiveRenderDistance() * 16) * 4.0F, 512.0F);
        return new Matrix4f().perspective(
                fov * ((float) Math.PI / 180F),
                aspectRatio,
                Camera.PROJECTION_Z_NEAR,
                farPlane,
                RenderSystem.getDevice().isZZeroToOne()
        );
    }
}