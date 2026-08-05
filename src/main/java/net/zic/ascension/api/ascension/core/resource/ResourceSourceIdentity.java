package net.zic.ascension.api.ascension.core.resource;

import net.minecraft.resources.Identifier;
import net.zic.ascension.AscensionCraft;

import java.util.Set;

public interface ResourceSourceIdentity {
    Identifier id();

    default Set<Identifier> tags() {
        return Set.of();
    }

    default boolean hasTag(Identifier tag) {
        return tag != null && tags().contains(tag);
    }

    static ResourceSourceIdentity of(Identifier id, Identifier... tags) {
        return new Simple(id, tags == null ? Set.of() : Set.of(tags));
    }

    record Simple(Identifier id, Set<Identifier> tags) implements ResourceSourceIdentity {
        public Simple {
            if (id == null) {
                throw new IllegalArgumentException("Resource source id cannot be null");
            }
            tags = tags == null ? Set.of() : Set.copyOf(tags);
        }
    }

    final class Tags {
        public static final Identifier MOVEMENT = AscensionCraft.prefix("movement");
        public static final Identifier COMBAT = AscensionCraft.prefix("combat");
        public static final Identifier SURVIVAL = AscensionCraft.prefix("survival");
        public static final Identifier REGENERATION = AscensionCraft.prefix("regeneration");
        public static final Identifier SKILL = AscensionCraft.prefix("skill");
        public static final Identifier CULTIVATION = AscensionCraft.prefix("cultivation");
        public static final Identifier ENVIRONMENTAL = AscensionCraft.prefix("environmental");

        private Tags() {
        }
    }
}
