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