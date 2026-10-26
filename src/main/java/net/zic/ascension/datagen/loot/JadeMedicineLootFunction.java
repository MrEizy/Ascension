package net.zic.ascension.datagen.loot;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.util.context.ContextKey;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.RegisterEvent;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.ascension.core.alchemy.AlchemyContext;
import net.zic.ascension.api.ascension.core.alchemy.AlchemyMaterial;
import net.zic.ascension.api.ascension.core.alchemy.AlchemyMaterials;
import net.zic.ascension.api.ascension.core.alchemy.medicinal.MedicinalCompositionEngine;
import net.zic.ascension.api.ascension.core.alchemy.medicinal.MedicinalPillFactory;
import net.zic.ascension.api.ascension.core.alchemy.medicinal.MedicinalDecay;
import net.zic.ascension.common.item.ModItems;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Optional;

/**
 * Generates a procedural pill from datapack-selected materials and seals it inside jade bottle loot.
 * The pill is generated with its complete components when loot rolls, independent of container opening.
 */
public record JadeMedicineLootFunction(List<Identifier> ingredients, int count,
                                       Optional<Long> initialAgeTicks, Optional<Double> remainingVitality) implements LootItemFunction {
    public static final MapCodec<JadeMedicineLootFunction> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Identifier.CODEC.listOf().fieldOf("ingredients").forGetter(JadeMedicineLootFunction::ingredients),
            Codec.intRange(1, 8).optionalFieldOf("count", 1).forGetter(JadeMedicineLootFunction::count),
            Codec.LONG.optionalFieldOf("initial_age_ticks").forGetter(JadeMedicineLootFunction::initialAgeTicks),
            Codec.doubleRange(0.0D, 1.0D).optionalFieldOf("remaining_vitality").forGetter(JadeMedicineLootFunction::remainingVitality)
    ).apply(instance, JadeMedicineLootFunction::new));

    public JadeMedicineLootFunction(List<Identifier> ingredients, int count) {
        this(ingredients, count, Optional.empty(), Optional.empty());
    }

    public JadeMedicineLootFunction {
        ingredients = ingredients == null ? List.of() : List.copyOf(ingredients.stream().limit(12).toList());
        if (ingredients.isEmpty()) throw new IllegalArgumentException("Bottle loot needs at least one ingredient");
        count = Math.max(1, Math.min(8, count));
        initialAgeTicks = initialAgeTicks == null ? Optional.empty() : initialAgeTicks;
        remainingVitality = remainingVitality == null ? Optional.empty() : remainingVitality;
        if (initialAgeTicks.isPresent() && remainingVitality.isPresent())
            throw new IllegalArgumentException("Use initial_age_ticks or remaining_vitality, not both");
    }

    @Override
    public ItemStack apply(ItemStack stack, LootContext context) {
        if (!stack.is(ModItems.JADE_BOTTLE.get())) return stack;
        List<AlchemyMaterial> materials = new ArrayList<>();
        AlchemyContext alchemyContext = AlchemyContext.of(context.getLevel(), BlockPos.ZERO, null);
        for (Identifier ingredient : ingredients) {
            Item item = BuiltInRegistries.ITEM.getValue(ingredient);
            if (item == null) continue;
            AlchemyMaterials.resolve(new ItemStack(item), alchemyContext).ifPresent(materials::add);
        }
        if (materials.isEmpty()) return stack;
        MedicinalCompositionEngine.Result result = MedicinalCompositionEngine.simulate(materials, alchemyContext);
        if (result.outcome() == net.zic.ascension.api.ascension.core.alchemy.AlchemyMergeResolver.Outcome.CATASTROPHIC) {
            return stack;
        }
        ItemStack pill = MedicinalPillFactory.condense(result.profile(), result.ingredientCount(),
                context.getLevel().registryAccess(), MedicinalDecay.now(context.getLevel()));
        if (pill.isEmpty()) return stack;
        pill.setCount(count);
        long now = MedicinalDecay.now(context.getLevel());
        if (initialAgeTicks.isPresent()) MedicinalDecay.setInitialAge(pill, now, initialAgeTicks.get());
        remainingVitality.ifPresent(value -> MedicinalDecay.setInitialVitality(pill, now, value));
        if (MedicinalDecay.expired(pill, now)) return stack;
        MedicinalDecay.transition(pill, now, MedicinalDecay.jadeRate());
        stack.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(List.of(pill)));
        return stack;
    }

    @Override
    public Set<ContextKey<?>> getReferencedContextParams() {
        return Set.of();
    }

    @Override
    public MapCodec<JadeMedicineLootFunction> codec() {
        return CODEC;
    }

    @EventBusSubscriber(modid = AscensionCraft.MOD_ID)
    public static final class Registration {
        @SubscribeEvent
        public static void onRegister(RegisterEvent event) {
            if (event.getRegistryKey().equals(Registries.LOOT_FUNCTION_TYPE)) {
                event.register(Registries.LOOT_FUNCTION_TYPE,
                        helper -> helper.register(AscensionCraft.prefix("bottled_medicine"), CODEC));
            }
        }
    }
}
