package net.zic.ascension.impl.core.cultivation;

import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.LivingEntity;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.ascension.core.cultivation.AuthorityCombatMath;
import net.zic.ascension.api.ascension.core.damage.AscensionDamageTypeHolders;
import net.zic.ascension.api.rpg_engine.damage.RPGEngineDamageSource;
import net.zic.ascension.api.rpg_engine.damage.RPGEngineEntityDamagedEvent;
import net.zic.ascension.impl.core.damage.DamageTrace;
import net.zic.zenithlib.value_containers.typed.Modifier;

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
        var channels = AuthorityCombatMath.channels(classifications, path, damageType, skill);

        final double ratio;
        try {
            ratio = AuthorityCombatMath.ratio(channels, values(CultivationAuthority.read(attacker)),
                    values(CultivationAuthority.read(event.getEntity())));
            if (!Double.isFinite(event.getDamage() * ratio)) {
                throw new IllegalArgumentException("Authority-scaled damage overflows double");
            }
        } catch (IllegalArgumentException | IllegalStateException exception) {
            if (WARNED_INVALID.compareAndSet(false, true)) {
                AscensionCraft.LOGGER.warn("Skipping cultivation authority in combat: {}", exception.getMessage());
            }
            return;
        }
        if (ratio == 1.0D) return;

        int count = AuthorityCombatMath.factorCount(ratio);
        double factor = count == 1 ? ratio : Math.pow(ratio, 1.0D / count);
        for (int i = 0; i < count; i++) {
            Identifier id = AscensionCraft.prefix("damage_authority_" + i);
            event.addMultiplierModifier(Modifier.multiplier(id, id, operationGroup, factor - 1.0D));
        }
        trace.value("Authority ratio " + channels, ratio);
    }

    private static double[] values(CultivationAuthority.Snapshot snapshot) {
        return new double[]{snapshot.body(), snapshot.essence(), snapshot.soul()};
    }
}
