package net.zic.ascension.api.core.movement;

import com.mojang.serialization.MapCodec;

public record MovementType(MapCodec<? extends MovementDefinition> codec) {
}
