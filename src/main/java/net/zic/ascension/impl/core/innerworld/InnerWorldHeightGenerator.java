package net.zic.ascension.impl.core.innerworld;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.FlatLevelSource;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings;

/**
 * A stand-in chunk generator used ONLY when asking vanilla structure code to plan a structure: it answers every height
 * question (getBaseHeight, getFirstFreeHeight, getFirstOccupiedHeight, getBaseColumn) with the inner world's real terrain
 * instead of a flat void, so villages, outposts and the rest sit on the hills we built. It is never registered and the
 * dimension keeps its normal flat generator (see InnerWorldStructures for why).
 */
public final class InnerWorldHeightGenerator extends FlatLevelSource {
    private final MinecraftServer server;

    public InnerWorldHeightGenerator(FlatLevelGeneratorSettings settings, MinecraftServer server) {
        super(settings);
        this.server = server;
    }

    @Override
    public int getBaseHeight(int x, int z, Heightmap.Types type, LevelHeightAccessor heightAccessor, RandomState randomState) {
        boolean water = type == Heightmap.Types.WORLD_SURFACE_WG || type == Heightmap.Types.WORLD_SURFACE;
        Integer y = InnerWorldStructures.surface(server, x, z, water);
        return y != null ? y : super.getBaseHeight(x, z, type, heightAccessor, randomState);
    }

    @Override
    public NoiseColumn getBaseColumn(int x, int z, LevelHeightAccessor heightAccessor, RandomState randomState) {
        Integer y = InnerWorldStructures.surface(server, x, z, false);
        if (y == null) {
            return super.getBaseColumn(x, z, heightAccessor, randomState);
        }
        int min = heightAccessor.getMinY();
        BlockState[] states = new BlockState[heightAccessor.getHeight()];
        BlockState stone = Blocks.STONE.defaultBlockState();
        BlockState air = Blocks.AIR.defaultBlockState();
        for (int i = 0; i < states.length; i++) {
            states[i] = min + i < y ? stone : air;
        }
        return new NoiseColumn(min, states);
    }
}