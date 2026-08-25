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

    public double getMultiplier(int majorRealm) {
        if (majorRealm < 0 || multipliers.isEmpty()) {
            return 1.0D;
        }

        int index = Math.min(majorRealm, multipliers.size() - 1);
        double multiplier = multipliers.get(index);
        return Double.isFinite(multiplier) && multiplier >= 0.0D ? multiplier : 1.0D;
    }

    public static double apply(OriginSource source, double value) {
        double result = value * getMultiplier(source);
        return Double.isFinite(result) ? result : value;
    }

    public static double apply(OriginSource source, double value, double responseExponent) {
        double safeExponent = Double.isFinite(responseExponent) ? responseExponent : 1.0D;
        double multiplier = Math.pow(getMultiplier(source), safeExponent);
        double result = value * multiplier;
        return Double.isFinite(result) ? result : value;
    }

    private double resolveMultiplier(OriginSource source) {
        if (source == null) {
            return 1.0D;
        }

        DataSourceInstance dataSource = source.hasDataSource(CoreHolderProviders.PATH_HOLDER_PROVIDER.getId()) ? source.getDataSource(CoreHolderProviders.PATH_HOLDER_PROVIDER.getId()) : null;
        if (!(dataSource instanceof PathHolder holder)) {
            return 1.0D;
        }

        int highestMajorRealm = -1;
        for (Identifier pathId : holder.getPaths()) {
            if (pathId == null || !pathId.getPath().startsWith(FOUNDATION_PATH_PREFIX)) {
                continue;
            }

            PathInstance pathInstance = holder.getPath(pathId);
            if (pathInstance != null) {
                highestMajorRealm = Math.max(highestMajorRealm, pathInstance.getCurrentMajorRealm());
            }
        }

        return getMultiplier(highestMajorRealm);
    }
}
