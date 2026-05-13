package net.thejadeproject.ascension.common.items;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.thejadeproject.ascension.common.effects.ModEffects;

public class ModFoodProperties {

    public static final FoodProperties CLEANSING_PILL_T1 = new FoodProperties.Builder().alwaysEdible().fast()
            .effect(() -> new MobEffectInstance(ModEffects.CLEANSING, 200), 1f).build();
    public static final FoodProperties CLEANSING_PILL_T2 = new FoodProperties.Builder().alwaysEdible().fast()
            .effect(() -> new MobEffectInstance(ModEffects.CLEANSING, 200, 1), 1f).build();
    public static final FoodProperties CLEANSING_PILL_T3 = new FoodProperties.Builder().alwaysEdible().fast()
            .effect(() -> new MobEffectInstance(ModEffects.CLEANSING, 200, 2), 1f).build();
    public static final FoodProperties CLEANSING_PILL_T4 = new FoodProperties.Builder().alwaysEdible().fast()
            .effect(() -> new MobEffectInstance(ModEffects.CLEANSING, 200, 3), 1f).build();
    public static final FoodProperties QI_DEVOURING_PARASITE_PILL = new FoodProperties.Builder().alwaysEdible().fast()
            .effect(() -> new MobEffectInstance(ModEffects.PARASITE, 400), 1f).build();

    public static final FoodProperties NEUTRALITY_PILL = new FoodProperties.Builder().alwaysEdible().fast()
            .effect(() -> new MobEffectInstance(ModEffects.NEUTRALITY, 1000), 1f).build();

    public static final FoodProperties REBIRTH_PILL = new FoodProperties.Builder().nutrition(0).alwaysEdible().saturationModifier(0).fast().build();
    public static final FoodProperties PILLS = new FoodProperties.Builder().nutrition(0).alwaysEdible().saturationModifier(0).fast().build();
    public static final FoodProperties FASTING_PILL_T1 = new FoodProperties.Builder().nutrition(6).saturationModifier(2).fast().build();
    public static final FoodProperties FASTING_PILL_T2 = new FoodProperties.Builder().nutrition(10).saturationModifier(1.4f).fast().build();
    public static final FoodProperties FASTING_PILL_T3 = new FoodProperties.Builder().nutrition(15).saturationModifier(2).fast().build();


    public static final FoodProperties PILL_RESIDUE = new FoodProperties.Builder().nutrition(0).saturationModifier(0).fast().alwaysEdible().build();

    public static final FoodProperties CULT_PILL = new FoodProperties.Builder().alwaysEdible().nutrition(0).saturationModifier(0).fast().build();



    public static final FoodProperties ANTIDOTE_PILL = new FoodProperties.Builder().nutrition(0).saturationModifier(0).fast().build();


    public static final FoodProperties MEDICINAL_PILL = new FoodProperties.Builder().nutrition(0).saturationModifier(0).fast().build();
    public static final FoodProperties POISON_PILL = new FoodProperties.Builder().nutrition(0).saturationModifier(0).fast().build();


    public static final FoodProperties MARROW_PILL = new FoodProperties.Builder().nutrition(0).saturationModifier(0).fast().build();
    public static final FoodProperties RITUAL_PILL = new FoodProperties.Builder().nutrition(0).saturationModifier(0).fast().alwaysEdible().build();



    public static final FoodProperties HUNDRED_YEAR_SNOW_GINSENG = new FoodProperties.Builder().nutrition(0).saturationModifier(0).fast().alwaysEdible().build();
    public static final FoodProperties HUNDRED_YEAR_FIRE_GINSENG = new FoodProperties.Builder().nutrition(0).saturationModifier(0).fast().alwaysEdible().build();
    public static final FoodProperties JADE_DEW_GRASS = new FoodProperties.Builder().nutrition(0).saturationModifier(0).fast().alwaysEdible().build();
    public static final FoodProperties GOLDEN_SUN_LEAF = new FoodProperties.Builder().fast().alwaysEdible()
            .effect(() -> new MobEffectInstance(MobEffects.REGENERATION, 50), 1f).build();
    public static final FoodProperties IRONWOOD_SPROUT = new FoodProperties.Builder().fast().alwaysEdible()
            .effect(() -> new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 50), 1f).build();
    public static final FoodProperties WHITE_JADE_ORCHID = new FoodProperties.Builder().fast().alwaysEdible()
            .effect(() -> new MobEffectInstance(ModEffects.CLEANSING, 50), 1f).build();
    public static final FoodProperties JADE_BAMBOO_OF_SERENITY = new FoodProperties.Builder().fast().alwaysEdible()
            .effect(() -> new MobEffectInstance(MobEffects.HERO_OF_THE_VILLAGE, 50), 1f).build();
    public static final FoodProperties HUNDRED_YEAR_GINSENG = new FoodProperties.Builder().fast().alwaysEdible()
            .effect(() -> new MobEffectInstance(MobEffects.WEAKNESS, 50), 1f).build();



    public static final FoodProperties PEACH = new FoodProperties.Builder()
            .effect(() -> new MobEffectInstance(ModEffects.CLEANSING, 200, 3), 1f).build();


}
