package net.zic.ascension.datapack.technique;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.datapack.TypeRegistries;
import net.zic.ascension.api.datapack.skill.SkillType;
import net.zic.ascension.api.datapack.technique.TechniqueType;
import net.zic.ascension.datapack.skill.passive.SimplePassiveSkillType;

public class AscensionTechniqueTypes {
    public static final DeferredRegister<TechniqueType> TECHNIQUE_TYPES =
            DeferredRegister.create(TypeRegistries.TECHNIQUE_TYPE_REGISTRY, AscensionCraft.MOD_ID);
    public static final DeferredHolder<TechniqueType,TechniqueType> SIMPLE_TECHNIQUE_TYPE = TECHNIQUE_TYPES.register(
            "simple_technique",
            SimpleTechniqueType::new
    );

    public static void register(IEventBus eventBus){

        TECHNIQUE_TYPES.register(eventBus);
    }
}
