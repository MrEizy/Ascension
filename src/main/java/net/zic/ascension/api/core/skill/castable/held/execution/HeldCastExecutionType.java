package net.zic.ascension.api.core.skill.castable.held.execution;

import com.mojang.serialization.MapCodec;

public final class HeldCastExecutionType {
    private final MapCodec<? extends HeldCastExecution> codec;

    public HeldCastExecutionType(MapCodec<? extends HeldCastExecution> codec) {
        this.codec = codec;
    }

    public MapCodec<? extends HeldCastExecution> codec() {
        return codec;
    }
}
