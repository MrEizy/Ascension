package net.zic.ascension.common.fluids;

import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.sounds.SoundEvents;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.common.SoundActions;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.zic.ascension.AscensionCraft;
import org.joml.Vector4f;

import java.util.function.Supplier;

public class AscFluidTypes {
    public static final DeferredRegister<FluidType> FLUID_TYPES =
            DeferredRegister.create(NeoForgeRegistries.Keys.FLUID_TYPES, AscensionCraft.MOD_ID);


    public static final Supplier<FluidType> LIQUIFIED_SPIRITUAL_QI_TYPE = FLUID_TYPES.register(
            "liquified_spiritual_qi_type",
            () -> new FluidType(FluidType.Properties.create()
                    .canSwim(true)
                    .canDrown(true)
                    .canPushEntity(true)
                    .canExtinguish(true)
                    .motionScale(0.014D)
                    .fallDistanceModifier(0.0F)
                    .density(1000)
                    .viscosity(1000)
                    .temperature(300)
                    .sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL)
                    .sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY)
            ));


    public static IClientFluidTypeExtensions LIQUIFIED_SPIRITUAL_QI_EXTENSION = new IClientFluidTypeExtensions() {
        @Override
        public void modifyFogColor(Camera camera, float partialTick, ClientLevel level, int renderDistance, float darkenWorldAmount, Vector4f fluidFogColor) {
            fluidFogColor.set(0.83f, 0.16f, 0.16f);
            IClientFluidTypeExtensions.super.modifyFogColor(camera, partialTick, level, renderDistance, darkenWorldAmount, fluidFogColor);
        }
    };

    public static void register(IEventBus eventBus) {
        FLUID_TYPES.register(eventBus);
    }
}
