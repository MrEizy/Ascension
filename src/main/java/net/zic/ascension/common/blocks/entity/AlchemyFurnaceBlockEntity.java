package net.zic.ascension.common.blocks.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.zic.ascension.api.ascension.core.alchemy.AlchemyBatch;
import net.zic.ascension.api.ascension.core.alchemy.AlchemyContext;
import net.zic.ascension.api.ascension.core.alchemy.AlchemyMaterial;
import net.zic.ascension.api.ascension.core.alchemy.AlchemyMergeResolver;
import net.zic.ascension.api.ascension.core.alchemy.AlchemyRefinementResolver;
import net.zic.ascension.api.ascension.core.alchemy.AlchemyFormulaResolver;

public class AlchemyFurnaceBlockEntity extends BlockEntity {
    public static final int MAX_INGREDIENTS = 12;

    private AlchemyBatch batch = AlchemyBatch.EMPTY;

    public AlchemyFurnaceBlockEntity(BlockPos pos, BlockState state) {
        super(AscBlockEntities.ALCHEMY_FURNACE_BE.get(), pos, state);
    }

    public boolean canInsert() {
        return batch.ingredientCount() < MAX_INGREDIENTS;
    }

    public boolean isEmpty() {
        return batch.isEmpty();
    }

    public int ingredientCount() {
        return batch.ingredientCount();
    }

    public AlchemyMergeResolver.MergeResult insert(AlchemyMaterial material, AlchemyContext context) {
        if (material == null || material.isEmpty() || !canInsert()) {
            return null;
        }

        AlchemyRefinementResolver.RefinementResult refinement = AlchemyRefinementResolver.refine(material, context);
        if (refinement.isEmpty()) {
            return null;
        }

        AlchemyMergeResolver.MergeResult result = AlchemyMergeResolver.merge(batch, refinement.substance(), context);
        batch = result.outcome() == AlchemyMergeResolver.Outcome.CATASTROPHIC ? AlchemyBatch.EMPTY : result.batch();
        setChanged();
        return result;
    }

    public AlchemyFormulaResolver.CondensationResult condense(AlchemyContext context) {
        AlchemyFormulaResolver.CondensationResult result = AlchemyFormulaResolver.condense(batch, context);
        batch = AlchemyBatch.EMPTY;
        setChanged();
        return result;
    }

    public void clear() {
        batch = AlchemyBatch.EMPTY;
        setChanged();
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.store("Batch", AlchemyBatch.CODEC, batch);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        batch = input.read("Batch", AlchemyBatch.CODEC).orElse(AlchemyBatch.EMPTY);
    }
}
