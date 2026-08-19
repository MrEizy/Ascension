package net.zic.ascension.skill_passives;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.ascension.capabilities.AscensionEntityDataProvider;
import net.zic.ascension.api.ascension.capabilities.CoreCapabilities;
import net.zic.ascension.api.ascension.core.CoreRegistries;
import net.zic.ascension.api.ascension.core.skill.Skill;
import net.zic.ascension.api.ascension.core.skill.SkillData;
import net.zic.ascension.api.ascension.core.skill.toggleable.ToggleableSkill;
import net.zic.ascension.api.ascension.core.source.AscensionOriginSourceHelper;
import net.zic.ascension.api.rpg_engine.source.OriginSource;

import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

@EventBusSubscriber(modid = AscensionCraft.MOD_ID)
public final class ToggleableSkillTicker {
    private static final Map<OriginSource, Long> LAST_SOURCE_TICK = new WeakHashMap<>();

    private ToggleableSkillTicker() {
    }

    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Pre event) {
        if (!(event.getEntity() instanceof LivingEntity entity) || entity.level().isClientSide()) {
            return;
        }
        AscensionEntityDataProvider holder = entity.getCapability(CoreCapabilities.ASCENSION_ENTITY_DATA_PROVIDER_CAPABILITY);
        if (holder == null) {
            return;
        }
        OriginSource source = holder.getData().getSource();
        long gameTime = entity.level().getGameTime();
        if (LAST_SOURCE_TICK.getOrDefault(source, Long.MIN_VALUE) == gameTime) {
            return;
        }
        LAST_SOURCE_TICK.put(source, gameTime);

        for (Identifier skillId : List.copyOf(AscensionOriginSourceHelper.getSkills(source))) {
            Skill skill = CoreRegistries.safeAccess(CoreRegistries.SKILL_REGISTRY, skillId, entity.registryAccess());
            SkillData data = AscensionOriginSourceHelper.getSkillData(source, skillId);
            if (!(skill instanceof ToggleableSkill toggleable) || data == null || !toggleable.isEnabled(data)) {
                continue;
            }
            if (toggleable.tickEnabled(entity, source, data)) {
                continue;
            }
            toggleable.onDisabled(source, data);
            toggleable.removeEnabledFromEntity(entity, data);
            toggleable.setEnabled(data, false);
            AscensionOriginSourceHelper.markSkillDirty(source, skillId);
        }
    }
}
