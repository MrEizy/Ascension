package net.zic.ascension.client.appearance;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Util;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.PlayerModelType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.client.renderer.AfterShaderPackDraws;
import net.zic.ascension.common.data_attachements.AscensionAttachments;
import org.jspecify.annotations.NonNull;

@EventBusSubscriber(modid = AscensionCraft.MOD_ID, value = Dist.CLIENT)
public final class PlayerAppearanceLayer extends RenderLayer<AvatarRenderState, PlayerModel> {
    private PlayerAppearanceLayer(RenderLayerParent<AvatarRenderState, PlayerModel> parent) {
        super(parent);
    }

    @SubscribeEvent
    public static void onAddLayers(EntityRenderersEvent.AddLayers event) {
        for (PlayerModelType skin : event.getSkins()) {
            AvatarRenderer<AbstractClientPlayer> renderer = event.getPlayerRenderer(skin);
            if (renderer != null) {
                renderer.addLayer(new PlayerAppearanceLayer(renderer));
            }
        }
    }

    @Override
    public void submit(@NonNull PoseStack poseStack, @NonNull SubmitNodeCollector collector, int lightCoords,
                       AvatarRenderState state, float yRot, float xRot) {
        if (state.isInvisible || state.isSpectator || Minecraft.getInstance().level == null) {
            return;
        }
        Entity entity = Minecraft.getInstance().level.getEntity(state.id);
        if (!(entity instanceof AbstractClientPlayer player) || !player.hasData(AscensionAttachments.PLAYER_APPEARANCES)) {
            return;
        }
        PlayerModel model = getParentModel();
        for (var id : player.getData(AscensionAttachments.PLAYER_APPEARANCES).appearances()) {
            PlayerAppearanceModels.Appearance appearance = PlayerAppearanceModels.get(id);
            if (appearance == null) {
                continue;
            }
            for (var attachment : appearance.parts()) {
                ModelPart anchor = anchor(model, attachment.anchor());
                if (anchor == null || !anchor.visible) {
                    continue;
                }
                poseStack.pushPose();
                model.root().translateAndRotate(poseStack);
                anchor.translateAndRotate(poseStack);
                float[] offset = attachment.offset();
                float[] rotation = attachment.rotation();
                poseStack.translate(offset[0] / 16.0F, offset[1] / 16.0F, offset[2] / 16.0F);
                poseStack.mulPose(Axis.XP.rotationDegrees(rotation[0]));
                poseStack.mulPose(Axis.YP.rotationDegrees(rotation[1]));
                poseStack.mulPose(Axis.ZP.rotationDegrees(rotation[2]));
                if (attachment.swayDegrees() != 0.0F && attachment.swaySpeed() != 0.0F) {
                    float sway = (float) Math.sin(Util.getMillis() / 1000.0D * attachment.swaySpeed() + state.id)
                            * attachment.swayDegrees();
                    Axis axis = switch (attachment.swayAxis()) {
                        case "x" -> Axis.XP;
                        case "z" -> Axis.ZP;
                        default -> Axis.YP;
                    };
                    poseStack.mulPose(axis.rotationDegrees(sway));
                }
                ModelPart geometry = attachment.geometry();
                AfterShaderPackDraws.submitCustomGeometry(collector, poseStack,
                        RenderTypes.entityCutout(appearance.texture()), (pose, buffer) -> {
                            PoseStack copy = new PoseStack();
                            copy.last().set(pose);
                            geometry.render(copy, buffer, lightCoords, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF);
                        });
                poseStack.popPose();
            }
        }
        for (var id : player.getData(AscensionAttachments.PLAYER_APPEARANCES).appearances()) {
            PlayerAppearanceModels.Appearance appearance = PlayerAppearanceModels.get(id);
            if (appearance == null) {
                continue;
            }
            for (var attachment : appearance.geckoParts()) {
                ModelPart anchor = anchor(model, attachment.anchor());
                if (anchor == null || !anchor.visible) {
                    continue;
                }
                poseStack.pushPose();
                model.root().translateAndRotate(poseStack);
                anchor.translateAndRotate(poseStack);
                float[] offset = attachment.offset();
                float[] rotation = attachment.rotation();
                poseStack.translate(offset[0] / 16.0F, offset[1] / 16.0F, offset[2] / 16.0F);
                poseStack.mulPose(Axis.XP.rotationDegrees(rotation[0]));
                poseStack.mulPose(Axis.YP.rotationDegrees(rotation[1]));
                poseStack.mulPose(Axis.ZP.rotationDegrees(rotation[2]));
                poseStack.scale(attachment.scale(), attachment.scale(), attachment.scale());
                PlayerAppearanceGeoRenderer.submit(player, poseStack, collector, lightCoords, appearance.texture(), attachment);
                poseStack.popPose();
            }
        }
    }

    private static ModelPart anchor(PlayerModel model, String name) {
        return switch (name) {
            case "head" -> model.head;
            case "body" -> model.body;
            case "left_arm" -> model.leftArm;
            case "right_arm" -> model.rightArm;
            case "left_leg" -> model.leftLeg;
            case "right_leg" -> model.rightLeg;
            default -> null;
        };
    }
}
