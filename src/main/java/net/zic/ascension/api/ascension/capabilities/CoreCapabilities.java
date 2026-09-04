package net.zic.ascension.api.ascension.capabilities;

import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.EntityCapability;
import net.neoforged.neoforge.capabilities.ItemCapability;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.ascension.capabilities.damage_provider.AscensionDamageSourceProvider;
import net.zic.ascension.api.ascension.core.qi.QiHandler;

public class CoreCapabilities {

    public static final EntityCapability<AscensionEntityDataProvider,Void> ASCENSION_ENTITY_DATA_PROVIDER_CAPABILITY =
            EntityCapability.create(
                    Identifier.fromNamespaceAndPath(AscensionCraft.MOD_ID,"ascension_entity_holder"),
                    AscensionEntityDataProvider.class,
                    Void.class
            );

    public static final EntityCapability<AscensionDamageSourceProvider, Void> ASCENSION_ENTITY_DAMAGE_SOURCE_PROVIDER =
            EntityCapability.create(
                    Identifier.fromNamespaceAndPath(AscensionCraft.MOD_ID,"ascension_entity_damage_source_provider"),
                    AscensionDamageSourceProvider.class,
                    Void.class
            );
    public static final EntityCapability<EntityQiProvider, Void> ASCENSION_ENTITY_QI_PROVIDER =
            EntityCapability.create(
                    Identifier.fromNamespaceAndPath(AscensionCraft.MOD_ID,"ascension_entity_qi_provider"),
                    EntityQiProvider.class,
                    Void.class
            );

    public static final ItemCapability<AscensionDamageSourceProvider, Void> ASCENSION_ITEM_STACK_DAMAGE_SOURCE_PROVIDER =
            ItemCapability.create(
                    Identifier.fromNamespaceAndPath(AscensionCraft.MOD_ID,"ascension_item_stack_damage_source_provider"),
                    AscensionDamageSourceProvider.class,
                    Void.class
            );

    public static final ItemCapability<QiHandler,Void> ITEM_QI_HANDLER =
            ItemCapability.create(
                    Identifier.fromNamespaceAndPath(AscensionCraft.MOD_ID,"item_qi_handler"),
                    QiHandler.class,
                    Void.class
            );
    public static final BlockCapability<QiHandler,Void> BLOCK_QI_HANDLER =
            BlockCapability.create(
                    Identifier.fromNamespaceAndPath(AscensionCraft.MOD_ID,"block_qi_handler"),
                    QiHandler.class,
                    Void.class
            );
}
