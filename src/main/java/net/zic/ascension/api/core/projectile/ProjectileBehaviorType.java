package net.zic.ascension.api.core.projectile;

import com.mojang.serialization.MapCodec;

public record ProjectileBehaviorType(MapCodec<? extends ProjectileBehavior> codec) {
}
