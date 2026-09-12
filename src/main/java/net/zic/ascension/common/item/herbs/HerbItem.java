package net.zic.ascension.common.item.herbs;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.zic.ascension.api.ascension.core.alchemy.AlchemySubstance;
import net.zic.ascension.common.blocks.crops.herbs.HerbCropBlock;
import net.zic.ascension.common.herbs.HerbDefinition;
import net.zic.ascension.common.item.components.AscensionComponents;
import net.zic.ascension.common.util.ModTags;

import java.util.function.Supplier;

public class HerbItem extends Item implements AlchemySubstance.Provider {
    private final HerbDefinition definition;
    private final Supplier<? extends Block> crop;

    public HerbItem(Properties properties, HerbDefinition definition) {
        this(properties, definition, null);
    }

    public HerbItem(Properties properties, HerbDefinition definition, Supplier<? extends Block> crop) {
        super(properties.component(AscensionComponents.HERB_DATA.get(), AscensionComponents.HerbData.DEFAULT));
        this.definition = definition;
        this.crop = crop;
    }

    public HerbDefinition definition() {
        return definition;
    }

    public AscensionComponents.HerbData data(ItemStack stack) {
        return stack.getOrDefault(AscensionComponents.HERB_DATA.get(), AscensionComponents.HerbData.DEFAULT);
    }

    public void applyHerbEffects(LivingEntity entity, ItemStack stack) {
        definition.applyEffects(entity, stack, data(stack));
    }

    @Override
    public AlchemySubstance.Material alchemyMaterial(ItemStack stack) {
        return new AlchemySubstance.Material(definition.alchemySubstance(data(stack)), definition.alchemyRefinementDifficulty());
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (definition.plantingType() != HerbDefinition.PlantingType.DIRECT || crop == null) {
            return super.useOn(context);
        }
        return plant(context, crop);
    }

    private static InteractionResult plant(UseOnContext context, Supplier<? extends Block> crop) {
        if (context.getClickedFace() != Direction.UP) {
            return InteractionResult.PASS;
        }

        Level level = context.getLevel();
        BlockPos soilPos = context.getClickedPos();
        BlockPos plantPos = soilPos.above();
        if (!level.getBlockState(soilPos).is(ModTags.Blocks.HERB_SOILS) || !level.isEmptyBlock(plantPos)) {
            return InteractionResult.PASS;
        }

        Block block = crop.get();
        BlockState state = block.defaultBlockState();
        if (block instanceof HerbCropBlock) {
            state = ((HerbCropBlock) block).plantedState();
        }

        if (!state.canSurvive(level, plantPos)) {
            return InteractionResult.FAIL;
        }

        Player player = context.getPlayer();
        if (!level.isClientSide()) {
            level.setBlock(plantPos, state, 3);
            if (player == null || !player.getAbilities().instabuild) {
                context.getItemInHand().shrink(1);
            }
        }
        return InteractionResult.SUCCESS;
    }

    public static class Seed extends Item {
        private final Supplier<? extends Block> crop;

        public Seed(Properties properties, Supplier<? extends Block> crop) {
            super(properties);
            this.crop = crop;
        }

        public Block cropBlock() {
            return crop.get();
        }

        @Override
        public InteractionResult useOn(UseOnContext context) {
            return HerbItem.plant(context, crop);
        }
    }
}
