package net.zic.ascension.impl.core.skill.castable.feature;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import net.zic.ascension.api.core.projectile.ProjectileLaunchDirection;
import net.zic.ascension.api.core.skill.castable.feature.SkillExecutionContext;
import net.zic.ascension.api.core.skill.castable.feature.SkillExecutionFeature;
import net.zic.ascension.api.core.skill.castable.feature.SkillExecutionFeatureType;
import net.zic.ascension.common.projectile.VirtualProjectileService;
import net.zic.ascension.impl.datapack.skill.castable.feature.AscensionSkillExecutionFeatureTypes;

public record VirtualProjectileFeature(
        Identifier projectile,
        ProjectileLaunchDirection direction
) implements SkillExecutionFeature {
    public static final MapCodec<VirtualProjectileFeature> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Identifier.CODEC.fieldOf("projectile").forGetter(VirtualProjectileFeature::projectile),
            ProjectileLaunchDirection.CODEC.optionalFieldOf("direction", ProjectileLaunchDirection.LOOK)
                    .forGetter(VirtualProjectileFeature::direction)
    ).apply(instance, VirtualProjectileFeature::new));

    @Override
    public SkillExecutionFeatureType getType() {
        return AscensionSkillExecutionFeatureTypes.VIRTUAL_PROJECTILE.get();
    }

    @Override
    public void apply(SkillExecutionContext context) {
        VirtualProjectileService.spawn(context, projectile, direction);
    }
}
