package net.zic.ascension.common.item.artifacts.consumable;

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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.zic.ascension.common.item.artifacts.base_templates.BaseTabletOfDestruction;
import net.zic.ascension.util.ModTags;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Ascendant Tablet — highest tier.
 * Shape is cycled by holding SHIFT while in main hand and pressing the cycle key.
 * All shapes are capped at 128 blocks broken.
 * Outline colour: purple/magenta dye.
 */
public class TabletOfDestructionAscendant extends BaseTabletOfDestruction {

    private static final int COOLDOWN = 60; // 3s
    private static final int MAX_BLOCKS = 128;

    public static final int OUTLINE_COLOR = 0xFF_8932B8; // purple dye

    // Shape NBT key
    private static final String SHAPE_TAG = "MineShape";

    public enum MineShape {
        SHAPELESS("ascension.tablet.ascendant.shape.shapeless"),
        TUNNEL("ascension.tablet.ascendant.shape.tunnel"),
        ESCAPE("ascension.tablet.ascendant.shape.escape"),
        DOME("ascension.tablet.ascendant.shape.dome");

        public final String langKey;
        MineShape(String langKey) { this.langKey = langKey; }

        public MineShape next() {
            MineShape[] vals = values();
            return vals[(this.ordinal() + 1) % vals.length];
        }
    }

    public TabletOfDestructionAscendant(Properties properties) {
        super(properties);
    }

    // ── These are unused by Ascendant (it has its own useOn) but required ─────
    @Override protected int getCooldownTicks()             { return COOLDOWN; }
    @Override protected int getWidth()                     { return 5; }
    @Override protected int getHeight()                    { return 5; }
    @Override protected int getDepth()                     { return 5; }

    @Override
    public boolean supportsDropBlocks() {
        return true;
    }

    @Override
    public boolean supportsContainerLinking() {
        return true;
    }

    // ── Shape accessors ───────────────────────────────────────────────────────

