package net.zic.ascension.configuration.mob_traits;

import net.minecraft.resources.Identifier;

import java.util.List;

public record MobTraitRoll(List<PathRoll> paths, List<Identifier> skills) {
    public static final MobTraitRoll EMPTY = new MobTraitRoll(List.of(), List.of());

    public MobTraitRoll {
        paths = List.copyOf(paths);
        skills = List.copyOf(skills);
    }

    public boolean isEmpty() {
        return paths.isEmpty() && skills.isEmpty();
    }

    public record PathRoll(Identifier path, int majorRealm, int minorRealm) {
    }
}
