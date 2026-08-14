package net.zic.ascension.common.blocks.crops.herbs;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.zic.ascension.common.herbs.HerbDefinition;
import net.zic.ascension.common.item.components.AscensionComponents;
import net.zic.ascension.common.util.ModTags;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Supplier;

/**
 * Generic ground-growing herb crop
 */
public class HerbCropBlock extends Block {
    public static final int MAX_STAGE = HerbDefinition.MAX_GROWTH_STAGES + HerbDefinition.MAX_AGE_TIERS - 2;
    public static final IntegerProperty STAGE = IntegerProperty.create("stage", 0, MAX_STAGE);
    public static final IntegerProperty QUALITY = IntegerProperty.create(
            "quality",
            0,
            HerbDefinition.Quality.values().length - 1
    );
    public static final BooleanProperty WILD = BooleanProperty.create("wild");

    private static final VoxelShape SHAPE = Block.box(1.0D, 0.0D, 1.0D, 15.0D, 14.0D, 15.0D);

    private final HerbDefinition definition;
    private final Supplier<? extends Item> harvestItem;

    public HerbCropBlock(
            Properties properties,
            HerbDefinition definition,
            Supplier<? extends Item> harvestItem
    ) {
        super(properties);
        this.definition = definition;
        this.harvestItem = harvestItem;
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(STAGE, 0)
                .setValue(QUALITY, definition.cultivatedQuality().ordinal())
                .setValue(WILD, false));
    }

    public HerbDefinition definition() {
        return definition;
    }

    public Item harvestItem() {
        return harvestItem.get();
    }

    public int growthStage(BlockState state) {
        return Math.min(state.getValue(STAGE), definition.maxGrowthStage());
    }

    public int ageTier(BlockState state) {
        return Mth.clamp(state.getValue(STAGE) - definition.maxGrowthStage(), 0, definition.maxAgeTier());
    }

    public int qualityTier(BlockState state) {
        return Mth.clamp(state.getValue(QUALITY), 0, HerbDefinition.Quality.values().length - 1);
    }

    public boolean isMature(BlockState state) {
        return state.getValue(STAGE) >= definition.maxGrowthStage();
    }

    public int maxStage() {
        return definition.maxGrowthStage() + definition.maxAgeTier();
    }

    public BlockState plantedState() {
        return defaultBlockState()
                .setValue(STAGE, 0)
                .setValue(QUALITY, definition.cultivatedQuality().ordinal())
                .setValue(WILD, false);
    }

    public BlockState matureState(boolean wild, int ageTier) {
        int qualityTier = wild
                ? HerbDefinition.Quality.COMMON.ordinal()
                : definition.cultivatedQuality().ordinal();
        return matureState(wild, ageTier, qualityTier);
    }

    public BlockState matureState(boolean wild, int ageTier, int qualityTier) {
        int stage = definition.maxGrowthStage() + Mth.clamp(ageTier, 0, definition.maxAgeTier());
        return defaultBlockState()
                .setValue(STAGE, stage)
                .setValue(QUALITY, Mth.clamp(qualityTier, 0, definition.qualityCap().ordinal()))
                .setValue(WILD, wild);
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return null;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(STAGE, QUALITY, WILD);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockPos pos = context.getClickedPos();
        BlockState below = context.getLevel().getBlockState(pos.below());
        if (!below.is(ModTags.Blocks.HERB_SOILS)) {
            return null;
        }
        return plantedState();
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockState below = level.getBlockState(pos.below());
        if (state.getValue(WILD)) {
            return definition.canWildSurviveOn(below);
        }
        return below.is(ModTags.Blocks.HERB_SOILS);
    }

    @Override
    protected boolean isRandomlyTicking(BlockState state) {
        return state.getValue(STAGE) < maxStage() || definition.canQualityAdvance(qualityTier(state));
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!canSurvive(state, level, pos)) {
            level.destroyBlock(pos, true);
            return;
        }

        double environmentMultiplier = definition.growthMultiplier(level, pos, state);
        if (environmentMultiplier <= 0.0D) {
            return;
        }

        int stage = state.getValue(STAGE);
        if (stage < definition.maxGrowthStage()) {
            double chance = Math.min(1.0D, definition.baseGrowthChance() * environmentMultiplier);
            if (random.nextDouble() < chance && definition.tryConsumeProgressQi(level, pos)) {
                level.setBlock(pos, state.setValue(STAGE, stage + 1), 2);
            }
            return;
        }

        BlockState nextState = state;
        boolean changed = false;

        int ageTier = ageTier(state);
        if (ageTier < definition.maxAgeTier()) {
            HerbDefinition.AgeThreshold threshold = definition.ageThreshold(ageTier);
            if (threshold.canAdvance()) {
                double chance = Math.min(1.0D, environmentMultiplier / threshold.averageRandomTicksToNext());
                if (random.nextDouble() < chance && definition.tryConsumeProgressQi(level, pos)) {
                    nextState = nextState.setValue(STAGE, stage + 1);
                    changed = true;
                }
            }
        }

        int qualityTier = qualityTier(state);
        double qualityChance = definition.qualityAdvanceChance(
                level,
                pos,
                nextState,
                state.getValue(WILD),
                qualityTier
        );
        if (qualityChance > 0.0D
                && random.nextDouble() < qualityChance
                && definition.tryConsumeProgressQi(level, pos)) {
            nextState = nextState.setValue(QUALITY, qualityTier + 1);
            changed = true;
        }

        if (changed) {
            level.setBlock(pos, nextState, 2);
        }
    }

    @Override
    protected BlockState updateShape(
            BlockState state,
            LevelReader level,
            ScheduledTickAccess scheduledTickAccess,
            BlockPos pos,
            Direction direction,
            BlockPos neighborPos,
            BlockState neighborState,
            RandomSource random
    ) {
        if (direction == Direction.DOWN && !canSurvive(state, level, pos)) {
            return Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(state, level, scheduledTickAccess, pos, direction, neighborPos, neighborState, random);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        List<ItemStack> drops = super.getDrops(state, params);

        if (!isMature(state)) {
            return drops;
        }

        boolean wild = state.getValue(WILD);
        int ageTier = ageTier(state);
        HerbDefinition.Quality grownQuality = HerbDefinition.Quality.byTier(qualityTier(state));

        Vec3 origin = params.getOptionalParameter(LootContextParams.ORIGIN);
        BlockPos pos = origin == null ? BlockPos.ZERO : BlockPos.containing(origin);
        HerbDefinition.Quality quality = definition.resolveQuality(
                params.getLevel(),
                pos,
                state,
                wild,
                grownQuality
        );

        AscensionComponents.HerbData herbData = new AscensionComponents.HerbData(
                ageTier,
                quality.ordinal(),
                wild
        );

        for (ItemStack drop : drops) {
            if (drop.getItem() == harvestItem.get()) {
                drop.set(AscensionComponents.HERB_DATA.get(), herbData);
            }
        }

        return drops;
    }
}
