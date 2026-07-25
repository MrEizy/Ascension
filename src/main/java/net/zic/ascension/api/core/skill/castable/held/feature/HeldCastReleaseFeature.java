package net.zic.ascension.api.core.skill.castable.held.feature;

import com.mojang.serialization.Codec;
import net.zic.ascension.api.datapack.TypeRegistries;

public interface HeldCastReleaseFeature {
    Codec<HeldCastReleaseFeature> CODEC = TypeRegistries.HELD_CAST_RELEASE_FEATURE_TYPE_REGISTRY.byNameCodec().dispatch(
            HeldCastReleaseFeature::getType,
            HeldCastReleaseFeatureType::codec
    );

    HeldCastReleaseFeatureType getType();

    void apply(HeldCastReleaseContext context);
}
