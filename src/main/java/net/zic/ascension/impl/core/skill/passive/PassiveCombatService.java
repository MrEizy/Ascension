package net.zic.ascension.impl.core.skill.passive;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.entity.projectile.arrow.ThrownTrident;
import net.minecraft.world.item.ItemStack;
import net.zic.ascension.api.ascension.capabilities.CoreCapabilities;
import net.zic.ascension.api.ascension.core.damage.AscensionDamageTypeHolders;
import net.zic.ascension.api.ascension.core.skill.castable.action.SkillActionContext;
import net.zic.ascension.api.ascension.core.source.AscensionOriginSourceHelper;
import net.zic.ascension.api.ascension.value.ScaledValue;
import net.zic.ascension.api.rpg_engine.damage.RPGEngineEntityDamagedEvent;
import net.zic.ascension.api.rpg_engine.source.OriginSource;

import java.util.Map;

public final class PassiveCombatService {
    private PassiveCombatService() {
    }

    public static double outgoingDamageMultiplier(RPGEngineEntityDamagedEvent.Pre event) {
        if (event.getDamage() <= 0.0D
                || event.getSource().hasDamageTypeHolder(AscensionDamageTypeHolders.ATTRIBUTION)
                || !(event.getSource().getEntity() instanceof ServerPlayer player)) {
            return 1.0D;
        }
        OriginSource source = AscensionOriginSourceHelper.getEntitySource(player);
        if (source == null) {
            return 1.0D;
        }
        double bestMultiplier = 1.0D;
        for (PassiveSkillService.Entry<PassiveModifiers.WeaponDamage> entry
                : PassiveSkillService.modifiers(source, player.registryAccess(), PassiveModifiers.WeaponDamage.class)) {
            if (!matchesWeaponDamage(player, event.getSource().getDirectEntity(), entry.modifier())) {
                continue;
            }
            double value = entry.modifier().multiplier().resolve(new ScaledValue.Context(
                    source,
                    entry.skillId(),
                    player,
                    event.getEntity(),
                    0.0D,
                    Map.of()
            ));
            if (Double.isFinite(value)) {
                bestMultiplier = Math.max(bestMultiplier, Math.max(0.0D, value));
            }
        }
        return bestMultiplier;
    }

    public static double incomingDamage(RPGEngineEntityDamagedEvent.Pre event, double incomingDamage) {
        LivingEntity target = event.getEntity();
        OriginSource source = originSource(target);
        if (source == null || incomingDamage <= 0.0D) {
            return Math.max(0.0D, incomingDamage);
        }
        double flatReduction = 0.0D;
        double retainedDamage = 1.0D;
        LivingEntity attacker = event.getSource().getEntity() instanceof LivingEntity living ? living : null;
        for (PassiveSkillService.Entry<PassiveModifiers.Defense> entry
                : PassiveSkillService.modifiers(source, target.registryAccess(), PassiveModifiers.Defense.class)) {
            PassiveModifiers.Defense defense = entry.modifier();
            if (!defense.filter().accepts(event.getSource())) {
                continue;
            }
            ScaledValue.Context context = new ScaledValue.Context(source, entry.skillId(), target, attacker, 0.0D, Map.of());
            double flat = defense.flatReduction().resolve(context);
            double percentage = defense.percentageReduction().resolve(context);
            if (Double.isFinite(flat) && flat > 0.0D) {
                flatReduction += flat;
            }
            if (Double.isFinite(percentage) && percentage > 0.0D) {
                retainedDamage *= 1.0D - Math.clamp(percentage, 0.0D, 0.95D);
            }
        }
        return Math.max(0.0D, (incomingDamage - flatReduction) * retainedDamage);
    }

    public static double staggerResistance(LivingEntity target, SkillActionContext actionContext) {
        OriginSource source = originSource(target);
        if (source == null) {
            return 0.0D;
        }
        double retainedStagger = 1.0D;
        LivingEntity attacker = actionContext == null ? null : actionContext.caster();
        double charge = actionContext == null ? 0.0D : actionContext.charge();
        Map<Identifier, Double> variables = actionContext == null ? Map.of() : actionContext.variables();
        for (PassiveSkillService.Entry<PassiveModifiers.Defense> entry
                : PassiveSkillService.modifiers(source, target.registryAccess(), PassiveModifiers.Defense.class)) {
            ScaledValue.Context context = new ScaledValue.Context(source, entry.skillId(), target, attacker, charge, variables);
            double resistance = entry.modifier().staggerResistance().resolve(context);
            if (Double.isFinite(resistance) && resistance > 0.0D) {
                retainedStagger *= 1.0D - Math.clamp(resistance, 0.0D, 0.95D);
            }
        }
        return Math.clamp(1.0D - retainedStagger, 0.0D, 0.95D);
    }

    private static boolean matchesWeaponDamage(ServerPlayer player, Entity direct, PassiveModifiers.WeaponDamage modifier) {
        ItemStack stack = player.getMainHandItem();
        boolean tagMatch = modifier.weaponTag().isPresent()
                && !stack.isEmpty()
                && stack.is(TagKey.create(Registries.ITEM, modifier.weaponTag().get()));
        return switch (modifier.match()) {
            case HELD_WEAPON -> direct == player && tagMatch;
            case EMPTY_HAND_OR_TAG -> direct == player && (stack.isEmpty() || tagMatch);
            case ARROW -> direct instanceof AbstractArrow && !(direct instanceof ThrownTrident);
            case TRIDENT -> direct instanceof ThrownTrident || direct == player && tagMatch;
        };
    }

    private static OriginSource originSource(LivingEntity entity) {
        var provider = entity.getCapability(CoreCapabilities.ASCENSION_ENTITY_DATA_PROVIDER_CAPABILITY);
        return provider == null ? null : provider.getData().getSource();
    }
}
