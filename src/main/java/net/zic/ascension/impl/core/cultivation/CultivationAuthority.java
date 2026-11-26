package net.zic.ascension.impl.core.cultivation;

import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.ascension.core.CoreDataSources;
import net.zic.ascension.api.ascension.core.cultivation.AuthorityCurve;
import net.zic.ascension.api.ascension.core.path.PathHolder;
import net.zic.ascension.api.ascension.core.path.PathInstance;
import net.zic.ascension.api.ascension.core.source.AscensionOriginSourceHelper;
import net.zic.ascension.configuration.ConfigurationRegistries;

public final class CultivationAuthority {
    public static final Identifier BODY = AscensionCraft.prefix("foundation/body");
    public static final Identifier ESSENCE = AscensionCraft.prefix("foundation/essence");
    public static final Identifier SOUL = AscensionCraft.prefix("foundation/soul");
    private static final Identifier DEFAULT = AscensionCraft.prefix("default");

    private CultivationAuthority() {}

    public record Snapshot(double body, double essence, double soul) {}

    public static Snapshot read(LivingEntity entity) {
        Registry<AuthorityCurve> registry = entity.registryAccess()
                .lookup(ConfigurationRegistries.REALM_EFFECTIVENESS_REGISTRY.key()).orElse(null);
        AuthorityCurve curve = registry == null ? null : registry.getValue(DEFAULT);
        if (curve == null) {
            throw new IllegalStateException("Missing realm effectiveness configuration: " + DEFAULT);
        }
        PathHolder holder = AscensionOriginSourceHelper.getEntitySource(entity)
                .getDataSource(CoreDataSources.PATH_DATA_SOURCE.get());
        return new Snapshot(read(holder, BODY, curve), read(holder, ESSENCE, curve), read(holder, SOUL, curve));
    }

    private static double read(PathHolder holder, Identifier id, AuthorityCurve curve) {
        PathInstance<?> path = holder == null ? null : holder.getPath(id);
        if (path == null || path.getCurrentMajorRealm() == -1) return curve.mortalAuthority();
        int major = path.getCurrentMajorRealm();
        return curve.at(major, path.getCurrentMinorRealm(), path.getMaxMinorRealm(major));
    }
}
