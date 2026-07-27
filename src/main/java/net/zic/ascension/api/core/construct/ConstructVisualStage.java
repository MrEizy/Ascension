package net.zic.ascension.api.core.construct;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;

public record ConstructVisualStage(
        double maximumStabilityFraction,
        Identifier visual
) {
    public static final Codec<ConstructVisualStage> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.doubleRange(0.0D, 1.0D).fieldOf("maximum_stability_fraction")
                    .forGetter(ConstructVisualStage::maximumStabilityFraction),
            Identifier.CODEC.fieldOf("visual").forGetter(ConstructVisualStage::visual)
    ).apply(instance, ConstructVisualStage::new));
}
