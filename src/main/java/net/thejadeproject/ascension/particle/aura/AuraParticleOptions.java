package net.thejadeproject.ascension.particle.aura;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public record AuraParticleOptions(
        ParticleType<AuraParticleOptions> type,
        float red,
        float green,
        float blue,
        float scale,
        int lifetime
) implements ParticleOptions {

    public static MapCodec<AuraParticleOptions> codec(ParticleType<AuraParticleOptions> type) {
        return RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.FLOAT.fieldOf("red").forGetter(AuraParticleOptions::red),
                Codec.FLOAT.fieldOf("green").forGetter(AuraParticleOptions::green),
                Codec.FLOAT.fieldOf("blue").forGetter(AuraParticleOptions::blue),
                Codec.FLOAT.fieldOf("scale").forGetter(AuraParticleOptions::scale),
                Codec.INT.fieldOf("lifetime").forGetter(AuraParticleOptions::lifetime)
        ).apply(instance, (red, green, blue, scale, lifetime) ->
                new AuraParticleOptions(type, red, green, blue, scale, lifetime)
        ));
    }

    public static StreamCodec<RegistryFriendlyByteBuf, AuraParticleOptions> streamCodec(
            ParticleType<AuraParticleOptions> type
    ) {
        return StreamCodec.of(
                (buf, options) -> {
                    buf.writeFloat(options.red());
                    buf.writeFloat(options.green());
                    buf.writeFloat(options.blue());
                    buf.writeFloat(options.scale());
                    buf.writeInt(options.lifetime());
                },
                buf -> new AuraParticleOptions(
                        type,
                        buf.readFloat(),
                        buf.readFloat(),
                        buf.readFloat(),
                        buf.readFloat(),
                        buf.readInt()
                )
        );
    }

    @Override
    public ParticleType<?> getType() {
        return type;
    }

    public static AuraParticleOptions of(
            ParticleType<AuraParticleOptions> type,
            AuraParticleColor color,
            float scale,
            int lifetime
    ) {
        return new AuraParticleOptions(
                type,
                color.red(),
                color.green(),
                color.blue(),
                scale,
                lifetime
        );
    }
}