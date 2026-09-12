package net.zic.ascension.common.blocks.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.zic.ascension.api.ascension.core.alchemy.AlchemyBatch;
import net.zic.ascension.api.ascension.core.alchemy.AlchemySubstance;
import net.zic.ascension.common.item.artifacts.pills.ModPills;

import java.util.Optional;

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

    public Optional<ItemStack> insert(AlchemySubstance substance) {
        if (substance == null || substance.isEmpty() || !canInsert()) {
            return Optional.empty();
        }

        batch = batch.merge(substance, Double.MAX_VALUE, Double.MAX_VALUE).batch();
        Optional<ItemStack> result = ModPills.condense(batch);
        if (result.isPresent()) {
            batch = AlchemyBatch.EMPTY;
        }
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
