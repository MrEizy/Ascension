package net.zic.ascension.api.ascension.core.skill.passive;

import com.mojang.serialization.Codec;
import net.minecraft.resources.Identifier;
import net.zic.ascension.api.ascension.datapack.CodecType;
import net.zic.ascension.api.rpg_engine.source.OriginSource;
import net.zic.ascension.impl.core.skill.passive.PassiveModifiers;

public interface PassiveModifier {
    Codec<PassiveModifier> CODEC = PassiveModifiers.REGISTRY.byNameCodec().dispatch(
            PassiveModifier::getType,
            CodecType<PassiveModifier>::codec
    );

    CodecType<PassiveModifier> getType();

    default void apply(OriginSource source, Identifier skillId) {
    }

    default void remove(OriginSource source, Identifier skillId) {
    }
}
