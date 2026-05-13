package net.thejadeproject.ascension.common.items.tools.herbs;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.thejadeproject.ascension.common.blocks.custom.crops.CropAgeCache;
import net.thejadeproject.ascension.common.blocks.custom.crops.GenericSlowCropBlock;
import net.thejadeproject.ascension.common.blocks.custom.crops.StemSlowCropBlock;
import net.thejadeproject.ascension.common.blocks.custom.crops.jadedew.JadeDewGrassCropBlock;
import net.thejadeproject.ascension.common.items.herbs.HerbQuality;

public class SpiritualMeal extends Item {
    private static final float GROW_CHANCE = 0.10f;

    public SpiritualMeal(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);
        Block block = state.getBlock();

        if (!isModdedCrop(block)) {
            return InteractionResult.PASS;
        }

        CropBlock crop = (CropBlock) block;
        int age = crop.getAge(state);
        int maxAge = crop.getMaxAge();
        //When done check so it doesn't fuck up Datacomp on items and forces higher age rating

        if (age >= maxAge) {
            return InteractionResult.PASS;
        }

        if (!level.isClientSide) {
            ItemStack stack = context.getItemInHand();
            Player player = context.getPlayer();

            if (player == null || !player.getAbilities().instabuild) {
                stack.shrink(1);
            }

            ServerLevel serverLevel = (ServerLevel) level;

            if (serverLevel.getRandom().nextFloat() < GROW_CHANCE) {
                int nextAge = age + 1;
                level.setBlock(pos, crop.getStateForAge(nextAge), 2);

                if (nextAge == maxAge) {
                    CropAgeCache.store(serverLevel, pos, serverLevel.getGameTime(), HerbQuality.rollQuality());
                }

                serverLevel.sendParticles(
                        ParticleTypes.HAPPY_VILLAGER,
                        pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D,
                        3, 0.2D, 0.2D, 0.2D, 0.0D
                );
            }
        }

        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    private boolean isModdedCrop(Block block) {
        return block instanceof GenericSlowCropBlock
                || block instanceof StemSlowCropBlock
                || block instanceof JadeDewGrassCropBlock;
    }
}
