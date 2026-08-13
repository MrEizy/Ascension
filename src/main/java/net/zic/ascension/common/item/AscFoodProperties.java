package net.zic.ascension.common.item;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.component.Consumables;
import net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect;

public class AscFoodProperties {
    public static final FoodProperties PEACH = new FoodProperties.Builder().nutrition(3).saturationModifier(0.25f).alwaysEdible().build();
    public static final FoodProperties GINSENG = new FoodProperties.Builder().nutrition(3).saturationModifier(0.25f).alwaysEdible().build();
    public static final FoodProperties MUSHROOM = new FoodProperties.Builder().nutrition(3).saturationModifier(0.25f).alwaysEdible().build();
    public static final FoodProperties ORCHID = new FoodProperties.Builder().nutrition(3).saturationModifier(0.25f).alwaysEdible().build();

//    public static final Consumable RADISH_EFFECT = Consumables.defaultFood().onConsume(
//            new ApplyStatusEffectsConsumeEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 400), 0.45f)).build();
}
