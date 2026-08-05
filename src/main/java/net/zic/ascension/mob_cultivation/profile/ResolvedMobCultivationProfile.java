package net.zic.ascension.mob_cultivation.profile;

import net.minecraft.resources.Identifier;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.mob_cultivation.MobCultivationCategory;
import net.zic.ascension.mob_cultivation.MobCultivationManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class ResolvedMobCultivationProfile {
    private final Map<Identifier, Double> foundationPathWeights;
    private final Map<Identifier, Double> subPathWeights;
    private final int minimumSubPaths;
    private final int maximumSubPaths;
    private final Set<Identifier> traits;
    private final Map<Identifier, Double> eliteTraitWeights;
    private final List<Identifier> skillPools;
    private final Identifier lootProfile;
    private final MobCultivationEliteSettings eliteSettings;
    private final double statMultiplier;
    private final double vitalityBias;
    private final double strengthBias;
    private final double agilityBias;
    private final double spiritBias;
    private final double growthMultiplier;
    private final double atmosphericQiSensitivity;
    private final double lootMultiplier;
    private final boolean showParticles;
    private final Integer initialMajorRealmMin;
    private final Integer initialMajorRealmMax;
    private final Integer initialMinorRealmMin;
    private final Integer initialMinorRealmMax;

    private ResolvedMobCultivationProfile(Builder builder) {
        foundationPathWeights = Collections.unmodifiableMap(new LinkedHashMap<>(builder.foundationPathWeights));
        subPathWeights = Collections.unmodifiableMap(new LinkedHashMap<>(builder.subPathWeights));
        minimumSubPaths = builder.minimumSubPaths;
        maximumSubPaths = Math.max(builder.minimumSubPaths, builder.maximumSubPaths);
        traits = Collections.unmodifiableSet(new LinkedHashSet<>(builder.traits));
        eliteTraitWeights = Collections.unmodifiableMap(new LinkedHashMap<>(builder.eliteTraitWeights));
        skillPools = Collections.unmodifiableList(new ArrayList<>(builder.skillPools));
        lootProfile = builder.lootProfile;
        eliteSettings = builder.eliteSettings;
        statMultiplier = builder.category.statMultiplier() * builder.statMultiplier;
        vitalityBias = builder.vitalityBias;
        strengthBias = builder.strengthBias;
        agilityBias = builder.agilityBias;
        spiritBias = builder.spiritBias;
        growthMultiplier = builder.category.growthMultiplier() * builder.growthMultiplier;
        atmosphericQiSensitivity = builder.atmosphericQiSensitivity;
        lootMultiplier = builder.lootMultiplier;
        showParticles = builder.showParticles;
        initialMajorRealmMin = builder.initialMajorRealmMin;
        initialMajorRealmMax = builder.initialMajorRealmMax;
        initialMinorRealmMin = builder.initialMinorRealmMin;
        initialMinorRealmMax = builder.initialMinorRealmMax;
    }

    public Map<Identifier, Double> foundationPathWeights() { return foundationPathWeights; }
    public Map<Identifier, Double> subPathWeights() { return subPathWeights; }
    public int minimumSubPaths() { return minimumSubPaths; }
    public int maximumSubPaths() { return maximumSubPaths; }
    public Set<Identifier> traits() { return traits; }
    public Map<Identifier, Double> eliteTraitWeights() { return eliteTraitWeights; }
    public List<Identifier> skillPools() { return skillPools; }
    public Identifier lootProfile() { return lootProfile; }
    public MobCultivationEliteSettings eliteSettings() { return eliteSettings; }
    public double statMultiplier() { return statMultiplier; }
    public double vitalityBias() { return vitalityBias; }
    public double strengthBias() { return strengthBias; }
    public double agilityBias() { return agilityBias; }
    public double spiritBias() { return spiritBias; }
    public double growthMultiplier() { return growthMultiplier; }
    public double atmosphericQiSensitivity() { return atmosphericQiSensitivity; }
    public double lootMultiplier() { return lootMultiplier; }
    public boolean showParticles() { return showParticles; }

    public boolean hasInitialRealmOverride() {
        return initialMajorRealmMin != null || initialMajorRealmMax != null
                || initialMinorRealmMin != null || initialMinorRealmMax != null;
    }
    public Integer initialMajorRealmMin() { return initialMajorRealmMin; }
    public Integer initialMajorRealmMax() { return initialMajorRealmMax; }
    public Integer initialMinorRealmMin() { return initialMinorRealmMin; }
    public Integer initialMinorRealmMax() { return initialMinorRealmMax; }

    static final class Builder {
        private final MobCultivationCategory category;
        private final Map<Identifier, Double> foundationPathWeights = new LinkedHashMap<>();
        private final Map<Identifier, Double> subPathWeights = new LinkedHashMap<>();
        private int minimumSubPaths = 1;
        private int maximumSubPaths = 1;
        private final Set<Identifier> traits = new LinkedHashSet<>();
        private final Map<Identifier, Double> eliteTraitWeights = new LinkedHashMap<>();
        private final List<Identifier> skillPools = new ArrayList<>();
        private Identifier lootProfile = AscensionCraft.prefix("default");
        private MobCultivationEliteSettings eliteSettings = MobCultivationEliteSettings.DEFAULT;
        private double statMultiplier = 1.0D;
        private double vitalityBias = 1.0D;
        private double strengthBias = 1.0D;
        private double agilityBias = 1.0D;
        private double spiritBias = 1.0D;
        private double growthMultiplier = 1.0D;
        private double atmosphericQiSensitivity = 1.0D;
        private double lootMultiplier = 1.0D;
        private boolean showParticles = true;
        private Integer initialMajorRealmMin;
        private Integer initialMajorRealmMax;
        private Integer initialMinorRealmMin;
        private Integer initialMinorRealmMax;

        Builder(MobCultivationCategory category) {
            this.category = category;
            eliteTraitWeights.put(AscensionCraft.prefix("regenerating"), 1.0D);
            eliteTraitWeights.put(AscensionCraft.prefix("armoured_body"), 1.0D);
            eliteTraitWeights.put(AscensionCraft.prefix("spiritual_body"), 0.7D);
            eliteTraitWeights.put(AscensionCraft.prefix("elemental_body"), 0.7D);
            eliteTraitWeights.put(AscensionCraft.prefix("pack_creature"), 0.35D);
            switch (category) {
                case PASSIVE -> {
                    foundationPathWeights.put(MobCultivationManager.BODY_PATH, 3.0D);
                    foundationPathWeights.put(MobCultivationManager.ESSENCE_PATH, 1.0D);
                    foundationPathWeights.put(MobCultivationManager.SOUL_PATH, 1.0D);
                }
                case HOSTILE, BOSS -> {
                    foundationPathWeights.put(MobCultivationManager.BODY_PATH, 1.0D);
                    foundationPathWeights.put(MobCultivationManager.ESSENCE_PATH, 1.0D);
                    foundationPathWeights.put(MobCultivationManager.SOUL_PATH, 1.0D);
                }
            }
        }

        void apply(MobCultivationProfile profile) {
            foundationPathWeights.putAll(profile.foundationPathWeights());
            subPathWeights.putAll(profile.subPathWeights());
            if (profile.minimumSubPaths() != null) minimumSubPaths = profile.minimumSubPaths();
            if (profile.maximumSubPaths() != null) maximumSubPaths = profile.maximumSubPaths();
            traits.addAll(profile.traits());
            eliteTraitWeights.putAll(profile.eliteTraitWeights());
            for (Identifier pool : profile.skillPools()) if (!skillPools.contains(pool)) skillPools.add(pool);
            if (profile.lootProfile() != null) lootProfile = profile.lootProfile();
            if (profile.eliteSettings() != null) eliteSettings = profile.eliteSettings();
            if (profile.statMultiplier() != null) statMultiplier = profile.statMultiplier();
            if (profile.vitalityBias() != null) vitalityBias = profile.vitalityBias();
            if (profile.strengthBias() != null) strengthBias = profile.strengthBias();
            if (profile.agilityBias() != null) agilityBias = profile.agilityBias();
            if (profile.spiritBias() != null) spiritBias = profile.spiritBias();
            if (profile.growthMultiplier() != null) growthMultiplier = profile.growthMultiplier();
            if (profile.atmosphericQiSensitivity() != null) atmosphericQiSensitivity = profile.atmosphericQiSensitivity();
            if (profile.lootMultiplier() != null) lootMultiplier = profile.lootMultiplier();
            if (profile.showParticles() != null) showParticles = profile.showParticles();
            if (profile.initialMajorRealmMin() != null) initialMajorRealmMin = profile.initialMajorRealmMin();
            if (profile.initialMajorRealmMax() != null) initialMajorRealmMax = profile.initialMajorRealmMax();
            if (profile.initialMinorRealmMin() != null) initialMinorRealmMin = profile.initialMinorRealmMin();
            if (profile.initialMinorRealmMax() != null) initialMinorRealmMax = profile.initialMinorRealmMax();
        }

        ResolvedMobCultivationProfile build() {
            foundationPathWeights.entrySet().removeIf(e -> e.getValue() == null || e.getValue() < 0.0D);
            subPathWeights.entrySet().removeIf(e -> e.getValue() == null || e.getValue() < 0.0D);
            eliteTraitWeights.entrySet().removeIf(e -> e.getValue() == null || e.getValue() <= 0.0D);
            if (foundationPathWeights.values().stream().noneMatch(v -> v > 0.0D)) {
                foundationPathWeights.clear();
                foundationPathWeights.put(MobCultivationManager.BODY_PATH, 1.0D);
                foundationPathWeights.put(MobCultivationManager.ESSENCE_PATH, 1.0D);
                foundationPathWeights.put(MobCultivationManager.SOUL_PATH, 1.0D);
            }
            minimumSubPaths = Math.max(0, minimumSubPaths);
            maximumSubPaths = Math.max(minimumSubPaths, maximumSubPaths);
            return new ResolvedMobCultivationProfile(this);
        }
    }
}
