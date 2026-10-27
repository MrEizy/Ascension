package net.zic.ascension.common.blocks.custom;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.zic.ascension.common.entities.AscEntities;
import net.zic.ascension.common.entities.CushionEntity;

import java.util.List;

public class CushionBlockBase extends Block {
    public static final MapCodec<CushionBlockBase> CODEC = simpleCodec(CushionBlockBase::new);
    private static final VoxelShape SHAPE = Block.box(0, 0, 0, 16, 7, 16);

    public CushionBlockBase(Properties properties) {
        super(properties);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!level.isClientSide()) {
            List<CushionEntity> entities = level.getEntities(AscEntities.CUSHION.get(), new AABB(pos), cushion -> true);
            CushionEntity seat = entities.isEmpty()
                    ? AscEntities.CUSHION.get().spawn((ServerLevel) level, pos, EntitySpawnReason.TRIGGERED)
                    : entities.get(0);

            if (seat != null && !player.startRiding(seat) && entities.isEmpty()) {
                seat.discard();
            }
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    public void fallOn(Level level, BlockState state, BlockPos pos, Entity entity, double fallDistance) {
        super.fallOn(level, state, pos, entity, fallDistance * 0.7);
    }

    @Override
    public void updateEntityMovementAfterFallOn(BlockGetter level, Entity entity) {
        if (entity.isSuppressingBounce()) {
            super.updateEntityMovementAfterFallOn(level, entity);
            return;
        }

        Vec3 movement = entity.getDeltaMovement();
        if (movement.y < 0.0D) {
            // Gentle bounce: 30% upward rebound
            entity.setDeltaMovement(movement.x, -movement.y * 0.3D, movement.z);
        }
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected MapCodec<? extends CushionBlockBase> codec() {
        return CODEC;
    }
}
