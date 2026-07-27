package net.zic.ascension.api.core.construct;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import net.zic.ascension.api.value.ScaledValue;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public record OwnerBoundConstructDefinition(
        ScaledValue duration,
        ScaledValue stability,
        Vec3 offset,
        boolean rotateWithOwner,
        Optional<Identifier> visual,
        List<ConstructVisualStage> visualStages
) {
    public static final Codec<OwnerBoundConstructDefinition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ScaledValue.CODEC.codec().fieldOf("duration").forGetter(OwnerBoundConstructDefinition::duration),
            ScaledValue.CODEC.codec().fieldOf("stability").forGetter(OwnerBoundConstructDefinition::stability),
            Codec.DOUBLE.optionalFieldOf("offset_x", 0.0D).forGetter(value -> value.offset().x),
            Codec.DOUBLE.optionalFieldOf("offset_y", 0.0D).forGetter(value -> value.offset().y),
            Codec.DOUBLE.optionalFieldOf("offset_z", 0.0D).forGetter(value -> value.offset().z),
            Codec.BOOL.optionalFieldOf("rotate_with_owner", true)
                    .forGetter(OwnerBoundConstructDefinition::rotateWithOwner),
            Identifier.CODEC.optionalFieldOf("visual").forGetter(OwnerBoundConstructDefinition::visual),
            ConstructVisualStage.CODEC.listOf().optionalFieldOf("visual_stages", List.of())
                    .forGetter(OwnerBoundConstructDefinition::visualStages)
    ).apply(instance, (duration, stability, x, y, z, rotate, visual, stages) ->
            new OwnerBoundConstructDefinition(
                    duration,
                    stability,
                    new Vec3(x, y, z),
                    rotate,
                    visual,
                    stages
            )
    ));

    public OwnerBoundConstructDefinition {
        visual = visual == null ? Optional.empty() : visual;
        visualStages = visualStages == null
                ? List.of()
                : visualStages.stream()
                .sorted(Comparator.comparingDouble(ConstructVisualStage::maximumStabilityFraction))
                .toList();
    }
}
