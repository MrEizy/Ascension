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
import org.jetbrains.annotations.NotNull;

public class AddPhysiqueItemModifier extends LootModifier {
    public static final MapCodec<AddPhysiqueItemModifier> CODEC = RecordCodecBuilder.mapCodec(inst ->
            codecStart(inst)
                    .and(ResourceLocation.CODEC.fieldOf("physiqueId").forGetter(m -> m.physiqueId))
                    .and(Codec.INT.optionalFieldOf("purity", 100).forGetter(m -> m.purity)) // Default to 100 if not specified
                    .apply(inst, AddPhysiqueItemModifier::new)
    );

    private final ResourceLocation physiqueId;
    private final int purity;

    public AddPhysiqueItemModifier(LootItemCondition[] conditionsIn, ResourceLocation physiqueId, int purity) {
        super(conditionsIn);
        this.physiqueId = physiqueId;
        this.purity = Math.min(Math.max(purity, 1), 100);
    }

    public AddPhysiqueItemModifier(LootItemCondition[] conditionsIn, String physiqueId) {
        this(conditionsIn, ResourceLocation.parse(physiqueId), 100);
    }

    public AddPhysiqueItemModifier(LootItemCondition[] conditionsIn, String physiqueId, int purity) {
        this(conditionsIn, ResourceLocation.parse(physiqueId), purity);
    }

    public AddPhysiqueItemModifier(LootItemCondition[] conditionsIn, ResourceLocation physiqueId) {
        this(conditionsIn, physiqueId, 100);
    }

    @Override
    protected @NotNull ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        ItemStack stack = new ItemStack(ModItems.PHYSIQUE_ESSENCE.get());
        stack.set(ModDataComponents.PHYSIQUE_ID.get(), physiqueId.toString());
        stack.set(ModDataComponents.PURITY.get(), purity);
        generatedLoot.add(stack);
        return generatedLoot;
    }

    @Override
    public MapCodec<? extends IGlobalLootModifier> codec() {
        return CODEC;
    }
}