package net.zic.ascension.api.core.projectile;

import com.mojang.serialization.Codec;
import net.zic.ascension.api.ascension.datapack.CodecType;
import net.zic.ascension.api.ascension.datapack.TypeRegistries;

public interface ProjectileBehavior {
    Codec<ProjectileBehavior> CODEC = TypeRegistries.PROJECTILE_BEHAVIOR_TYPE_REGISTRY.byNameCodec().dispatch(
            ProjectileBehavior::getType,
            CodecType<ProjectileBehavior>::codec
    );

    CodecType<ProjectileBehavior> getType();

    default boolean beforeMove(ProjectileBehaviorContext context) {
        return true;
    }

    default void afterEntityHit(ProjectileBehaviorContext context) {
    }

    default void afterBlockHit(ProjectileBehaviorContext context) {
    }

    default void onExpire(ProjectileBehaviorContext context) {
    }
}
