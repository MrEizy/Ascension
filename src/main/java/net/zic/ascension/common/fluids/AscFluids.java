package net.zic.ascension.common.fluids;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.common.blocks.ModBlocks;
import net.zic.ascension.common.item.ModItems;

import java.util.function.Supplier;

public class AscFluids {
    public static final DeferredRegister<Fluid> FLUIDS =
            DeferredRegister.create(BuiltInRegistries.FLUID, AscensionCraft.MOD_ID);



    public static final Supplier<FlowingFluid> LIQUIFIED_SPIRITUAL_QI_SOURCE = FLUIDS.register("liquified_spiritual_qi",
            () -> new BaseFlowingFluid.Source(AscFluids.LIQUIFIED_SPIRITUAL_QI_PROPERTIES));

    public static final Supplier<FlowingFluid> LIQUIFIED_SPIRITUAL_QI_FLOWING = FLUIDS.register("liquified_spiritual_qi_flowing",
            () -> new BaseFlowingFluid.Flowing(AscFluids.LIQUIFIED_SPIRITUAL_QI_PROPERTIES));


    private static final BaseFlowingFluid.Properties LIQUIFIED_SPIRITUAL_QI_PROPERTIES =
            new BaseFlowingFluid.Properties(
                    AscFluidTypes.LIQUIFIED_SPIRITUAL_QI_TYPE,
                    LIQUIFIED_SPIRITUAL_QI_SOURCE,
                    LIQUIFIED_SPIRITUAL_QI_FLOWING)
                    .slopeFindDistance(2)
                    .levelDecreasePerBlock(1)
                    .block(ModBlocks.LIQUIFIED_SPIRITUAL_QI_BLOCK)
                    .bucket(ModItems.LIQUIFIED_SPIRITUAL_QI_BUCKET);


    public static void register(IEventBus eventBus) {
        FLUIDS.register(eventBus);
    }
}
