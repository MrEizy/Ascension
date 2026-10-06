package net.zic.ascension.configuration.mob_traits.traits;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.ascension.datapack.TypeRegistries;
import net.zic.ascension.api.ascension.datapack.bloodline.BloodlineType;
import net.zic.ascension.configuration.ConfigurationRegistries;
import net.zic.ascension.configuration.mob_traits.MobTraitDefinitionType;
import net.zic.ascension.impl.datapack.bloodline.SimpleBloodlineType;

public class AscensionTraitTypes {
    public static final DeferredRegister<MobTraitDefinitionType> TRAIT_DEFINITION_TYPES =
            DeferredRegister.create(ConfigurationRegistries.MOB_TRAIT_TYPES, AscensionCraft.MOD_ID);

    public static final DeferredHolder<MobTraitDefinitionType,MobTraitDefinitionType> SIMPLE_TRAIT_TYPE = TRAIT_DEFINITION_TYPES.register(
            "simple_trait",
            SimpleTraitDefinitionType::new
    );

    public static void register(IEventBus eventBus){

        TRAIT_DEFINITION_TYPES.register(eventBus);
    }
}
