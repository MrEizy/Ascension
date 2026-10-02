package net.zic.ascension.worldgen.density;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.zic.ascension.AscensionCraft;


public final class AscDensityFunctionTypes {

    private static final DeferredRegister<MapCodec<? extends DensityFunction>> TYPES =
            DeferredRegister.create(BuiltInRegistries.DENSITY_FUNCTION_TYPE, AscensionCraft.MOD_ID);

    static {
        TYPES.register("domain_warp", () -> DomainWarpDensityFunction.DATA_CODEC);
        TYPES.register("slope", () -> SlopeDensityFunction.DATA_CODEC);
        TYPES.register("erosion", () -> ErosionDensityFunction.DATA_CODEC);
        TYPES.register("terrace", () -> TerraceDensityFunction.DATA_CODEC);
        TYPES.register("height_target", () -> HeightTargetDensityFunction.DATA_CODEC);
        TYPES.register("hermite_spline", () -> HermiteSplineDensityFunction.DATA_CODEC);
        TYPES.register("clamp", () -> ClampDensityFunction.DATA_CODEC);
        TYPES.register("mapped", () -> MappedDensityFunction.DATA_CODEC);
        TYPES.register("range_choice", () -> RangeChoiceDensityFunction.DATA_CODEC);
    }

    private AscDensityFunctionTypes() {
    }

    public static void register(IEventBus eventBus) {
        TYPES.register(eventBus);
    }
}
