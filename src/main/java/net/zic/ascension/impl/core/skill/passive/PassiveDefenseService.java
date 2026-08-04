package net.zic.ascension.impl.core.skill.passive;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.ascension.capabilities.CoreCapabilities;
import net.zic.ascension.api.ascension.core.CoreRegistries;
import net.zic.ascension.api.ascension.core.skill.castable.feature.SkillExecutionContext;
import net.zic.ascension.api.ascension.core.source.AscensionOriginSourceHelper;
import net.zic.ascension.api.ascension.value.ScaledValueContext;
import net.zic.ascension.api.rpg_engine.damage.RPGEngineEntityDamagedEvent;
import net.zic.ascension.api.rpg_engine.source.OriginSource;
import net.zic.ascension.impl.core.skill.SimplePassiveSkill;

import java.util.Map;

@EventBusSubscriber(modid = AscensionCraft.MOD_ID)
public final class PassiveDefenseService {
    private PassiveDefenseService() {
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onDamage(RPGEngineEntityDamagedEvent.Pre event) {
        LivingEntity target = event.getEntity();
        OriginSource source = originSource(target);
        if (source == null) {
            return;
        }

        double flatReduction = 0.0D;
        double retainedDamage = 1.0D;
        LivingEntity attacker = event.getSource().getEntity() instanceof LivingEntity living ? living : null;

        for (Identifier skillId : AscensionOriginSourceHelper.getSkills(source)) {
            if (!(CoreRegistries.safeAccess(
                    CoreRegistries.SKILL_REGISTRY,
                    skillId,
                    target.registryAccess()
            ) instanceof SimplePassiveSkill passive) || passive.getDefense().isEmpty()) {
                continue;
            }

            SimplePassiveSkill.Defense defense = passive.getDefense().get();
            if (!defense.filter().accepts(event.getSource())) {
                continue;
            }

            ScaledValueContext context = new ScaledValueContext(
                    source,
                    skillId,
                    target,
                    attacker,
                    0.0D,
                    Map.of()
            );
            double flat = defense.flatReduction().resolve(context);
            double percentage = defense.percentageReduction().resolve(context);
            if (Double.isFinite(flat) && flat > 0.0D) {
                flatReduction += flat;
            }
            if (Double.isFinite(percentage) && percentage > 0.0D) {
                retainedDamage *= 1.0D - Math.clamp(percentage, 0.0D, 0.95D);
            }
        }

        if (flatReduction <= 0.0D && retainedDamage >= 1.0D) {
            return;
        }
        event.setDamage(Math.max(0.0D, (event.getDamage() - flatReduction) * retainedDamage));
    }

    public static double staggerResistance(LivingEntity target, SkillExecutionContext executionContext) {
        OriginSource source = originSource(target);
        if (source == null) {
            return 0.0D;
        }

        double retainedStagger = 1.0D;
        LivingEntity attacker = executionContext == null ? null : executionContext.caster();
        double charge = executionContext == null ? 0.0D : executionContext.charge();
        Map<Identifier, Double> variables = executionContext == null ? Map.of() : executionContext.variables();

        for (Identifier skillId : AscensionOriginSourceHelper.getSkills(source)) {
            if (!(CoreRegistries.safeAccess(
                    CoreRegistries.SKILL_REGISTRY,
                    skillId,
                    target.registryAccess()
            ) instanceof SimplePassiveSkill passive) || passive.getDefense().isEmpty()) {
                continue;
            }

            ScaledValueContext context = new ScaledValueContext(
                    source,
                    skillId,
                    target,
                    attacker,
                    charge,
                    variables
            );
            double resistance = passive.getDefense().get().staggerResistance().resolve(context);
            if (Double.isFinite(resistance) && resistance > 0.0D) {
                retainedStagger *= 1.0D - Math.clamp(resistance, 0.0D, 0.95D);
            }
        }
        return Math.clamp(1.0D - retainedStagger, 0.0D, 0.95D);
    }

    private static OriginSource originSource(LivingEntity entity) {
        var provider = entity.getCapability(CoreCapabilities.ASCENSION_ENTITY_DATA_PROVIDER_CAPABILITY);
        return provider == null ? null : provider.getData(entity).getSource();
    }
}
