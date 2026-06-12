package net.zic.ascension.client.renderer;

import com.mojang.blaze3d.systems.RenderSystem;
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
import net.zic.ascension.common.item.artifacts.consumable.TabletOfDestructionHeaven;
import net.zic.ascension.util.ModTags;

import java.util.ArrayList;
import java.util.List;

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
        List<BlockPos> tunnelBlocks = computeTunnelBlocks(player, tabletItem, level);

        boolean onCooldown = player.getCooldowns().isOnCooldown(tabletItem.getDefaultInstance());
        int color;
        if (onCooldown) {
            color = toARGB(255, 220, 50, 50);
        } else {
            int raw = getOutlineColor(tablet.getItem());
            color = toARGB(255, (raw >> 16) & 0xFF, (raw >> 8) & 0xFF, raw & 0xFF);
        }

        if (!tunnelBlocks.isEmpty()) {
            VertexConsumer lines = bufferSource.getBuffer(RenderTypes.lines());
            drawBoundingBox(poseStack, lines, tunnelBlocks, camPos, color);
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

                    VertexConsumer lines = bufferSource.getBuffer(RenderTypes.lines());
                    drawBoundingBox(poseStack, lines, linkedBlocks, camPos, linkedColor);
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
                        // Only add non-air blocks
                        if (!level.getBlockState(target).isAir()) {
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
                        // Only add non-air blocks
                        if (!level.getBlockState(target).isAir()) {
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

    // ── Bounding box drawing ──────────────────────────────────────────────────

    /**
     * Draws a single bounding box around the entire block list,
     * visible through walls (depth test disabled by caller).
     */
    private void drawBoundingBox(PoseStack poseStack,
                                 VertexConsumer lines,
                                 List<BlockPos> blocks,
                                 Vec3 camPos,
                                 int color) {
        if (blocks.isEmpty()) return;

        int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE, minZ = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE, maxZ = Integer.MIN_VALUE;

        for (BlockPos pos : blocks) {
            if (pos.getX() < minX) minX = pos.getX();
            if (pos.getY() < minY) minY = pos.getY();
            if (pos.getZ() < minZ) minZ = pos.getZ();
            if (pos.getX() > maxX) maxX = pos.getX();
            if (pos.getY() > maxY) maxY = pos.getY();
            if (pos.getZ() > maxZ) maxZ = pos.getZ();
        }

        final double E = 0.002; // expand slightly so the box sits outside blocks
        double x0 = minX - camPos.x - E;
        double y0 = minY - camPos.y - E;
        double z0 = minZ - camPos.z - E;
        double x1 = maxX - camPos.x + 1 + E;
        double y1 = maxY - camPos.y + 1 + E;
        double z1 = maxZ - camPos.z + 1 + E;

        int a = (color >> 24) & 0xFF;
        int r = (color >> 16) & 0xFF;
        int g = (color >>  8) & 0xFF;
        int b = (color      ) & 0xFF;

        PoseStack.Pose pose = poseStack.last();

        // Bottom face edges
        line(lines, pose, x0,y0,z0, x1,y0,z0, r,g,b,a);
        line(lines, pose, x1,y0,z0, x1,y0,z1, r,g,b,a);
        line(lines, pose, x1,y0,z1, x0,y0,z1, r,g,b,a);
        line(lines, pose, x0,y0,z1, x0,y0,z0, r,g,b,a);

        // Top face edges
        line(lines, pose, x0,y1,z0, x1,y1,z0, r,g,b,a);
        line(lines, pose, x1,y1,z0, x1,y1,z1, r,g,b,a);
        line(lines, pose, x1,y1,z1, x0,y1,z1, r,g,b,a);
        line(lines, pose, x0,y1,z1, x0,y1,z0, r,g,b,a);

        // Vertical edges
        line(lines, pose, x0,y0,z0, x0,y1,z0, r,g,b,a);
        line(lines, pose, x1,y0,z0, x1,y1,z0, r,g,b,a);
        line(lines, pose, x1,y0,z1, x1,y1,z1, r,g,b,a);
        line(lines, pose, x0,y0,z1, x0,y1,z1, r,g,b,a);
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

    /** Packs a, r, g, b (0–255 each) into a single ARGB int. */
    private static int toARGB(int a, int r, int g, int b) {
        return (a << 24) | (r << 16) | (g << 8) | b;
    }
}