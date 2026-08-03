package net.zic.ascension.api.ascension.core.resource.source;

import net.minecraft.resources.Identifier;

import java.util.Set;

public record SimpleResourceSourceIdentity(
        Identifier id,
        Set<Identifier> tags
) implements ResourceSourceIdentity {
    public SimpleResourceSourceIdentity {
        if (id == null) {
            throw new IllegalArgumentException("Resource source id cannot be null");
        }
        tags = tags == null ? Set.of() : Set.copyOf(tags);
    }

    public static SimpleResourceSourceIdentity of(Identifier id, Identifier... tags) {
        return new SimpleResourceSourceIdentity(id, tags == null ? Set.of() : Set.of(tags));
    }
}
