package net.thejadeproject.ascension.refactor_packages.bloodlines;

import net.minecraft.resources.ResourceLocation;

import java.util.HashSet;
import java.util.Set;

public class BloodlineSuppression {
    public static final int NO_TIER = 0;

    public static final double SUPPRESSION_RANGE = 16.0;

    public static final int CHECK_INTERVAL_TICKS = 20;

    public int tier = NO_TIER;

    private final Set<ResourceLocation> specificTargets = new HashSet<>();

    public BloodlineSuppression setTier(int tier) {
        this.tier = tier;
        return this;
    }

    public BloodlineSuppression addSpecificTarget(ResourceLocation bloodlineId) {
        specificTargets.add(bloodlineId);
        return this;
    }

    public int getTier() {
        return tier;
    }

    public Set<ResourceLocation> getSpecificTargets() {
        return specificTargets;
    }

    public boolean suppresses(ResourceLocation targetBloodlineKey, int targetTier) {
        if (tier != NO_TIER && targetTier != NO_TIER && targetTier < tier) return true;
        return specificTargets.contains(targetBloodlineKey);
    }

    public boolean hasSuppression() {
        return tier != NO_TIER || !specificTargets.isEmpty();
    }
}
