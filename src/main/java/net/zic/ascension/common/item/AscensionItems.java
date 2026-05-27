package net.zic.ascension.common.item;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.common.item.transfer_item.PhysiqueTransferItem;
import net.zic.zenithlib.registry.RegistryHelper;

public class AscensionItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(AscensionCraft.MOD_ID);


    public static final DeferredItem<Item> PHYSIQUE_ESSENCE = ITEMS.register("physique_essence",
            () -> new PhysiqueTransferItem(new Item.Properties()
                    .setId(ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(AscensionCraft.MOD_ID,"physique_essence")))//TODO create a helper for this
                    .stacksTo(1)));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
