package net.zic.ascension.mob_cultivation.generation;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.core.CoreRegistries;
import net.zic.ascension.api.core.path.Path;
import net.zic.ascension.impl.core.path.foundation.FoundationPath;
import net.zic.ascension.mob_cultivation.MobCultivationData;
import net.zic.ascension.mob_cultivation.MobCultivationManager;
import net.zic.ascension.mob_cultivation.profile.ResolvedMobCultivationProfile;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class MobCultivationSubPathGenerator {
    public static final Identifier FIRE = AscensionCraft.prefix("elemental/fire");
    public static final Identifier WATER = AscensionCraft.prefix("elemental/water");
    public static final Identifier EARTH = AscensionCraft.prefix("elemental/earth");
    public static final Identifier METAL = AscensionCraft.prefix("elemental/metal");
    public static final Identifier WOOD = AscensionCraft.prefix("elemental/wood");
    public static final Identifier ICE = AscensionCraft.prefix("elemental/ice");
    public static final Identifier LIGHTNING = AscensionCraft.prefix("elemental/lightning");
    public static final Identifier POISON = AscensionCraft.prefix("elemental/poison");
    public static final Identifier DEATH = AscensionCraft.prefix("elemental/death");
    public static final Identifier DARK = AscensionCraft.prefix("elemental/dark");
    public static final Identifier SPACE = AscensionCraft.prefix("elemental/space");
    public static final Identifier YIN = AscensionCraft.prefix("elemental/yin");
    public static final Identifier SWORD = AscensionCraft.prefix("weapon/sword");
    public static final Identifier BOW = AscensionCraft.prefix("weapon/bow");
    public static final Identifier SPEAR = AscensionCraft.prefix("weapon/spear");
    public static final Identifier FIST = AscensionCraft.prefix("weapon/fist");
    public static final Identifier AXE = AscensionCraft.prefix("weapon/axe");
    public static final Identifier MACE = AscensionCraft.prefix("weapon/mace");

    private MobCultivationSubPathGenerator() {
    }

    public static void generate(Mob mob, MobCultivationData data, ResolvedMobCultivationProfile profile) {
        Map<Identifier, Double> weights = new LinkedHashMap<>(profile.subPathWeights());
        addFoundationDefaults(data.getFoundationPath(), weights);
        addEnvironmentalWeights(mob, weights);
        weights.entrySet().removeIf(entry -> !isValidSubPath(mob, entry.getKey()) || entry.getValue() <= 0.0D);

        int min = Math.min(profile.minimumSubPaths(), weights.size());
        int max = Math.min(Math.max(min, profile.maximumSubPaths()), weights.size());
        int count = min >= max ? min : min + mob.getRandom().nextInt(max - min + 1);
        List<Identifier> selected = new ArrayList<>();
        for (int i = 0; i < count && !weights.isEmpty(); i++) {
            Identifier path = chooseWeighted(mob.getRandom(), weights);
            selected.add(path);
            weights.remove(path);
        }
        data.setSubPaths(selected);
    }

    public static boolean isValidSubPath(Mob mob, Identifier id) {
        Path path = CoreRegistries.safeAccess(CoreRegistries.PATH_REGISTRY, id, mob.registryAccess());
        return path != null && !(path instanceof FoundationPath);
    }

    private static void addFoundationDefaults(Identifier foundation, Map<Identifier, Double> weights) {
        if (MobCultivationManager.BODY_PATH.equals(foundation)) {
            add(weights, EARTH, 1.5D);
            add(weights, METAL, 1.25D);
            add(weights, FIST, 1.5D);
        } else if (MobCultivationManager.ESSENCE_PATH.equals(foundation)) {
            add(weights, FIRE, 1.0D);
            add(weights, WATER, 1.0D);
            add(weights, LIGHTNING, 0.8D);
        } else if (MobCultivationManager.SOUL_PATH.equals(foundation)) {
            add(weights, YIN, 1.0D);
            add(weights, DEATH, 0.8D);
            add(weights, SPACE, 0.6D);
        }
    }

    private static void addEnvironmentalWeights(Mob mob, Map<Identifier, Double> weights) {
        if (mob.level().dimension() == Level.NETHER) {
            add(weights, FIRE, 3.0D);
            add(weights, METAL, 0.75D);
        } else if (mob.level().dimension() == Level.END) {
            add(weights, SPACE, 3.0D);
            add(weights, DARK, 1.5D);
        }
        if (mob.isInWater()) {
            add(weights, WATER, 2.0D);
        }

        String biomePath = mob.level().getBiome(mob.blockPosition())
                .unwrapKey()
                .map(key -> key.identifier().getPath())
                .orElse("");
        if (containsAny(biomePath, "snow", "frozen", "ice", "cold")) add(weights, ICE, 2.5D);
        if (containsAny(biomePath, "desert", "badlands", "savanna")) add(weights, FIRE, 1.5D);
        if (containsAny(biomePath, "swamp", "mangrove")) add(weights, POISON, 1.75D);
        if (containsAny(biomePath, "forest", "jungle", "grove")) add(weights, WOOD, 1.5D);
        if (containsAny(biomePath, "mountain", "peak", "hills")) add(weights, EARTH, 1.5D);

        Identifier heldId = BuiltInRegistries.ITEM.getKey(mob.getMainHandItem().getItem());
        String held = heldId == null ? "" : heldId.getPath();
        if (held.contains("bow")) add(weights, BOW, 4.0D);
        if (held.contains("sword")) add(weights, SWORD, 4.0D);
        if (held.contains("spear") || held.contains("trident")) add(weights, SPEAR, 4.0D);
        if (held.contains("axe")) add(weights, AXE, 4.0D);
        if (held.contains("mace") || held.contains("hammer")) add(weights, MACE, 4.0D);
    }

    private static boolean containsAny(String value, String... fragments) {
        for (String fragment : fragments) if (value.contains(fragment)) return true;
        return false;
    }

    private static void add(Map<Identifier, Double> weights, Identifier id, double amount) {
        weights.merge(id, amount, Double::sum);
    }

    private static Identifier chooseWeighted(RandomSource random, Map<Identifier, Double> weights) {
        double total = weights.values().stream().mapToDouble(Double::doubleValue).sum();
        double roll = random.nextDouble() * total;
        for (Map.Entry<Identifier, Double> entry : weights.entrySet()) {
            roll -= entry.getValue();
            if (roll <= 0.0D) return entry.getKey();
        }
        return weights.keySet().stream().reduce((a, b) -> b).orElse(FIST);
    }
}
