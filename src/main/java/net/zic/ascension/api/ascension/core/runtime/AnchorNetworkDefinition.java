package net.zic.ascension.api.ascension.core.runtime;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import net.zic.ascension.api.ascension.core.skill.DefinitionRef;
import net.zic.ascension.api.ascension.datapack.CodecHelpers;
import net.zic.ascension.api.ascension.value.ScaledValue;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public record AnchorNetworkDefinition(
        ScaledValue duration,
        List<Node> nodes,
        List<Link> links,
        boolean rotateWithCaster,
        Optional<DefinitionRef<AreaFieldDefinition>> field,
        Optional<Identifier> visual
) {
    public static final Codec<AnchorNetworkDefinition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ScaledValue.COMPACT_CODEC.fieldOf("duration").forGetter(AnchorNetworkDefinition::duration),
            Node.CODEC.listOf().fieldOf("nodes").forGetter(AnchorNetworkDefinition::nodes),
            Link.CODEC.listOf().optionalFieldOf("links", List.of()).forGetter(AnchorNetworkDefinition::links),
            Codec.BOOL.optionalFieldOf("rotate_with_caster", true)
                    .forGetter(AnchorNetworkDefinition::rotateWithCaster),
            DefinitionRef.codec(AreaFieldDefinition.CODEC).optionalFieldOf("field").forGetter(AnchorNetworkDefinition::field),
            Identifier.CODEC.optionalFieldOf("visual").forGetter(AnchorNetworkDefinition::visual)
    ).apply(instance, AnchorNetworkDefinition::new));

    public AnchorNetworkDefinition {
        nodes = nodes == null ? List.of() : List.copyOf(nodes);
        links = links == null ? List.of() : List.copyOf(links);
        field = field == null ? Optional.empty() : field;
        visual = visual == null ? Optional.empty() : visual;
    }

    public record Node(Identifier id, Vec3 offset) {
        public static final Codec<Node> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Identifier.CODEC.fieldOf("id").forGetter(Node::id),
                CodecHelpers.VEC3.optionalFieldOf("offset", Vec3.ZERO).forGetter(Node::offset)
        ).apply(instance, Node::new));
    }

    public record Link(Identifier from, Identifier to) {
        public static final Codec<Link> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Identifier.CODEC.fieldOf("from").forGetter(Link::from),
                Identifier.CODEC.fieldOf("to").forGetter(Link::to)
        ).apply(instance, Link::new));
    }

    public interface View {
        UUID runtimeId();

        UUID ownerId();

        Identifier definitionId();

        Vec3 center();

        Map<Identifier, Vec3> nodes();

        long expiresAt();
    }
}
