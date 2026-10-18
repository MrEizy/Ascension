package net.zic.ascension.impl.core.innerworld;

import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.zic.ascension.api.ascension.core.path.PathInstance;
import net.zic.ascension.api.ascension.core.source.AscensionOriginSourceHelper;
import net.zic.ascension.api.rpg_engine.source.OriginSource;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.SplittableRandom;
import java.util.concurrent.ThreadLocalRandom;

/**
 * A player's permanent, hidden Inner World seed. Created ONCE, at first cast, and never shown or changed.
 *
 * Cultivation decides WHICH themes are possible (only variants from profiles of paths the player actually
 * has; randomness never adds a theme outside their paths). The random roll decides how many themes (3-5),
 * which variants of each path are picked, and every noise field in SeededWorld.
 *
 * Path weight = (major realm + 1) + progress through the current major realm. Paths under 10% of the total
 * are dropped (the strongest path is always kept). Each path's variants then share that path's slice of the
 * world by their own "weight". The strongest path's heaviest variant is always theme 0, which owns the core
 * around the dais.
 */
public final class InnerWorldSeed {
    private InnerWorldSeed() {
    }

    public static final double MIN_PATH_SHARE = 0.10;

    public static final class ThemeRef {
        public String profile = "";
        public int variant;
        public double weight;
    }

    public static final class Seed {
        public long value;
        public List<ThemeRef> themes = new ArrayList<>();
    }

    /** Returns null if the player has no cultivation path at all, in which case the cast must be blocked. */
    public static Seed create(ServerPlayer player) {
        OriginSource source = AscensionOriginSourceHelper.getEntitySource(player);
        if (source == null) {
            return null;
        }

        Map<Identifier, Double> weights = new LinkedHashMap<>();
        double total = 0.0;
        for (Identifier path : AscensionOriginSourceHelper.getPaths(source)) {
            PathInstance instance = AscensionOriginSourceHelper.getPathInstance(source, path);
            if (instance == null) {
                continue;
            }
            int major = instance.getCurrentMajorRealm();
            int minor = instance.getCurrentMinorRealm();
            int minorCount = Math.max(1, instance.getMaxMinorRealm(major) + 1);
            double weight = (major + 1) + Math.min(1.0, (double) minor / minorCount);
            weights.put(path, weight);
            total += weight;
        }
        if (weights.isEmpty()) {
            return null;
        }

        Identifier strongest = null;
        for (Map.Entry<Identifier, Double> entry : weights.entrySet()) {
            if (strongest == null || entry.getValue() > weights.get(strongest)) {
                strongest = entry.getKey();
            }
        }
        final Identifier keep = strongest;
        final double fullTotal = total;
        weights.entrySet().removeIf(e -> !e.getKey().equals(keep) && e.getValue() / fullTotal < MIN_PATH_SHARE);
        double keptTotal = 0.0;
        for (double weight : weights.values()) {
            keptTotal += weight;
        }

        RegistryAccess access = player.level().getServer().registryAccess();
        List<ThemeRef> candidates = new ArrayList<>();
        final double normalise = keptTotal;
        access.lookupOrThrow(InnerWorldProfiles.REGISTRY).listElements().forEach(holder -> {
            InnerWorldProfile profile = holder.value();
            Double pathWeight = weights.get(profile.path());
            if (pathWeight == null || profile.variants().isEmpty()) {
                return;
            }
            double share = pathWeight / normalise * profile.weightMultiplier();
            double variantTotal = 0.0;
            for (InnerWorldProfile.Variant variant : profile.variants()) {
                variantTotal += Math.max(0.0001, variant.weight());
            }
            for (int i = 0; i < profile.variants().size(); i++) {
                ThemeRef ref = new ThemeRef();
                ref.profile = holder.key().identifier().toString();
                ref.variant = i;
                ref.weight = share * Math.max(0.0001, profile.variants().get(i).weight()) / variantTotal;
                candidates.add(ref);
            }
        });

        Seed seed = new Seed();
        seed.value = ThreadLocalRandom.current().nextLong() ^ player.getUUID().getLeastSignificantBits();
        if (candidates.isEmpty()) {
            // Paths exist but none ship a profile yet: an empty theme list makes SeededWorld use its built-in fallback.
            return seed;
        }

        SplittableRandom random = new SplittableRandom(seed.value);
        int target = Math.min(3 + random.nextInt(3), candidates.size());

        ThemeRef core = candidates.get(0);
        for (ThemeRef candidate : candidates) {
            if (candidate.weight > core.weight) {
                core = candidate;
            }
        }
        seed.themes.add(core);
        candidates.remove(core);

        while (seed.themes.size() < target && !candidates.isEmpty()) {
            double sum = 0.0;
            for (ThemeRef candidate : candidates) {
                sum += candidate.weight;
            }
            double roll = random.nextDouble() * sum;
            ThemeRef chosen = candidates.get(candidates.size() - 1);
            for (ThemeRef candidate : candidates) {
                roll -= candidate.weight;
                if (roll <= 0.0) {
                    chosen = candidate;
                    break;
                }
            }
            seed.themes.add(chosen);
            candidates.remove(chosen);
        }
        return seed;
    }
}