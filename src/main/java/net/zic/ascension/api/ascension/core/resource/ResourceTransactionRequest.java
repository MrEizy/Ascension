package net.zic.ascension.api.ascension.core.resource;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public record ResourceTransactionRequest(
        LivingEntity entity,
        Identifier resource,
        ResourceOperation operation,
        double amount,
        ResourceSourceIdentity source,
        Identifier skill,
        LivingEntity target,
        Map<Identifier, Double> values,
        Set<Flag> flags
) {
    public ResourceTransactionRequest {
        values = values == null ? Map.of() : Map.copyOf(values);
        flags = flags == null || flags.isEmpty()
                ? Set.of()
                : Set.copyOf(EnumSet.copyOf(flags));
    }

    public static ResourceTransactionRequest of(
            LivingEntity entity,
            Identifier resource,
            ResourceOperation operation,
            double amount,
            ResourceSourceIdentity source
    ) {
        return new ResourceTransactionRequest(
                entity,
                resource,
                operation,
                amount,
                source,
                null,
                null,
                Map.of(),
                Set.of()
        );
    }

    public boolean hasFlag(Flag flag) {
        return flag != null && flags.contains(flag);
    }

    public ResourceTransactionRequest withSkill(Identifier skill) {
        return new ResourceTransactionRequest(entity, resource, operation, amount, source, skill, target, values, flags);
    }

    public ResourceTransactionRequest withTarget(LivingEntity target) {
        return new ResourceTransactionRequest(entity, resource, operation, amount, source, skill, target, values, flags);
    }

    public ResourceTransactionRequest withValues(Map<Identifier, Double> values) {
        return new ResourceTransactionRequest(entity, resource, operation, amount, source, skill, target, values, flags);
    }

    public ResourceTransactionRequest withFlags(Set<Flag> flags) {
        return new ResourceTransactionRequest(entity, resource, operation, amount, source, skill, target, values, flags);
    }
    public enum Flag {
        ALLOW_REENTRY,
        BYPASS_MODIFIERS,
        BYPASS_EVENTS,
        SIMULATE
    }

    public record SourceSelector(
            Set<Identifier> sources,
            Set<Identifier> sourceTags,
            Set<Identifier> excludedSources,
            Set<Identifier> excludedSourceTags
    ) {
        private static final Codec<Set<Identifier>> IDENTIFIER_SET_CODEC = Identifier.CODEC.listOf().xmap(Set::copyOf, List::copyOf);
        public static final MapCodec<SourceSelector> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                IDENTIFIER_SET_CODEC.optionalFieldOf("sources", Set.of()).forGetter(SourceSelector::sources),
                IDENTIFIER_SET_CODEC.optionalFieldOf("source_tags", Set.of()).forGetter(SourceSelector::sourceTags),
                IDENTIFIER_SET_CODEC.optionalFieldOf("excluded_sources", Set.of()).forGetter(SourceSelector::excludedSources),
                IDENTIFIER_SET_CODEC.optionalFieldOf("excluded_source_tags", Set.of()).forGetter(SourceSelector::excludedSourceTags)
        ).apply(instance, SourceSelector::new));

        public SourceSelector {
            sources = sources == null ? Set.of() : Set.copyOf(sources);
            sourceTags = sourceTags == null ? Set.of() : Set.copyOf(sourceTags);
            excludedSources = excludedSources == null ? Set.of() : Set.copyOf(excludedSources);
            excludedSourceTags = excludedSourceTags == null ? Set.of() : Set.copyOf(excludedSourceTags);
        }

        public static SourceSelector any() {
            return new SourceSelector(Set.of(), Set.of(), Set.of(), Set.of());
        }

        public boolean matches(ResourceSourceIdentity source) {
            if (source == null || excludedSources.contains(source.id()) || excludedSourceTags.stream().anyMatch(source::hasTag)) {
                return false;
            }
            return sources.isEmpty() && sourceTags.isEmpty()
                    || sources.contains(source.id())
                    || sourceTags.stream().anyMatch(source::hasTag);
        }
    }

    public record Selector(
            Set<Identifier> resources,
            Set<ResourceOperation> operations,
            SourceSelector source
    ) {
        private static final Codec<Set<Identifier>> IDENTIFIER_SET_CODEC = Identifier.CODEC.listOf().xmap(Set::copyOf, List::copyOf);
        private static final Codec<Set<ResourceOperation>> OPERATION_SET_CODEC = ResourceOperation.CODEC.listOf().xmap(Set::copyOf, List::copyOf);
        public static final MapCodec<Selector> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                IDENTIFIER_SET_CODEC.optionalFieldOf("resources", Set.of()).forGetter(Selector::resources),
                OPERATION_SET_CODEC.optionalFieldOf("operations", Set.of()).forGetter(Selector::operations),
                SourceSelector.CODEC.codec().optionalFieldOf("source", SourceSelector.any()).forGetter(Selector::source)
        ).apply(instance, Selector::new));

        public Selector {
            resources = resources == null ? Set.of() : Set.copyOf(resources);
            operations = operations == null ? Set.of() : Set.copyOf(operations);
            source = source == null ? SourceSelector.any() : source;
        }

        public boolean matches(ResourceTransactionService.Context context) {
            return context != null
                    && (resources.isEmpty() || resources.contains(context.request().resource()))
                    && (operations.isEmpty() || operations.contains(context.request().operation()))
                    && source.matches(context.request().source());
        }
    }

}
