package net.zic.ascension.mob_cultivation.oliver_rewrite;

import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.Config;
import net.zic.ascension.api.ascension.core.CoreRegistries;
import net.zic.ascension.api.ascension.core.skill.castable.ActiveCastDefinition;
import net.zic.ascension.api.ascension.core.skill.castable.CastingSkillInstance;
import net.zic.ascension.api.ascension.core.source.AscensionOriginSourceHelper;
import net.zic.ascension.api.rpg_engine.source.OriginSource;
import net.zic.ascension.common.data_attachements.AscensionAttachments;
import net.zic.ascension.configuration.mobs.MobSkillCastingConfigurations;
import net.zic.ascension.impl.core.skill.castable.ActiveSkill;
import net.zic.ascension.skill_casting.SkillCaster;
import net.zic.zenithlib.common.ZenithAttachments;

import java.util.ArrayList;
import java.util.List;

@EventBusSubscriber(modid = AscensionCraft.MOD_ID)
public final class MobSkillCastingHandler {
    private MobSkillCastingHandler() {
    }

    private record Option(CastingSkillInstance<?, ?, ?> cast, int weight) {
    }

    @SubscribeEvent
    public static void onMobTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof Mob mob) || !(mob.level() instanceof ServerLevel level)
                || !mob.isAlive() || !Config.MOB_CULTIVATION_SERVER.CASTING_ENABLED.get()
                || !mob.hasData(AscensionAttachments.MOB_CONFIG_HOLDER)) return;

        MobSkillCastingConfigurations.Settings settings = MobSkillCastingConfigurations.current();
        if (!settings.enabled() || settings.skills().isEmpty()) return;

        int interval = settings.interval();
        if (Math.floorMod((long) mob.tickCount + mob.getId(), interval) != 0) return;

        SkillCaster existing = SkillCaster.getExisting(mob);
        if (existing != null && existing.getCasting() != null) return;

        LivingEntity target = mob.getTarget();
        if (target == null || !target.isAlive() || target.isRemoved() || mob.isAlliedTo(target)) return;

        double frequency = Config.MOB_CULTIVATION_SERVER.CASTING_FREQUENCY_MULTIPLIER.get();
        if (frequency <= 0.0D || mob.getRandom().nextDouble() >= Math.min(1.0D, settings.attemptChance() * frequency)) return;

        OriginSource source = MobSpawnHelper.getConfigHolder(mob).getSource();
        if (source == null || !source.isAttachedTo(mob)) return;

        double distance = Math.sqrt(mob.distanceToSqr(target));
        double health = mob.getMaxHealth() > 0.0F ? mob.getHealth() / mob.getMaxHealth() : 1.0D;
        List<Option> options = new ArrayList<>();
        int totalWeight = 0;

        for (Identifier skillId : AscensionOriginSourceHelper.getSkills(source)) {
            MobSkillCastingConfigurations.Rule rule = settings.skills().get(skillId);
            if (rule == null || distance < rule.minimumDistance() || distance > rule.maximumDistance()
                    || health > rule.healthBelow() || rule.requireLineOfSight() && !mob.hasLineOfSight(target)) continue;
            if (!compatible(rule.behavior(), distance, health)) continue;
            if (mob.getData(ZenithAttachments.COOLDOWN_HANDLER).isOnCooldown(skillId)) continue;
            if (!(CoreRegistries.safeAccess(CoreRegistries.SKILL_REGISTRY, skillId, level.registryAccess()) instanceof ActiveSkill skill)
                    || skill.cast().mode() != ActiveCastDefinition.Mode.INSTANT) continue;
            CastingSkillInstance<?, ?, ?> cast = CastingSkillInstance.create(mob, skillId, level.registryAccess());
            if (cast == null || !cast.tryCast().isSuccess()) continue;
            options.add(new Option(cast, rule.weight()));
            totalWeight += rule.weight();
        }
        if (totalWeight <= 0) return;

        int roll = mob.getRandom().nextInt(totalWeight);
        for (Option option : options) {
            roll -= option.weight();
            if (roll >= 0) continue;
            mob.getLookControl().setLookAt(target, 30.0F, 30.0F);
            SkillCaster.of(mob).startCast(option.cast());
            return;
        }
    }

    private static boolean compatible(String behavior, double distance, double health) {
        return switch (behavior) {
            case "melee" -> true;
            case "ranged" -> distance >= 2.0D;
            case "area", "utility", "support" -> true;
            case "defensive" -> health < 0.85D;
            case "escape" -> health < 0.65D && distance < 9.0D;
            default -> false;
        };
    }
}
