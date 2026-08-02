package net.zic.ascension.common.item;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.common.item.artifacts.consumable.TabletOfDestructionAscendant;
import net.zic.ascension.common.item.artifacts.consumable.TabletOfDestructionEarth;
import net.zic.ascension.common.item.artifacts.consumable.TabletOfDestructionHeaven;
import net.zic.ascension.common.item.artifacts.consumable.TabletOfDestructionHuman;
import net.zic.ascension.common.item.transfer_item.BloodlineTransferItem;
import net.zic.ascension.common.item.transfer_item.PhysiqueTransferItem;
import net.zic.ascension.common.item.transfer_item.TechniqueTransferItem;
import net.zic.zenithlib.registry.RegistryHelper;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(AscensionCraft.MOD_ID);


    public static final DeferredItem<Item> PHYSIQUE_ESSENCE = ITEMS.register("physique_essence",
            () -> new PhysiqueTransferItem(new Item.Properties()
                    .setId(RegistryHelper.key(Registries.ITEM,AscensionCraft.MOD_ID,"physique_essence"))
                    .stacksTo(1)));
    public static final DeferredItem<Item> BLOODLINE_ESSENCE = ITEMS.register("bloodline_essence",
            ()->new BloodlineTransferItem(new Item.Properties()
                    .setId(RegistryHelper.key(Registries.ITEM,AscensionCraft.MOD_ID,"bloodline_essence"))
                    .stacksTo(1)
            ));
    public static final DeferredItem<Item> TECHNIQUE_MANUAL = ITEMS.register("technique_manual",
            ()->new TechniqueTransferItem(new Item.Properties()
                    .setId(RegistryHelper.key(Registries.ITEM,AscensionCraft.MOD_ID,"technique_manual"))
                    .stacksTo(1)
            ));


    public static final DeferredItem<Item> TABLET_OF_DESTRUCTION_HUMAN = ITEMS.register("tablet_of_destruction_human",
            () -> new TabletOfDestructionHuman(new Item.Properties()
                    .setId(RegistryHelper.key(Registries.ITEM, AscensionCraft.MOD_ID, "tablet_of_destruction_human"))));
    public static final DeferredItem<Item> TABLET_OF_DESTRUCTION_EARTH = ITEMS.register("tablet_of_destruction_earth",
            () -> new TabletOfDestructionEarth(new Item.Properties()
                    .setId(RegistryHelper.key(Registries.ITEM, AscensionCraft.MOD_ID, "tablet_of_destruction_earth"))));
    public static final DeferredItem<Item> TABLET_OF_DESTRUCTION_HEAVEN = ITEMS.register("tablet_of_destruction_heaven",
            () -> new TabletOfDestructionHeaven(new Item.Properties()
                    .setId(RegistryHelper.key(Registries.ITEM, AscensionCraft.MOD_ID, "tablet_of_destruction_heaven"))));
    public static final DeferredItem<Item> TABLET_OF_DESTRUCTION_ASCENDANT =
            ITEMS.register("tablet_of_destruction_ascendant",
                    () -> new TabletOfDestructionAscendant(new Item.Properties()
                            .setId(RegistryHelper.key(Registries.ITEM, AscensionCraft.MOD_ID, "tablet_of_destruction_ascendant"))));




    public static final DeferredItem<Item> JADE = ITEMS.registerSimpleItem("jade");

    public static final DeferredItem<Item> RAW_BLACK_IRON = ITEMS.registerSimpleItem("raw_black_iron");
    public static final DeferredItem<Item> BLACK_IRON_NUGGET = ITEMS.registerSimpleItem("black_iron_nugget");
    public static final DeferredItem<Item> BLACK_IRON_INGOT = ITEMS.registerSimpleItem("black_iron_ingot");

    public static final DeferredItem<Item> RAW_FROST_SILVER = ITEMS.registerSimpleItem("raw_frost_silver");
    public static final DeferredItem<Item> FROST_SILVER_NUGGET = ITEMS.registerSimpleItem("frost_silver_nugget");
    public static final DeferredItem<Item> FROST_SILVER_INGOT = ITEMS.registerSimpleItem("frost_silver_ingot");

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
