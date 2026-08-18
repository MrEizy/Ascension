package net.zic.ascension.configuration;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.ascension.core.CoreHolderProviders;
import net.zic.ascension.api.ascension.core.path.PathHolder;
import net.zic.ascension.api.ascension.core.path.PathInstance;
import net.zic.ascension.api.rpg_engine.source.OriginSource;
import net.zic.ascension.api.rpg_engine.source.data_source.DataSourceInstance;

import java.util.List;
import java.util.Map;

public record RealmEffectivenessConfiguration(
        Map<Identifier, List<Double>> paths,
        Map<Identifier, EffectivenessRule> stats,
        Map<String, EffectivenessRule> attributeScalings
) {
    public static final Identifier DEFAULT_ID = AscensionCraft.prefix("default");

    public static final Codec<RealmEffectivenessConfiguration> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.unboundedMap(Identifier.CODEC, Codec.DOUBLE.listOf()).optionalFieldOf("paths", Map.of()).forGetter(RealmEffectivenessConfiguration::paths),
            Codec.unboundedMap(Identifier.CODEC, EffectivenessRule.CODEC).optionalFieldOf("stats", Map.of()).forGetter(RealmEffectivenessConfiguration::stats),
            Codec.unboundedMap(Codec.STRING, EffectivenessRule.CODEC).optionalFieldOf("attribute_scalings", Map.of()).forGetter(RealmEffectivenessConfiguration::attributeScalings)
    ).apply(instance, RealmEffectivenessConfiguration::new));

    private static final RealmEffectivenessConfiguration DISABLED =
            new RealmEffectivenessConfiguration(Map.of(), Map.of(), Map.of());

    public RealmEffectivenessConfiguration {
        paths = paths == null ? Map.of() : Map.copyOf(paths);
        stats = stats == null ? Map.of() : Map.copyOf(stats);
        attributeScalings = attributeScalings == null ? Map.of() : Map.copyOf(attributeScalings);
    }

    public static RealmEffectivenessConfiguration get(OriginSource source) {
        if (source == null || source.getRegistryAccess() == null) {
            return DISABLED;
        }

        var registry = ConfigurationRegistries.REALM_EFFECTIVENESS_REGISTRY.get(source.getRegistryAccess());
        RealmEffectivenessConfiguration configuration = registry.getValue(DEFAULT_ID);
        return configuration == null ? DISABLED : configuration;
    }

    public static double getPathEffectiveness(OriginSource source, Identifier path) {
        return get(source).resolvePathEffectiveness(source, path);
    }

    public static double applyToStat(OriginSource source, Identifier stat, double rawValue) {
        return applyToStat(source, stat, rawValue, 1.0D);
    }

    public static double getStatMultiplier(OriginSource source, Identifier stat) {
        RealmEffectivenessConfiguration configuration = get(source);
        EffectivenessRule rule = configuration.stats.get(stat);
        return rule == null ? 1.0D : configuration.resolveRuleMultiplier(source, rule);
    }

    public static double applyToStat(OriginSource source, Identifier stat, double rawValue, double exponentScale) {
        RealmEffectivenessConfiguration configuration = get(source);
        EffectivenessRule rule = configuration.stats.get(stat);
        if (rule == null) {
            return rawValue;
        }
        return rawValue * configuration.resolveRuleMultiplier(source, rule, exponentScale);
    }

    public static double getAttributeScalingMultiplier(OriginSource source, String scalingName) {
        RealmEffectivenessConfiguration configuration = get(source);
        EffectivenessRule rule = configuration.attributeScalings.get(scalingName);
        return rule == null ? 1.0D : configuration.resolveRuleMultiplier(source, rule);
    }

    public static double apply(OriginSource source, Identifier path, double value, double exponent) {
        double potency = getPathEffectiveness(source, path);
        double multiplier = Math.pow(potency, exponent);
        return Double.isFinite(multiplier) ? value * multiplier : value;
    }

    private double resolveRuleMultiplier(OriginSource source, EffectivenessRule rule) {
        return resolveRuleMultiplier(source, rule, 1.0D);
    }

    private double resolveRuleMultiplier(OriginSource source, EffectivenessRule rule, double exponentScale) {
        if (rule.paths().isEmpty()) {
            return 1.0D;
        }

        double strongest = 0.0D;
        boolean found = false;
        for (Identifier path : rule.paths()) {
            Double potency = resolveOwnedPathEffectiveness(source, path);
            if (potency == null) {
                continue;
            }
            strongest = found ? Math.max(strongest, potency) : potency;
            found = true;
        }

        if (!found) {
            return 1.0D;
        }

        double safeExponentScale = Double.isFinite(exponentScale) ? exponentScale : 1.0D;
        double multiplier = Math.pow(strongest, rule.exponent() * safeExponentScale);
        return Double.isFinite(multiplier) ? multiplier : 1.0D;
    }

    private double resolvePathEffectiveness(OriginSource source, Identifier path) {
        Double potency = resolveOwnedPathEffectiveness(source, path);
        return potency == null ? 1.0D : potency;
    }

    private Double resolveOwnedPathEffectiveness(OriginSource source, Identifier path) {
        if (source == null || path == null) {
            return null;
        }

        DataSourceInstance dataSource = source.hasDataSource(CoreHolderProviders.PATH_HOLDER_PROVIDER.getId()) ? source.getDataSource(CoreHolderProviders.PATH_HOLDER_PROVIDER.getId()) : null;
        if (!(dataSource instanceof PathHolder holder)) {
            return null;
        }

        PathInstance pathInstance = holder.getPath(path);
        if (pathInstance == null) {
            return null;
        }

        List<Double> curve = paths.get(path);
        if (curve == null || curve.isEmpty()) {
            return 1.0D;
        }

        int majorRealm = pathInstance.getCurrentMajorRealm();
        if (majorRealm < 0) {
            return 1.0D;
        }

        int index = Math.min(majorRealm, curve.size() - 1);
        double potency = curve.get(index);
        return Double.isFinite(potency) && potency >= 0.0D ? potency : 1.0D;
    }

    public record EffectivenessRule(List<Identifier> paths, double exponent) {
        public static final Codec<EffectivenessRule> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Identifier.CODEC.listOf().fieldOf("paths").forGetter(EffectivenessRule::paths),
                Codec.DOUBLE.optionalFieldOf("exponent", 1.0D).forGetter(EffectivenessRule::exponent)
        ).apply(instance, EffectivenessRule::new));

        public EffectivenessRule {
            paths = paths == null ? List.of() : List.copyOf(paths);
            exponent = Double.isFinite(exponent) ? exponent : 1.0D;
        }
    }
}
