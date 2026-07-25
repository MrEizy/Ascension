package net.zic.ascension.api.core.resource.source;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;

import java.util.List;
import java.util.Set;

public record ResourceSourceSelector(
        Set<Identifier> sources,
        Set<Identifier> sourceTags,
        Set<Identifier> excludedSources,
        Set<Identifier> excludedSourceTags
) {
    private static final Codec<Set<Identifier>> IDENTIFIER_SET_CODEC = Identifier.CODEC.listOf().xmap(
            Set::copyOf,
            List::copyOf
    );

    public static final MapCodec<ResourceSourceSelector> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            IDENTIFIER_SET_CODEC.optionalFieldOf("sources", Set.of()).forGetter(ResourceSourceSelector::sources),
            IDENTIFIER_SET_CODEC.optionalFieldOf("source_tags", Set.of()).forGetter(ResourceSourceSelector::sourceTags),
            IDENTIFIER_SET_CODEC.optionalFieldOf("excluded_sources", Set.of()).forGetter(ResourceSourceSelector::excludedSources),
            IDENTIFIER_SET_CODEC.optionalFieldOf("excluded_source_tags", Set.of()).forGetter(ResourceSourceSelector::excludedSourceTags)
    ).apply(instance, ResourceSourceSelector::new));

    public ResourceSourceSelector {
        sources = sources == null ? Set.of() : Set.copyOf(sources);
        sourceTags = sourceTags == null ? Set.of() : Set.copyOf(sourceTags);
        excludedSources = excludedSources == null ? Set.of() : Set.copyOf(excludedSources);
        excludedSourceTags = excludedSourceTags == null ? Set.of() : Set.copyOf(excludedSourceTags);
    }

    public static ResourceSourceSelector any() {
        return new ResourceSourceSelector(Set.of(), Set.of(), Set.of(), Set.of());
    }

    public boolean matches(ResourceSourceIdentity source) {
        if (source == null || excludedSources.contains(source.id())) {
            return false;
        }
        if (excludedSourceTags.stream().anyMatch(source::hasTag)) {
            return false;
        }
        boolean hasIncludes = !sources.isEmpty() || !sourceTags.isEmpty();
        if (!hasIncludes) {
            return true;
        }
        return sources.contains(source.id()) || sourceTags.stream().anyMatch(source::hasTag);
    }
}
