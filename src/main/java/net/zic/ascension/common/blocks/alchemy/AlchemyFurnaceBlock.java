package net.zic.ascension.common.blocks.alchemy;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.zic.ascension.common.blocks.entity.AlchemyFurnaceBlockEntity;
import net.zic.ascension.common.blocks.entity.AscBlockEntities;
import org.jetbrains.annotations.Nullable;

public class AlchemyFurnaceBlock extends Block implements EntityBlock {
    public static final MapCodec<AlchemyFurnaceBlock> CODEC = simpleCodec(AlchemyFurnaceBlock::new);

    public AlchemyFurnaceBlock(Properties properties) { super(properties); }
    @Override protected MapCodec<? extends Block> codec() { return CODEC; }
    @Override public RenderShape getRenderShape(BlockState state) { return RenderShape.INVISIBLE; }
    @Nullable @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new AlchemyFurnaceBlockEntity(pos, state); }
    @Nullable @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide() || type != AscBlockEntities.ALCHEMY_FURNACE_BE.get()) return null;
        return (world, pos, blockState, entity) -> AlchemyFurnaceBlockEntity.serverTick(world, pos, blockState, (AlchemyFurnaceBlockEntity) entity);
    }
    @Override protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, net.minecraft.world.InteractionHand hand, BlockHitResult hit) {
        return InteractionResult.TRY_WITH_EMPTY_HAND;
    }
    @Override public InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        if (player instanceof ServerPlayer serverPlayer && level.getBlockEntity(pos) instanceof AlchemyFurnaceBlockEntity furnace) {
            serverPlayer.openMenu(furnace, buf -> buf.writeBlockPos(pos));
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.FAIL;
    }
}
