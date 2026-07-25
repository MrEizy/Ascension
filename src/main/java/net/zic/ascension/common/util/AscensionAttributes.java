package net.zic.ascension.common.util;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.zic.ascension.AscensionCraft;

public class AscensionAttributes {
    public static final DeferredRegister<Attribute> ATTRIBUTES = DeferredRegister.create(
            BuiltInRegistries.ATTRIBUTE, AscensionCraft.MOD_ID);

    public static final Holder<Attribute> MAX_QI = ATTRIBUTES.register("max_qi",()->new RangedAttribute(
            "attributes.ascension.max_qi",
            100,
            0,Double.MAX_VALUE).setSyncable(true));
    public static final Holder<Attribute> QI_REGEN_RATE = ATTRIBUTES.register("qi_regen_rate",()->new RangedAttribute(
            "attributes.ascension.qi_regen_rate",
            1,
            0,10000).setSyncable(true));
    public static final Holder<Attribute> MAX_STAMINA = ATTRIBUTES.register("max_stamina", () -> new RangedAttribute(
            "attributes.ascension.max_stamina",
            100.0D,
            0.0D,
            Double.MAX_VALUE).setSyncable(true));
    public static final Holder<Attribute> STAMINA_REGEN_RATE = ATTRIBUTES.register("stamina_regen_rate", () -> new RangedAttribute(
            "attributes.ascension.stamina_regen_rate",
            0.5D,
            0.0D,
            10000.0D).setSyncable(true));
    public static final Holder<Attribute> STAMINA_REGEN_DELAY = ATTRIBUTES.register("stamina_regen_delay", () -> new RangedAttribute(
            "attributes.ascension.stamina_regen_delay",
            40.0D,
            0.0D,
            12000.0D).setSyncable(true));

    public Holder<Attribute> PROGRESS_RATE = ATTRIBUTES.register("progress_rate",()->new RangedAttribute(
            "attributes.ascensoin.progress_rate", 0,
            0,1).setSyncable(true)
    );

    public static final Holder<Attribute> PROGRESS_GAIN = ATTRIBUTES.register("progress_gain",()->new RangedAttribute(
            "attributes.ascensoin.progress_gain",
            1,
            0,10000).setSyncable(true));

    public static void register(IEventBus modEventBus){
        ATTRIBUTES.register(modEventBus);

    }
}
