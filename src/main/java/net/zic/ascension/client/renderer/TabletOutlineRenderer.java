package net.zic.ascension.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.ShapeRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.zic.ascension.common.item.artifacts.base_templates.BaseTabletOfDestruction;
import net.zic.ascension.common.item.artifacts.consumable.TabletOfDestructionHeaven;
import net.zic.ascension.util.ModTags;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TabletOutlineRenderer {

    @SubscribeEvent
    public void onRenderLevelStage(RenderLevelStageEvent.AfterTranslucentFeatures event) {
        Minecraft mc     = Minecraft.getInstance();
        Player    player = mc.player;
        if (player == null) return;

        ItemStack tablet = findTablet(player);
        if (tablet.isEmpty() || !(tablet.getItem() instanceof BaseTabletOfDestruction tabletItem))
            return;

        Level level  = player.level();
        // Camera comes from gameRenderer in 26.1.x — the stage events don't carry it
        Vec3  camPos = mc.gameRenderer.getMainCamera().position();

        PoseStack                    poseStack    = event.getPoseStack();
        MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();


        // ── Tunnel preview ────────────────────────────────────────────────────
        List<BlockPos> tunnelBlocks = computeTunnelBlocks(player, tabletItem, level);

        boolean onCooldown = player.getCooldowns().isOnCooldown(tabletItem.getDefaultInstance());
        int color;
        if (onCooldown) {
            color = toARGB(220, 50, 50, 255);
        } else {
            int raw = getOutlineColor(tablet.getItem());
            color = toARGB((raw >> 16) & 0xFF, (raw >> 8) & 0xFF, raw & 0xFF, 255);
        }

        if (!tunnelBlocks.isEmpty()) {
            VertexConsumer lines = bufferSource.getBuffer(RenderTypes.lines());
            drawBlockListOutline(poseStack, lines, tunnelBlocks, camPos, color);
            bufferSource.endLastBatch();
        }

        // ── Linked-container highlight (Heaven only) ──────────────────────────
        if (tabletItem instanceof TabletOfDestructionHeaven heavenTablet) {
            BaseTabletOfDestruction.LinkedContainerData link =
                    heavenTablet.getLinkedContainer(tablet);

            if (link.pos() != null && link.dimension() != null
                    && link.dimension().equals(
                    level.dimension().identifier().toString())) {

                List<BlockPos> linkedBlocks = resolveLinkedBlocks(link.pos(), level);
                if (!linkedBlocks.isEmpty()) {
                    float pulse = (float)
                            ((Math.sin(System.currentTimeMillis() / 500.0 * Math.PI) + 1.0) / 2.0);
                    int alpha = 130 + (int) (pulse * 125f);
                    int linkedColor = toARGB(255, 199, 0, alpha);

                    VertexConsumer lines = bufferSource.getBuffer(RenderTypes.lines());
                    drawBlockListOutline(poseStack, lines, linkedBlocks, camPos, linkedColor);
                    bufferSource.endLastBatch();
                }
            }
        }
    }

    // ── Tunnel block computation ──────────────────────────────────────────────

    private List<BlockPos> computeTunnelBlocks(Player player,
                                               BaseTabletOfDestruction tabletItem,
                                               Level level) {
        HitResult hit = Minecraft.getInstance().hitResult;
        if (!(hit instanceof BlockHitResult bhr)
                || bhr.getType() == HitResult.Type.MISS) return List.of();

        BlockPos startPos = bhr.getBlockPos();
        if (!level.getBlockState(startPos).is(ModTags.Blocks.DESTRUCTIBLE_BLOCKS))
            return List.of();

        int width  = tabletItem.getWidthPublic();
        int height = tabletItem.getHeightPublic();
        int depth  = tabletItem.getDepthPublic();

        List<BlockPos> result = new ArrayList<>();

        if (BaseTabletOfDestruction.isVerticalAim(player)) {
            int stepY = player.getXRot() > 0 ? -1 : 1;
            for (int x = -width; x <= width; x++) {
                for (int z = -width; z <= width; z++) {
                    for (int y = 0; y < depth; y++) {
                        BlockPos target = startPos.offset(x, stepY * y, z);
                        if (!level.isInWorldBounds(target)) break;
                        if (level.getBlockState(target).isAir()) break;
                        if (level.getBlockState(target).is(ModTags.Blocks.DESTRUCTIBLE_BLOCKS))
                            result.add(target);
                    }
                }
            }
        } else {
            Direction direction = player.getDirection();
            int dx = direction.getStepX();
            int dz = direction.getStepZ();

            for (int x = -width; x <= width; x++) {
                for (int z = 0; z <= depth; z++) {
                    BlockPos colBase = startPos.offset(
                            dx * z + dz * x, 0, dz * z + dx * x);

                    boolean hasAir = false;
                    for (int y = -1; y <= height; y++) {
                        if (level.getBlockState(colBase.above(y)).isAir()) {
                            hasAir = true;
                            break;
                        }
                    }
                    if (hasAir) continue;

                    for (int y = -1; y <= height; y++) {
                        BlockPos target = colBase.above(y);
                        if (!level.isInWorldBounds(target)) continue;
                        BlockState bs = level.getBlockState(target);
                        if (!bs.isAir() && bs.is(ModTags.Blocks.DESTRUCTIBLE_BLOCKS))
                            result.add(target);
                    }
                }
            }
        }

        return result;
    }

    // ── Linked container resolution ───────────────────────────────────────────

    private List<BlockPos> resolveLinkedBlocks(BlockPos pos, Level level) {
        List<BlockPos> blocks = new ArrayList<>();
        blocks.add(pos);

        net.minecraft.world.level.block.Block linkedBlock =
                level.getBlockState(pos).getBlock();

        for (Direction dir : Direction.Plane.HORIZONTAL) {
            BlockPos neighbour = pos.relative(dir);
            if (level.getBlockState(neighbour).getBlock() == linkedBlock) {
                blocks.add(neighbour);
                break;
            }
        }
        return blocks;
    }

    // ── Outline drawing ───────────────────────────────────────────────────────
    private void drawBlockListOutline(PoseStack poseStack,
                                      VertexConsumer lines,
                                      List<BlockPos> blocks,
                                      Vec3 camPos,
                                      int color) {
        Set<BlockPos> blockSet = new HashSet<>(blocks);

        for (BlockPos pos : blocks) {
            double ox = pos.getX() - camPos.x;
            double oy = pos.getY() - camPos.y;
            double oz = pos.getZ() - camPos.z;

            for (Direction face : Direction.values()) {
                if (blockSet.contains(pos.relative(face))) continue;
                ShapeRenderer.renderShape(
                        poseStack, lines,
                        getFaceShape(face),
                        ox, oy, oz,
                        color, 1);
            }
        }
    }
    /**
     * A thin VoxelShape slab on one face of a unit cube.
     * ShapeRenderer draws the edges of this shape, giving us the face outline.
     */
    private static VoxelShape getFaceShape(Direction face) {
        final double T = 0.002;
        return switch (face) {
            case DOWN  -> Shapes.box(0,   0,   0,   1,   T,   1  );
            case UP    -> Shapes.box(0,   1-T, 0,   1,   1,   1  );
            case NORTH -> Shapes.box(0,   0,   0,   1,   1,   T  );
            case SOUTH -> Shapes.box(0,   0,   1-T, 1,   1,   1  );
            case WEST  -> Shapes.box(0,   0,   0,   T,   1,   1  );
            case EAST  -> Shapes.box(1-T, 0,   0,   1,   1,   1  );
        };
    }

    // ── Utility ───────────────────────────────────────────────────────────────

    private ItemStack findTablet(Player player) {
        ItemStack main = player.getMainHandItem();
        if (main.getItem() instanceof BaseTabletOfDestruction) return main;
        ItemStack off  = player.getOffhandItem();
        if (off.getItem()  instanceof BaseTabletOfDestruction) return off;
        return ItemStack.EMPTY;
    }

    private static int getOutlineColor(net.minecraft.world.item.Item item) {
        try {
            return item.getClass().getField("OUTLINE_COLOR").getInt(null);
        } catch (Exception e) {
            return 0xFF_FFFFFF;
        }
    }

    /** Packs r, g, b, a (0–255 each) into a single ARGB int. */
    private static int toARGB(int r, int g, int b, int a) {
        return (a << 24) | (r << 16) | (g << 8) | b;
    }
}