    public MineShape getShape(ItemStack stack) {
        int ord = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY)
                .copyTag().getInt(SHAPE_TAG).orElse(0);
        MineShape[] vals = MineShape.values();
        return vals[Math.min(ord, vals.length - 1)];
    }

    public void cycleShape(ItemStack stack, ServerPlayer player) {
        var tag   = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        MineShape next = getShape(stack).next();
        tag.putInt(SHAPE_TAG, next.ordinal());
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        player.sendOverlayMessage(
                Component.translatable("ascension.tablet.ascendant.shape")
                        .append(Component.translatable(next.langKey)
                                .withStyle(ChatFormatting.LIGHT_PURPLE)));
    }

    // ── Override useOn — Ascendant ignores vertical aim, uses shapes ──────────

    @Nonnull
    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        Level  level  = ctx.getLevel();
        Player player = ctx.getPlayer();
        if (player == null) return InteractionResult.PASS;

        ItemStack stack      = ctx.getItemInHand();
        BlockPos  clickedPos = ctx.getClickedPos();

        // Shift + main hand = cycle shape
        if (player.isShiftKeyDown()
                && player.getMainHandItem() == stack) {
            if (!level.isClientSide()) {
                cycleShape(stack, (ServerPlayer) player);
            }
            return InteractionResult.SUCCESS;
        }

        if (level.isClientSide()) {
            if (player.getCooldowns().isOnCooldown(this.getDefaultInstance())) {
                player.sendOverlayMessage(getCooldownMessage());
            }
            return InteractionResult.SUCCESS;
        }

        if (player.getCooldowns().isOnCooldown(this.getDefaultInstance())) {
            player.sendOverlayMessage(getCooldownMessage());
            return InteractionResult.FAIL;
        }

        ServerLevel serverLevel = (ServerLevel) level;

        serverLevel.playSeededSound(null,
                clickedPos.getX(), clickedPos.getY(), clickedPos.getZ(),
                SoundEvents.LODESTONE_COMPASS_LOCK, SoundSource.PLAYERS,
                0.6F, 1.4F, serverLevel.getRandom().nextLong());

        player.getCooldowns().addCooldown(this.getDefaultInstance(), getCooldownTicks());

        boolean dropBlocks = getDropMode(stack) == DROP_ON;
        MineShape shape = getShape(stack);

        List<BlockPos> toBreak = computeShape(serverLevel, clickedPos, player, shape);

        List<BlockStatePos> toDrop = new ArrayList<>();
        for (BlockPos pos : toBreak) {
            if (!shouldRemoveBlock(serverLevel, pos)) continue;
            BlockState state = serverLevel.getBlockState(pos);
            if (dropBlocks) toDrop.add(new BlockStatePos(pos, state));
            serverLevel.removeBlock(pos, false);
        }

        if (dropBlocks) {
            for (BlockStatePos bd : toDrop) {
                Block.dropResources(bd.state(), serverLevel, bd.pos(),
                        serverLevel.getBlockEntity(bd.pos()));
            }
        }

        serverLevel.playSeededSound(null,
                clickedPos.getX(), clickedPos.getY(), clickedPos.getZ(),
                SoundEvents.ITEM_BREAK, SoundSource.BLOCKS,
                1.0F, 0.8F, serverLevel.getRandom().nextLong());

        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }

        return InteractionResult.SUCCESS;
    }

    // ── Shape computation ─────────────────────────────────────────────────────

    /**
     * Returns up to MAX_BLOCKS positions to break for the given shape.
     * Tunnel and Escape always end on a full 3x3 cross-section slice.
     */
    public List<BlockPos> computeShape(Level level, BlockPos origin,
                                       Player player, MineShape shape) {
        return switch (shape) {
            case SHAPELESS -> computeShapeless(level, origin);
            case TUNNEL    -> computeStairTunnel(level, origin, player, false);
            case ESCAPE    -> computeStairTunnel(level, origin, player, true);
            case DOME      -> computeDome(level, origin);
        };
    }

    // ── Shapeless: sphere of radius up to 128 blocks total ───────────────────

    private List<BlockPos> computeShapeless(Level level, BlockPos origin) {
        List<BlockPos> result = new ArrayList<>();
        // Expand outward in a sphere, add destructible blocks until cap
        for (int r = 0; r <= 7 && result.size() < MAX_BLOCKS; r++) {
            for (int x = -r; x <= r && result.size() < MAX_BLOCKS; x++) {
                for (int y = -r; y <= r && result.size() < MAX_BLOCKS; y++) {
                    for (int z = -r; z <= r && result.size() < MAX_BLOCKS; z++) {
                        if (Math.abs(x) != r && Math.abs(y) != r && Math.abs(z) != r)
                            continue; // shell only
                        BlockPos pos = origin.offset(x, y, z);
                        if (!level.isInWorldBounds(pos)) continue;
                        if (level.getBlockState(pos).is(ModTags.Blocks.DESTRUCTIBLE_BLOCKS)
                                && !result.contains(pos)) {
                            result.add(pos);
                        }
                    }
                }
            }
        }
        return result;
    }

    // ── Staircase tunnel: 3 wide x 3 tall, descends/ascends 1 per forward ────

    /**
     * Builds a staircase tunnel.
     * Each step forward also moves down (TUNNEL) or up (ESCAPE) by 1.
     * The total block count is always a multiple of 3x3=9 so it never ends
     * mid-slice. We compute how many full slices fit within MAX_BLOCKS.
     */
    private List<BlockPos> computeStairTunnel(Level level, BlockPos origin,
                                              Player player, boolean goingUp) {
        Direction forward = player.getDirection();
        int dx = forward.getStepX();
        int dz = forward.getStepZ();
        // Perpendicular for the 3-wide cross
        int px = forward.getClockWise().getStepX();
        int pz = forward.getClockWise().getStepZ();

        int stepY    = goingUp ? 1 : -1;
        int sliceSize = 9; // 3 wide x 3 tall
        int maxSlices = MAX_BLOCKS / sliceSize; // 14 full slices = 126 blocks

        List<BlockPos> result = new ArrayList<>();

        for (int step = 0; step < maxSlices; step++) {
            int yOffset = stepY * step;
            // Centre of this slice
            int cx = origin.getX() + dx * (step + 1);
            int cy = origin.getY() + yOffset;
            int cz = origin.getZ() + dz * (step + 1);

            for (int w = -1; w <= 1; w++) {       // 3 wide
                for (int h = 0; h <= 2; h++) {     // 3 tall
                    BlockPos pos = new BlockPos(
                            cx + px * w,
                            cy + h,
                            cz + pz * w);
                    if (level.isInWorldBounds(pos)) {
                        result.add(pos);
                    }
                }
            }
        }
        return result;
    }

    // ── Dome: hemisphere centred on aimed block ───────────────────────────────

    private List<BlockPos> computeDome(Level level, BlockPos origin) {
        List<BlockPos> result = new ArrayList<>();
        // Radius grows until we fill MAX_BLOCKS
        // A hemisphere of radius r has roughly (2/3)π r³ blocks
        // r=4 gives ~134 so we use r=4 and cap
        int radius = 4;
        for (int x = -radius; x <= radius && result.size() < MAX_BLOCKS; x++) {
            for (int y = 0; y <= radius && result.size() < MAX_BLOCKS; y++) {
                for (int z = -radius; z <= radius && result.size() < MAX_BLOCKS; z++) {
                    double dist = Math.sqrt(x * x + y * y + z * z);
                    if (dist <= radius) {
                        BlockPos pos = origin.offset(x, y, z);
                        if (!level.isInWorldBounds(pos)) continue;
                        if (level.getBlockState(pos)
                                .is(ModTags.Blocks.DESTRUCTIBLE_BLOCKS)) {
                            result.add(pos);
                        }
                    }
                }
            }
        }
        return result;
    }

    // ── Tooltip ───────────────────────────────────────────────────────────────

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context,
                                TooltipDisplay display, Consumer<Component> tooltip,
                                TooltipFlag flag) {
        super.appendHoverText(stack, context, display, tooltip, flag);

        MineShape shape = getShape(stack);
        tooltip.accept(Component.translatable("ascension.tablet.ascendant.shape")
                .append(Component.translatable(shape.langKey)
                        .withStyle(ChatFormatting.LIGHT_PURPLE)));
        tooltip.accept(Component.translatable("ascension.tablet.ascendant.shape_info")
                .withStyle(ChatFormatting.DARK_GRAY));
    }

    @Override
    protected Component getCooldownMessage() {
        return Component.translatable(
                "item.ascension.tablet_of_destruction_ascendant.cooldown");
    }

    // Expose for renderer
    public MineShape getShapeForRenderer(ItemStack stack) {
        return getShape(stack);
    }
}