package net.thejadeproject.ascension.datagen;


import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.client.model.generators.ItemModelBuilder;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.thejadeproject.ascension.AscensionCraft;
import net.thejadeproject.ascension.common.blocks.ModBlocks;
import net.thejadeproject.ascension.common.items.ModItems;

import java.util.Objects;

public class ModItemModelProvider extends ItemModelProvider {
    public ModItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, AscensionCraft.MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerModels() {




        //Block Items
        buttonItem(ModBlocks.GOLDEN_PALM_BUTTON, ModBlocks.GOLDEN_PALM_PLANKS);
        fenceItem(ModBlocks.GOLDEN_PALM_FENCE, ModBlocks.GOLDEN_PALM_PLANKS);
        basicItem(ModBlocks.GOLDEN_PALM_DOOR.asItem());
        buttonItem(ModBlocks.IRONWOOD_BUTTON, ModBlocks.IRONWOOD_PLANKS);
        fenceItem(ModBlocks.IRONWOOD_FENCE, ModBlocks.IRONWOOD_PLANKS);
        basicItem(ModBlocks.IRONWOOD_DOOR.asItem());

        /** Walls */
        wallItem(ModBlocks.MARBLE_BRICK_WALLS, ModBlocks.MARBLE_BRICKS);
        wallItem(ModBlocks.MARBLE_TILE_WALLS, ModBlocks.MARBLE_TILES);
        wallItem(ModBlocks.LIGHT_GRAY_MARBLE_BRICK_WALLS, ModBlocks.LIGHT_GRAY_MARBLE_BRICKS);
        wallItem(ModBlocks.LIGHT_GRAY_MARBLE_TILE_WALLS, ModBlocks.LIGHT_GRAY_MARBLE_TILES);
        wallItem(ModBlocks.GRAY_MARBLE_BRICK_WALLS, ModBlocks.GRAY_MARBLE_BRICKS);
        wallItem(ModBlocks.GRAY_MARBLE_TILE_WALLS, ModBlocks.GRAY_MARBLE_TILES);
        wallItem(ModBlocks.CHARRED_MARBLE_BRICK_WALLS, ModBlocks.CHARRED_MARBLE_BRICKS);
        wallItem(ModBlocks.CHARRED_MARBLE_TILE_WALLS, ModBlocks.CHARRED_MARBLE_TILES);
        wallItem(ModBlocks.BROWN_MARBLE_BRICK_WALLS, ModBlocks.BROWN_MARBLE_BRICKS);
        wallItem(ModBlocks.BROWN_MARBLE_TILE_WALLS, ModBlocks.BROWN_MARBLE_TILES);
        wallItem(ModBlocks.RED_MARBLE_BRICK_WALLS, ModBlocks.RED_MARBLE_BRICKS);
        wallItem(ModBlocks.RED_MARBLE_TILE_WALLS, ModBlocks.RED_MARBLE_TILES);
        wallItem(ModBlocks.ORANGE_MARBLE_BRICK_WALLS, ModBlocks.ORANGE_MARBLE_BRICKS);
        wallItem(ModBlocks.ORANGE_MARBLE_TILE_WALLS, ModBlocks.ORANGE_MARBLE_TILES);
        wallItem(ModBlocks.YELLOW_MARBLE_BRICK_WALLS, ModBlocks.YELLOW_MARBLE_BRICKS);
        wallItem(ModBlocks.YELLOW_MARBLE_TILE_WALLS, ModBlocks.YELLOW_MARBLE_TILES);
        wallItem(ModBlocks.LIME_MARBLE_BRICK_WALLS, ModBlocks.LIME_MARBLE_BRICKS);
        wallItem(ModBlocks.LIME_MARBLE_TILE_WALLS, ModBlocks.LIME_MARBLE_TILES);
        wallItem(ModBlocks.GREEN_MARBLE_BRICK_WALLS, ModBlocks.GREEN_MARBLE_BRICKS);
        wallItem(ModBlocks.GREEN_MARBLE_TILE_WALLS, ModBlocks.GREEN_MARBLE_TILES);
        wallItem(ModBlocks.CYAN_MARBLE_BRICK_WALLS, ModBlocks.CYAN_MARBLE_BRICKS);
        wallItem(ModBlocks.CYAN_MARBLE_TILE_WALLS, ModBlocks.CYAN_MARBLE_TILES);
        wallItem(ModBlocks.LIGHT_BLUE_MARBLE_BRICK_WALLS, ModBlocks.LIGHT_BLUE_MARBLE_BRICKS);
        wallItem(ModBlocks.LIGHT_BLUE_MARBLE_TILE_WALLS, ModBlocks.LIGHT_BLUE_MARBLE_TILES);
        wallItem(ModBlocks.BLUE_MARBLE_BRICK_WALLS, ModBlocks.BLUE_MARBLE_BRICKS);
        wallItem(ModBlocks.BLUE_MARBLE_TILE_WALLS, ModBlocks.BLUE_MARBLE_TILES);
        wallItem(ModBlocks.PURPLE_MARBLE_BRICK_WALLS, ModBlocks.PURPLE_MARBLE_BRICKS);
        wallItem(ModBlocks.PURPLE_MARBLE_TILE_WALLS, ModBlocks.PURPLE_MARBLE_TILES);
        wallItem(ModBlocks.MAGENTA_MARBLE_BRICK_WALLS, ModBlocks.MAGENTA_MARBLE_BRICKS);
        wallItem(ModBlocks.MAGENTA_MARBLE_TILE_WALLS, ModBlocks.MAGENTA_MARBLE_TILES);
        wallItem(ModBlocks.PINK_MARBLE_BRICK_WALLS, ModBlocks.PINK_MARBLE_BRICKS);
        wallItem(ModBlocks.PINK_MARBLE_TILE_WALLS, ModBlocks.PINK_MARBLE_TILES);





        //Artifacts


        basicItem(ModItems.SPATIAL_RING.get());
        basicItem(ModItems.REPAIR_SLIP.get());
        basicItem(ModItems.ENDER_POUCH.get());


        talisman(ModItems.SOUL_ANCHOR_TALISMAN.get());



        talisman(ModItems.SPATIAL_RUPTURE_TALISMAN_T1.get());
        talisman(ModItems.SPATIAL_RUPTURE_TALISMAN_T2.get());
        talisman(ModItems.SPATIAL_RUPTURE_TALISMAN_T3.get());

        talisman(ModItems.SOULSTEAD_RETURN_TALISMAN.get());
        talisman(ModItems.WORLD_AXIS_TALISMAN.get());
        talisman(ModItems.VOID_MARKING_TALISMAN.get());
        talisman(ModItems.DEATH_RECALL_TALISMAN.get());


        //Crafting Ingredients
        basicItem(ModItems.TALISMAN_PAPER.get());


        //Tools & Armors & Weapons

        basicItem(ModItems.SPIRITUAL_MEAL.get());
        basicItem(ModItems.MORTAR_PESTLE.get());

        handheldItem(ModItems.WOODEN_BLADE.get());
        handheldItem(ModItems.STONE_BLADE.get());
        handheldItem(ModItems.IRON_BLADE.get());
        handheldItem(ModItems.GOLD_BLADE.get());
        handheldItem(ModItems.DIAMOND_BLADE.get());
        handheldItem(ModItems.NETHERITE_BLADE.get());

        handheldItem(ModItems.WOODEN_SPEAR.get());
        handheldItem(ModItems.STONE_SPEAR.get());
        handheldItem(ModItems.IRON_SPEAR.get());
        handheldItem(ModItems.GOLD_SPEAR.get());
        handheldItem(ModItems.DIAMOND_SPEAR.get());
        handheldItem(ModItems.NETHERITE_SPEAR.get());


        handheldItem(ModItems.SPIRITUAL_STONE_AXE.get());
        handheldItem(ModItems.SPIRITUAL_STONE_PICKAXE.get());
        handheldItem(ModItems.SPIRITUAL_STONE_HOE.get());
        handheldItem(ModItems.SPIRITUAL_STONE_SHOVEL.get());


        handheldItem(ModItems.FAN.get());


        //Items
        basicItem(ModItems.JADE.get());
        basicItem(ModItems.JADE_NUGGET.get());


        basicItem(ModItems.SPATIAL_STONE_TIER_1.get());

        basicItem(ModItems.RAW_BLACK_IRON.get());
        basicItem(ModItems.BLACK_IRON_INGOT.get());
        basicItem(ModItems.BLACK_IRON_NUGGET.get());
        basicItem(ModItems.RAW_FROST_SILVER.get());
        basicItem(ModItems.FROST_SILVER_INGOT.get());
        basicItem(ModItems.FROST_SILVER_NUGGET.get());



        //Drops
        basicItem(ModItems.LIVING_CORE.get());
        basicItem(ModItems.UNDEAD_CORE.get());



        withExistingParent(Objects.requireNonNull(BuiltInRegistries.ITEM.getKey(ModItems.TECHNIQUE_MANUAL.get())).getPath(), "item/generated")
                .texture("layer0", "ascension:item/technique_manual")
                .override().predicate(ResourceLocation.fromNamespaceAndPath("ascension", "technique_variant"), 1.0F)
                    .model(new ModelFile.UncheckedModelFile("ascension:item/fire_body_technique")).end()
                .override().predicate(ResourceLocation.fromNamespaceAndPath("ascension", "technique_variant"), 2.0F)
                    .model(new ModelFile.UncheckedModelFile("ascension:item/water_body_technique")).end()
                .override().predicate(ResourceLocation.fromNamespaceAndPath("ascension", "technique_variant"), 3.0F)
                    .model(new ModelFile.UncheckedModelFile("ascension:item/wood_body_technique")).end()
                .override().predicate(ResourceLocation.fromNamespaceAndPath("ascension", "technique_variant"), 4.0F)
                    .model(new ModelFile.UncheckedModelFile("ascension:item/earth_body_technique")).end()
                .override().predicate(ResourceLocation.fromNamespaceAndPath("ascension", "technique_variant"), 5.0F)
                    .model(new ModelFile.UncheckedModelFile("ascension:item/metal_body_technique")).end();
        basicItem(ModItems.TECHNIQUE_PAGE.get());
        basicItem(ModItems.TECHNIQUE_BINDER.get());

        //Spiritual Fires
        basicItem(ModItems.CRIMSON_LOTUS_FLAME.get());
        basicItem(ModItems.FLAME.get());
        basicItem(ModItems.SOUL_FLAME.get());



        //Not Used For Anything in mod except being icons etc...

        //Tablet Of Destructions
        tablet(ModItems.TABLET_OF_DESTRUCTION_HUMAN.get());
        tablet(ModItems.TABLET_OF_DESTRUCTION_EARTH.get());
        tablet(ModItems.TABLET_OF_DESTRUCTION_HEAVEN.get());

        //Pills
        pills(ModItems.PILL_RESIDUE.get());

        pills(ModItems.NEUTRALITY_PILL.get());

        pills(ModItems.FASTING_PILL_T1.get());
        pills(ModItems.FASTING_PILL_T2.get());
        pills(ModItems.FASTING_PILL_T3.get());
        pills(ModItems.CLEANSING_PILL_T1.get());
        pills(ModItems.CLEANSING_PILL_T2.get());
        pills(ModItems.CLEANSING_PILL_T3.get());
        pills(ModItems.CLEANSING_PILL_T4.get());


        pills(ModItems.QI_ENHANCED_REGEN_PILL.get());



        pills(ModItems.ESSENCE_GATHERING_PILL.get());
        pills(ModItems.SOUL_FOCUS_PILL.get());
        pills(ModItems.INNER_REINFORCEMENT_PILL.get());


        pills(ModItems.QI_REPLENISHING_PILL.get());


        pills(ModItems.ANTIDOTE_PILL_QDP.get());


        pills(ModItems.MARROW_CLEANSE_PILL.get());
        pills(ModItems.CRIMSON_LOTUS_BONE_PILL.get());


        pills(ModItems.QI_DEVOURING_PARASITE_PILL.get());


        //Powders
        powder(ModItems.QI_DEVOURING_POWDER.get());






        //herbs
        herbs(ModItems.GOLDEN_SUN_LEAF.get());
        herbs(ModItems.JADE_BAMBOO_OF_SERENITY.get());
        herbs(ModItems.WHITE_JADE_ORCHID.get());


        herbs(ModItems.PEACH.get());

        herbs(ModItems.HUNDRED_YEAR_GINSENG.get());
        herbs(ModItems.HUNDRED_YEAR_SNOW_GINSENG.get());
        herbs(ModItems.HUNDRED_YEAR_FIRE_GINSENG.get());

        herbs(ModItems.IRONWOOD_SPROUT.get());


        herbsBlockItem(ModBlocks.WHITE_JADE_ORCHID_CROP);
        herbsBlockItem(ModBlocks.HUNDRED_YEAR_SNOW_GINSENG_CROP);
        herbsBlockItem(ModBlocks.HUNDRED_YEAR_FIRE_GINSENG_CROP);
        herbsBlockItem(ModBlocks.HUNDRED_YEAR_GINSENG_CROP);

        herbs(ModItems.JADE_DEW_GRASS.get());
        basicItem(ModItems.JADE_DEW_GRASS_SEEDS.get());

        basicItem(ModItems.HERB_POUCH.get());

        //Saplings
        saplingItem(ModBlocks.GOLDEN_PALM_SAPLING);
        saplingItem(ModBlocks.IRONWOOD_SAPLING);


        //MobEggs
        withExistingParent(ModItems.RAT_SPAWN_EGG.getId().getPath(), mcLoc("item/template_spawn_egg"));

        // Scholarly Pages
        basicItem(ModItems.SCHOLARLY_SOUL_RECTIFICATION_OF_NAMES.get());
        basicItem(ModItems.SCHOLARLY_SOUL_GREAT_LEARNING.get());
        basicItem(ModItems.SCHOLARLY_SOUL_THOUSAND_COMMENTARIES.get());
        basicItem(ModItems.SCHOLARLY_SOUL_SAGE_MANDATE.get());



    }

