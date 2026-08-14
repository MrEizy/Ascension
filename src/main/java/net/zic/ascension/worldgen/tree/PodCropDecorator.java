package net.zic.ascension.worldgen.tree;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecorator;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecoratorType;
import net.zic.ascension.common.blocks.ModBlocks;
import net.zic.ascension.common.blocks.crops.herbs.PodHerbBlock;
import net.zic.ascension.worldgen.AscTreeDecoratorTypes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Same idea as vanilla's CocoaDecorator, except it scans the tree's placed LEAVES
 * instead of its logs, and attaches PodHerbBlock instead of cocoa. Support can be any
 * horizontal side of the leaf — FACING is set toward the leaf so the model rotates to match.
 */
public class PodCropDecorator extends TreeDecorator {

    private static final int HEAVENLY_THUNDER_MIN_Y = 90;
    private static final int HEAVENLY_THUNDER_CHANCE = 16;

    public static final MapCodec<PodCropDecorator> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.floatRange(0f, 1f).fieldOf("probability").forGetter(d -> d.probability)
    ).apply(instance, PodCropDecorator::new));

    private final float probability;

    public PodCropDecorator(float probability) {
        this.probability = probability;
    }

    @Override
    protected TreeDecoratorType<?> type() {
        return AscTreeDecoratorTypes.PEACH_POD.get();
    }

    @Override
    public void place(Context context) {
        RandomSource random = context.random();
        List<BlockPos> leaves = context.leaves();
        if (leaves.isEmpty()) {
            return;
        }

        for (BlockPos leafPos : leaves) {
            if (random.nextFloat() >= probability) {
                continue;
            }

            List<Direction> dirs = new ArrayList<>(List.of(
                    Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST));
            Collections.shuffle(dirs, new Random(random.nextLong()));

            for (Direction dir : dirs) {
                BlockPos podPos = leafPos.relative(dir);
                if (!context.isAir(podPos)) {
                    continue;
                }

                boolean heavenlyThunder = leafPos.getY() >= HEAVENLY_THUNDER_MIN_Y && random.nextInt(HEAVENLY_THUNDER_CHANCE) == 0;
                PodHerbBlock pod = heavenlyThunder ? ModBlocks.HEAVENLY_THUNDER_PEACH_POD.get() : ModBlocks.PEACH_POD.get();

                BlockState state = pod.wildState(dir.getOpposite(), random);

                if (state.canSurvive(context.level(), podPos)) {
                    context.setBlock(podPos, state);
                    break;
                }
            }
        }
    }
}