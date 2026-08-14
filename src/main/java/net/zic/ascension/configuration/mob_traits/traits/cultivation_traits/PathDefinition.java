package net.zic.ascension.configuration.mob_traits.traits.cultivation_traits;

import com.mojang.serialization.Codec;
import net.minecraft.resources.Identifier;

import java.util.List;
import java.util.stream.Collectors;

//TODO figure out how dafuc im going to handle tribulations, could add potential tribulation results, which provides pre made completed tribulation
public record PathDefinition(Identifier path, List<PotentialRealmDefinition> potentialRealms){



    public static final Codec<List<PathDefinition>> CODEC = Codec.unboundedMap(
            Identifier.CODEC,
            PotentialRealmDefinition.CODEC.listOf()
    ).xmap(
            rawInput-> rawInput.entrySet().stream()
            .map(entry -> new PathDefinition(entry.getKey(), entry.getValue()))
            .collect(Collectors.toList()),
            rawOutput-> rawOutput.stream().collect(Collectors.toMap(PathDefinition::path, PathDefinition::potentialRealms))

    );
}
