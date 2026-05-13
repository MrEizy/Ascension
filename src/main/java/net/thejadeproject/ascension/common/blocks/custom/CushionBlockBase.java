package net.thejadeproject.ascension.common.blocks.custom;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MobSpawnType;
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
import net.thejadeproject.ascension.entity.ModEntities;
import net.thejadeproject.ascension.entity.custom.CushionEntity;

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
            Entity entity = null;
            List<CushionEntity> entities = level.getEntities(ModEntities.CUSHION_ENTITY.get(), new AABB(pos), cushion -> true);
            if (entities.isEmpty()) {
                entity = ModEntities.CUSHION_ENTITY.get().spawn(((ServerLevel) level), pos, MobSpawnType.TRIGGERED);
            } else {
                entity = entities.get(0);
            }

            player.startRiding(entity);
        }

        return InteractionResult.SUCCESS;
    }


    @Override
    public void fallOn(Level level, BlockState state, BlockPos pos, Entity entity, float fallDistance) {
        super.fallOn(level, state, pos, entity, fallDistance * 0.7F);
    }

    @Override
    public void updateEntityAfterFallOn(BlockGetter reader, Entity entity) {
        if (entity.isSuppressingBounce()) {
            super.updateEntityAfterFallOn(reader, entity);
            return;
        }

        Vec3 vec3 = entity.getDeltaMovement();
        if (vec3.y < 0.0D) {
            // Gentle bounce: 30% upward rebound
            entity.setDeltaMovement(vec3.x, -vec3.y * 0.3D, vec3.z);
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
