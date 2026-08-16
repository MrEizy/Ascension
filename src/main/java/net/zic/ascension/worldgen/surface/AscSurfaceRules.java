package net.zic.ascension.worldgen.surface;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.SurfaceRules;
import net.zic.ascension.worldgen.biome.AscBiomes;

public final class AscSurfaceRules {
    private AscSurfaceRules() {
    }

    public static SurfaceRules.RuleSource overworld(SurfaceRules.RuleSource vanillaFallback) {
        return SurfaceRules.sequence(
                SurfaceRules.ifTrue(
                        SurfaceRules.isBiome(AscBiomes.HEAVENREACH_PEAKS),
                        SurfaceRules.state(Blocks.STONE.defaultBlockState())
                ),
                vanillaFallback
        );
    }
}
