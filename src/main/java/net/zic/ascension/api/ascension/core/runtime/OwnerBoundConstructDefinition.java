package net.zic.ascension.api.ascension.core.runtime;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import net.zic.ascension.api.ascension.datapack.CodecHelpers;
import net.zic.ascension.api.ascension.value.ScaledValue;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public record OwnerBoundConstructDefinition(
        ScaledValue duration,
        ScaledValue stability,
        Vec3 offset,
        boolean rotateWithOwner,
        Optional<Identifier> visual,
        List<VisualStage> visualStages
) {
    public static final Codec<OwnerBoundConstructDefinition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ScaledValue.CODEC.codec().fieldOf("duration").forGetter(OwnerBoundConstructDefinition::duration),
            ScaledValue.CODEC.codec().fieldOf("stability").forGetter(OwnerBoundConstructDefinition::stability),
            CodecHelpers.VEC3.optionalFieldOf("offset", Vec3.ZERO).forGetter(OwnerBoundConstructDefinition::offset),
            Codec.BOOL.optionalFieldOf("rotate_with_owner", true)
                    .forGetter(OwnerBoundConstructDefinition::rotateWithOwner),
            Identifier.CODEC.optionalFieldOf("visual").forGetter(OwnerBoundConstructDefinition::visual),
            VisualStage.CODEC.listOf().optionalFieldOf("visual_stages", List.of())
                    .forGetter(OwnerBoundConstructDefinition::visualStages)
    ).apply(instance, OwnerBoundConstructDefinition::new));

    public OwnerBoundConstructDefinition {
        visual = visual == null ? Optional.empty() : visual;
        visualStages = visualStages == null
                ? List.of()
                : visualStages.stream()
                .sorted(Comparator.comparingDouble(VisualStage::maximumStabilityFraction))
                .toList();
    }

    public record VisualStage(double maximumStabilityFraction, Identifier visual) {
        public static final Codec<VisualStage> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.doubleRange(0.0D, 1.0D).fieldOf("maximum_stability_fraction")
                        .forGetter(VisualStage::maximumStabilityFraction),
                Identifier.CODEC.fieldOf("visual").forGetter(VisualStage::visual)
        ).apply(instance, VisualStage::new));
    }

    public interface View {
        UUID runtimeId();

        UUID ownerId();

        Identifier definitionId();

        double stability();

        double maximumStability();

        Vec3 position();

        long expiresAt();
    }
}
