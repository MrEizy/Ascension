package net.zic.ascension.common.particle;

import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.zic.ascension.AscensionCraft;

import java.util.function.Supplier;

public final class AscensionParticles {
    private static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES =
            DeferredRegister.create(BuiltInRegistries.PARTICLE_TYPE, AscensionCraft.MOD_ID);

    public static final Supplier<SimpleParticleType> PARTICLE_FIELD_SPARK = register("particle_field_spark");
    public static final Supplier<SimpleParticleType> PARTICLE_FIELD_WISP = register("particle_field_wisp");
    public static final Supplier<SimpleParticleType> PARTICLE_FIELD_BLOB = register("particle_field_blob");
    public static final Supplier<SimpleParticleType> PARTICLE_FIELD_MOTE = register("particle_field_mote");
    public static final Supplier<SimpleParticleType> PARTICLE_FIELD_PETAL = register("particle_field_petal");
    public static final Supplier<SimpleParticleType> PARTICLE_FIELD_RUNE = register("particle_field_rune");
    public static final Supplier<SimpleParticleType> PARTICLE_FIELD_THREAD = register("particle_field_thread");


    public static final Supplier<SimpleParticleType> PARTCILE_FIELD_PETAL_LOTUS = register("particle_field_petal_lotus");

    private AscensionParticles() {
    }

    public static void register(IEventBus bus) {
        PARTICLE_TYPES.register(bus);
    }

    private static Supplier<SimpleParticleType> register(String name) {
        return PARTICLE_TYPES.register(name, () -> new SimpleParticleType(false));
    }
}
