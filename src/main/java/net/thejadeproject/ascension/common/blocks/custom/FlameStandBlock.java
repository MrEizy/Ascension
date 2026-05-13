package net.thejadeproject.ascension.common.blocks.custom;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.thejadeproject.ascension.Config;
import net.thejadeproject.ascension.common.blocks.entity.FlameStandBlockEntity;
import net.thejadeproject.ascension.common.blocks.entity.ModBlockEntities;
import net.thejadeproject.ascension.common.items.tools.FanItem;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * The Flame Stand. Sits directly below the Pill Cauldron.
 *
 * Interactions:
 *   Fire item (config)  → lights the stand and sets purity/realm bonus
 *   FanItem             → raises temperature (handled in FanItem.useOn)
 *   Empty hand          → small temperature boost (convenience fallback)
 *   Shovel              → extinguishes
 */
public class FlameStandBlock extends BaseEntityBlock {

    public static final VoxelShape SHAPE = Block.box(2, 0, 2, 14, 8, 14);
    public static final MapCodec<FlameStandBlock> CODEC = simpleCodec(FlameStandBlock::new);
    public static final BooleanProperty LIT = BlockStateProperties.LIT;

    public FlameStandBlock(Properties properties) {
        super(properties.sound(SoundType.STONE).strength(2.5f).requiresCorrectToolForDrops());
        this.registerDefaultState(this.stateDefinition.any().setValue(LIT, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LIT);
    }

    @Override protected MapCodec<? extends BaseEntityBlock> codec() { return CODEC; }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        return SHAPE;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new FlameStandBlockEntity(pos, state);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) { return RenderShape.MODEL; }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level,
                                              BlockPos pos, Player player, InteractionHand hand,
                                              BlockHitResult hit) {
        if (level.isClientSide()) return ItemInteractionResult.SUCCESS;

        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof FlameStandBlockEntity flameStand)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        // ── Shovel extinguishes ──────────────────────────────────
        if (stack.getItem() instanceof net.minecraft.world.item.ShovelItem && state.getValue(LIT)) {
            level.setBlock(pos, state.setValue(LIT, false), 3);
            flameStand.extinguish();
            level.playSound(null, pos, net.minecraft.sounds.SoundEvents.FIRE_EXTINGUISH,
                    net.minecraft.sounds.SoundSource.BLOCKS, 1.0f, 1.0f);
            return ItemInteractionResult.sidedSuccess(false);
        }

        // ── FanItem is handled by FanItem.useOn — nothing else fans ─
        if (stack.getItem() instanceof FanItem) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        // ── Fire items light the stand ───────────────────────────
        Map<String, Integer> heatItems = Config.COMMON.getHeatItems();
        String itemId = stack.getItem().builtInRegistryHolder().key().location().toString();

        if (heatItems.containsKey(itemId)) {
            int purityBonus = Config.COMMON.getFlameStandPurityBonus(itemId);
            int realmBonus  = Config.COMMON.getFlameStandRealmBonus(itemId);

            flameStand.light(stack, purityBonus, realmBonus);
            level.setBlock(pos, state.setValue(LIT, true), 3);

            if (!player.getAbilities().instabuild) stack.shrink(1);
            level.playSound(null, pos, net.minecraft.sounds.SoundEvents.FLINTANDSTEEL_USE,
                    net.minecraft.sounds.SoundSource.BLOCKS, 1.0f, 1.0f);
            return ItemInteractionResult.sidedSuccess(false);
        }

        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos,
                            BlockState newState, boolean moving) {
        if (state.getBlock() != newState.getBlock()) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof FlameStandBlockEntity fs) fs.onRemoved();
        }
        super.onRemove(state, level, pos, newState, moving);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
                                                                  BlockEntityType<T> type) {
        if (level.isClientSide()) return null;
        return createTickerHelper(type, ModBlockEntities.FLAME_STAND.get(),
                (l, pos, bs, be) -> be.tick(l, pos, bs));
    }
}