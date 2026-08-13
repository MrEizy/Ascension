package net.zic.ascension.common.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.common.blocks.blockentities.FermentingBarrelBlock;
import net.zic.ascension.common.blocks.crops.herbs.HerbCropBlock;
import net.zic.ascension.common.blocks.crops.herbs.HerbStemCropBlock;
import net.zic.ascension.common.blocks.crops.herbs.PodHerbBlock;
import net.zic.ascension.common.blocks.crops.mushrooms.LingzhiMushroomBlock;
import net.zic.ascension.common.fluids.AscFluids;
import net.zic.ascension.common.herbs.ModHerbs;
import net.zic.ascension.common.item.ModItems;
import net.zic.ascension.worldgen.tree.AscTreeGrowers;

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



    //Fluids

    public static final DeferredBlock<LiquidBlock> LIQUIFIED_SPIRITUAL_QI_BLOCK = BLOCKS.registerBlock("liquified_spiritual_qi_block",
            properties -> new LiquidBlock(AscFluids.LIQUIFIED_SPIRITUAL_QI_SOURCE.get(), properties
                    .mapColor(MapColor.WATER).replaceable().noCollision().strength(100.0F)
                    .pushReaction(PushReaction.DESTROY).noLootTable().liquid().sound(SoundType.EMPTY)));



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
                            .pushReaction(PushReaction.DESTROY),
                    ModHerbs.JADE_DEW_GRASS,
                    () -> ModItems.JADE_DEW_GRASS.get()
            ));

    public static final DeferredBlock<HerbCropBlock> GINSENG_CROP = BLOCKS.registerBlock("ginseng_crop",
            properties -> new HerbCropBlock(
                    properties
                            .strength(0.0f)
                            .sound(SoundType.GRASS)
                            .noCollision()
                            .randomTicks()
                            .pushReaction(PushReaction.DESTROY),
                    ModHerbs.GINSENG,
                    () -> ModItems.GINSENG.get()
            ));

    public static final DeferredBlock<HerbCropBlock> FIRE_GINSENG_CROP = BLOCKS.registerBlock("fire_ginseng_crop",
            properties -> new HerbCropBlock(
                    properties
                            .strength(0.0f)
                            .sound(SoundType.GRASS)
                            .noCollision()
                            .randomTicks()
                            .pushReaction(PushReaction.DESTROY),
                    ModHerbs.FIRE_GINSENG,
                    () -> ModItems.FIRE_GINSENG.get()
            ));

    public static final DeferredBlock<HerbCropBlock> SNOW_GINSENG_CROP = BLOCKS.registerBlock("snow_ginseng_crop",
            properties -> new HerbCropBlock(
                    properties
                            .strength(0.0f)
                            .sound(SoundType.GRASS)
                            .noCollision()
                            .randomTicks()
                            .pushReaction(PushReaction.DESTROY),
                    ModHerbs.SNOW_GINSENG,
                    () -> ModItems.SNOW_GINSENG.get()
            ));
    public static final DeferredBlock<HerbCropBlock> WHITE_JADE_ORCHID_CROP = BLOCKS.registerBlock("white_jade_orchid_crop",
            properties -> new HerbStemCropBlock(
                    properties
                            .strength(0.0f)
                            .sound(SoundType.GRASS)
                            .noCollision()
                            .randomTicks()
                            .pushReaction(PushReaction.DESTROY),
                    ModHerbs.WHITE_JADE_ORCHID,
                    () -> ModItems.WHITE_JADE_ORCHID.get()
            ));

    public static final DeferredBlock<LingzhiMushroomBlock> LINGZHI_MUSHROOM_B = registerBlock("lingzhi_mushroom_b",
            properties -> new LingzhiMushroomBlock(
                    properties
                            .strength(0.2f)
                            .sound(SoundType.STEM)
                            .randomTicks()
                            .pushReaction(PushReaction.DESTROY),
                    ModHerbs.LINGZHI_MUSHROOM,
                    () -> ModItems.LINGZHI_MUSHROOM.get()
            ));

    public static final DeferredBlock<LingzhiMushroomBlock> BLOOD_LINGZHI_MUSHROOM_B = registerBlock("blood_lingzhi_mushroom_b",
            properties -> new LingzhiMushroomBlock(
                    properties
                            .strength(0.2f)
                            .sound(SoundType.STEM)
                            .randomTicks()
                            .pushReaction(PushReaction.DESTROY),
                    ModHerbs.BLOOD_LINGZHI_MUSHROOM,
                    () -> ModItems.BLOOD_LINGZHI_MUSHROOM.get()
            ));

    public static final DeferredBlock<PodHerbBlock> PEACH_POD = registerBlock("peach_pod",
            properties -> new PodHerbBlock(properties
                    .strength(0.2f, 3.0f)
                    .sound(SoundType.WOOD)
                    .noOcclusion()
                    .randomTicks()
                    .pushReaction(PushReaction.DESTROY)
                    .mapColor(MapColor.PLANT),
                    ModHerbs.PEACH,
                    () -> ModItems.PEACH.get()));


    //Trees
    public static final DeferredBlock<Block> PEACH_LOG = registerBlock("peach_log",
            properties -> new AscFlammableRotatedPillarBlock(properties.sound(SoundType.WOOD).strength(2f)
                    .ignitedByLava()));
    public static final DeferredBlock<Block> PEACH_WOOD = registerBlock("peach_wood",
            properties -> new AscFlammableRotatedPillarBlock(properties.sound(SoundType.WOOD).strength(2f)
                    .ignitedByLava()));
    public static final DeferredBlock<Block> STRIPPED_PEACH_LOG = registerBlock("stripped_peach_log",
            properties -> new AscFlammableRotatedPillarBlock(properties.sound(SoundType.WOOD).strength(2f)
                    .ignitedByLava()));
    public static final DeferredBlock<Block> STRIPPED_PEACH_WOOD = registerBlock("stripped_peach_wood",
            properties -> new AscFlammableRotatedPillarBlock(properties.sound(SoundType.WOOD).strength(2f)
                    .ignitedByLava()));

    public static final DeferredBlock<Block> PEACH_PLANKS = registerBlock("peach_planks",
            properties -> new Block(properties.sound(SoundType.WOOD).strength(2f).ignitedByLava()) {
                @Override
                public boolean isFlammable(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
                    return true;
                }

                @Override
                public int getFlammability(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
                    return 20;
                }

                @Override
                public int getFireSpreadSpeed(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
                    return 5;
                }
            });

    public static final DeferredBlock<Block> PEACH_LEAVES = registerBlock("peach_leaves",
            properties -> new UntintedParticleLeavesBlock(0f, ParticleTypes.CHERRY_LEAVES,
                    properties.mapColor(MapColor.PLANT).strength(0.2F)
                            .randomTicks().sound(SoundType.CHERRY_LEAVES)
                            .noOcclusion().isValidSpawn(Blocks::ocelotOrParrot)
                            .isSuffocating((state, level, pos) -> false)
                            .isViewBlocking((state, level, pos) -> false)
                            .ignitedByLava().pushReaction(PushReaction.DESTROY)
                            .isRedstoneConductor((state, level, pos) -> false)) {
                @Override
                public boolean isFlammable(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
                    return true;
                }

                @Override
                public int getFlammability(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
                    return 60;
                }

                @Override
                public int getFireSpreadSpeed(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
                    return 30;
                }
            });

    public static final DeferredBlock<Block> PEACH_SAPLING = registerBlock("peach_sapling",
            properties -> new SaplingBlock(AscTreeGrowers.PEACH, properties.mapColor(MapColor.PLANT).noCollision()
                    .randomTicks().instabreak().sound(SoundType.GRASS).pushReaction(PushReaction.DESTROY)));


    public static final DeferredBlock<Block> POTTED_PEACH_SAPLING = BLOCKS.registerBlock("potted_peach_sapling",
            properties -> new FlowerPotBlock(() -> (FlowerPotBlock) Blocks.FLOWER_POT, PEACH_SAPLING,
                    properties.noOcclusion().instabreak().pushReaction(PushReaction.DESTROY)));




    //Block Entities
    public static final DeferredBlock<FermentingBarrelBlock> FERMENTING_BARREL = registerBlock("fermenting_barrel",
            properties -> new FermentingBarrelBlock(properties.strength(2).explosionResistance(3.5f)
                    .requiresCorrectToolForDrops().sound(SoundType.WOOD)));





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
