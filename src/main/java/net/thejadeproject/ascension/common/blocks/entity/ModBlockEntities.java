package net.thejadeproject.ascension.common.blocks.entity;


import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.thejadeproject.ascension.AscensionCraft;
import net.thejadeproject.ascension.common.blocks.ModBlocks;

import java.util.function.Supplier;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, AscensionCraft.MOD_ID);


    public static final Supplier<BlockEntityType<PillCauldronLowHumanEntity>> PILL_CAULDRON_LOW_HUMAN =
            BLOCK_ENTITIES.register("pill_cauldron_low_human", () -> BlockEntityType.Builder.of(
                    PillCauldronLowHumanEntity::new, ModBlocks.PILL_CAULDRON_HUMAN_LOW.get()).build(null));


    public static final Supplier<BlockEntityType<SpiritVeinBlockEntity>> SPIRIT_VEIN_BE =
            BLOCK_ENTITIES.register("spirit_vein", () -> BlockEntityType.Builder.of(
                    SpiritVeinBlockEntity::new, ModBlocks.SPIRIT_VEIN.get()).build(null));



    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TechniqueStandBlockEntity>> TECHNIQUE_STAND_BE =
            BLOCK_ENTITIES.register("technique_stand",
                    () -> BlockEntityType.Builder
                            .of(TechniqueStandBlockEntity::new, ModBlocks.TECHNIQUE_STAND.get())
                            .build(null));




    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<FlameStandBlockEntity>> FLAME_STAND =
            BLOCK_ENTITIES.register("flame_stand",
                    () -> BlockEntityType.Builder.of(
                            FlameStandBlockEntity::new,
                            ModBlocks.FLAME_STAND_BLOCK.get()
                    ).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SpiritCondenserBlockEntity>> SPIRIT_CONDENSER =
            BLOCK_ENTITIES.register("spirit_condenser",
                    () -> BlockEntityType.Builder.of(
                            SpiritCondenserBlockEntity::new,
                            ModBlocks.SPIRIT_CONDENSER_BLOCK.get()
                    ).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CauldronPedestalBlockEntity>> CAULDRON_PEDESTAL =
            BLOCK_ENTITIES.register("cauldron_pedestal",
                    () -> BlockEntityType.Builder.of(
                            CauldronPedestalBlockEntity::new,
                            ModBlocks.CAULDRON_PEDESTAL_BLOCK.get()
                    ).build(null));



    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}
