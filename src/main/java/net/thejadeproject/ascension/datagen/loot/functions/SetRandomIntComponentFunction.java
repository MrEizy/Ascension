package net.thejadeproject.ascension.datagen.loot.functions;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.thejadeproject.ascension.AscensionCraft;

public class SetRandomIntComponentFunction implements LootItemFunction {

    public static final DeferredRegister<LootItemFunctionType<?>> LOOT_FUNCTION_TYPES =
            DeferredRegister.create(Registries.LOOT_FUNCTION_TYPE, AscensionCraft.MOD_ID);

    public static final MapCodec<SetRandomIntComponentFunction> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            ResourceLocation.CODEC.fieldOf("component").forGetter(f -> BuiltInRegistries.DATA_COMPONENT_TYPE.getKey(f.component)),
            Codec.INT.fieldOf("min").forGetter(f -> f.min),
            Codec.INT.fieldOf("max").forGetter(f -> f.max)
    ).apply(inst, (id, min, max) -> {
        DataComponentType<?> raw = BuiltInRegistries.DATA_COMPONENT_TYPE.get(id);
        if (raw == null) throw new IllegalStateException("Unknown data component: " + id);
        @SuppressWarnings("unchecked")
        DataComponentType<Integer> component = (DataComponentType<Integer>) raw;
        return new SetRandomIntComponentFunction(component, min, max);
    }));

    public static final DeferredHolder<LootItemFunctionType<?>, LootItemFunctionType<SetRandomIntComponentFunction>> TYPE =
            LOOT_FUNCTION_TYPES.register("set_random_int_component",
                    () -> new LootItemFunctionType<>(CODEC));

    private final DataComponentType<Integer> component;
    private final int min;
    private final int max;

    public SetRandomIntComponentFunction(DataComponentType<Integer> component, int min, int max) {
        this.component = component;
        this.min = min;
        this.max = max;
    }

    @Override
    public ItemStack apply(ItemStack stack, LootContext context) {
        int value = min + context.getRandom().nextInt(max - min + 1);
        stack.set(component, value);
        return stack;
    }

    @Override
    public LootItemFunctionType<SetRandomIntComponentFunction> getType() {
        return TYPE.get();
    }

    /** Use this in datagen */
    public static Builder builder(DataComponentType<Integer> component, int min, int max) {
        return new Builder(component, min, max);
    }

    public static class Builder implements LootItemFunction.Builder {
        private final DataComponentType<Integer> component;
        private final int min;
        private final int max;

        public Builder(DataComponentType<Integer> component, int min, int max) {
            this.component = component;
            this.min = min;
            this.max = max;
        }

        @Override
        public LootItemFunction build() {
            return new SetRandomIntComponentFunction(component, min, max);
        }
    }
}