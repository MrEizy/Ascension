package net.thejadeproject.ascension.common.blocks.custom.crops;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.thejadeproject.ascension.common.items.data_components.ModDataComponents;
import net.thejadeproject.ascension.common.items.ModItems;
import net.thejadeproject.ascension.common.items.herbs.HerbQuality;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Slow-growing generic crop for Ascension herbs.
 *
 * ── Growth speed reference (default randomTickSpeed = 3) ─────────────────
 * One random tick fires on average every ~68.27 seconds per block.
 * Expected time per stage  =  68.27s / growthChance
 * Total time (3 stages)    =  3 × (68.27s / growthChance)
 *
 *   growthChance  │  avg per stage  │  avg total (3 stages)
 *   ──────────────┼─────────────────┼──────────────────────
 *   0.014         │  ~81 min        │  ~4 h          ← DEFAULT (max realistic ceiling)
 *   0.025         │  ~45 min        │  ~2.3 h
 *   0.05          │  ~23 min        │  ~1.1 h
 *
 * Use HerbGrowthSpeed constants below for named presets.
 *
 * ── Age & Quality stamping ────────────────────────────────────────────────
 * When the crop first reaches max age (3) via randomTick, the game-time is
 * stored in CropAgeCache. On harvest, (currentTime - grownSince) is stamped
 * as HERB_AGE_TICKS and quality is copied to every drop ItemStack.
 */
public class GenericSlowCropBlock extends CropBlock {

    // ── Growth speed presets ──────────────────────────────────────────────
    /** ~4 h total (3 stages). The slowest recommended ceiling. */
    public static final float SPEED_SLOW   = 0.014f;
    /** ~2.3 h total. */
    public static final float SPEED_MEDIUM = 0.025f;
    /** ~1.1 h total. A faster common-tier herb. */
    public static final float SPEED_FAST   = 0.05f;

    public static final int MAX_AGE = 3;
    public static final IntegerProperty AGE = IntegerProperty.create("age", 0, 3);

    private static final VoxelShape[] SHAPE_BY_AGE = {
            Block.box(0, 0, 0, 16, 2, 16),
            Block.box(0, 0, 0, 16, 4, 16),
            Block.box(0, 0, 0, 16, 6, 16),
            Block.box(0, 0, 0, 16, 8, 16)
    };

    private final Supplier<? extends ItemLike> seedItem;
    private final float                        growthChance;

    // ── Constructors ──────────────────────────────────────────────────────

    /** Convenience factory — kept for backwards compatibility. */
    public static GenericSlowCropBlock createHundredYearGinseng(Properties properties) {
        return new GenericSlowCropBlock(properties, ModItems.HUNDRED_YEAR_GINSENG, SPEED_SLOW);
    }

    /**
     * Full constructor. Accepts a {@code Supplier<ItemLike>} so the seed item
     * is resolved lazily — pass {@code () -> ModItems.YOUR_ITEM.get()} to avoid
     * the circular ModBlocks <-> ModItems static-initialiser NPE.
     *
     * @param seedItem      Lazy supplier for the seed/drop item.
     * @param growthChance  Probability (0-1) of advancing one age stage per random tick.
     *                      Use the SPEED_* constants or pass a custom float.
     */
    public GenericSlowCropBlock(Properties properties, Supplier<? extends ItemLike> seedItem, float growthChance) {
        super(properties);
        this.seedItem     = seedItem;
        this.growthChance = growthChance;
    }

    /** Defaults to SPEED_SLOW (~4 h total). */
    public GenericSlowCropBlock(Properties properties, Supplier<? extends ItemLike> seedItem) {
        this(properties, seedItem, SPEED_SLOW);
    }

    // ── Shape / Age ───────────────────────────────────────────────────────

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        return SHAPE_BY_AGE[state.getValue(AGE)];
    }

    @Override protected ItemLike getBaseSeedId()       { return seedItem.get(); }
    @Override public  IntegerProperty getAgeProperty() { return AGE; }
    @Override public  int getMaxAge()                  { return MAX_AGE; }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AGE);
    }

    // ── Growth ────────────────────────────────────────────────────────────

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!level.isAreaLoaded(pos, 1)) return;
        if (level.getRawBrightness(pos, 0) < 9) return;

        int currentAge = getAge(state);
        if (currentAge >= getMaxAge()) return;

        if (random.nextFloat() < growthChance) {
            int nextAge = currentAge + 1;
            level.setBlock(pos, getStateForAge(nextAge), 2);

            if (nextAge == getMaxAge()) {
                CropAgeCache.store(level, pos, level.getGameTime(), HerbQuality.rollQuality());
            }
        }
    }

    // ── Drops ─────────────────────────────────────────────────────────────

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        List<ItemStack> drops = new ArrayList<>(super.getDrops(state, builder));
        if (getAge(state) < getMaxAge()) return drops;

        ServerLevel level = null;
        BlockPos pos = null;
        try {
            if (builder.getLevel() instanceof ServerLevel sl) level = sl;
            var origin = builder.getOptionalParameter(
                    net.minecraft.world.level.storage.loot.parameters.LootContextParams.ORIGIN);
            if (origin != null) pos = BlockPos.containing(origin);
        } catch (Exception ignored) {}

        long ageTicks = 0L;
        int  quality  = HerbQuality.BASIC;

        if (level != null && pos != null) {
            CropAgeCache.CropData data = CropAgeCache.retrieve(level, pos);
            if (data != null) {
                ageTicks = Math.max(0, level.getGameTime() - data.grownSince());
                quality  = data.quality();
                CropAgeCache.remove(level, pos);
            }
        }

        for (ItemStack drop : drops) {
            if (!drop.isEmpty()) {
                drop.set(ModDataComponents.HERB_AGE_TICKS.get(), ageTicks);
                drop.set(ModDataComponents.HERB_QUALITY.get(), quality);
            }
        }

        return drops;
    }

    // ── Bonemeal disabled ─────────────────────────────────────────────────

    @Override public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state) { return false; }
    @Override public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) { return false; }
    @Override protected int getBonemealAgeIncrease(Level level) { return 0; }
}