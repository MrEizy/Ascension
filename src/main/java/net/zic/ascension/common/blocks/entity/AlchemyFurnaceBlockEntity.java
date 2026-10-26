package net.zic.ascension.common.blocks.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.zic.ascension.api.ascension.core.alchemy.AlchemyContext;
import net.zic.ascension.api.ascension.core.alchemy.AlchemyMaterial;
import net.zic.ascension.api.ascension.core.alchemy.AlchemyMergeResolver;
import net.zic.ascension.api.ascension.core.alchemy.AlchemyProcessHooks;
import net.zic.ascension.api.ascension.core.alchemy.AlchemyRefinementResolver;
import net.zic.ascension.api.ascension.core.alchemy.medicinal.MedicinalComposition;
import net.zic.ascension.api.ascension.core.alchemy.medicinal.MedicinalEffectResolver;
import net.zic.ascension.api.ascension.core.alchemy.medicinal.MedicinalPillFactory;
import net.zic.ascension.api.ascension.core.alchemy.medicinal.MedicinalReactionResolver;

public class AlchemyFurnaceBlockEntity extends BlockEntity {
    public static final int MAX_INGREDIENTS = 12;

    private MedicinalComposition composition = MedicinalComposition.EMPTY;
    private int ingredientCount;

    public AlchemyFurnaceBlockEntity(BlockPos pos, BlockState state) {
        super(AscBlockEntities.ALCHEMY_FURNACE_BE.get(), pos, state);
    }

    public boolean canInsert() {
        return ingredientCount < MAX_INGREDIENTS;
    }

    public boolean isEmpty() {
        return ingredientCount == 0 || composition.isEmpty();
    }

    public int ingredientCount() {
        return ingredientCount;
    }

    public MedicinalReactionResolver.Step insert(AlchemyMaterial material, AlchemyContext context) {
        if (material == null || material.isEmpty() || !canInsert() || context == null || context.level() == null) return null;
        AlchemyRefinementResolver.RefinementResult refinement = AlchemyRefinementResolver.refine(material, context);
        if (refinement.isEmpty()) return null;
        AlchemyProcessHooks.ProcessSettings settings = AlchemyProcessHooks.resolve(context);
        MedicinalReactionResolver.Step result = MedicinalReactionResolver.add(composition, ingredientCount,
                refinement.substance(), settings.safeEnergyDelta(), settings.explosionEnergyDelta(),
                context.level().registryAccess());
        if (result.outcome() == AlchemyMergeResolver.Outcome.CATASTROPHIC) {
            composition = MedicinalComposition.EMPTY;
            ingredientCount = 0;
        } else {
            composition = result.composition();
            ingredientCount = result.ingredientCount();
        }
        setChanged();
        return result;
    }

    public ItemStack condense(AlchemyContext context) {
        if (isEmpty() || context == null || context.level() == null) return ItemStack.EMPTY;
        ItemStack pill = MedicinalPillFactory.condense(
                MedicinalEffectResolver.resolve(composition, context.level().registryAccess()), ingredientCount);
        clear();
        return pill;
    }

    public void clear() {
        composition = MedicinalComposition.EMPTY;
        ingredientCount = 0;
        setChanged();
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.store("MedicinalComposition", MedicinalComposition.CODEC, composition);
        output.putInt("IngredientCount", ingredientCount);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        composition = input.read("MedicinalComposition", MedicinalComposition.CODEC).orElse(MedicinalComposition.EMPTY);
        ingredientCount = Math.max(0, Math.min(MAX_INGREDIENTS, input.getIntOr("IngredientCount", 0)));
        if (composition.isEmpty()) ingredientCount = 0;
    }
}
