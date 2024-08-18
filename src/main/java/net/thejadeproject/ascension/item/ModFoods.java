package net.thejadeproject.ascension.item;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;

public class ModFoods {
    public static final FoodProperties PEACH = new FoodProperties.Builder().nutrition(4)
            .saturationModifier(0.4f).effect(() -> new MobEffectInstance(MobEffects.REGENERATION, 200), 0.5f).build();
}
