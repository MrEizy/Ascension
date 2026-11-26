package net.zic.ascension.impl.core.damage;

import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.LivingEntity;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.ascension.core.damage.AscensionDamageTypeHolders;
import net.zic.ascension.api.rpg_engine.damage.RPGEngineDamageSource;
import net.zic.ascension.api.rpg_engine.damage.RPGEngineEntityDamagedEvent;
import net.zic.ascension.impl.core.path.CultivationAuthority;
import net.zic.zenithlib.value_containers.typed.Modifier;

import java.util.EnumSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public final class AuthorityCombat {
    private static final AtomicBoolean WARNED_INVALID = new AtomicBoolean();

    private AuthorityCombat() {}

    public static void apply(RPGEngineEntityDamagedEvent.Pre event, DamageTrace trace, int operationGroup) {
        RPGEngineDamageSource source = event.getSource();
        if (!(source.getEntity() instanceof LivingEntity attacker) || attacker == event.getEntity()) return;
        if (!Double.isFinite(event.getDamage()) || event.getDamage() <= 0.0D) return;

        Set<String> classifications = source.getDamageTypeHolder(AscensionDamageTypeHolders.CLASSIFICATIONS)
                instanceof AscensionDamageTypeHolders.Classifications values
                ? values.values().stream().map(Identifier::toString).collect(Collectors.toSet()) : Set.of();
        String path = source.getDamageTypeHolder(AscensionDamageTypeHolders.PATH)
                instanceof AscensionDamageTypeHolders.Path value && value.path() != null ? value.path().toString() : null;
        String damageType = source.typeHolder().unwrapKey().map(ResourceKey::identifier).map(Identifier::toString).orElse(null);
        boolean skill = classifications.contains("ascension:skill")
                || source.hasDamageTypeHolder(AscensionDamageTypeHolders.PROFILE)
                || source.hasDamageTypeHolder(AscensionDamageTypeHolders.ATTRIBUTION);
        var channels = channels(classifications, path, damageType, skill);

        final double ratio;
        final double[] offense;
        final double[] rawDefense;
        final double[] defense;
        try {
            offense = CultivationAuthority.read(attacker).offensiveAuthorities();
            var defender = CultivationAuthority.read(event.getEntity());
            rawDefense = defender.offensiveAuthorities();
            defense = defender.defensiveAuthorities();
            ratio = ratio(channels, offense, defense);
            if (!Double.isFinite(event.getDamage() * ratio)) {
                throw new IllegalArgumentException("Authority-scaled damage overflows double");
            }
        } catch (IllegalArgumentException | IllegalStateException exception) {
            if (WARNED_INVALID.compareAndSet(false, true)) {
                AscensionCraft.LOGGER.warn("Skipping cultivation authority in combat: {}", exception.getMessage());
            }
            return;
        }
        if (trace.enabled()) {
            for (var channel : channels) {
                int index = channel.ordinal();
                trace.value("Attacker " + channel, offense[index]);
                trace.value("Defender " + channel + " (raw)", rawDefense[index]);
                trace.value("Defender " + channel + " (effective)", defense[index]);
            }
            trace.value("Authority ratio " + channels, ratio);
        }
        if (ratio == 1.0D) return;

        int count = factorCount(ratio);
        double factor = count == 1 ? ratio : Math.pow(ratio, 1.0D / count);
        for (int i = 0; i < count; i++) {
            Identifier id = AscensionCraft.prefix("damage_authority_" + i);
            event.addMultiplierModifier(Modifier.multiplier(id, id, operationGroup, factor - 1.0D));
        }
    }

    private enum Channel { BODY, ESSENCE, SOUL }

    private static EnumSet<Channel> channels(Set<String> classifications, String path, String damageType, boolean skill) {
        EnumSet<Channel> result = EnumSet.noneOf(Channel.class);
        if (classifications.contains("ascension:body")) result.add(Channel.BODY);
        if (classifications.contains("ascension:essence")) result.add(Channel.ESSENCE);
        if (classifications.contains("ascension:soul")) result.add(Channel.SOUL);
        if (!result.isEmpty()) return result;
        if (classifications.contains("ascension:qi")) return EnumSet.of(Channel.ESSENCE);
        if ("ascension:foundation/body".equals(path)) return EnumSet.of(Channel.BODY);
        if ("ascension:foundation/essence".equals(path)) return EnumSet.of(Channel.ESSENCE);
        if ("ascension:foundation/soul".equals(path)) return EnumSet.of(Channel.SOUL);
        if (damageType != null && damageType.startsWith("ascension:") && damageType.endsWith("_qi")) {
            return EnumSet.of(Channel.ESSENCE);
        }
        if (skill && path != null && path.startsWith("ascension:elemental/")) return EnumSet.of(Channel.ESSENCE);
        if ("minecraft:magic".equals(damageType) || "minecraft:indirect_magic".equals(damageType)
                || "minecraft:dragon_breath".equals(damageType)) return EnumSet.of(Channel.ESSENCE);
        return EnumSet.of(Channel.BODY);
    }

    private static double ratio(Set<Channel> channels, double[] attacker, double[] defender) {
        if (channels.isEmpty() || attacker.length != 3 || defender.length != 3) {
            throw new IllegalArgumentException("Authority combat needs channels and three foundation values");
        }
        double logRatio = 0.0D;
        for (Channel channel : channels) {
            double a = attacker[channel.ordinal()];
            double d = defender[channel.ordinal()];
            if (!Double.isFinite(a) || !Double.isFinite(d) || a <= 0.0D || d <= 0.0D) {
                throw new IllegalArgumentException("Combat authority must be positive and finite");
            }
            logRatio += Math.log(a) - Math.log(d);
        }
        double ratio = Math.exp(logRatio / channels.size());
        if (!Double.isFinite(ratio) || ratio <= 0.0D) {
            throw new IllegalArgumentException("Combat authority ratio is outside double precision range");
        }
        return ratio;
    }

    private static int factorCount(double ratio) {
        if (!Double.isFinite(ratio) || ratio <= 0.0D) throw new IllegalArgumentException("Invalid authority ratio");
        return Math.max(1, (int) Math.ceil(-Math.log10(ratio) / 4.0D));
    }
}
