package net.zic.ascension.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.zic.ascension.common.item.artifacts.base_templates.BaseTabletOfDestruction;
import net.zic.ascension.common.item.artifacts.consumable.TabletOfDestructionAscendant;
import net.zic.ascension.common.item.artifacts.consumable.TabletOfDestructionHeaven;
import net.zic.ascension.common.util.ModTags;

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
        Vec3  camPos = mc.gameRenderer.getMainCamera().position();

        PoseStack                      poseStack    = event.getPoseStack();
        MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();

        // ── Tunnel preview ────────────────────────────────────────────────────
        List<BlockPos> tunnelBlocks = computeTunnelBlocks(player, tabletItem, tablet, level);

        boolean onCooldown = player.getCooldowns().isOnCooldown(tabletItem.getDefaultInstance());
        int color;
        if (onCooldown) {
            color = toARGB(255, 220, 50, 50);
        } else {
            int raw = getOutlineColor(tablet.getItem());
            color = toARGB(255, (raw >> 16) & 0xFF, (raw >> 8) & 0xFF, raw & 0xFF);
        }

        if (!tunnelBlocks.isEmpty()) {
            VertexConsumer lines = bufferSource.getBuffer(ModRenderTypes.linesNoDepth());
            drawBlockGroupOutline(poseStack, lines, tunnelBlocks, camPos, color);
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
                    int linkedColor = toARGB(alpha, 255, 199, 0);

                    VertexConsumer lines = bufferSource.getBuffer(ModRenderTypes.linesNoDepth());
                    drawBlockGroupOutline(poseStack, lines, linkedBlocks, camPos, linkedColor);
                    bufferSource.endLastBatch();
                }
            }
        }
    }

    // ── Tunnel block computation ──────────────────────────────────────────────

    private List<BlockPos> computeTunnelBlocks(Player player,
                                               BaseTabletOfDestruction tabletItem,
                                               ItemStack stack,
                                               Level level) {
        HitResult hit = Minecraft.getInstance().hitResult;
        if (!(hit instanceof BlockHitResult bhr)
                || bhr.getType() == HitResult.Type.MISS) return List.of();

        BlockPos startPos = bhr.getBlockPos();
        if (!level.getBlockState(startPos).is(ModTags.Blocks.DESTRUCTIBLE_BLOCKS))
            return List.of();

        // Ascendant uses its own shape system
        if (tabletItem instanceof TabletOfDestructionAscendant ascendant) {
            TabletOfDestructionAscendant.MineShape shape =
                    ascendant.getShapeForRenderer(stack);
            List<BlockPos> raw = ascendant.computeShape(level, startPos, player, shape);
            List<BlockPos> result = new ArrayList<>();
            for (BlockPos pos : raw) {
                if (!level.getBlockState(pos).isAir()
                        && level.getBlockState(pos).is(ModTags.Blocks.DESTRUCTIBLE_BLOCKS)) {
                    result.add(pos);
                }
            }
            return result;
        }

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
                        if (!level.getBlockState(target).isAir()
                                && level.getBlockState(target).is(ModTags.Blocks.DESTRUCTIBLE_BLOCKS)) {
                            result.add(target);
                        }
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

                    for (int y = -1; y <= height; y++) {
                        BlockPos target = colBase.above(y);
                        if (!level.isInWorldBounds(target)) continue;
                        if (!level.getBlockState(target).isAir()
                                && level.getBlockState(target).is(ModTags.Blocks.DESTRUCTIBLE_BLOCKS)) {
                            result.add(target);
                        }
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

    // ── Per-block exposed-face outline ────────────────────────────────────────

    /**
     * Draws only the silhouette/perimeter edges of the exposed surface,
     * skipping internal edges shared between two coplanar exposed faces.
     * This produces one continuous outline that hugs the volume and wraps
     * around any internal air pockets.
     */
    private void drawBlockGroupOutline(PoseStack poseStack,
                                       VertexConsumer lines,
                                       List<BlockPos> blocks,
                                       Vec3 camPos,
                                       int color) {
        if (blocks.isEmpty()) return;

        Set<BlockPos> blockSet = new HashSet<>(blocks);
        PoseStack.Pose pose = poseStack.last();

        int a = (color >> 24) & 0xFF;
        int r = (color >> 16) & 0xFF;
        int g = (color >>  8) & 0xFF;
        int b = (color      ) & 0xFF;

        for (BlockPos pos : blocks) {
            double ox = pos.getX() - camPos.x;
            double oy = pos.getY() - camPos.y;
            double oz = pos.getZ() - camPos.z;

            for (Direction face : Direction.values()) {
                // Only consider faces exposed to a position not in the set
                if (blockSet.contains(pos.relative(face))) continue;

                Direction[] uv = getFaceAxes(face);
                Direction u = uv[0];
                Direction v = uv[1];

                // Edge at u=0 — shared with neighbor in -u direction
                drawEdgeIfBoundary(lines, pose, blockSet, pos, face, u.getOpposite(),
                        ox, oy, oz, face, 0, 0, 0, 1, u, v, r, g, b, a);

                // Edge at u=1 — shared with neighbor in +u direction
                drawEdgeIfBoundary(lines, pose, blockSet, pos, face, u,
                        ox, oy, oz, face, 1, 0, 1, 1, u, v, r, g, b, a);

                // Edge at v=0 — shared with neighbor in -v direction
                drawEdgeIfBoundary(lines, pose, blockSet, pos, face, v.getOpposite(),
                        ox, oy, oz, face, 0, 0, 1, 0, u, v, r, g, b, a);

                // Edge at v=1 — shared with neighbor in +v direction
                drawEdgeIfBoundary(lines, pose, blockSet, pos, face, v,
                        ox, oy, oz, face, 0, 1, 1, 1, u, v, r, g, b, a);
            }
        }
    }

    /**
     * Returns the two perpendicular "in-plane" directions for a given face,
     * used to walk along its edges.
     */
    private Direction[] getFaceAxes(Direction face) {
        return switch (face) {
            case DOWN, UP    -> new Direction[]{ Direction.EAST, Direction.SOUTH };
            case NORTH, SOUTH -> new Direction[]{ Direction.EAST, Direction.UP };
            case WEST, EAST  -> new Direction[]{ Direction.SOUTH, Direction.UP };
        };
    }

    /**
     * Returns the base corner (u=0, v=0) of the face's unit square, in local
     * 0..1 cube coordinates.
     */
    private double[] getFaceBase(Direction face) {
        return switch (face) {
            case DOWN  -> new double[]{0, 0, 0};
            case UP    -> new double[]{0, 1, 0};
            case NORTH -> new double[]{0, 0, 0};
            case SOUTH -> new double[]{0, 0, 1};
            case WEST  -> new double[]{0, 0, 0};
            case EAST  -> new double[]{1, 0, 0};
        };
    }

    /**
     * Checks whether the neighbor block (in the given in-plane direction) also
     * has an exposed face in the same direction `face`. If it does, this edge
     * is internal/shared and is skipped. Otherwise the edge is part of the
     * silhouette and gets drawn from corner (u0,v0) to (u1,v1).
     */
    private void drawEdgeIfBoundary(VertexConsumer lines, PoseStack.Pose pose,
                                    Set<BlockPos> blockSet, BlockPos pos,
                                    Direction face, Direction neighborDir,
                                    double ox, double oy, double oz,
                                    Direction faceForBase,
                                    double u0, double v0, double u1, double v1,
                                    Direction uAxis, Direction vAxis,
                                    int r, int g, int b, int a) {
        BlockPos neighbor = pos.relative(neighborDir);
        boolean neighborAlsoExposed = blockSet.contains(neighbor)
                && !blockSet.contains(neighbor.relative(face));
        if (neighborAlsoExposed) return; // internal edge — skip

        double[] base = getFaceBase(faceForBase);

        double ux = uAxis.getStepX();
        double uy = uAxis.getStepY();
        double uz = uAxis.getStepZ();
        double vx = vAxis.getStepX();
        double vy = vAxis.getStepY();
        double vz = vAxis.getStepZ();

        double x0 = ox + base[0] + u0 * ux + v0 * vx;
        double y0 = oy + base[1] + u0 * uy + v0 * vy;
        double z0 = oz + base[2] + u0 * uz + v0 * vz;

        double x1 = ox + base[0] + u1 * ux + v1 * vx;
        double y1 = oy + base[1] + u1 * uy + v1 * vy;
        double z1 = oz + base[2] + u1 * uz + v1 * vz;

        line(lines, pose, x0, y0, z0, x1, y1, z1, r, g, b, a);
    }

    private void line(VertexConsumer lines, PoseStack.Pose pose,
                      double x0, double y0, double z0,
                      double x1, double y1, double z1,
                      int r, int g, int b, int a) {
        float nx = (float)(x1 - x0);
        float ny = (float)(y1 - y0);
        float nz = (float)(z1 - z0);
        float len = (float) Math.sqrt(nx * nx + ny * ny + nz * nz);
        if (len > 0) { nx /= len; ny /= len; nz /= len; }

        lines.addVertex(pose, (float) x0, (float) y0, (float) z0)
                .setColor(r, g, b, a)
                .setNormal(pose, nx, ny, nz)
                .setLineWidth(2.0f);
        lines.addVertex(pose, (float) x1, (float) y1, (float) z1)
                .setColor(r, g, b, a)
                .setNormal(pose, nx, ny, nz)
                .setLineWidth(2.0f);
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

    private static int toARGB(int a, int r, int g, int b) {
        return (a << 24) | (r << 16) | (g << 8) | b;
    }
}