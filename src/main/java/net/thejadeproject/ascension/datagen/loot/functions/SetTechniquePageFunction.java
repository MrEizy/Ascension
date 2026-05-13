package net.thejadeproject.ascension.datagen.loot.functions;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.thejadeproject.ascension.AscensionCraft;
import net.thejadeproject.ascension.common.items.techniques.TechniquePageItem;

public class SetTechniquePageFunction implements LootItemFunction {

    public static final DeferredRegister<LootItemFunctionType<?>> LOOT_FUNCTION_TYPES =
            DeferredRegister.create(Registries.LOOT_FUNCTION_TYPE, AscensionCraft.MOD_ID);

    public static final MapCodec<SetTechniquePageFunction> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            ResourceLocation.CODEC.fieldOf("techniqueId").forGetter(f -> f.techniqueId),
            Codec.INT.fieldOf("minPage").forGetter(f -> f.minPage),
            Codec.INT.fieldOf("maxPage").forGetter(f -> f.maxPage)
    ).apply(inst, SetTechniquePageFunction::new));

    public static final DeferredHolder<LootItemFunctionType<?>, LootItemFunctionType<SetTechniquePageFunction>> TYPE =
            LOOT_FUNCTION_TYPES.register("set_technique_page", () -> new LootItemFunctionType<>(CODEC));

    private final ResourceLocation techniqueId;
    private final int minPage;
    private final int maxPage;

    public SetTechniquePageFunction(ResourceLocation techniqueId, int minPage, int maxPage) {
        this.techniqueId = techniqueId;
        this.minPage = minPage;
        this.maxPage = maxPage;
    }

    @Override
    public ItemStack apply(ItemStack stack, LootContext context) {
        int page = minPage;
        if (minPage != maxPage) {
            page = minPage + context.getRandom().nextInt(maxPage - minPage + 1);
        }
        ItemStack result = TechniquePageItem.createWithTechnique(techniqueId.toString(), page);
        result.setCount(stack.getCount());
        return result;
    }

    @Override
    public LootItemFunctionType<SetTechniquePageFunction> getType() {
        return TYPE.get();
    }

    public static Builder builder(ResourceLocation techniqueId, int page) {
        return new Builder(techniqueId, page, page);
    }

    public static Builder builder(ResourceLocation techniqueId, int minPage, int maxPage) {
        return new Builder(techniqueId, minPage, maxPage);
    }

    public static class Builder implements LootItemFunction.Builder {
        private final ResourceLocation techniqueId;
        private final int minPage;
        private final int maxPage;

        public Builder(ResourceLocation techniqueId, int minPage, int maxPage) {
            this.techniqueId = techniqueId;
            this.minPage = minPage;
            this.maxPage = maxPage;
        }

        @Override
        public LootItemFunction build() {
            return new SetTechniquePageFunction(techniqueId, minPage, maxPage);
        }
    }
}
