package net.zic.ascension.refactor_packages.forms.forms;

import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.refactor_packages.forms.IEntityForm;
import net.zic.ascension.refactor_packages.forms.forms.generic.GenericForm;
import net.zic.ascension.refactor_packages.registries.AscensionRegistries;

public class ModForms {
    public static final DeferredRegister<IEntityForm> FORMS =DeferredRegister.create(AscensionRegistries.EntityForms.ENTITY_FORMS_REGISTRY, AscensionCraft.MOD_ID);

    public static final DeferredHolder<IEntityForm, ? extends GenericForm> MORTAL_VESSEL = FORMS.register("mortal_vessel",()->
                new GenericForm(Component.literal("Mortal Vessel"))
            );

    public static final DeferredHolder<IEntityForm, ? extends GenericForm> SOUL_FORM = FORMS.register("soul",()->
            new GenericForm(Component.literal("Soul"))
    );




    public static void register(IEventBus modEventBus){
        FORMS.register(modEventBus);
    }
}
