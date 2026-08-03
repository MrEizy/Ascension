package net.zic.ascension.api.ascension.core.resource.source;

import net.minecraft.resources.Identifier;

import java.util.Set;

public interface ResourceSourceIdentity {
    Identifier id();

    default Set<Identifier> tags() {
        return Set.of();
    }

    default boolean hasTag(Identifier tag) {
        return tag != null && tags().contains(tag);
    }
}
