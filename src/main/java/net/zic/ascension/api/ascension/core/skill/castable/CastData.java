package net.zic.ascension.api.ascension.core.skill.castable;

import io.netty.buffer.ByteBuf;

public interface CastData {
    void encode(ByteBuf buf);

    default boolean isDirty() {
        return false;
    }

    default void resolveDirty() {
    }
}
