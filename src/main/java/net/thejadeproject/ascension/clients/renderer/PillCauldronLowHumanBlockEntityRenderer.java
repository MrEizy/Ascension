package net.thejadeproject.ascension.clients.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.thejadeproject.ascension.common.blocks.ModBlocks;
import net.thejadeproject.ascension.common.blocks.entity.PillCauldronLowHumanEntity;
import net.thejadeproject.ascension.shaders.client.ModRenderTypes;

/**
 * Renders translucent ghost blocks at each MISSING multiblock position so the
 * player can see where to place the Flame Stand and the three Pedestals.
 *
 * All positions are obtained from the entity's own {@code relativeOffset()}
 * method, which already accounts for the cauldron's FACING — so this renderer
 * never needs to know anything about compass directions.
 *
 * Register in client setup:
 *   event.registerBlockEntityRenderer(
 *       ModBlockEntities.PILL_CAULDRON_LOW_HUMAN.get(),
 *       PillCauldronLowHumanBlockEntityRenderer::new);
 */
@OnlyIn(Dist.CLIENT)
public class PillCauldronLowHumanBlockEntityRenderer
        implements BlockEntityRenderer<PillCauldronLowHumanEntity> {

    public PillCauldronLowHumanBlockEntityRenderer(BlockEntityRendererProvider.Context ctx) {}

    @Override
    public void render(PillCauldronLowHumanEntity be, float partialTick,
                       PoseStack poseStack, MultiBufferSource bufferSource,
                       int combinedLight, int combinedOverlay) {

        if (be.getLevel() == null) return;

        BlockPos origin = be.getBlockPos();
        BlockState pedestalState  = ModBlocks.CAULDRON_PEDESTAL_BLOCK.get().defaultBlockState();
        BlockState flameStandState = ModBlocks.FLAME_STAND_BLOCK.get().defaultBlockState();

        // ── Flame Stand: directly below ───────────────────────────
        renderGhostIfAbsent(be, poseStack, bufferSource, combinedLight, combinedOverlay,
                be.getBlockPos().below(), origin, flameStandState);

        // ── Pedestals: use entity's own relative positions ────────
        // These are already rotated to match the cauldron's FACING.
        renderGhostIfAbsent(be, poseStack, bufferSource, combinedLight, combinedOverlay,
                be.pedestalLeftPos(), origin, pedestalState);

        renderGhostIfAbsent(be, poseStack, bufferSource, combinedLight, combinedOverlay,
                be.pedestalBackPos(), origin, pedestalState);

        renderGhostIfAbsent(be, poseStack, bufferSource, combinedLight, combinedOverlay,
                be.pedestalRightPos(), origin, pedestalState);
    }

    /**
     * Renders a ghost block at {@code targetPos} only if that world position is air.
     * The PoseStack is in block-local space (origin = cauldron block's world pos),
     * so we translate by (target - origin) to position the ghost correctly.
     */
    private void renderGhostIfAbsent(PillCauldronLowHumanEntity be,
                                     PoseStack poseStack,
                                     MultiBufferSource bufferSource,
                                     int combinedLight, int combinedOverlay,
                                     BlockPos targetPos, BlockPos originPos,
                                     BlockState ghostState) {

        if (!be.getLevel().getBlockState(targetPos).isAir()) return;

        // Offset from cauldron origin to the target in block-local space
        int dx = targetPos.getX() - originPos.getX();
        int dy = targetPos.getY() - originPos.getY();
        int dz = targetPos.getZ() - originPos.getZ();

        BlockRenderDispatcher blockRenderer = Minecraft.getInstance().getBlockRenderer();
        BakedModel model = blockRenderer.getBlockModel(ghostState);

        poseStack.pushPose();
        poseStack.translate(dx, dy, dz);

        RenderType renderType;
        try {
            renderType = ModRenderTypes.GHOST;
        } catch (Exception | Error e) {
            renderType = RenderType.translucent();
        }

        VertexConsumer consumer = bufferSource.getBuffer(renderType)
                .setColor(1f, 1f, 1f, 0.45f);

        blockRenderer.getModelRenderer().renderModel(
                poseStack.last(),
                consumer,
                ghostState,
                model,
                1f, 1f, 1f,
                combinedLight,
                combinedOverlay,
                ModelData.EMPTY,
                renderType
        );

        poseStack.popPose();
    }

    @Override
    public AABB getRenderBoundingBox(PillCauldronLowHumanEntity be) {
        return AABB.INFINITE;
    }

    @Override
    public boolean shouldRenderOffScreen(PillCauldronLowHumanEntity be) {
        return true;
    }
}