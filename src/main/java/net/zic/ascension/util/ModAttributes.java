package net.zic.ascension.util;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.zic.ascension.AscensionCraft;

public class ModAttributes {
    public static final DeferredRegister<Attribute> ATTRIBUTES = DeferredRegister.create(
            BuiltInRegistries.ATTRIBUTE, AscensionCraft.MOD_ID);
    public static int MAX_CASTING_INSTANCE_NUMBER = 4;
    public static final Holder<Attribute> MAX_CASTING_INSTANCES = ATTRIBUTES.register("max_casting_instances", () -> new RangedAttribute(
            // The translation key to use.
            "attributes.ascension.max_casting_instances",
            // The default value.
            0,
            // Min and max values.
            0,
            MAX_CASTING_INSTANCE_NUMBER).setSyncable(true));
    public static final Holder<Attribute> PLAYER_MAX_QI = ATTRIBUTES.register("player_qi", () -> new RangedAttribute(
            // The translation key to use.
            "attributes.ascension.player_qi",
            // The default value.
            100,
            // Min and max values.
            0,
            1000000000).setSyncable(true));
    public static final Holder<Attribute> MAX_QI = ATTRIBUTES.register("max_qi",()->new RangedAttribute(
            "attributes.ascension.max_qi",
            100,
            0,1000000000).setSyncable(true));
    public static final Holder<Attribute> QI_REGEN_RATE = ATTRIBUTES.register("qi_regen_rate",()->new RangedAttribute(
            "attributes.ascensoin.qi_regen_rate",
            1,
            0,1000000).setSyncable(true));
    public static final Holder<Attribute> PLAYER_QI_REGEN_RATE = ATTRIBUTES.register("player_qi_regen_rate", () -> new RangedAttribute(
            // The translation key to use.
            "attributes.ascension.player_qi_regen_rate",
            // The default value.
            2,
            // Min and max values.
            0,
            1000000).setSyncable(true));

    public static final Holder<Attribute> SKILL_DAMAGE_MULTIPLIER = ATTRIBUTES.register("skill_damage_multiplier", () -> new RangedAttribute(
            // The translation key to use
            "attributes.ascension.skill_damage_multiplier",
            1.0,
            0.0,
            1000.0)
            .setSyncable(true));

    public static void register(IEventBus modEventBus){
        ATTRIBUTES.register(modEventBus);

    }
}
