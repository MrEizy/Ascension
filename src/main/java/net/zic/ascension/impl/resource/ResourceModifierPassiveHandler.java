package net.zic.ascension.impl.resource;

import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.ascension.capabilities.AscensionEntityDataProvider;
import net.zic.ascension.api.ascension.capabilities.CoreCapabilities;
import net.zic.ascension.api.ascension.core.CoreRegistries;
import net.zic.ascension.api.ascension.core.entity.AscensionEntityData;
import net.zic.ascension.api.ascension.core.resource.modifier.ResourceModifierDefinition;
import net.zic.ascension.api.ascension.core.skill.levelled.SkillLevelResolver;
import net.zic.ascension.api.ascension.core.source.AscensionOriginSourceHelper;
import net.zic.ascension.api.ascension.core.skill.Skill;
import net.zic.ascension.api.ascension.core.skill.SkillData;
import net.zic.ascension.api.ascension.event.resource.ResourceTransactionEvent;
import net.zic.ascension.api.rpg_engine.source.OriginSource;
import net.zic.ascension.impl.core.skill.passive.resource.ResourceModifierPassiveSkill;
import net.zic.ascension.impl.core.skill.passive.resource.ResourceModifierPassiveSkillData;

@EventBusSubscriber(modid = AscensionCraft.MOD_ID)
public final class ResourceModifierPassiveHandler {
    private ResourceModifierPassiveHandler() {
    }

    @SubscribeEvent
    public static void collectModifiers(ResourceTransactionEvent.Modify event) {
        AscensionEntityDataProvider provider = event.getContext().request().entity().getCapability(
                CoreCapabilities.ASCENSION_ENTITY_DATA_PROVIDER_CAPABILITY
        );
        if (provider == null) {
            return;
        }

        AscensionEntityData entityData = provider.getData(event.getContext().request().entity());
        if (entityData == null || entityData.getSource() == null) {
            return;
        }

        OriginSource source = entityData.getSource();
        for (Identifier skillId : AscensionOriginSourceHelper.getSkills(source)) {
            Skill skill = CoreRegistries.safeAccess(
                    CoreRegistries.SKILL_REGISTRY,
                    skillId,
                    source.getRegistryAccess()
            );
            SkillData skillData = AscensionOriginSourceHelper.getSkillData(source, skillId);
            if (!(skill instanceof ResourceModifierPassiveSkill passive)
                    || !(skillData instanceof ResourceModifierPassiveSkillData)) {
                continue;
            }

            int level = SkillLevelResolver.resolve(source, skillId).effectiveLevel();
            for (ResourceModifierDefinition definition : passive.getModifiers(level)) {
                if (definition.matches(event.getContext())) {
                    event.getCollector().add(definition.resolve(event.getContext(), source, skillId));
                }
            }
        }
    }
}
