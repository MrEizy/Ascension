package net.zic.ascension.api.ascension.core.tribulation;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.ExtraCodecs;

import java.util.Optional;

/**
 * configuration for the storm clouds rendered above a tribulation
 * @param radius radius of the cloud whirlpool in blocks
 * @param rotationSpeed degrees rotated per tick, negative rotates the other way
 * @param renderHeight the world y value of the cloud base
 * @param tint multiplied onto the cloud colours, empty leaves them as they are. does not affect the eye
 * @param pixelSize size of a cloud pixel in blocks at the outer edge, pixels shrink towards the eye
 * @param cloudColors the colours the clouds are shaded with, from the darkest undersides to the brightest tops
 * @param eyeColors the colours of the glowing eye, from dim to flaring. the highlight also colours the lightning
 * @param forming how the storm gathers before the tribulation starts
 * @param dissipating how the storm breaks up once the tribulation is over
 */
public record TribulationCloudSettings(float radius, float rotationSpeed, double renderHeight, Optional<Integer> tint, float pixelSize,
                                       TribulationCloudPalette cloudColors, TribulationCloudPalette eyeColors,
                                       TribulationCloudTransition forming, TribulationCloudTransition dissipating) {
    //greys from 0.2 to 0.9
    public static final TribulationCloudPalette DEFAULT_CLOUD_COLORS = new TribulationCloudPalette(0x333333, 0x8C8C8C, 0xE6E6E6);
    //a dim purple glow that flares towards a pale blue, the same blue as vanilla lightning
    public static final TribulationCloudPalette DEFAULT_EYE_COLORS = new TribulationCloudPalette(0x110F1D, 0x24213F, 0x9999BF);
    //the eyewall and band cores condense first, the eye lights once it is mostly gathered
    public static final TribulationCloudTransition DEFAULT_FORMING = new TribulationCloudTransition(
            100, TribulationCloudTransition.Style.CORES, TribulationCloudTransition.Easing.LINEAR, 0.25F, 0.85F, 0.6F, 0.8F, 0.3F
    );
    //the same played backwards, a little quicker
    public static final TribulationCloudTransition DEFAULT_DISSIPATING = new TribulationCloudTransition(
            60, TribulationCloudTransition.Style.CORES, TribulationCloudTransition.Easing.LINEAR, 0.25F, 0.85F, 0.6F, 0.8F, 0.3F
    );
    public static final TribulationCloudSettings DEFAULT = new TribulationCloudSettings(
            64.0F, 0.5F, 200.0D, Optional.empty(), 3.0F, DEFAULT_CLOUD_COLORS, DEFAULT_EYE_COLORS, DEFAULT_FORMING, DEFAULT_DISSIPATING
    );

    public static final Codec<TribulationCloudSettings> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    Codec.floatRange(1.0F, 1000.0F).optionalFieldOf("radius", DEFAULT.radius()).forGetter(TribulationCloudSettings::radius),
                    Codec.FLOAT.optionalFieldOf("rotation_speed", DEFAULT.rotationSpeed()).forGetter(TribulationCloudSettings::rotationSpeed),
                    Codec.DOUBLE.optionalFieldOf("render_height", DEFAULT.renderHeight()).forGetter(TribulationCloudSettings::renderHeight),
                    //accepts "#RRGGBB", an int, or [r, g, b] floats
                    ExtraCodecs.STRING_RGB_COLOR.optionalFieldOf("tint").forGetter(TribulationCloudSettings::tint),
                    Codec.floatRange(0.25F, 64.0F).optionalFieldOf("pixel_size", DEFAULT.pixelSize()).forGetter(TribulationCloudSettings::pixelSize),
                    TribulationCloudPalette.codec(DEFAULT_CLOUD_COLORS).optionalFieldOf("cloud_colors", DEFAULT_CLOUD_COLORS).forGetter(TribulationCloudSettings::cloudColors),
                    TribulationCloudPalette.codec(DEFAULT_EYE_COLORS).optionalFieldOf("eye_colors", DEFAULT_EYE_COLORS).forGetter(TribulationCloudSettings::eyeColors),
                    TribulationCloudTransition.codec(DEFAULT_FORMING).optionalFieldOf("forming", DEFAULT_FORMING).forGetter(TribulationCloudSettings::forming),
                    TribulationCloudTransition.codec(DEFAULT_DISSIPATING).optionalFieldOf("dissipating", DEFAULT_DISSIPATING).forGetter(TribulationCloudSettings::dissipating)
            ).apply(instance, TribulationCloudSettings::new)
    );
}
