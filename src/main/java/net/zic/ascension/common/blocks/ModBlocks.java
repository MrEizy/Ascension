package net.zic.ascension.common.blocks;

import net.minecraft.network.chat.Component;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DropExperienceBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.common.blocks.crops.herbs.HerbCropBlock;
import net.zic.ascension.common.blocks.crops.mushrooms.LingzhiMushroomBlock;
import net.zic.ascension.common.herbs.ModHerbs;
import net.zic.ascension.common.item.ModItems;

import java.util.function.Consumer;
import java.util.function.Function;

public class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(AscensionCraft.MOD_ID);

    //Ores

    public static final DeferredBlock<Block> BLACK_IRON_ORE = registerBlock("black_iron_ore",
            properties -> new Block(properties.strength(4.5f).explosionResistance(6.0f)
                    .requiresCorrectToolForDrops().sound(SoundType.STONE)));
    public static final DeferredBlock<Block> FROST_SILVER_ORE = registerBlock("frost_silver_ore",
            properties -> new Block(properties.strength(4.5f).explosionResistance(6.0f)
                    .requiresCorrectToolForDrops().sound(SoundType.STONE)));
    public static final DeferredBlock<Block> JADE_ORE = registerBlock("jade_ore",
            properties -> new DropExperienceBlock(UniformInt.of(2, 4), properties.strength(4.5f).explosionResistance(3.5f)
                    .requiresCorrectToolForDrops().sound(SoundType.STONE)));




    public static final DeferredBlock<Block> JADE_BLOCK  = registerBlock("jade_block",
            properties -> new Block(properties.strength(5.5f).explosionResistance(4.5f)
                    .requiresCorrectToolForDrops().sound(SoundType.METAL)));
    public static final DeferredBlock<Block> BLACK_IRON_BLOCK  = registerBlock("black_iron_block",
            properties -> new Block(properties.strength(5.5f).explosionResistance(4.5f)
                    .requiresCorrectToolForDrops().sound(SoundType.METAL)));
    public static final DeferredBlock<Block> FROST_SILVER_BLOCK  = registerBlock("frost_silver_block",
            properties -> new Block(properties.strength(5.5f).explosionResistance(4.5f)
                    .requiresCorrectToolForDrops().sound(SoundType.METAL)));



    //Herb Blocks
    public static final DeferredBlock<Block> CULTIVATION_SOIL = registerBlock("cultivation_soil",
            properties -> new CultivationSoilBlock(properties
                    .strength(0.6f)
                    .sound(SoundType.WET_GRASS)));

    public static final DeferredBlock<HerbCropBlock> JADE_DEW_GRASS_CROP = BLOCKS.registerBlock("jade_dew_grass_crop",
            properties -> new HerbCropBlock(
                    properties
                            .strength(0.0f)
                            .sound(SoundType.GRASS)
                            .noCollision()
                            .randomTicks()
                            .noLootTable()
                            .pushReaction(PushReaction.DESTROY),
                    ModHerbs.JADE_DEW_GRASS,
                    () -> ModItems.JADE_DEW_GRASS.get(),
                    () -> ModItems.JADE_DEW_GRASS_SEEDS.get()
            ));

    public static final DeferredBlock<HerbCropBlock> GINSENG_CROP = BLOCKS.registerBlock("ginseng_crop",
            properties -> new HerbCropBlock(
                    properties
                            .strength(0.0f)
                            .sound(SoundType.GRASS)
                            .noCollision()
                            .randomTicks()
                            .noLootTable()
                            .pushReaction(PushReaction.DESTROY),
                    ModHerbs.GINSENG,
                    () -> ModItems.GINSENG.get(),
                    () -> ModItems.GINSENG.get()
            ));
    public static final DeferredBlock<HerbCropBlock> FIRE_GINSENG_CROP = BLOCKS.registerBlock("fire_ginseng_crop",
            properties -> new HerbCropBlock(
                    properties
                            .strength(0.0f)
                            .sound(SoundType.GRASS)
                            .noCollision()
                            .randomTicks()
                            .noLootTable()
                            .pushReaction(PushReaction.DESTROY),
                    ModHerbs.FIRE_GINSENG,
                    () -> ModItems.FIRE_GINSENG.get(),
                    () -> ModItems.FIRE_GINSENG.get()
            ));
    public static final DeferredBlock<HerbCropBlock> SNOW_GINSENG_CROP = BLOCKS.registerBlock("snow_ginseng_crop",
            properties -> new HerbCropBlock(
                    properties
                            .strength(0.0f)
                            .sound(SoundType.GRASS)
                            .noCollision()
                            .randomTicks()
                            .noLootTable()
                            .pushReaction(PushReaction.DESTROY),
                    ModHerbs.SNOW_GINSENG,
                    () -> ModItems.SNOW_GINSENG.get(),
                    () -> ModItems.SNOW_GINSENG.get()
            ));

    public static final DeferredBlock<Block> LINGZHI_MUSHROOM_B = registerBlock("lingzhi_mushroom_b",
            properties -> new LingzhiMushroomBlock(properties
                    .strength(0.2f)
                    .sound(SoundType.STEM)
                    .pushReaction(PushReaction.DESTROY),
                    BlockTags.LOGS));
    public static final DeferredBlock<Block> BLOOD_LINGZHI_MUSHROOM_B = registerBlock("blood_lingzhi_mushroom_b",
            properties -> new LingzhiMushroomBlock(properties
                    .strength(0.2f)
                    .sound(SoundType.STEM)
                    .pushReaction(PushReaction.DESTROY),
                    Blocks.BONE_BLOCK));



    private static <T extends Block> DeferredBlock<T> registerBlock(String name, Function<BlockBehaviour.Properties, T> function, Component... components) {
        DeferredBlock<T> toReturn = BLOCKS.registerBlock(name, function);
        registerBlockItem(name, toReturn, components);
        return toReturn;
    }

    private static <T extends Block> void registerBlockItem(String name, DeferredBlock<T> block, Component... components) {
        ModItems.ITEMS.registerItem(name, properties -> new BlockItem(block.get(), properties.useBlockDescriptionPrefix()) {
            @Override
            public void appendHoverText(ItemStack itemStack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag tooltipFlag) {
                for(var component : components) {
                    builder.accept(component);
                }
                super.appendHoverText(itemStack, context, display, builder, tooltipFlag);
            }
        });
    }

    private static <T extends Block> DeferredBlock<T> registerBlock(String name, Function<BlockBehaviour.Properties, T> function) {
        DeferredBlock<T> toReturn = BLOCKS.registerBlock(name, function);
        registerBlockItem(name, toReturn);
        return toReturn;
    }

    private static <T extends Block> void registerBlockItem(String name, DeferredBlock<T> block) {
        ModItems.ITEMS.registerItem(name, properties -> new BlockItem(block.get(), properties.useBlockDescriptionPrefix()));
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}
