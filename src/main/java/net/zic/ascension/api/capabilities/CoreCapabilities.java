package net.zic.ascension.api.capabilities;

import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.capabilities.EntityCapability;
import net.neoforged.neoforge.capabilities.ItemCapability;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.capabilities.damage_provider.AscensionDamageSourceProvider;

public class CoreCapabilities {

    public static final EntityCapability<AscensionEntityDataHolder,Void> ASCENSION_ENTITY_DATA_HOLDER_CAPABILITY =
            EntityCapability.create(
                    Identifier.fromNamespaceAndPath(AscensionCraft.MOD_ID,"ascension_entity_holder"),
                    AscensionEntityDataHolder.class,
                    Void.class
            );

    public static final EntityCapability<AscensionDamageSourceProvider, Void> ASCENSION_ENTITY_DAMAGE_SOURCE_PROVIDER =
            EntityCapability.create(
                    Identifier.fromNamespaceAndPath(AscensionCraft.MOD_ID,"ascension_entity_damage_source_provider"),
                    AscensionDamageSourceProvider.class,
                    Void.class
            );


    public static final ItemCapability<AscensionDamageSourceProvider, Void> ASCENSION_ITEM_STACK_DAMAGE_SOURCE_PROVIDER =
            ItemCapability.create(
                    Identifier.fromNamespaceAndPath(AscensionCraft.MOD_ID,"ascension_item_stack_damage_source_provider"),
                    AscensionDamageSourceProvider.class,
                    Void.class
            );

}
