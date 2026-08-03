package net.zic.ascension.api.ascension.core.resource;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import net.zic.ascension.api.ascension.core.resource.source.ResourceSourceSelector;

import java.util.List;
import java.util.Set;

public record ResourceTransactionSelector(
        Set<Identifier> resources,
        Set<ResourceOperation> operations,
        ResourceSourceSelector source
) {
    private static final Codec<Set<Identifier>> IDENTIFIER_SET_CODEC =
            Identifier.CODEC.listOf().xmap(Set::copyOf, List::copyOf);

    private static final Codec<Set<ResourceOperation>> OPERATION_SET_CODEC =
            ResourceOperation.CODEC.listOf().xmap(Set::copyOf, List::copyOf);

    public static final MapCodec<ResourceTransactionSelector> CODEC =
            RecordCodecBuilder.mapCodec(instance -> instance.group(
                    IDENTIFIER_SET_CODEC.optionalFieldOf("resources", Set.of()).forGetter(ResourceTransactionSelector::resources),
                    OPERATION_SET_CODEC.optionalFieldOf("operations", Set.of()).forGetter(ResourceTransactionSelector::operations),
                    ResourceSourceSelector.CODEC.codec().optionalFieldOf("source", ResourceSourceSelector.any()).forGetter(ResourceTransactionSelector::source)
            ).apply(instance, ResourceTransactionSelector::new));

    public ResourceTransactionSelector {
        resources = resources == null ? Set.of() : Set.copyOf(resources);
        operations = operations == null ? Set.of() : Set.copyOf(operations);
        source = source == null ? ResourceSourceSelector.any() : source;
    }

    public boolean matches(ResourceTransactionContext context) {
        if (context == null) {
            return false;
        }

        if (!resources.isEmpty()
                && !resources.contains(context.request().resource())) {
            return false;
        }

        if (!operations.isEmpty()
                && !operations.contains(context.request().operation())) {
            return false;
        }

        return source.matches(context.request().source());
    }
}