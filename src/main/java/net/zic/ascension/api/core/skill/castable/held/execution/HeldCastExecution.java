package net.zic.ascension.api.core.skill.castable.held.execution;

import com.mojang.serialization.Codec;
import net.zic.ascension.api.datapack.TypeRegistries;

public interface HeldCastExecution {
    Codec<HeldCastExecution> CODEC = TypeRegistries.HELD_CAST_EXECUTION_TYPE_REGISTRY.byNameCodec().dispatch(
            HeldCastExecution::getType,
            HeldCastExecutionType::codec
    );

    HeldCastExecutionType getType();

    void execute(HeldCastExecutionContext context);
}
