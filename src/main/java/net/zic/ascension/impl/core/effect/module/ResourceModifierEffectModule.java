package net.zic.ascension.impl.core.effect.module;

import net.zic.ascension.api.ascension.datapack.CodecType;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.ascension.core.effect.SkillEffectContext;
import net.zic.ascension.api.ascension.core.effect.SkillEffectModule;
import net.zic.ascension.api.ascension.core.resource.ResourceTransactionContext;
import net.zic.ascension.api.ascension.core.resource.ResourceTransactionSelector;
import net.zic.ascension.api.ascension.core.resource.modifier.ResourceModifier;
import net.zic.ascension.api.ascension.core.resource.modifier.ResourceModifierOperation;
import net.zic.ascension.api.ascension.value.ScaledValue;
import net.zic.ascension.api.ascension.value.ScaledValueContext;
import net.zic.ascension.impl.datapack.effect.AscensionSkillEffectModuleTypes;

import java.util.Map;

public record ResourceModifierEffectModule(
        Identifier id,
        ResourceTransactionSelector selector,
        ResourceModifierOperation operation,
        ScaledValue value,
        int priority
) implements SkillEffectModule {
    private static final Identifier EFFECT_POTENCY = AscensionCraft.prefix("effect_potency");
    private static final Identifier EFFECT_STACKS = AscensionCraft.prefix("effect_stacks");

    public static final MapCodec<ResourceModifierEffectModule> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Identifier.CODEC.fieldOf("id").forGetter(ResourceModifierEffectModule::id),
            ResourceTransactionSelector.CODEC.fieldOf("selector").forGetter(ResourceModifierEffectModule::selector),
            ResourceModifierOperation.CODEC.fieldOf("operation").forGetter(ResourceModifierEffectModule::operation),
            ScaledValue.CODEC.codec().optionalFieldOf("value", ScaledValue.constant(0.0D))
                    .forGetter(ResourceModifierEffectModule::value),
            Codec.INT.optionalFieldOf("priority", 0).forGetter(ResourceModifierEffectModule::priority)
    ).apply(instance, ResourceModifierEffectModule::new));

    @Override
    public CodecType<SkillEffectModule> getType() {
        return AscensionSkillEffectModuleTypes.RESOURCE_MODIFIER.get();
    }

    public boolean matches(ResourceTransactionContext context) {
        return selector.matches(context);
    }

    public ResourceModifier resolve(ResourceTransactionContext context, SkillEffectContext effect) {
        ScaledValueContext scaledContext = new ScaledValueContext(
                null,
                effect.sourceSkill(),
                context.request().entity(),
                context.request().target(),
                effect.potency(),
                Map.of(
                        EFFECT_POTENCY, effect.potency(),
                        EFFECT_STACKS, (double) effect.stacks()
                )
        );
        Identifier resolvedId = Identifier.fromNamespaceAndPath(
                id.getNamespace(),
                "skill_effect/" + effect.definition().getNamespace() + "/" + effect.definition().getPath() + "/" + id.getPath()
        );
        return new ResourceModifier(resolvedId, operation, value.resolve(scaledContext), priority);
    }
}
