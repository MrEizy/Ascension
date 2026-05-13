package net.thejadeproject.ascension.common.effects;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.thejadeproject.ascension.AscensionCraft;

public class ModEffects {
    public static final DeferredRegister<MobEffect> MOB_EFFECT =
            DeferredRegister.create(BuiltInRegistries.MOB_EFFECT, AscensionCraft.MOD_ID);

    public static final Holder<MobEffect> CLEANSING = MOB_EFFECT.register("cleansing",
            () -> new CleanseEffect(MobEffectCategory.BENEFICIAL, 0xDDF3F5));


    public static final Holder<MobEffect> NEUTRALITY = MOB_EFFECT.register("neutrality",
            () -> new NeutralityEffect(MobEffectCategory.BENEFICIAL, 0xE8E8E8));


    public static final Holder<MobEffect> PARASITE = MOB_EFFECT.register("qi_devouring_parasite",
            () -> new QiDevouringParasiteEffect(MobEffectCategory.HARMFUL, 0x4a5c0a)); // Sickly green color

    public static final Holder<MobEffect> QI_ENHANCED_REGEN = MOB_EFFECT.register("qi_enhanced_regeneration",
            () -> new QiEnhancedRegeneration(MobEffectCategory.HARMFUL, 0x00FFE1)); // Sickly green color




    public static void register(IEventBus eventBus) {
        MOB_EFFECT.register(eventBus);
    }
}
