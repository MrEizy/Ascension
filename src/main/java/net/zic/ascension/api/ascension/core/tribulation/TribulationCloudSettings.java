package net.zic.ascension.api.ascension.core.tribulation;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.ExtraCodecs;

import java.util.Optional;

/**
 * configuration for the storm clouds rendered above a tribulation
 * @param radius radius of the cloud whirlpool in blocks
 * @param rotationSpeed degrees rotated per tick, negative rotates the other way
 * @param renderHeight the world y value of the eye
 * @param tint multiplied onto the greyscale clouds, empty leaves them greyscale
 * @param pixelSize size of a cloud pixel in blocks at the outer edge, pixels shrink towards the eye
 */
public record TribulationCloudSettings(float radius, float rotationSpeed, double renderHeight, Optional<Integer> tint, float pixelSize) {
    public static final TribulationCloudSettings DEFAULT = new TribulationCloudSettings(64.0F, 0.5F, 200.0D, Optional.empty(), 3.0F);

    public static final Codec<TribulationCloudSettings> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    Codec.floatRange(1.0F, 512.0F).optionalFieldOf("radius", DEFAULT.radius()).forGetter(TribulationCloudSettings::radius),
                    Codec.FLOAT.optionalFieldOf("rotation_speed", DEFAULT.rotationSpeed()).forGetter(TribulationCloudSettings::rotationSpeed),
                    Codec.DOUBLE.optionalFieldOf("render_height", DEFAULT.renderHeight()).forGetter(TribulationCloudSettings::renderHeight),
                    //accepts "#RRGGBB", an int, or [r, g, b] floats
                    ExtraCodecs.STRING_RGB_COLOR.optionalFieldOf("tint").forGetter(TribulationCloudSettings::tint),
                    Codec.floatRange(0.25F, 64.0F).optionalFieldOf("pixel_size", DEFAULT.pixelSize()).forGetter(TribulationCloudSettings::pixelSize)
            ).apply(instance, TribulationCloudSettings::new)
    );
}
