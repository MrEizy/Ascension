package net.zic.ascension.api.ascension.core.skill;

import com.mojang.serialization.Codec;
import net.minecraft.resources.Identifier;
import net.zic.ascension.api.ascension.datapack.CodecType;
import net.zic.ascension.api.rpg_engine.source.OriginSource;
import net.zic.ascension.impl.core.skill.passive.PassiveModules;

public interface PassiveModule {
    Codec<PassiveModule> CODEC = PassiveModules.REGISTRY.byNameCodec().dispatch(
            PassiveModule::getType,
            CodecType<PassiveModule>::codec
    );

    CodecType<PassiveModule> getType();

    default void apply(OriginSource source, Identifier skillId) {
    }

    default void remove(OriginSource source, Identifier skillId) {
    }
}
