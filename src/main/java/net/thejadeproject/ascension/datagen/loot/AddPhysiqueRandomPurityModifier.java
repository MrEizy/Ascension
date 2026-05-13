package net.thejadeproject.ascension.datagen.loot;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;
import net.thejadeproject.ascension.common.items.data_components.ModDataComponents;
import net.thejadeproject.ascension.common.items.ModItems;

public class AddPhysiqueRandomPurityModifier extends LootModifier {
    public static final MapCodec<AddPhysiqueRandomPurityModifier> CODEC = RecordCodecBuilder.mapCodec(
            inst ->
                    codecStart(inst)
                            .and(ResourceLocation.CODEC.fieldOf("physiqueId").forGetter(m -> m.physiqueId))
                            .and(Codec.INT.optionalFieldOf("minPurity", 1).forGetter(m -> m.minPurity))
                            .and(Codec.INT.optionalFieldOf("maxPurity", 100).forGetter(m -> m.maxPurity))
                            .apply(inst, AddPhysiqueRandomPurityModifier::new)
    );

    private final ResourceLocation physiqueId;
    private final int minPurity;
    private final int maxPurity;

    public AddPhysiqueRandomPurityModifier(LootItemCondition[] conditionsIn, ResourceLocation physiqueId, int minPurity, int maxPurity) {
        super(conditionsIn);
        this.physiqueId = physiqueId;
        this.minPurity = Math.max(1, minPurity);
        this.maxPurity = Math.min(100, Math.max(minPurity, maxPurity));
    }

    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        ItemStack stack = new ItemStack(ModItems.PHYSIQUE_ESSENCE.get());
        stack.set(ModDataComponents.PHYSIQUE_ID.get(), physiqueId.toString());

        int purity = minPurity + context.getRandom().nextInt(maxPurity - minPurity +1);
        stack.set(ModDataComponents.PURITY.get(), purity);

        generatedLoot.add(stack);
        return generatedLoot;
    }

    @Override
    public MapCodec<? extends IGlobalLootModifier> codec() {
        return CODEC;
    }
}
