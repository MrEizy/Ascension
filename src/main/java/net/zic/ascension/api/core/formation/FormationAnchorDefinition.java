package net.zic.ascension.api.core.formation;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;

public record FormationAnchorDefinition(
        Identifier id,
        Vec3 offset
) {
    public static final Codec<FormationAnchorDefinition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Identifier.CODEC.fieldOf("id").forGetter(FormationAnchorDefinition::id),
            Codec.DOUBLE.optionalFieldOf("x", 0.0D).forGetter(value -> value.offset().x),
            Codec.DOUBLE.optionalFieldOf("y", 0.0D).forGetter(value -> value.offset().y),
            Codec.DOUBLE.optionalFieldOf("z", 0.0D).forGetter(value -> value.offset().z)
    ).apply(instance, (id, x, y, z) -> new FormationAnchorDefinition(id, new Vec3(x, y, z))));
}
