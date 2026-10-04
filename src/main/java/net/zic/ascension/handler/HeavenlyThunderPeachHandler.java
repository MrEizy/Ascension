package net.zic.ascension.handler;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.common.blocks.ModBlocks;
import net.zic.ascension.common.blocks.crops.herbs.PodHerbBlock;

@EventBusSubscriber(modid = AscensionCraft.MOD_ID)
public final class HeavenlyThunderPeachHandler {
    private static final String PROCESSED_TAG = "ascension:heavenly_thunder_peach_mutation";
    private static final int MUTATION_RADIUS = 6;
    private static final int MUTATION_CHANCE = 4;

    private HeavenlyThunderPeachHandler() {
    }

    @SubscribeEvent
    public static void onLightningTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof LightningBolt lightning)
                || !(lightning.level() instanceof ServerLevel level)
                || lightning.entityTags().contains(PROCESSED_TAG)) {
            return;
        }

        lightning.addTag(PROCESSED_TAG);
        mutateNearbyPeaches(level, lightning.blockPosition());
    }

    private static void mutateNearbyPeaches(ServerLevel level, BlockPos strikePos) {
        BlockPos min = strikePos.offset(-MUTATION_RADIUS, -MUTATION_RADIUS, -MUTATION_RADIUS);
        BlockPos max = strikePos.offset(MUTATION_RADIUS, MUTATION_RADIUS, MUTATION_RADIUS);

        for (BlockPos pos : BlockPos.betweenClosed(min, max)) {
            BlockState state = level.getBlockState(pos);
            if (!state.is(ModBlocks.PEACH_POD.get()) || level.getRandom().nextInt(MUTATION_CHANCE) != 0) {
                continue;
            }

            BlockState transformed = ModBlocks.HEAVENLY_THUNDER_PEACH_POD.get().defaultBlockState()
                    .setValue(PodHerbBlock.FACING, state.getValue(PodHerbBlock.FACING))
                    .setValue(PodHerbBlock.STAGE, state.getValue(PodHerbBlock.STAGE))
                    .setValue(PodHerbBlock.QUALITY, state.getValue(PodHerbBlock.QUALITY))
                    .setValue(PodHerbBlock.WILD, state.getValue(PodHerbBlock.WILD));

            if (!transformed.canSurvive(level, pos)) {
                continue;
            }

            if (level.setBlock(pos, transformed, Block.UPDATE_ALL)) {
                level.sendParticles(
                        ParticleTypes.ELECTRIC_SPARK,
                        pos.getX() + 0.5D,
                        pos.getY() + 0.5D,
                        pos.getZ() + 0.5D,
                        12,
                        0.25D,
                        0.3D,
                        0.25D,
                        0.06D
                );
            }
        }
    }
}
