package net.zic.ascension.client.event;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.client.renderer.SphericalDestructionRenderer;
import org.joml.Matrix4f;

@EventBusSubscriber(modid = AscensionCraft.MOD_ID, value = Dist.CLIENT)
public final class SphericalDestructionEvents {
    private SphericalDestructionEvents() {
    }

    @SubscribeEvent
    public static void onAfterLevel(RenderLevelStageEvent.AfterLevel event) {
        SphericalDestructionRenderer.renderWave(
                new Matrix4f(event.getModelViewMatrix()),
                new Matrix4f(event.getLevelRenderState().cameraRenderState.projectionMatrix)
        );
    }
}
