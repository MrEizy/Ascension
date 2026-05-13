package net.thejadeproject.ascension.datagen.loot.conditions;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;
import net.thejadeproject.ascension.common.items.techniques.TechniqueTransferItem;

public class AddTechniqueManualModifier extends LootModifier {

    public static final MapCodec<AddTechniqueManualModifier> CODEC = RecordCodecBuilder.mapCodec(inst ->
            codecStart(inst)
                    .and(ResourceLocation.CODEC.fieldOf("techniqueId").forGetter(m -> m.techniqueId))
                    .apply(inst, AddTechniqueManualModifier::new)
    );

    private final ResourceLocation techniqueId;

    public AddTechniqueManualModifier(LootItemCondition[] conditionsIn, ResourceLocation techniqueId) {
        super(conditionsIn);
        this.techniqueId = techniqueId;
    }

    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        generatedLoot.add(TechniqueTransferItem.createWithTechnique(techniqueId.toString()));
        return generatedLoot;
    }

    @Override
    public MapCodec<? extends IGlobalLootModifier> codec() {
        return CODEC;
    }
}
