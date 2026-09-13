package net.zic.ascension.common.blocks.entity;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.common.blocks.ModBlocks;


import java.util.function.Supplier;

public class AscBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, AscensionCraft.MOD_ID);


    public static final Supplier<BlockEntityType<SpiritVeinBlockEntity>> SPIRIT_VEIN_BE =
            BLOCK_ENTITIES.register("spirit_vein_be", () -> new BlockEntityType<>(
                    SpiritVeinBlockEntity::new, ModBlocks.SPIRIT_VEIN.get()));

    public static final Supplier<BlockEntityType<AlchemyFurnaceBlockEntity>> ALCHEMY_FURNACE_BE =
            BLOCK_ENTITIES.register("alchemy_furnace_be", () -> new BlockEntityType<>(
                    AlchemyFurnaceBlockEntity::new, ModBlocks.ALCHEMY_FURNACE.get()));






    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}
