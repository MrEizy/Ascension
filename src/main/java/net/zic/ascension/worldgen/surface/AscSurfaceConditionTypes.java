package net.zic.ascension.worldgen.surface;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.SurfaceRules;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.zic.ascension.AscensionCraft;

public final class AscSurfaceConditionTypes {
    private static final DeferredRegister<MapCodec<? extends SurfaceRules.ConditionSource>> TYPES =
            DeferredRegister.create(Registries.MATERIAL_CONDITION, AscensionCraft.MOD_ID);

    static {
        TYPES.register("density_threshold", () -> DensitySurfaceCondition.DATA_CODEC);
    }

    private AscSurfaceConditionTypes() {
    }

    public static void register(IEventBus eventBus) {
        TYPES.register(eventBus);
    }
}
