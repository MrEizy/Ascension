package net.zic.ascension.impl.core.innerworld;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.common.herbs.HerbDefinition;

import java.util.Optional;

/**
 * BEST-EFFORT herb placement, until I can see your herb block code. Everything that decides WHERE a herb goes and WHAT
 * age and quality it gets is done (InnerWorldHerbs + SeededWorld.herbAt); this only has to put it into the world.
 *
 * What it does: looks up a block registered under the herb's own id (your item models suggest "ascension:<herb id>"),
 * sets an integer blockstate property named stage/age/growth_stage to the growth stage if the block has one, and places
 * it. What it CANNOT do: apply the age tier and quality tier, because I don't know where your herb stores them
 * (a block entity? AscensionComponents.HerbData?). So herbs spawn, but start with whatever your block's defaults are.
 * It logs once saying exactly that, so you know. Paste the herb block + block entity + your worldgen placer and I'll apply
 * age and quality properly.
 */
final class InnerWorldHerbPlacer {
    private InnerWorldHerbPlacer() {
    }

    private static boolean warnedMissing;
    private static boolean warnedData;

    static boolean place(ServerLevel level, BlockPos pos, HerbDefinition herb, int ageTier, int qualityTier, int growthStage) {
        Optional<Block> block = BuiltInRegistries.BLOCK.getOptional(herb.id());
        if (block.isEmpty()) {
            if (!warnedMissing) {
                warnedMissing = true;
                AscensionCraft.LOGGER.warn("Inner World herbs: no block is registered as {} (InnerWorldHerbPlacer), so it cannot be placed.", herb.id());
            }
            return false;
        }

        BlockState state = block.get().defaultBlockState();
        for (Property<?> property : state.getProperties()) {
            String name = property.getName();
            if (property instanceof IntegerProperty integer && (name.equals("stage") || name.equals("age") || name.equals("growth_stage"))) {
                int min = Integer.MAX_VALUE;
                int max = Integer.MIN_VALUE;
                for (int value : integer.getPossibleValues()) {
                    min = Math.min(min, value);
                    max = Math.max(max, value);
                }
                state = state.setValue(integer, Math.max(min, Math.min(max, growthStage)));
            }
        }
        level.setBlock(pos, state, 2);

        if (!warnedData) {
            warnedData = true;
            AscensionCraft.LOGGER.warn("Inner World herbs are being placed by registry id only: age tier and quality tier are NOT applied until InnerWorldHerbPlacer is wired to your herb data (age {}, quality {} were decided for {}).", ageTier, qualityTier, herb.id());
        }
        return true;
    }
}