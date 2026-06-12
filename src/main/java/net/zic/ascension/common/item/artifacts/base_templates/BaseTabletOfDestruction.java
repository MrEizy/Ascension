package net.zic.ascension.common.item.artifacts.base_templates;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.zic.ascension.util.ModTags;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public abstract class BaseTabletOfDestruction extends Item {

    private static final String DROP_MODE_TAG     = "DropMode";
    protected static final String LINKED_POS_TAG  = "LinkedPos";
    protected static final String LINKED_DIM_TAG  = "LinkedDimension";

    public static final int DROP_OFF = 0;
    public static final int DROP_ON  = 1;

    public BaseTabletOfDestruction(Properties properties) {
        super(properties);
    }

    // ── Abstract configuration ────────────────────────────────────────────────

    protected abstract int getCooldownTicks();
    protected abstract int getWidth();
    protected abstract int getHeight();
    protected abstract int getDepth();
    protected abstract boolean supportsDropBlocks();
    protected abstract boolean supportsContainerLinking();

    // ── Public dimension accessors (used by client renderer) ──────────────────

    public int getWidthPublic()  { return getWidth();  }
    public int getHeightPublic() { return getHeight(); }
    public int getDepthPublic()  { return getDepth();  }


    // Added for custom tooltip support - sortOfSmart?
    public final boolean supportsBlockDrops() {
        return supportsDropBlocks();
    }
    public final boolean supportsContainerLinks() {
        return supportsContainerLinking();
    }

    // ── Interaction ───────────────────────────────────────────────────────────

    @Nonnull
    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        Level  level  = ctx.getLevel();
        Player player = ctx.getPlayer();
        if (player == null) return InteractionResult.PASS;

        ItemStack stack      = ctx.getItemInHand();
        BlockPos  clickedPos = ctx.getClickedPos();

        if (supportsContainerLinking() && player.isShiftKeyDown()) {
            return handleContainerLinking(level, player, stack, clickedPos);
        }

        // Client side: show cooldown message if needed, always return SUCCESS
        // so the server-side packet is always sent
        if (level.isClientSide()) {
            if (player.getCooldowns().isOnCooldown(this.getDefaultInstance())) {
                player.sendOverlayMessage(getCooldownMessage());
            }
            return InteractionResult.SUCCESS;
        }

        // ── Server side only ──────────────────────────────────────────────────

        if (player.getCooldowns().isOnCooldown(this.getDefaultInstance())) {
            player.sendOverlayMessage(getCooldownMessage());
            return InteractionResult.FAIL;
        }

        ServerLevel serverLevel = (ServerLevel) level;

        serverLevel.playSeededSound(
                null,
                clickedPos.getX(), clickedPos.getY(), clickedPos.getZ(),
                SoundEvents.LODESTONE_COMPASS_LOCK,
                SoundSource.PLAYERS,
                0.6F, 1.4F, serverLevel.getRandom().nextLong());

        player.getCooldowns().addCooldown(this.getDefaultInstance(), getCooldownTicks());

        boolean isVertical = isVerticalAim(player);
        boolean dropBlocks = supportsDropBlocks() && getDropMode(stack) == DROP_ON;

        BlockPos linkedPos = null;
        String   linkedDim = null;
        if (supportsContainerLinking()) {
            LinkedContainerData link = getLinkedContainer(stack);
            linkedPos = link.pos();
            linkedDim = link.dimension();
        }

        if (isVertical) {
            clearAreaVertical(serverLevel, clickedPos, player,
                    dropBlocks, linkedPos, linkedDim);
        } else {
            clearAreaHorizontal(serverLevel, clickedPos, player.getDirection(),
                    player.position(), dropBlocks, linkedPos, linkedDim);
        }

        serverLevel.playSeededSound(
                null,
                clickedPos.getX(), clickedPos.getY(), clickedPos.getZ(),
                SoundEvents.ITEM_BREAK,
                SoundSource.BLOCKS,
                1.0F, 0.8F, serverLevel.getRandom().nextLong());

        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }

        return InteractionResult.SUCCESS;
    }

    // ── Vertical-aim helper ───────────────────────────────────────────────────

    public static boolean isVerticalAim(Player player) {
        return Math.abs(player.getXRot()) >= 45f;
    }

    // ── Container linking ─────────────────────────────────────────────────────

    private InteractionResult handleContainerLinking(Level level, Player player,
                                                     ItemStack stack, BlockPos pos) {
        if (level.isClientSide()) return InteractionResult.SUCCESS;

        BlockState state = level.getBlockState(pos);
        if (!state.is(ModTags.Blocks.LINKABLE_CONTAINERS)) {
            player.sendOverlayMessage(
                    Component.translatable("item.ascension.tablet.link_invalid"));
            return InteractionResult.FAIL;
        }

        ResourceHandler<ItemResource> handler =
                level.getCapability(Capabilities.Item.BLOCK, pos, null);
        if (handler == null) {
            player.sendOverlayMessage(
                    Component.translatable("item.ascension.tablet.link_invalid"));
            return InteractionResult.FAIL;
        }

        LinkedContainerData current   = getLinkedContainer(stack);
        String              currentDim = level.dimension().identifier().toString();

        if (current.pos() != null
                && current.pos().equals(pos)
                && currentDim.equals(current.dimension())) {
            clearLinkedContainer(stack);
            player.sendOverlayMessage(
                    Component.translatable("item.ascension.tablet.unlink_success"));
        } else {
            setLinkedContainer(stack, pos, currentDim);
            player.sendOverlayMessage(
                    Component.translatable("item.ascension.tablet.link_success",
                            state.getBlock().getName().getString(),
                            pos.getX(), pos.getY(), pos.getZ()));
        }
        return InteractionResult.SUCCESS;
    }

    // ── Horizontal clearing ───────────────────────────────────────────────────

    private void clearAreaHorizontal(ServerLevel level, BlockPos startPos,
                                     Direction direction, Vec3 playerPos,
                                     boolean dropBlocks,
                                     @Nullable BlockPos linkedContainerPos,
                                     @Nullable String linkedDimension) {
        int width  = getWidth();
        int height = getHeight();
        int depth  = getDepth();
        int dx     = direction.getStepX();
        int dz     = direction.getStepZ();

        List<BlockStatePos> toDrop = new ArrayList<>();

        for (int x = -width; x <= width; x++) {
            for (int z = 0; z <= depth; z++) {
                BlockPos colBase = startPos.offset(
                        dx * z + dz * x, 0, dz * z + dx * x);

                for (int y = height; y >= -1; y--) {
                    BlockPos target = colBase.above(y);
                    if (!shouldRemoveBlock(level, target)) continue;
                    BlockState state = level.getBlockState(target);
                    if (dropBlocks) toDrop.add(new BlockStatePos(target, state));
                    level.removeBlock(target, false);
                }
            }
        }

        placeSupportBeamsHorizontal(level, startPos, direction, depth, width, height);

        if (dropBlocks && !toDrop.isEmpty()) {
            handleBlockDrops(level, toDrop, playerPos, direction,
                    linkedContainerPos, linkedDimension);
        }
    }

    // ── Vertical clearing ─────────────────────────────────────────────────────

    private void clearAreaVertical(ServerLevel level, BlockPos startPos,
                                   Player player, boolean dropBlocks,
                                   @Nullable BlockPos linkedContainerPos,
                                   @Nullable String linkedDimension) {
        int width = getWidth();
        int depth = getDepth();
        int stepY = player.getXRot() > 0 ? -1 : 1;

        List<BlockStatePos> toDrop = new ArrayList<>();

        for (int x = -width; x <= width; x++) {
            for (int z = -width; z <= width; z++) {
                for (int y = 0; y < depth; y++) {
                    BlockPos target = startPos.offset(x, stepY * y, z);
                    if (!level.isInWorldBounds(target)) break;
                    if (!shouldRemoveBlock(level, target)) continue;
                    BlockState state = level.getBlockState(target);
                    if (dropBlocks) toDrop.add(new BlockStatePos(target, state));
                    level.removeBlock(target, false);
                }
            }
        }

        placeLaddersVertical(level, startPos, player, depth, stepY);

        if (dropBlocks && !toDrop.isEmpty()) {
            handleBlockDrops(level, toDrop, player.position(),
                    player.getDirection(), linkedContainerPos, linkedDimension);
        }
    }

    // ── Support beams ─────────────────────────────────────────────────────────

    private void placeSupportBeamsHorizontal(ServerLevel level, BlockPos startPos,
                                             Direction direction, int depth,
                                             int width, int height) {
        int dx = direction.getStepX();
        int dz = direction.getStepZ();
        int px = direction.getClockWise().getStepX();
        int pz = direction.getClockWise().getStepZ();

        for (int z = 7; z <= depth; z += 7) {
            BlockPos beamBase = startPos.offset(dx * z, 0, dz * z);

            BlockPos ceilCheck = beamBase.above(height + 1);
            if (!level.getBlockState(ceilCheck).isSolidRender()) continue;

            int ceilY = height;
            for (int scan = height + 1; scan <= height + 4; scan++) {
                BlockPos cp = beamBase.above(scan);
                if (level.getBlockState(cp).isSolidRender()) {
                    ceilY = scan - 1;
                    break;
                }
            }

            for (int side : new int[]{ -width, width }) {
                // Start one below startPos so y=0 is the floor level
                BlockPos wallBase = beamBase.offset(px * side, 0, pz * side).below();

                for (int y = 0; y <= ceilY + 1; y++) {
                    BlockPos plankPos = wallBase.above(y);
                    if (level.getBlockState(plankPos).isAir()) {
                        level.setBlock(plankPos,
                                Blocks.SPRUCE_PLANKS.defaultBlockState(),
                                Block.UPDATE_ALL);
                    }

                    // Torch on second plank from floor (y == 1)
                    if (y == 1) {
                        Direction torchFacing = (side < 0)
                                ? direction.getClockWise()
                                : direction.getCounterClockWise();
                        BlockPos torchPos = plankPos.relative(torchFacing);
                        if (level.getBlockState(torchPos).isAir()) {
                            level.setBlock(torchPos,
                                    Blocks.WALL_TORCH.defaultBlockState()
                                            .setValue(BlockStateProperties.HORIZONTAL_FACING,
                                                    torchFacing),
                                    Block.UPDATE_ALL);
                        }
                    }
                }
            }

            // Lintel across the top
            for (int s = -width; s <= width; s++) {
                BlockPos lintPos = beamBase.offset(px * s, ceilY, pz * s);
                if (level.getBlockState(lintPos).isAir()) {
                    level.setBlock(lintPos,
                            Blocks.SPRUCE_PLANKS.defaultBlockState(),
                            Block.UPDATE_ALL);
                }
            }
        }
    }

    // ── Ladders ───────────────────────────────────────────────────────────────

    private void placeLaddersVertical(ServerLevel level, BlockPos startPos,
                                      Player player, int depth, int stepY) {
        Direction ladderWall   = player.getDirection().getOpposite();
        Direction ladderFacing = ladderWall.getOpposite();
        BlockPos  wallBase     = startPos.relative(ladderWall, getWidth());

        for (int y = 0; y < depth; y++) {
            BlockPos target      = wallBase.above(stepY * y);
            BlockPos wallSupport = target.relative(ladderWall);

            if (!level.isInWorldBounds(target)) break;

            // If the wall support block is air, place stone so the ladder has something to attach to
            if (level.getBlockState(wallSupport).isAir()) {
                level.setBlock(wallSupport,
                        Blocks.STONE.defaultBlockState(),
                        Block.UPDATE_ALL);
            }

            // Place ladder if the target is air and now has a solid support
            if (level.getBlockState(target).isAir()
                    && level.getBlockState(wallSupport)
                    .isFaceSturdy(level, wallSupport, ladderFacing)) {
                level.setBlock(target,
                        Blocks.LADDER.defaultBlockState()
                                .setValue(LadderBlock.FACING, ladderFacing),
                        Block.UPDATE_ALL);
            }
        }
    }

    // ── Block drop handling ───────────────────────────────────────────────────

    private void handleBlockDrops(ServerLevel level, List<BlockStatePos> blocks,
                                  Vec3 playerPos, Direction direction,
                                  @Nullable BlockPos linkedContainerPos,
                                  @Nullable String linkedDimension) {
        for (BlockStatePos bd : blocks) {
            Block.dropResources(bd.state(), level, bd.pos(),
                    level.getBlockEntity(bd.pos()));
        }
    }

    // ── Drop-mode cycling ─────────────────────────────────────────────────────

    public int getDropMode(ItemStack stack) {
        return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY)
                .copyTag()
                .getInt(DROP_MODE_TAG)
                .orElse(0);
    }

    public void cycleDropMode(ItemStack stack, ServerPlayer player) {
        if (!supportsDropBlocks()) return;

        var tag  = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        int cur  = tag.getInt(DROP_MODE_TAG).orElse(0);
        int next = (cur >= DROP_ON) ? DROP_OFF : DROP_ON;
        tag.putInt(DROP_MODE_TAG, next);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));

        Component label = next == DROP_ON
                ? Component.translatable("ascension.tablet.drop_mode.on")
                .withStyle(ChatFormatting.GREEN)
                : Component.translatable("ascension.tablet.drop_mode.off")
                .withStyle(ChatFormatting.RED);

        player.sendOverlayMessage(
                Component.translatable("ascension.tablet.drop_mode").append(label));
    }

    // ── Linked-container data ─────────────────────────────────────────────────

    public record LinkedContainerData(@Nullable BlockPos pos,
                                      @Nullable String dimension) {}

    public LinkedContainerData getLinkedContainer(ItemStack stack) {
        var tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY)
                .copyTag();
        if (tag.contains(LINKED_POS_TAG)) {
            return new LinkedContainerData(
                    BlockPos.of(tag.getLong(LINKED_POS_TAG).orElse(0L)),
                    tag.getString(LINKED_DIM_TAG).orElse(null));
        }
        return new LinkedContainerData(null, null);
    }

    protected void setLinkedContainer(ItemStack stack, BlockPos pos, String dimension) {
        var tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        tag.putLong(LINKED_POS_TAG, pos.asLong());
        tag.putString(LINKED_DIM_TAG, dimension);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    protected void clearLinkedContainer(ItemStack stack) {
        var tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        tag.remove(LINKED_POS_TAG);
        tag.remove(LINKED_DIM_TAG);
        if (tag.isEmpty()) stack.remove(DataComponents.CUSTOM_DATA);
        else stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    // ── Tooltip ───────────────────────────────────────────────────────────────

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context,
                                TooltipDisplay display, Consumer<Component> tooltip,
                                TooltipFlag flag) {
        super.appendHoverText(stack, context, display, tooltip, flag);

        if (supportsDropBlocks()) {
            int dropMode = getDropMode(stack);
            Component modeLabel = dropMode == DROP_ON
                    ? Component.translatable("ascension.tablet.drop_mode.on")
                    .withStyle(ChatFormatting.GREEN)
                    : Component.translatable("ascension.tablet.drop_mode.off")
                    .withStyle(ChatFormatting.RED);
            tooltip.accept(Component.translatable("ascension.tablet.drop_mode").append(modeLabel));
            tooltip.accept(Component.translatable("ascension.tablet.cycle_mode_info")
                    .withStyle(ChatFormatting.DARK_GRAY));
        }
    }

    // ── Overrideable helpers ──────────────────────────────────────────────────

    protected Component getCooldownMessage() {
        return Component.translatable("ascension.tablet.cooldown");
    }

    protected boolean shouldRemoveBlock(ServerLevel level, BlockPos pos) {
        return level.getBlockState(pos).is(ModTags.Blocks.DESTRUCTIBLE_BLOCKS);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return supportsDropBlocks() && getDropMode(stack) == DROP_ON;
    }

    // ── Internal record ───────────────────────────────────────────────────────

    protected record BlockStatePos(BlockPos pos, BlockState state) {}
}