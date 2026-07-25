package net.zic.ascension.api.core.resource.modifier;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import net.zic.ascension.api.core.resource.ResourceTransactionContext;
import net.zic.ascension.api.core.resource.ResourceTransactionSelector;
import net.zic.ascension.api.core.source.OriginSource;
import net.zic.ascension.api.value.ScaledValue;

public record ResourceModifierDefinition(
        Identifier id,
        ResourceTransactionSelector selector,
        ResourceModifierOperation operation,
        ScaledValue value,
        int priority
) {
    public static final MapCodec<ResourceModifierDefinition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Identifier.CODEC.fieldOf("id").forGetter(ResourceModifierDefinition::id),
            ResourceTransactionSelector.CODEC.fieldOf("selector").forGetter(ResourceModifierDefinition::selector),
            ResourceModifierOperation.CODEC.fieldOf("operation").forGetter(ResourceModifierDefinition::operation),
            ScaledValue.CODEC.codec().optionalFieldOf("value", ScaledValue.constant(0.0D)).forGetter(ResourceModifierDefinition::value),
            Codec.INT.optionalFieldOf("priority", 0).forGetter(ResourceModifierDefinition::priority)
    ).apply(instance, ResourceModifierDefinition::new));

    public boolean matches(ResourceTransactionContext context) {
        return selector.matches(context);
    }

    public ResourceModifier resolve(ResourceTransactionContext context, OriginSource source, Identifier skillId) {
        Identifier resolvedId = Identifier.fromNamespaceAndPath(
                skillId.getNamespace(),
                "resource_modifier/" + skillId.getPath() + "/" + id.getNamespace() + "/" + id.getPath()
        );

        return new ResourceModifier(
                resolvedId,
                operation,
                value.resolve(context.scaledValueContext(source, skillId)),
                priority
        );
    }
}