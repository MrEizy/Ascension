package net.zic.ascension.impl.core.skill.passive;

import net.zic.ascension.api.ascension.value.ScaledValue;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.zic.ascension.api.ascension.capabilities.CoreCapabilities;
import net.zic.ascension.api.ascension.core.CoreRegistries;
import net.zic.ascension.api.ascension.core.skill.Skill;
import net.zic.ascension.api.ascension.core.skill.SkillData;
import net.zic.ascension.api.ascension.core.skill.castable.action.SkillActionContext;
import net.zic.ascension.api.ascension.core.skill.SkillLevelResolver;
import net.zic.ascension.api.ascension.core.source.AscensionOriginSourceHelper;
import net.zic.ascension.api.rpg_engine.damage.RPGEngineEntityDamagedEvent;
import net.zic.ascension.api.rpg_engine.source.OriginSource;
import net.zic.ascension.impl.core.skill.SimplePassiveSkill;
import net.zic.ascension.impl.core.skill.passive.ResourceModifierPassiveSkill.Data;

import java.util.List;
import java.util.Map;

public final class PassiveDefenseService {
    private PassiveDefenseService() {
    }

    public static double resolveDamage(RPGEngineEntityDamagedEvent.Pre event, double incomingDamage) {
        LivingEntity target = event.getEntity();
        OriginSource source = originSource(target);
        if (source == null || incomingDamage <= 0.0D) {
            return Math.max(0.0D, incomingDamage);
        }
        double flatReduction = 0.0D;
        double retainedDamage = 1.0D;
        LivingEntity attacker = event.getSource().getEntity() instanceof LivingEntity living ? living : null;
        for (Identifier skillId : AscensionOriginSourceHelper.getSkills(source)) {
            Skill skill = CoreRegistries.safeAccess(CoreRegistries.SKILL_REGISTRY, skillId, target.registryAccess());
            SkillData data = AscensionOriginSourceHelper.getSkillData(source, skillId);
            for (PassiveModules.Defense defense : defenses(skill, data, source, skillId)) {
                if (!defense.filter().accepts(event.getSource())) {
                    continue;
                }
                ScaledValue.Context context = new ScaledValue.Context(source, skillId, target, attacker, 0.0D, Map.of());
                double flat = defense.flatReduction().resolve(context);
                double percentage = defense.percentageReduction().resolve(context);
                if (Double.isFinite(flat) && flat > 0.0D) {
                    flatReduction += flat;
                }
                if (Double.isFinite(percentage) && percentage > 0.0D) {
                    retainedDamage *= 1.0D - Math.clamp(percentage, 0.0D, 0.95D);
                }
            }
        }
        return Math.max(0.0D, (incomingDamage - flatReduction) * retainedDamage);
    }

    public static double staggerResistance(LivingEntity target, SkillActionContext executionContext) {
        OriginSource source = originSource(target);
        if (source == null) {
            return 0.0D;
        }
        double retainedStagger = 1.0D;
        LivingEntity attacker = executionContext == null ? null : executionContext.caster();
        double charge = executionContext == null ? 0.0D : executionContext.charge();
        Map<Identifier, Double> variables = executionContext == null ? Map.of() : executionContext.variables();
        for (Identifier skillId : AscensionOriginSourceHelper.getSkills(source)) {
            Skill skill = CoreRegistries.safeAccess(CoreRegistries.SKILL_REGISTRY, skillId, target.registryAccess());
            SkillData data = AscensionOriginSourceHelper.getSkillData(source, skillId);
            for (PassiveModules.Defense defense : defenses(skill, data, source, skillId)) {
                ScaledValue.Context context = new ScaledValue.Context(source, skillId, target, attacker, charge, variables);
                double resistance = defense.staggerResistance().resolve(context);
                if (Double.isFinite(resistance) && resistance > 0.0D) {
                    retainedStagger *= 1.0D - Math.clamp(resistance, 0.0D, 0.95D);
                }
            }
        }
        return Math.clamp(1.0D - retainedStagger, 0.0D, 0.95D);
    }

    private static List<PassiveModules.Defense> defenses(
            Skill skill,
            SkillData data,
            OriginSource source,
            Identifier skillId
    ) {
        if (skill instanceof SimplePassiveSkill passive && passive.getDefense().isPresent()) {
            SimplePassiveSkill.Defense value = passive.getDefense().get();
            return List.of(new PassiveModules.Defense(
                    value.flatReduction(),
                    value.percentageReduction(),
                    value.staggerResistance(),
                    value.filter()
            ));
        }
        if (skill instanceof ResourceModifierPassiveSkill passive
                && data instanceof Data passiveData
                && (!(passive instanceof net.zic.ascension.api.ascension.core.skill.toggleable.ToggleableSkill)
                || passiveData.isEnabled())) {
            return passive.defenses(SkillLevelResolver.resolve(source, skillId).effectiveLevel());
        }
        return List.of();
    }

    private static OriginSource originSource(LivingEntity entity) {
        var provider = entity.getCapability(CoreCapabilities.ASCENSION_ENTITY_DATA_PROVIDER_CAPABILITY);
        return provider == null ? null : provider.getData().getSource();
    }
}
