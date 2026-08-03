package net.zic.ascension.handler;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.ascension.core.CoreRegistries;
import net.zic.ascension.api.ascension.core.effect.SkillEffectDefinition;
import net.zic.ascension.api.ascension.event.resource.ResourceTransactionEvent;
import net.zic.ascension.common.data_attachements.AscensionAttachments;
import net.zic.ascension.impl.core.effect.module.ResourceModifierEffectModule;
import net.zic.ascension.impl.core.effect.SkillEffectContainer;
import net.zic.ascension.impl.core.effect.SkillEffectInstance;

@EventBusSubscriber(modid = AscensionCraft.MOD_ID)
public final class SkillEffectResourceModifierHandler {
    private SkillEffectResourceModifierHandler() {
    }

    @SubscribeEvent
    public static void collect(ResourceTransactionEvent.Modify event) {
        SkillEffectContainer container = event.getContext().request().entity().getData(
                AscensionAttachments.ACTIVE_SKILL_EFFECTS
        );
        if (container.isEmpty()) {
            return;
        }

        for (SkillEffectInstance active : container.instances()) {
            SkillEffectDefinition definition = CoreRegistries.safeAccess(
                    CoreRegistries.SKILL_EFFECT_REGISTRY,
                    active.definition(),
                    event.getContext().request().entity().registryAccess()
            );
            if (definition == null) {
                continue;
            }
            for (var module : definition.modules()) {
                if (module instanceof ResourceModifierEffectModule resourceModifier
                        && resourceModifier.matches(event.getContext())) {
                    event.getCollector().add(resourceModifier.resolve(event.getContext(), active));
                }
            }
        }
    }
}
