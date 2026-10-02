package net.zic.ascension.api.ascension.core.projectile;

import com.mojang.serialization.Codec;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.zic.ascension.api.ascension.core.skill.castable.action.SkillActionContext;

import java.util.Set;
import java.util.UUID;
import net.zic.ascension.api.ascension.datapack.CodecType;
import net.zic.ascension.api.ascension.datapack.TypeRegistries;

public interface ProjectileBehavior {
    Codec<ProjectileBehavior> CODEC = TypeRegistries.PROJECTILE_BEHAVIOR_TYPE_REGISTRY.byNameCodec().dispatch(
            ProjectileBehavior::getType,
            CodecType<ProjectileBehavior>::codec
    );

    CodecType<ProjectileBehavior> getType();

    default boolean beforeMove(Context context) {
        return true;
    }

    default void afterEntityHit(Context context) {
    }

    default void afterBlockHit(Context context) {
    }

    default void onExpire(Context context) {
    }
    record Context(
            ServerLevel level,
            LivingEntity owner,
            LivingEntity target,
            Access projectile,
            SkillActionContext execution
    ) {
    }

    interface Access {
        UUID runtimeId();
        UUID ownerId();
        Identifier skillId();
        Vec3 position();
        void setPosition(Vec3 position);
        Vec3 velocity();
        void setVelocity(Vec3 velocity);
        UUID targetId();
        void setTargetId(UUID targetId);
        double travelled();
        int pierces();
        Set<UUID> hitEntities();
    }

}
