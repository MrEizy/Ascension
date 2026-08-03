package net.zic.ascension.impl.core.skill.castable.feature;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import net.zic.ascension.api.ascension.core.projectile.ProjectileLaunchDirection;
import net.zic.ascension.api.ascension.core.skill.castable.feature.SkillExecutionContext;
import net.zic.ascension.api.ascension.core.skill.castable.feature.SkillExecutionFeature;
import net.zic.ascension.api.ascension.datapack.CodecType;
import net.zic.ascension.impl.runtime.projectile.VirtualProjectiles;
import net.zic.ascension.impl.datapack.skill.AscensionSkillExecutionFeatureTypes;

public record SpawnProjectileFeature(
        Identifier definition,
        ProjectileLaunchDirection direction
) implements SkillExecutionFeature {
    public static final MapCodec<SpawnProjectileFeature> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Identifier.CODEC.fieldOf("definition").forGetter(SpawnProjectileFeature::definition),
            ProjectileLaunchDirection.CODEC.optionalFieldOf("direction", ProjectileLaunchDirection.LOOK)
                    .forGetter(SpawnProjectileFeature::direction)
    ).apply(instance, SpawnProjectileFeature::new));

    @Override
    public CodecType<SkillExecutionFeature> getType() {
        return AscensionSkillExecutionFeatureTypes.SPAWN_PROJECTILE.get();
    }

    @Override
    public void apply(SkillExecutionContext context) {
        VirtualProjectiles.spawn(context, definition, direction);
    }
}
