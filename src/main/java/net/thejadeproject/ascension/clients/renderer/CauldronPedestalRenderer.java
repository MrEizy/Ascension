package net.thejadeproject.ascension.clients.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.thejadeproject.ascension.common.blocks.entity.CauldronPedestalBlockEntity;

/**
 * Renders the ItemStack stored in a CauldronPedestal floating above the block,
 * spinning slowly and bobbing. Uses proper block+sky light like the MysticalTrees
 * pedestal renderers.
 */
@OnlyIn(Dist.CLIENT)
public class CauldronPedestalRenderer implements BlockEntityRenderer<CauldronPedestalBlockEntity> {

    public CauldronPedestalRenderer(BlockEntityRendererProvider.Context context) {
        // context unused; we grab Minecraft.getInstance() at render time
    }

    @Override
    public void render(CauldronPedestalBlockEntity be, float partialTick,
                       PoseStack poseStack, MultiBufferSource bufferSource,
                       int combinedLight, int combinedOverlay) {

        ItemStack stack = be.getItem();
        if (stack.isEmpty()) return;

        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
        Level level = be.getLevel();

        poseStack.pushPose();

        // Center horizontally, sit above pedestal top face
        poseStack.translate(0.5f, 1.35f, 0.5f);
        poseStack.scale(0.5f, 0.5f, 0.5f);

        // Spin
        poseStack.mulPose(Axis.YP.rotationDegrees(be.getRenderingRotation()));

        // Bob
        long gameTime = (level != null) ? level.getGameTime() : 0;
        float bob = (float) Math.sin((gameTime + partialTick) * 0.05f) * 0.08f;
        poseStack.translate(0, bob, 0);

        int lightLevel = getLightLevel(level, be.getBlockPos());

        itemRenderer.renderStatic(
                stack,
                ItemDisplayContext.FIXED,
                lightLevel,
                OverlayTexture.NO_OVERLAY,
                poseStack,
                bufferSource,
                level,
                1
        );

        poseStack.popPose();
    }

    /** Packs block + sky light just like the MysticalTrees pedestal renderers. */
    private int getLightLevel(Level level, BlockPos pos) {
        if (level == null) return LightTexture.pack(15, 15);
        int bLight = level.getBrightness(LightLayer.BLOCK, pos);
        int sLight = level.getBrightness(LightLayer.SKY,   pos);
        return LightTexture.pack(bLight, sLight);
    }
}