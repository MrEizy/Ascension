package net.zic.ascension.skill_passives;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.capabilities.AscensionEntityDataHolder;
import net.zic.ascension.api.capabilities.CoreCapabilities;
import net.zic.ascension.api.core.CoreRegistries;
import net.zic.ascension.api.core.skill.Skill;
import net.zic.ascension.api.core.skill.SkillData;
import net.zic.ascension.api.core.skill.toggleable.ToggleableSkill;
import net.zic.ascension.api.core.source.OriginSource;
import net.zic.ascension.api.core.source.ServerOriginSource;

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
        if (!(event.getEntity() instanceof LivingEntity entity)
                || entity.level().isClientSide()) {
            return;
        }

        AscensionEntityDataHolder holder = entity.getCapability(
                CoreCapabilities.ASCENSION_ENTITY_DATA_HOLDER_CAPABILITY
        );
        if (holder == null) {
            return;
        }

        OriginSource originSource = holder.getData(entity).getSource();
        if (!(originSource instanceof ServerOriginSource source)) {
            return;
        }

        long gameTime = entity.level().getGameTime();
        if (LAST_SOURCE_TICK.getOrDefault(source, Long.MIN_VALUE) == gameTime) {
            return;
        }
        LAST_SOURCE_TICK.put(source, gameTime);

        for (Identifier skillId : List.copyOf(source.getSkills())) {
            Skill skill = CoreRegistries.safeAccess(
                    CoreRegistries.SKILL_REGISTRY,
                    skillId,
                    entity.registryAccess()
            );
            SkillData data = source.getSkillData(skillId);

            if (!(skill instanceof ToggleableSkill toggleable)
                    || data == null
                    || !toggleable.isEnabled(data)) {
                continue;
            }

            if (!toggleable.tickEnabled(entity, source, data)) {
                source.setSkillEnabled(entity, skillId, false);
            }
        }
    }
}
