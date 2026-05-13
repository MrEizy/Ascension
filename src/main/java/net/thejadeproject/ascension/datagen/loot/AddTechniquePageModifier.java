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
import net.thejadeproject.ascension.common.items.techniques.TechniquePageItem;

public class AddTechniquePageModifier extends LootModifier {

    public static final MapCodec<AddTechniquePageModifier> CODEC = RecordCodecBuilder.mapCodec(inst ->
            codecStart(inst)
                    .and(ResourceLocation.CODEC.fieldOf("techniqueId").forGetter(m -> m.techniqueId))
                    .and(Codec.INT.fieldOf("pageIndex").forGetter(m -> m.pageIndex))
                    .apply(inst, AddTechniquePageModifier::new)
    );

    private final ResourceLocation techniqueId;
    private final int pageIndex;

    public AddTechniquePageModifier(LootItemCondition[] conditionsIn, ResourceLocation techniqueId, int pageIndex) {
        super(conditionsIn);
        this.techniqueId = techniqueId;
        this.pageIndex = pageIndex;
    }

    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        generatedLoot.add(TechniquePageItem.createWithTechnique(techniqueId.toString(), pageIndex));
        return generatedLoot;
    }

    @Override
    public MapCodec<? extends IGlobalLootModifier> codec() {
        return CODEC;
    }
}