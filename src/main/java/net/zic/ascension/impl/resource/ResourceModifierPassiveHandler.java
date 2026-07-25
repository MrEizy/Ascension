package net.zic.ascension.impl.resource;

import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.capabilities.AscensionEntityDataHolder;
import net.zic.ascension.api.capabilities.CoreCapabilities;
import net.zic.ascension.api.core.CoreRegistries;
import net.zic.ascension.api.core.entity.AscensionEntityData;
import net.zic.ascension.api.core.resource.modifier.ResourceModifierDefinition;
import net.zic.ascension.api.core.skill.Skill;
import net.zic.ascension.api.core.skill.SkillData;
import net.zic.ascension.api.core.skill.levelled.SkillLevelResolver;
import net.zic.ascension.api.core.source.OriginSource;
import net.zic.ascension.api.event.resource.ResourceTransactionEvent;
import net.zic.ascension.impl.core.skill.passive.resource.ResourceModifierPassiveSkill;
import net.zic.ascension.impl.core.skill.passive.resource.ResourceModifierPassiveSkillData;

@EventBusSubscriber(modid = AscensionCraft.MOD_ID)
public final class ResourceModifierPassiveHandler {
    private ResourceModifierPassiveHandler() {
    }

    @SubscribeEvent
    public static void collectModifiers(ResourceTransactionEvent.Modify event) {
        AscensionEntityDataHolder holder = event.getContext().request().entity().getCapability(
                CoreCapabilities.ASCENSION_ENTITY_DATA_HOLDER_CAPABILITY
        );
        if (holder == null) {
            return;
        }

        AscensionEntityData entityData = holder.getData(event.getContext().request().entity());
        if (entityData == null || entityData.getSource() == null) {
            return;
        }

        OriginSource source = entityData.getSource();
        for (Identifier skillId : source.getSkills()) {
            Skill skill = CoreRegistries.safeAccess(
                    CoreRegistries.SKILL_REGISTRY,
                    skillId,
                    source.getRegistryAccess()
            );
            SkillData skillData = source.getSkillData(skillId);
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
