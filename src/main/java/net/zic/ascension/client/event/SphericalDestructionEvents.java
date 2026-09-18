package net.zic.ascension.client.event;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.client.renderer.SphericalDestructionRenderer;
import net.zic.ascension.client.visual.SphericalDestructionClientState;
import net.zic.ascension.network.SphericalDestructionPayload;
import org.joml.Matrix4f;

/**
 * TODO: the actual payload -> handler wiring (whatever registers
 * SphericalDestructionPayload.TYPE with a client handler, e.g. a
 * PayloadRegistrar#playToClient call in your networking setup class) isn't
 * something I have an example of — connect handleClient(...) below to
 * wherever that registration happens. Same shape as RailgunClient.receive()
 * from the orbital mod: parse payload, mutate client state, play the sound.
 *
 * TODO: SOUND_ID currently has no matching sounds.json entry / .ogg file —
 * this needs a real sound resource added (or point it at an existing
 * Ascension impact/explosion sound if one already exists) before it'll
 * actually play anything.
 */
@EventBusSubscriber(modid = AscensionCraft.MOD_ID, value = Dist.CLIENT)
public final class SphericalDestructionEvents {

    private static final Identifier SOUND_ID = AscensionCraft.prefix("spherical_destruction");
    private static final float SOUND_VOLUME = 4.0F;
    private static final float SOUND_PITCH = 1.0F;

    private SphericalDestructionEvents() {
    }

    @SubscribeEvent
    public static void onAfterLevel(RenderLevelStageEvent.AfterLevel event) {
        SphericalDestructionRenderer.renderWave(
                new Matrix4f(event.getModelViewMatrix()),
                new Matrix4f(event.getLevelRenderState().cameraRenderState.projectionMatrix)
        );
    }

    public static void handleClient(SphericalDestructionPayload payload) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            return;
        }
        Vec3 center = payload.center();
        SphericalDestructionClientState.get().start(center, payload.radius(), 0xFF6A1B);
        mc.level.playLocalSound(
                center.x, center.y, center.z,
                SoundEvent.createVariableRangeEvent(SOUND_ID),
                SoundSource.HOSTILE,
                SOUND_VOLUME,
                SOUND_PITCH,
                false
        );
    }
}