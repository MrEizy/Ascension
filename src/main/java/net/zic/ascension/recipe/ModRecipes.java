package net.zic.ascension.recipe;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.*;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.zic.ascension.AscensionCraft;


public class ModRecipes {
    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
            DeferredRegister.create(Registries.RECIPE_SERIALIZER, AscensionCraft.MOD_ID);
    public static final DeferredRegister<RecipeType<?>> TYPES =
            DeferredRegister.create(Registries.RECIPE_TYPE, AscensionCraft.MOD_ID);

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<LowHumanPillCauldronRecipe>> CAULDRON_LOW_HUMAN_SERIALIZER =
            SERIALIZERS.register("pill_cauldron_low_human", LowHumanPillCauldronRecipe.Serializer::new);
    public static final DeferredHolder<RecipeType<?>, RecipeType<LowHumanPillCauldronRecipe>> CAULDRON_LOW_HUMAN_TYPE =
            TYPES.register("pill_cauldron_low_human", () -> new RecipeType<LowHumanPillCauldronRecipe>() {
                @Override
                public String toString() {
                    return "pill_cauldron_low_human";
                }
            });







    public static void register(IEventBus eventBus) {
        SERIALIZERS.register(eventBus);
        TYPES.register(eventBus);
    }
}
