package net.zic.ascension.client.event;

import com.google.common.reflect.TypeToken;
import net.minecraft.client.entity.ClientAvatarEntity;
import net.minecraft.client.renderer.entity.ItemEntityRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.Avatar;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.renderstate.AvatarRenderStateModifier;
import net.neoforged.neoforge.client.renderstate.RegisterRenderStateModifiersEvent;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.client.renderer.DivineSenseRenderer;
import net.zic.ascension.client.visual.DivineSenseClientState;
import org.joml.Matrix4f;


@EventBusSubscriber(modid = AscensionCraft.MOD_ID, value = Dist.CLIENT)
public final class DivineSenseEvents {

    private DivineSenseEvents() {
    }

    @SubscribeEvent
    public static void onRegisterRenderStateModifiers(RegisterRenderStateModifiersEvent event) {
        event.registerEntityModifier(
                new TypeToken<LivingEntityRenderer<LivingEntity, LivingEntityRenderState, ?>>() {
                },
                DivineSenseEvents::applyHighlight
        );
        event.registerEntityModifier(ItemEntityRenderer.class, DivineSenseEvents::applyHighlight);
        event.registerAvatarEntityModifier(new AvatarRenderStateModifier() {
            @Override
            public <T extends Avatar & ClientAvatarEntity> void accept(T avatar, AvatarRenderState renderState) {
                applyHighlight(avatar, renderState);
            }
        });
    }

    @SubscribeEvent
    public static void onAfterLevel(RenderLevelStageEvent.AfterLevel event) {
        DivineSenseRenderer.renderWave(
                new Matrix4f(event.getModelViewMatrix()),
                new Matrix4f(event.getLevelRenderState().cameraRenderState.projectionMatrix)
        );
    }

    private static void applyHighlight(Entity entity, EntityRenderState renderState) {
        DivineSenseClientState state = DivineSenseClientState.get();
        if (state.isHighlighted(entity)) {
            renderState.outlineColor = state.outlineColor(entity);
        }
    }
}
