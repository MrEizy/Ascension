package net.thejadeproject.ascension.common.blocks.custom.crops.jadedew;

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
import net.thejadeproject.ascension.common.blocks.custom.crops.CropAgeCache;
import net.thejadeproject.ascension.common.items.ModItems;
import net.thejadeproject.ascension.common.items.data_components.ModDataComponents;
import net.thejadeproject.ascension.common.items.herbs.HerbQuality;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * 7-stage grass-type crop for Jade Dew Grass.
 *
 * ── Growth speed reference (default randomTickSpeed = 3) ─────────────────
 * One random tick fires on average every ~68.27 seconds per block.
 * Expected time per stage  =  68.27s / growthChance
 * Total time (7 stages)    =  7 × (68.27s / growthChance)
 *
 *   growthChance  │  avg per stage  │  avg total (7 stages)
 *   ──────────────┼─────────────────┼──────────────────────
 *   0.044         │  ~26 min        │  ~3 h          ← SPEED_SLOW (default, max ceiling)
 *   0.07          │  ~16 min        │  ~1.9 h        ← SPEED_MEDIUM
 *   0.13          │  ~8.7 min       │  ~1 h          ← SPEED_FAST
 */
public class JadeDewGrassCropBlock extends CropBlock {

    // ── Growth speed presets ──────────────────────────────────────────────
    /** ~3 h total (7 stages). The slowest recommended ceiling. */
    public static final float SPEED_SLOW   = 0.044f;
    /** ~1.9 h total. */
    public static final float SPEED_MEDIUM = 0.07f;
    /** ~1 h total. */
    public static final float SPEED_FAST   = 0.13f;

    public static final int MAX_AGE = 7;
    public static final IntegerProperty AGE = IntegerProperty.create("age", 0, 7);

    private static final VoxelShape[] SHAPE_BY_AGE = {
            Block.box(0, 0, 0, 16, 2, 16),
            Block.box(0, 0, 0, 16, 3, 16),
            Block.box(0, 0, 0, 16, 4, 16),
            Block.box(0, 0, 0, 16, 5, 16),
            Block.box(0, 0, 0, 16, 6, 16),
            Block.box(0, 0, 0, 16, 7, 16),
            Block.box(0, 0, 0, 16, 8, 16),
            Block.box(0, 0, 0, 16, 9, 16)
    };

    private final Supplier<? extends ItemLike> seedItem;
    private final float                        growthChance;

    // ── Constructors ──────────────────────────────────────────────────────

    /**
     * Full constructor. Accepts a Supplier<ItemLike> so the seed item
     * is resolved lazily — pass () -> ModItems.YOUR_ITEM.get() to avoid
     * the circular ModBlocks <-> ModItems static-initialiser NPE.
     *
     * @param seedItem      Lazy supplier for the seed/drop item.
     * @param growthChance  Probability (0-1) of advancing one age stage per random tick.
     *                      Use the SPEED_* constants or pass a custom float.
     */
    public JadeDewGrassCropBlock(Properties properties, Supplier<? extends ItemLike> seedItem, float growthChance) {
        super(properties);
        this.seedItem     = seedItem;
        this.growthChance = growthChance;
    }

    /** Defaults to SPEED_SLOW (~3 h total). */
    public JadeDewGrassCropBlock(Properties properties, Supplier<? extends ItemLike> seedItem) {
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
            // Only stamp the herb item (JADE_DEW_GRASS), not the seeds
            if (!drop.isEmpty() && drop.getItem() == ModItems.JADE_DEW_GRASS.get()) {
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