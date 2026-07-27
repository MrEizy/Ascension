package net.zic.ascension.api.core.movement;

import com.mojang.serialization.Codec;
import net.zic.ascension.api.datapack.TypeRegistries;

public interface MovementDefinition {
    Codec<MovementDefinition> CODEC = TypeRegistries.MOVEMENT_TYPE_REGISTRY.byNameCodec().dispatch(
            MovementDefinition::getType,
            MovementType::codec
    );

    MovementType getType();

    MovementResult execute(MovementContext context);
}