    private ItemModelBuilder saplingItem(DeferredBlock<Block> item) {
        return withExistingParent(item.getId().getPath(),
                ResourceLocation.parse("item/generated")).texture("layer0",
                ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID,"block/" + item.getId().getPath()));
    }
    private ItemModelBuilder herbsBlockItem(DeferredBlock<Block> item) {
        return withExistingParent(item.getId().getPath(),
                ResourceLocation.parse("item/generated")).texture("layer0",
                ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID,"block/herbs/" + item.getId().getPath()));
    }


    public void buttonItem(DeferredBlock<?> block, DeferredBlock<Block> baseBlock) {
        this.withExistingParent(block.getId().getPath(), mcLoc("block/button_inventory"))
                .texture("texture",  ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID,
                        "block/" + baseBlock.getId().getPath()));
    }

    public void fenceItem(DeferredBlock<?> block, DeferredBlock<Block> baseBlock) {
        this.withExistingParent(block.getId().getPath(), mcLoc("block/fence_inventory"))
                .texture("texture",  ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID,
                        "block/" + baseBlock.getId().getPath()));
    }

    public void wallItem(DeferredBlock<?> block, DeferredBlock<Block> baseBlock) {
        this.withExistingParent(block.getId().getPath(), mcLoc("block/wall_inventory"))
                .texture("wall",  ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID,
                        "block/" + baseBlock.getId().getPath()));
    }
    public ItemModelBuilder manual(Item item){
        return basicItemWithSharedTexture(item,ResourceLocation.fromNamespaceAndPath(
                AscensionCraft.MOD_ID,
                "generic_manual_texture"
        ));
    }
    public ItemModelBuilder tablet(Item item){
        return basicItemWithSharedTexture(item,ResourceLocation.fromNamespaceAndPath(
                AscensionCraft.MOD_ID,
                "tablet_of_destruction"
        ));
    }
    public ItemModelBuilder talisman(Item item){
        return basicItemWithSharedTexture(item,ResourceLocation.fromNamespaceAndPath(
                AscensionCraft.MOD_ID,
                "talismans" //Talismans Texture made by Dewgon
        ));
    }
    public ItemModelBuilder pills(Item item) {
        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(item);
        String itemName = itemId.getPath();
        ResourceLocation textureLoc = ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "item/pills/" + itemName);

        return getBuilder(itemName)
                .parent(new ModelFile.UncheckedModelFile("item/generated"))
                .texture("layer0", textureLoc);
    }
    public ItemModelBuilder powder(Item item) {
        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(item);
        String itemName = itemId.getPath();
        ResourceLocation textureLoc = ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "item/powders/" + itemName);

        return getBuilder(itemName)
                .parent(new ModelFile.UncheckedModelFile("item/generated"))
                .texture("layer0", textureLoc);
    }
    public ItemModelBuilder tokens(Item item) {
        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(item);
        String itemName = itemId.getPath();
        ResourceLocation textureLoc = ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "item/formation_slip/" + itemName);

        return getBuilder(itemName)
                .parent(new ModelFile.UncheckedModelFile("item/generated"))
                .texture("layer0", textureLoc);
    }
    public ItemModelBuilder herbs(Item item) {
        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(item);
        String itemName = itemId.getPath();
        ResourceLocation textureLoc = ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "item/herbs/" + itemName);

        return getBuilder(itemName)
                .parent(new ModelFile.UncheckedModelFile("item/generated"))
                .texture("layer0", textureLoc);
    }
    public ItemModelBuilder basicItemWithSharedTexture(Item item,ResourceLocation texture){
        return basicItemWithSharedTexture(
                Objects.requireNonNull(BuiltInRegistries.ITEM.getKey(item)),
                texture
        );
    }
    public ItemModelBuilder basicItemWithSharedTexture(ResourceLocation item,ResourceLocation texture) {
        return (this.getBuilder(item.toString())).parent(new ModelFile.UncheckedModelFile("item/generated"))
                .texture("layer0", ResourceLocation.fromNamespaceAndPath(texture.getNamespace(), "item/" + texture.getPath()));
    }

    // New method for tinted ingot models
    /*public ItemModelBuilder tintedIngotItem(Item item, int argbColor) {
        String itemName = Objects.requireNonNull(BuiltInRegistries.ITEM.getKey(item)).getPath();
        String hexColor = String.format("%08X", argbColor); // Convert ARGB to hex string

        return getBuilder(itemName)
                .parent(new ModelFile.UncheckedModelFile("item/generated"))
                .customLoader((builder, helper) -> builder
                        .custom("neoforge:item_layers")
                        .end()
                        .element()
                        .texture("layer0", ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "item/ingot_template"))
                        .color(0, hexColor)
                        .end());
    }*/

}
