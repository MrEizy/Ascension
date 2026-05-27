package net.zic.ascension.api.capabilities;

import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.capabilities.EntityCapability;
import net.zic.ascension.AscensionCraft;

public class CoreCapabilities {

    public static final EntityCapability<AscensionEntityDataHolder,Void> ASCENSION_ENTITY_DATA_HOLDER_CAPABILITY =
            EntityCapability.create(
                    Identifier.fromNamespaceAndPath(AscensionCraft.MOD_ID,"ascension_entity_holder"),
                    AscensionEntityDataHolder.class,
                    Void.class
            );



}
