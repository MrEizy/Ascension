package net.zic.ascension.impl.core.path;

import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.ascension.core.CoreDataSources;
import net.zic.ascension.api.ascension.core.path.PathHolder;
import net.zic.ascension.api.ascension.core.path.PathInstance;
import net.zic.ascension.api.ascension.core.source.AscensionOriginSourceHelper;
import net.zic.ascension.configuration.ConfigurationRegistries;
import net.zic.ascension.configuration.cultivation.RealmEffectivenessDefinition;

public final class CultivationAuthority {
    public static final Identifier BODY = AscensionCraft.prefix("foundation/body");
    public static final Identifier ESSENCE = AscensionCraft.prefix("foundation/essence");
    public static final Identifier SOUL = AscensionCraft.prefix("foundation/soul");
    private static final Identifier DEFAULT = AscensionCraft.prefix("default");

    private CultivationAuthority() {}

    public record Snapshot(double body, double essence, double soul, double existentialProtection) {
        public Snapshot(double body, double essence, double soul) {
            this(body, essence, soul, RealmEffectivenessDefinition.DEFAULT_EXISTENTIAL_PROTECTION);
        }

        public double[] offensiveAuthorities() {
            return new double[]{body, essence, soul};
        }

        public double[] defensiveAuthorities() {
            return CultivationAuthority.defensiveAuthorities(offensiveAuthorities(), existentialProtection);
        }
    }

    public static Snapshot read(LivingEntity entity) {
        Registry<RealmEffectivenessDefinition> registry = entity.registryAccess()
                .lookup(ConfigurationRegistries.REALM_EFFECTIVENESS_REGISTRY.key()).orElse(null);
        RealmEffectivenessDefinition curve = registry == null ? null : registry.getValue(DEFAULT);
        if (curve == null) {
            throw new IllegalStateException("Missing realm effectiveness configuration: " + DEFAULT);
        }
        PathHolder holder = AscensionOriginSourceHelper.getEntitySource(entity)
                .getDataSource(CoreDataSources.PATH_DATA_SOURCE.get());
        return new Snapshot(read(holder, BODY, curve), read(holder, ESSENCE, curve), read(holder, SOUL, curve),
                curve.existentialProtection());
    }

    private static double read(PathHolder holder, Identifier id, RealmEffectivenessDefinition curve) {
        PathInstance<?> path = holder == null ? null : holder.getPath(id);
        if (path == null || path.getCurrentMajorRealm() == -1) return curve.mortalAuthority();
        int major = path.getCurrentMajorRealm();
        return curve.at(major, path.getCurrentMinorRealm(), path.getMaxMinorRealm(major));
    }

    private static double[] defensiveAuthorities(double[] authority, double protection) {
        if (authority.length != 3 || !Double.isFinite(protection)
                || protection < 0.0D || protection > 1.0D) {
            throw new IllegalArgumentException("Invalid existential protection or foundation values");
        }
        double highest = 0.0D;
        for (double value : authority) {
            if (!Double.isFinite(value) || value <= 0.0D) {
                throw new IllegalArgumentException("Combat authority must be positive and finite");
            }
            highest = Math.max(highest, value);
        }
        double floor = highest * protection;
        double[] result = authority.clone();
        for (int i = 0; i < result.length; i++) result[i] = Math.max(result[i], floor);
        return result;
    }
}
