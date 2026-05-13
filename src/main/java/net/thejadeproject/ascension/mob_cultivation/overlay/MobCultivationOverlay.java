package net.thejadeproject.ascension.mob_cultivation.overlay;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.EntityHitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLivingEvent;
import net.thejadeproject.ascension.AscensionCraft;
import net.thejadeproject.ascension.data_attachments.ModAttachments;
import net.thejadeproject.ascension.mob_cultivation.MobCultivationData;
import org.joml.Matrix4f;

/*
* TODO: Remove in favour of Sensing Skill eventually
* */

@EventBusSubscriber(modid = AscensionCraft.MOD_ID, value = Dist.CLIENT)
public class MobCultivationOverlay {

    private static final double MAX_RENDER_DISTANCE_SQR = 64.0 * 64.0;

    private static String formatRealm(String realmId) {
        String[] parts = realmId.split("_");
        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < parts.length; i++) {
            if (parts[i].isEmpty()) continue;

            String word = parts[i];
            builder.append(Character.toUpperCase(word.charAt(0)))
                    .append(word.substring(1));

            if (i < parts.length - 1) {
                builder.append(" ");
            }
        }

        return builder.toString();
    }

    @SubscribeEvent
    public static void onRenderLivingPost(RenderLivingEvent.Post<?, ?> event) {
        if (!(event.getEntity() instanceof LivingEntity entity)) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        if (mc.options.hideGui) return;
        if (entity == mc.player && mc.options.getCameraType().isFirstPerson()) return;

        if (!(mc.hitResult instanceof EntityHitResult hit)) return;
        if (hit.getEntity() != entity) return;

        if (mc.player.distanceToSqr(entity) > MAX_RENDER_DISTANCE_SQR) return;
        if (!mc.player.hasLineOfSight(entity)) return;

        MobCultivationData data = entity.getData(ModAttachments.MOB_RANK);
        if (data == null || !data.isInitialized()) return;

        String realm = formatRealm(data.getRealmId());
        int stage = data.getStage();

        Component text = Component.literal( "[" + realm + " " + stage + "]");

        renderFloatingText(
                entity,
                text,
                event.getPoseStack(),
                event.getMultiBufferSource(),
                event.getPackedLight()
        );
    }

    private static void renderFloatingText(LivingEntity entity,
                                           Component text,
                                           PoseStack poseStack,
                                           MultiBufferSource buffer,
                                           int packedLight) {
        Minecraft mc = Minecraft.getInstance();
        Font font = mc.font;

        poseStack.pushPose();


        boolean hasVanillaName = entity.hasCustomName();

        double yOffset = entity.getBbHeight() + (hasVanillaName ? 0.95D : 0.5D);

        poseStack.translate(0.0D, yOffset, 0.0D);
        poseStack.mulPose(mc.getEntityRenderDispatcher().cameraOrientation());
        poseStack.scale(0.025F, -0.025F, 0.025F);

        Matrix4f matrix = poseStack.last().pose();
        var visualText = text.getVisualOrderText();

        float x = -font.width(visualText) / 2.0F;

        int backgroundAlpha = (int) (mc.options.getBackgroundOpacity(0.25F) * 255.0F) << 24;

        font.drawInBatch(
                visualText,
                x,
                0,
                0xFFFFFFFF,
                false,
                matrix,
                buffer,
                Font.DisplayMode.POLYGON_OFFSET,
                backgroundAlpha,
                packedLight
        );

        poseStack.popPose();
    }

}