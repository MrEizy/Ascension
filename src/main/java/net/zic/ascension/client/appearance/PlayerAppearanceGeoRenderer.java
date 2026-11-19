package net.zic.ascension.client.appearance;

import com.geckolib.animatable.GeoAnimatable;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.RawAnimation;
import com.geckolib.animation.object.PlayState;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.GeoObjectRenderer;
import com.geckolib.renderer.base.GeoRenderState;
import com.geckolib.renderer.base.RenderPassInfo;
import com.geckolib.util.GeckoLibUtil;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.resources.Identifier;

import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

public final class PlayerAppearanceGeoRenderer {
    private static final Map<PlayerAppearanceModels.GeoAttachment, AttachmentRenderer> RENDERERS = new HashMap<>();
    private static final Map<AbstractClientPlayer, Map<PlayerAppearanceModels.GeoAttachment, AttachmentAnimatable>> ANIMATABLES = new WeakHashMap<>();

    private PlayerAppearanceGeoRenderer() {
    }

    public static void clear() {
        RENDERERS.clear();
        ANIMATABLES.clear();
    }

    public static void submit(AbstractClientPlayer player, PoseStack poseStack, SubmitNodeCollector collector,
                              int lightCoords, Identifier texture, PlayerAppearanceModels.GeoAttachment part) {
        CameraRenderState camera = Minecraft.getInstance().gameRenderer.getGameRenderState()
                .levelRenderState.cameraRenderState;
        if (camera == null) {
            return;
        }
        AttachmentRenderer renderer = RENDERERS.computeIfAbsent(part, key -> new AttachmentRenderer(key, texture));
        AttachmentAnimatable animatable = ANIMATABLES.computeIfAbsent(player, key -> new HashMap<>())
                .computeIfAbsent(part, AttachmentAnimatable::new);
        animatable.setMoving(player.getDeltaMovement().horizontalDistanceSqr() > 0.001D);
        renderer.performRenderPass(animatable, null, poseStack, collector, camera, lightCoords,
                Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(true));
    }

    private static final class AttachmentRenderer
            extends GeoObjectRenderer<AttachmentAnimatable, Void, GeoRenderState.Impl> {
        private AttachmentRenderer(PlayerAppearanceModels.GeoAttachment part, Identifier texture) {
            super(new AttachmentModel(part.model(), part.animation(), texture));
        }

        @Override
        public void adjustRenderPose(RenderPassInfo<GeoRenderState.Impl> renderPassInfo) {
            renderPassInfo.poseStack().mulPose(Axis.ZP.rotationDegrees(180.0F));
        }
    }

    private static final class AttachmentModel extends GeoModel<AttachmentAnimatable> {
        private final Identifier model;
        private final Identifier animation;
        private final Identifier texture;

        private AttachmentModel(Identifier model, Identifier animation, Identifier texture) {
            this.model = model;
            this.animation = animation;
            this.texture = texture;
        }

        @Override
        public Identifier getModelResource(GeoRenderState renderState) {
            return model;
        }

        @Override
        public Identifier getTextureResource(GeoRenderState renderState) {
            return texture;
        }

        @Override
        public Identifier getAnimationResource(AttachmentAnimatable animatable) {
            return animation;
        }
    }

    private static final class AttachmentAnimatable implements GeoAnimatable {
        private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
        private final String idle;
        private final String walk;
        private boolean moving;

        private AttachmentAnimatable(PlayerAppearanceModels.GeoAttachment part) {
            this.idle = part.idle();
            this.walk = part.walk();
        }

        private void setMoving(boolean moving) {
            this.moving = moving;
        }

        @Override
        public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
            if (idle.isEmpty() && walk.isEmpty()) {
                return;
            }
            RawAnimation idleAnimation = idle.isEmpty() ? null : RawAnimation.begin().thenLoop(idle);
            RawAnimation walkAnimation = walk.isEmpty() ? null : RawAnimation.begin().thenLoop(walk);
            controllers.add(new AnimationController<>("appearance", 5, state -> {
                RawAnimation current = moving && walkAnimation != null ? walkAnimation : idleAnimation;
                return current == null ? PlayState.STOP : state.setAndContinue(current);
            }));
        }

        @Override
        public AnimatableInstanceCache getAnimatableInstanceCache() {
            return cache;
        }
    }
}
