package net.zic.ascension.configuration;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.ascension.core.CoreHolderProviders;
import net.zic.ascension.api.ascension.core.path.PathHolder;
import net.zic.ascension.api.ascension.core.path.PathInstance;
import net.zic.ascension.api.ascension.core.source.AscensionOriginSourceHelper;
import net.zic.ascension.api.rpg_engine.source.OriginSource;
import net.zic.ascension.api.rpg_engine.source.data_source.DataSourceInstance;

import java.util.List;

/**
 * Global realm-effectiveness curve.
 */
public record RealmEffectivenessConfiguration(List<Double> multipliers) {
    public static final Identifier DEFAULT_ID = AscensionCraft.prefix("default");
    private static final String FOUNDATION_PATH_PREFIX = "foundation/";

    public static final Codec<RealmEffectivenessConfiguration> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.DOUBLE.listOf().optionalFieldOf("multipliers", List.of(1.0D)).forGetter(RealmEffectivenessConfiguration::multipliers)
    ).apply(instance, RealmEffectivenessConfiguration::new));

    private static final RealmEffectivenessConfiguration DISABLED =
            new RealmEffectivenessConfiguration(List.of(1.0D));

    public RealmEffectivenessConfiguration {
        multipliers = multipliers == null || multipliers.isEmpty() ? List.of(1.0D) : List.copyOf(multipliers);
    }

    public static RealmEffectivenessConfiguration get(OriginSource source) {
        if (source == null || source.getRegistryAccess() == null) {
            return DISABLED;
        }

        var registry = ConfigurationRegistries.REALM_EFFECTIVENESS_REGISTRY.get(source.getRegistryAccess());
        RealmEffectivenessConfiguration configuration = registry.getValue(DEFAULT_ID);
        return configuration == null ? DISABLED : configuration;
    }

    public static double getMultiplier(OriginSource source) {
        return get(source).resolveMultiplier(source);
    }

    public static double getMultiplier(LivingEntity entity) {
        if (entity == null) {
            return 1.0D;
        }

        OriginSource source = AscensionOriginSourceHelper.getEntitySource(entity);
        return source == null ? 1.0D : getMultiplier(source);
    }

    public double getMultiplier(int majorRealm) {
        if (majorRealm < 0 || multipliers.isEmpty()) {
            return 1.0D;
        }

        int index = Math.min(majorRealm, multipliers.size() - 1);
        double multiplier = multipliers.get(index);
        return Double.isFinite(multiplier) && multiplier > 0.0D ? multiplier : 1.0D;
    }

    public static double apply(OriginSource source, double value) {
        return apply(value, getMultiplier(source));
    }

    public static double apply(LivingEntity entity, double value) {
        return apply(value, getMultiplier(entity));
    }


    public static double getRelativeEffectiveness(LivingEntity attacker, LivingEntity defender) {
        double attackerMultiplier = getMultiplier(attacker);
        double defenderMultiplier = getMultiplier(defender);

        if (!Double.isFinite(attackerMultiplier) || attackerMultiplier <= 0.0D || !Double.isFinite(defenderMultiplier) || defenderMultiplier <= 0.0D) {
            return 1.0D;
        }

        double relative = attackerMultiplier / defenderMultiplier;
        return Double.isFinite(relative) && relative > 0.0D ? relative : 1.0D;
    }

    private static double apply(double value, double multiplier) {
        double result = value * multiplier;
        return Double.isFinite(result) ? result : value;
    }

    private double resolveMultiplier(OriginSource source) {
        if (source == null) {
            return 1.0D;
        }

        int highestMajorRealm = -1;
        for (Identifier pathId : AscensionOriginSourceHelper.getPaths(source)) {
            if (!pathId.getPath().startsWith(FOUNDATION_PATH_PREFIX)) {
                continue;
            }

            PathInstance pathInstance =
                    AscensionOriginSourceHelper.getPathInstance(source, pathId);

            if (pathInstance != null) {
                highestMajorRealm = Math.max(
                        highestMajorRealm,
                        pathInstance.getCurrentMajorRealm()
                );
            }
        }

        return getMultiplier(highestMajorRealm);
    }
}
