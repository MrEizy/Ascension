package net.thejadeproject.ascension.clients.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.thejadeproject.ascension.common.blocks.entity.FlameStandBlockEntity;

/**
 * Renders the flame item currently burning in the Flame Stand,
 * floating just above the block and spinning slowly.
 *
 * Only visible when the stand is lit (litFlameItem is non-empty).
 *
 * Register in client setup alongside the other BESR registrations:
 *   event.registerBlockEntityRenderer(
 *       ModBlockEntities.FLAME_STAND.get(),
 *       FlameStandRenderer::new);
 */
@OnlyIn(Dist.CLIENT)
public class FlameStandRenderer implements BlockEntityRenderer<FlameStandBlockEntity> {

    public FlameStandRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public void render(FlameStandBlockEntity be, float partialTick,
                       PoseStack poseStack, MultiBufferSource bufferSource,
                       int combinedLight, int combinedOverlay) {

        if (!be.isLit()) return;

        ItemStack stack = be.getLitFlameItem();
        if (stack.isEmpty()) return;

        Level level = be.getLevel();

        poseStack.pushPose();

        // Centre over the block, raise to just above the top face
        poseStack.translate(0.5f, 0.75f, 0.5f);
        poseStack.scale(0.4f, 0.4f, 0.4f);

        // Slow spin
        poseStack.mulPose(Axis.YP.rotationDegrees(be.getRenderingRotation()));

        // Gentle bob
        long gameTime = (level != null) ? level.getGameTime() : 0;
        float bob = (float) Math.sin((gameTime + partialTick) * 0.07f) * 0.06f;
        poseStack.translate(0, bob, 0);

        int light = getLightLevel(level, be.getBlockPos());

        Minecraft.getInstance().getItemRenderer().renderStatic(
                stack,
                ItemDisplayContext.FIXED,
                light,
                OverlayTexture.NO_OVERLAY,
                poseStack,
                bufferSource,
                level,
                1
        );

        poseStack.popPose();
    }

    private int getLightLevel(Level level, BlockPos pos) {
        if (level == null) return LightTexture.pack(15, 15);
        // Flame stand is lit, so give it a minimum brightness so the item is
        // always clearly visible even in dark areas
        int bLight = Math.max(10, level.getBrightness(LightLayer.BLOCK, pos));
        int sLight = level.getBrightness(LightLayer.SKY, pos);
        return LightTexture.pack(bLight, sLight);
    }
}