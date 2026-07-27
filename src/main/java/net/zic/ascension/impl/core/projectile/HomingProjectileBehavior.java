package net.zic.ascension.impl.core.projectile;

import net.zic.ascension.api.datapack.CodecType;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.zic.ascension.api.core.projectile.ProjectileBehavior;
import net.zic.ascension.api.core.projectile.ProjectileBehaviorContext;
import net.zic.ascension.api.core.targeting.SkillTarget;
import net.zic.ascension.api.core.targeting.TargetingContext;
import net.zic.ascension.api.core.targeting.TargetingDefinition;
import net.zic.ascension.api.core.targeting.TargetingResult;
import net.zic.ascension.api.value.ScaledValue;
import net.zic.ascension.impl.datapack.projectile.AscensionProjectileBehaviorTypes;

import java.util.Optional;

public record HomingProjectileBehavior(
        ScaledValue turnRate,
        Optional<TargetingDefinition> acquisition,
        boolean reacquire
) implements ProjectileBehavior {
    public static final MapCodec<HomingProjectileBehavior> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ScaledValue.CODEC.codec().fieldOf("turn_rate").forGetter(HomingProjectileBehavior::turnRate),
            TargetingDefinition.CODEC.optionalFieldOf("acquisition").forGetter(HomingProjectileBehavior::acquisition),
            Codec.BOOL.optionalFieldOf("reacquire", true).forGetter(HomingProjectileBehavior::reacquire)
    ).apply(instance, HomingProjectileBehavior::new));

    public HomingProjectileBehavior {
        acquisition = acquisition == null ? Optional.empty() : acquisition;
    }

    @Override
    public CodecType<ProjectileBehavior> getType() {
        return AscensionProjectileBehaviorTypes.HOMING.get();
    }

    @Override
    public boolean beforeMove(ProjectileBehaviorContext context) {
        LivingEntity target = context.target();
        if ((target == null || target.isRemoved() || !target.isAlive()) && reacquire && acquisition.isPresent()) {
            TargetingResult result = acquisition.get().resolve(new TargetingContext(
                    context.level(),
                    context.owner(),
                    context.projectile().skillId(),
                    context.execution().variables().getOrDefault(TargetingContext.EFFECTIVE_LEVEL, 0.0D).intValue(),
                    context.execution().charge(),
                    context.execution().variables()
            ));
            SkillTarget acquired = result.primaryTarget();
            target = acquired == null ? null : acquired.entity();
            context.projectile().setTargetId(target == null ? null : target.getUUID());
        }
        if (target == null || target.isRemoved() || !target.isAlive()) {
            return true;
        }

        Vec3 velocity = context.projectile().velocity();
        Vec3 desired = target.getBoundingBox().getCenter().subtract(context.projectile().position());
        if (velocity.lengthSqr() <= 1.0E-8D || desired.lengthSqr() <= 1.0E-8D) {
            return true;
        }

        double radians = turnRate.resolve(context.execution().scaledValueContext());
        if (!Double.isFinite(radians) || radians <= 0.0D) {
            return true;
        }

        Vec3 currentDirection = velocity.normalize();
        Vec3 desiredDirection = desired.normalize();
        double angle = Math.acos(Math.clamp(currentDirection.dot(desiredDirection), -1.0D, 1.0D));
        if (angle <= 1.0E-6D) {
            return true;
        }

        double blend = Math.min(1.0D, radians / angle);
        Vec3 direction = currentDirection.scale(1.0D - blend).add(desiredDirection.scale(blend)).normalize();
        context.projectile().setVelocity(direction.scale(velocity.length()));
        return true;
    }
}
