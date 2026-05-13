package net.thejadeproject.ascension.particle;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.thejadeproject.ascension.AscensionCraft;
import net.thejadeproject.ascension.particle.aura.AuraParticleOptions;

import java.util.function.Supplier;

public class ModParticles {
    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES =
            DeferredRegister.create(BuiltInRegistries.PARTICLE_TYPE, AscensionCraft.MOD_ID);


    public static final Supplier<SimpleParticleType> CULTIVATION_PARTICLES =
            PARTICLE_TYPES.register("cultivation_particles", () -> new SimpleParticleType(true));

    public static final Supplier<SimpleParticleType> FIRE_PARTICLE =
            PARTICLE_TYPES.register("fire_particle",()->new SimpleParticleType(true));
    public static void register(IEventBus eventBus) {

        PARTICLE_TYPES.register(eventBus);
    }

    public static final DeferredHolder<ParticleType<?>, ParticleType<AuraParticleOptions>> AURA_SMOKE =
            PARTICLE_TYPES.register("aura_smoke", () ->
                    new ParticleType<AuraParticleOptions>(true) {
                        @Override
                        public MapCodec<AuraParticleOptions> codec() {
                            return AuraParticleOptions.codec(this);
                        }

                        @Override
                        public StreamCodec<? super RegistryFriendlyByteBuf, AuraParticleOptions> streamCodec() {
                            return AuraParticleOptions.streamCodec(this);
                        }
                    }
            );

    public static final DeferredHolder<ParticleType<?>, ParticleType<AuraParticleOptions>> AURA_SPARK =
            PARTICLE_TYPES.register("aura_spark", () ->
                    new ParticleType<AuraParticleOptions>(true) {
                        @Override
                        public MapCodec<AuraParticleOptions> codec() {
                            return AuraParticleOptions.codec(this);
                        }

                        @Override
                        public StreamCodec<? super RegistryFriendlyByteBuf, AuraParticleOptions> streamCodec() {
                            return AuraParticleOptions.streamCodec(this);
                        }
                    }
            );

    public static final DeferredHolder<ParticleType<?>, ParticleType<AuraParticleOptions>> AURA_FLAME =
            PARTICLE_TYPES.register("aura_flame", () ->
                    new ParticleType<AuraParticleOptions>(true) {
                        @Override
                        public MapCodec<AuraParticleOptions> codec() {
                            return AuraParticleOptions.codec(this);
                        }

                        @Override
                        public StreamCodec<? super RegistryFriendlyByteBuf, AuraParticleOptions> streamCodec() {
                            return AuraParticleOptions.streamCodec(this);
                        }
                    }
            );

    public static final DeferredHolder<ParticleType<?>, ParticleType<AuraParticleOptions>> AURA_RING =
            PARTICLE_TYPES.register("aura_ring", () ->
                    new ParticleType<AuraParticleOptions>(true) {
                        @Override
                        public MapCodec<AuraParticleOptions> codec() {
                            return AuraParticleOptions.codec(this);
                        }

                        @Override
                        public StreamCodec<? super RegistryFriendlyByteBuf, AuraParticleOptions> streamCodec() {
                            return AuraParticleOptions.streamCodec(this);
                        }
                    }
            );

}
