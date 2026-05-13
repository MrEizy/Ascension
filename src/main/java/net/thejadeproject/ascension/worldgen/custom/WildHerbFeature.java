package net.thejadeproject.ascension.worldgen.custom;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.thejadeproject.ascension.common.blocks.custom.crops.CropAgeCache;
import net.thejadeproject.ascension.common.blocks.custom.crops.GenericSlowCropBlock;
import net.thejadeproject.ascension.common.blocks.custom.crops.StemSlowCropBlock;
import net.thejadeproject.ascension.common.blocks.custom.crops.jadedew.JadeDewGrassCropBlock;
import net.thejadeproject.ascension.common.items.herbs.HerbQuality;

public class WildHerbFeature extends Feature<WildHerbFeatureConfig> {

    public WildHerbFeature(Codec<WildHerbFeatureConfig> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<WildHerbFeatureConfig> ctx) {
        var config = ctx.config();
        var level  = ctx.level();   // WorldGenLevel
        var origin = ctx.origin();
        var random = ctx.random();

        // ── Find the surface ──────────────────────────────────────────────────
        // Try scanning upward first (origin may be inside terrain), then downward.
        BlockPos groundPos = findGround(level, origin, true);
        if (groundPos == null) groundPos = findGround(level, origin, false);
        if (groundPos == null) return false;

        BlockPos plantPos   = groundPos.above();
        BlockState groundState = level.getBlockState(groundPos);

        // ── Validate ground block ─────────────────────────────────────────────
        boolean validGround = config.validGround().stream().anyMatch(b -> groundState.is(b));
        if (!validGround) return false;

        // ── Validate plant position ───────────────────────────────────────────
        // Must be air. Do NOT call cropState.canSurvive() — CropBlock hardcodes
        // a farmland check that would always reject wild herb placement.
        if (!level.getBlockState(plantPos).isAir()) return false;

        // ── Build fully-grown crop state ──────────────────────────────────────
        BlockState cropState;
        if (config.cropBlock() instanceof GenericSlowCropBlock generic) {
            cropState = generic.getStateForAge(GenericSlowCropBlock.MAX_AGE);
        } else if (config.cropBlock() instanceof StemSlowCropBlock stem) {
            cropState = stem.getStateForAge(StemSlowCropBlock.MAX_AGE);
        } else if (config.cropBlock() instanceof JadeDewGrassCropBlock jadeDew) {
            cropState = jadeDew.getStateForAge(JadeDewGrassCropBlock.MAX_AGE);
        } else {
            cropState = config.cropBlock().defaultBlockState();
        }

        // ── Place and stamp ───────────────────────────────────────────────────
        level.setBlock(plantPos, cropState, 2);

        long ageRange   = HerbQuality.AGE_ELDER - HerbQuality.AGE_MATURE;
        long wildAge    = HerbQuality.AGE_MATURE + (long)(random.nextDouble() * ageRange);
        int  quality    = HerbQuality.rollQuality();
        ServerLevel sl  = level.getLevel();
        CropAgeCache.store(sl, plantPos, sl.getGameTime() - wildAge, quality);

        return true;
    }

    private static BlockPos findGround(net.minecraft.world.level.WorldGenLevel level,
                                       BlockPos origin, boolean upward) {
        BlockPos scan = origin;
        for (int i = 0; i < 32; i++) {
            BlockState here  = level.getBlockState(scan);
            BlockState above = level.getBlockState(scan.above());
            if (!here.isAir() && above.isAir()) return scan;
            scan = upward ? scan.above() : scan.below();
        }
        return null;
    }
}