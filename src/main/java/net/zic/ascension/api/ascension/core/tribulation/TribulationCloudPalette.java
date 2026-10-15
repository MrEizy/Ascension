package net.zic.ascension.api.ascension.core.tribulation;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.ExtraCodecs;

/**
 * three colours a part of the storm is shaded with, from its darkest tone to its brightest
 * @param shadow the darkest tone, deep cloud undersides or a dim eye
 * @param base the middle tone
 * @param highlight the brightest tone, puff tops or a flaring eye
 */
public record TribulationCloudPalette(int shadow, int base, int highlight) {

    //every colour is optional and falls back to the matching one in defaults. accepts "#RRGGBB", an int, or [r, g, b] floats
    public static Codec<TribulationCloudPalette> codec(TribulationCloudPalette defaults) {
        return RecordCodecBuilder.create(
                instance -> instance.group(
                        ExtraCodecs.STRING_RGB_COLOR.optionalFieldOf("shadow", defaults.shadow()).forGetter(TribulationCloudPalette::shadow),
                        ExtraCodecs.STRING_RGB_COLOR.optionalFieldOf("base", defaults.base()).forGetter(TribulationCloudPalette::base),
                        ExtraCodecs.STRING_RGB_COLOR.optionalFieldOf("highlight", defaults.highlight()).forGetter(TribulationCloudPalette::highlight)
                ).apply(instance, TribulationCloudPalette::new)
        );
    }
}
