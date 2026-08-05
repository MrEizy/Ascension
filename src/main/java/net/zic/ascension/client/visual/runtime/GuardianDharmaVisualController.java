package net.zic.ascension.client.visual.runtime;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.ascension.core.runtime.RuntimeVisualState;

public final class GuardianDharmaVisualController implements RuntimeVisualController {
    private static final GuardianDharmaModel MODEL = new GuardianDharmaModel(
            AscensionCraft.prefix("models/runtime/guardian_dharma.json")
    );

    private static final double MODEL_SCALE = 1.35D;
    private static final int FULL_BRIGHT = 0x00F000F0;

    public static void registerDefault() {
        ClientRuntimeVisuals.register(
                AscensionCraft.prefix("guardian_dharma_idol"),
                new GuardianDharmaVisualController()
        );
    }

    @Override
    public void onSpawn(RuntimeVisualState state) {
        MODEL.invalidate();
    }

    @Override
    public void render(
            RuntimeVisualState state,
            RenderLevelStageEvent.AfterTranslucentFeatures event
    ) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            return;
        }

        float partialTick = ClientRuntimeVisuals.partialTick();
        Vec3 camera = minecraft.gameRenderer.getMainCamera().position();
        Vec3 origin = ClientRuntimeVisuals.position(state, partialTick)
                .subtract(camera);

        Player owner = state.ownerId() == null
                ? null
                : minecraft.level.getPlayerByUUID(state.ownerId());

        float ownerYaw = owner == null
                ? 0.0F
                : Mth.rotLerp(
                partialTick,
                owner.yRotO,
                owner.getYRot()
        );

        double time = minecraft.level.getGameTime()
                + partialTick
                + (state.seed() & 63L);
        double pulse = 1.0D + Math.sin(time * 0.11D) * 0.018D;
        double hover = Math.sin(time * 0.075D) * 0.035D;

        float stability = Math.clamp(state.progress(), 0.0F, 1.0F);
        int red = 255;
        int green = Math.clamp(
                Math.round(150.0F + stability * 105.0F),
                0,
                255
        );
        int blue = Math.clamp(
                Math.round(55.0F + stability * 155.0F),
                0,
                255
        );
        int alpha = Math.clamp(
                Math.round(145.0F + stability * 95.0F),
                80,
                240
        );

        MultiBufferSource.BufferSource buffers = minecraft.renderBuffers()
                .bufferSource();

        MODEL.render(
                event.getPoseStack().last(),
                buffers,
                origin.add(0.0D, hover, 0.0D),
                Math.toRadians(-ownerYaw),
                MODEL_SCALE * pulse,
                red,
                green,
                blue,
                alpha,
                FULL_BRIGHT
        );

        buffers.endLastBatch();
    }
}
