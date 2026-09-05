package net.zic.ascension.common.item;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.common.fluids.AscFluids;
import net.zic.ascension.common.item.artifacts.consumable.TabletOfDestructionAscendant;
import net.zic.ascension.common.item.artifacts.consumable.TabletOfDestructionEarth;
import net.zic.ascension.common.item.artifacts.consumable.TabletOfDestructionHeaven;
import net.zic.ascension.common.blocks.ModBlocks;
import net.zic.ascension.common.herbs.ModHerbs;
import net.zic.ascension.common.item.artifacts.consumable.TabletOfDestructionHuman;
import net.zic.ascension.common.item.artifacts.pills.JadeBottleItem;
import net.zic.ascension.common.item.herbs.HerbItem;
import net.zic.ascension.common.item.qi_holder.SpiritStone;
import net.zic.ascension.common.item.transfer_item.BloodlineTransferItem;
import net.zic.ascension.common.item.transfer_item.PhysiqueTransferItem;
import net.zic.ascension.common.item.transfer_item.TechniqueTransferItem;
import net.zic.zenithlib.registry.RegistryHelper;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(AscensionCraft.MOD_ID);




    //Key Items
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


    //Spirit Stone stuff

    public static final DeferredItem<Item> SPIRIT_STONE = ITEMS.registerItem("spirit_stone",
            properties -> new SpiritStone(properties.stacksTo(64)));



    //Fluids
    public static final DeferredItem<Item> LIQUIFIED_SPIRITUAL_QI_BUCKET = ITEMS.registerItem("liquified_spiritual_qi_bucket",
            properties -> new BucketItem(AscFluids.LIQUIFIED_SPIRITUAL_QI_SOURCE.get(), properties.stacksTo(1).craftRemainder(Items.BUCKET)));



    //Artifacts
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




    public static final DeferredItem<Item> JADE_BOTTLE = ITEMS.register("jade_bottle",
            () -> new JadeBottleItem(new Item.Properties()
                    .setId(RegistryHelper.key(Registries.ITEM, AscensionCraft.MOD_ID, "jade_bottle"))));




    //Ores
    public static final DeferredItem<Item> JADE = ITEMS.registerSimpleItem("jade");

    public static final DeferredItem<Item> RAW_BLACK_IRON = ITEMS.registerSimpleItem("raw_black_iron");
    public static final DeferredItem<Item> BLACK_IRON_NUGGET = ITEMS.registerSimpleItem("black_iron_nugget");
    public static final DeferredItem<Item> BLACK_IRON_INGOT = ITEMS.registerSimpleItem("black_iron_ingot");

    public static final DeferredItem<Item> RAW_FROST_SILVER = ITEMS.registerSimpleItem("raw_frost_silver");
    public static final DeferredItem<Item> FROST_SILVER_NUGGET = ITEMS.registerSimpleItem("frost_silver_nugget");
    public static final DeferredItem<Item> FROST_SILVER_INGOT = ITEMS.registerSimpleItem("frost_silver_ingot");


    //Herbs Items
    public static final DeferredItem<Item> JADE_DEW_GRASS = ITEMS.registerItem("jade_dew_grass",
            properties -> new HerbItem(properties, ModHerbs.JADE_DEW_GRASS, () -> ModBlocks.JADE_DEW_GRASS_CROP.get()));
    public static final DeferredItem<Item> JADE_DEW_GRASS_SEEDS = ITEMS.registerItem("jade_dew_grass_seeds",
            properties -> new HerbItem.Seed(properties, () -> ModBlocks.JADE_DEW_GRASS_CROP.get()));


    public static final DeferredItem<Item> GINSENG = ITEMS.registerItem("ginseng",
            properties -> new HerbItem(properties.food(AscFoodProperties.GINSENG), ModHerbs.GINSENG, () -> ModBlocks.GINSENG_CROP.get()));
    public static final DeferredItem<Item> FIRE_GINSENG = ITEMS.registerItem("fire_ginseng",
            properties -> new HerbItem(properties.food(AscFoodProperties.GINSENG), ModHerbs.FIRE_GINSENG, () -> ModBlocks.FIRE_GINSENG_CROP.get()));
    public static final DeferredItem<Item> SNOW_GINSENG = ITEMS.registerItem("snow_ginseng",
            properties -> new HerbItem(properties.food(AscFoodProperties.GINSENG), ModHerbs.SNOW_GINSENG, () -> ModBlocks.SNOW_GINSENG_CROP.get()));
    public static final DeferredItem<Item> WHITE_JADE_ORCHID = ITEMS.registerItem("white_jade_orchid",
            properties -> new HerbItem(properties.food(AscFoodProperties.ORCHID), ModHerbs.WHITE_JADE_ORCHID, () -> ModBlocks.WHITE_JADE_ORCHID_CROP.get()));


    public static final DeferredItem<Item> LINGZHI_MUSHROOM = ITEMS.registerItem("lingzhi_mushroom",
            properties -> new HerbItem(properties.food(AscFoodProperties.MUSHROOM), ModHerbs.LINGZHI_MUSHROOM));
    public static final DeferredItem<Item> BLOOD_LINGZHI_MUSHROOM = ITEMS.registerItem("blood_lingzhi_mushroom",
            properties -> new HerbItem(properties.food(AscFoodProperties.MUSHROOM), ModHerbs.BLOOD_LINGZHI_MUSHROOM));
    public static final DeferredItem<Item> PEACH = ITEMS.registerItem("peach",
            properties -> new HerbItem(properties.food(AscFoodProperties.PEACH), ModHerbs.PEACH));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
