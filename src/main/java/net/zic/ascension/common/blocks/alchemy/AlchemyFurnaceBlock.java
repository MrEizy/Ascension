package net.zic.ascension.common.blocks.alchemy;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.zic.ascension.api.ascension.core.alchemy.AlchemySubstance;
import net.zic.ascension.common.blocks.entity.AlchemyFurnaceBlockEntity;
import net.zic.ascension.common.item.ModItems;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class AlchemyFurnaceBlock extends Block implements EntityBlock {
    public static final MapCodec<AlchemyFurnaceBlock> CODEC = simpleCodec(AlchemyFurnaceBlock::new);

    public AlchemyFurnaceBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new AlchemyFurnaceBlockEntity(pos, state);
    }

    @Override
    protected InteractionResult useItemOn(
            ItemStack stack,
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            InteractionHand hand,
            BlockHitResult hitResult
    ) {
        Optional<AlchemySubstance.Material> material = AlchemySubstance.resolve(stack);
        if (material.isEmpty()) {
            return InteractionResult.PASS;
        }

        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        if (!(level.getBlockEntity(pos) instanceof AlchemyFurnaceBlockEntity furnace)) {
            return InteractionResult.FAIL;
        }

        if (!furnace.canInsert()) {
            player.sendOverlayMessage(Component.translatable("ascension.alchemy_furnace.full"));
            return InteractionResult.SUCCESS;
        }

        if (!furnace.insert(material.get().substance())) {
            return InteractionResult.FAIL;
        }

        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }

        player.sendOverlayMessage(Component.translatable(
                "ascension.alchemy_furnace.inserted",
                furnace.ingredientCount(),
                AlchemyFurnaceBlockEntity.MAX_INGREDIENTS
        ));
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResult useWithoutItem(
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            BlockHitResult hitResult
    ) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        if (!(level.getBlockEntity(pos) instanceof AlchemyFurnaceBlockEntity furnace)) {
            return InteractionResult.FAIL;
        }

        if (player.isShiftKeyDown()) {
            if (furnace.isEmpty()) {
                player.sendOverlayMessage(Component.translatable("ascension.alchemy_furnace.empty"));
            } else {
                furnace.clear();
                player.sendOverlayMessage(Component.translatable("ascension.alchemy_furnace.cleared"));
            }
            return InteractionResult.SUCCESS;
        }

        if (furnace.isEmpty()) {
            player.sendOverlayMessage(Component.translatable("ascension.alchemy_furnace.empty"));
            return InteractionResult.SUCCESS;
        }

        Optional<ItemStack> result = furnace.condense();
        if (result.isPresent()) {
            ItemStack pill = result.get();
            popResource(level, pos.above(), pill);
            if (level instanceof ServerLevel serverLevel) {
                spawnSuccessParticles(serverLevel, pos);
            }
            player.sendOverlayMessage(Component.translatable("ascension.alchemy_furnace.condensed", pill.getHoverName()));
        } else {
            popResource(level, pos.above(), new ItemStack(ModItems.PILL_RESIDUE.get()));
            if (level instanceof ServerLevel serverLevel) {
                spawnFailureParticles(serverLevel, pos);
            }
            player.sendOverlayMessage(Component.translatable("ascension.alchemy_furnace.residue"));
        }
        return InteractionResult.SUCCESS;
    }

    private static void spawnSuccessParticles(ServerLevel level, BlockPos pos) {
        double x = pos.getX() + 0.5D;
        double y = pos.getY() + 1.1D;
        double z = pos.getZ() + 0.5D;
        level.sendParticles(ParticleTypes.WITCH, x, y, z, 14, 0.3D, 0.25D, 0.3D, 0.02D);
        level.sendParticles(ParticleTypes.END_ROD, x, y, z, 6, 0.2D, 0.2D, 0.2D, 0.01D);
    }

    private static void spawnFailureParticles(ServerLevel level, BlockPos pos) {
        double x = pos.getX() + 0.5D;
        double y = pos.getY() + 1.1D;
        double z = pos.getZ() + 0.5D;
        level.sendParticles(ParticleTypes.SMOKE, x, y, z, 10, 0.25D, 0.2D, 0.25D, 0.01D);
    }
}
