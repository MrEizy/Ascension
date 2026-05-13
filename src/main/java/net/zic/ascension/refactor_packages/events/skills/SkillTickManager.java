package net.zic.ascension.refactor_packages.events.skills;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.zic.ascension.refactor_packages.entity_data.IEntityData;
import net.zic.ascension.refactor_packages.skills.ITickingSkill;

import java.util.HashMap;
import java.util.Map;

public class SkillTickManager {

    private static final Map<ResourceLocation, ITickingSkill> TICKING_SKILLS = new HashMap<>();

    public static void register(ResourceLocation skillId, ITickingSkill tickingSkill) {
        TICKING_SKILLS.put(skillId, tickingSkill);
    }

    public static void tickPlayer(ServerPlayer player, IEntityData entityData) {
        for (ResourceLocation skillId : entityData.getAllSkills()) {
            ITickingSkill tickingSkill = TICKING_SKILLS.get(skillId);

            if (tickingSkill != null) {
                tickingSkill.onPlayerTick(player, entityData);
            }
        }
    }
}