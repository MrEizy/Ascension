package net.zic.ascension.api.ascension.core.runtime;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.util.StringRepresentable;
import net.zic.ascension.api.ascension.core.skill.castable.feature.SkillExecutionFeature;
import net.zic.ascension.api.ascension.core.targeting.TargetFilterDefinition;
import net.zic.ascension.api.ascension.value.ScaledValue;

import java.util.List;
import java.util.Optional;

public record AreaFieldDefinition(
        Shape shape,
        ScaledValue radius,
        ScaledValue height,
        ScaledValue duration,
        int tickInterval,
        TargetFilterDefinition filter,
        List<SkillExecutionFeature> onEnter,
        List<SkillExecutionFeature> onTick,
        List<SkillExecutionFeature> onExit,
        Optional<Identifier> visual
) {
    public static final Codec<AreaFieldDefinition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Shape.CODEC.optionalFieldOf("shape", Shape.CYLINDER).forGetter(AreaFieldDefinition::shape),
            ScaledValue.CODEC.codec().fieldOf("radius").forGetter(AreaFieldDefinition::radius),
            ScaledValue.CODEC.codec().optionalFieldOf("height", ScaledValue.constant(4.0D))
                    .forGetter(AreaFieldDefinition::height),
            ScaledValue.CODEC.codec().fieldOf("duration").forGetter(AreaFieldDefinition::duration),
            Codec.intRange(1, 1200).optionalFieldOf("tick_interval", 20)
                    .forGetter(AreaFieldDefinition::tickInterval),
            TargetFilterDefinition.CODEC.codec().optionalFieldOf("filter", TargetFilterDefinition.hostile())
                    .forGetter(AreaFieldDefinition::filter),
            SkillExecutionFeature.CODEC.listOf().optionalFieldOf("on_enter", List.of())
                    .forGetter(AreaFieldDefinition::onEnter),
            SkillExecutionFeature.CODEC.listOf().optionalFieldOf("on_tick", List.of())
                    .forGetter(AreaFieldDefinition::onTick),
            SkillExecutionFeature.CODEC.listOf().optionalFieldOf("on_exit", List.of())
                    .forGetter(AreaFieldDefinition::onExit),
            Identifier.CODEC.optionalFieldOf("visual").forGetter(AreaFieldDefinition::visual)
    ).apply(instance, AreaFieldDefinition::new));

    public AreaFieldDefinition {
        shape = shape == null ? Shape.CYLINDER : shape;
        filter = filter == null ? TargetFilterDefinition.hostile() : filter;
        onEnter = onEnter == null ? List.of() : List.copyOf(onEnter);
        onTick = onTick == null ? List.of() : List.copyOf(onTick);
        onExit = onExit == null ? List.of() : List.copyOf(onExit);
        visual = visual == null ? Optional.empty() : visual;
    }

    public enum Shape implements StringRepresentable {
        SPHERE("sphere"),
        CYLINDER("cylinder");

        public static final Codec<Shape> CODEC = StringRepresentable.fromEnum(Shape::values);

        private final String name;

        Shape(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }
}
