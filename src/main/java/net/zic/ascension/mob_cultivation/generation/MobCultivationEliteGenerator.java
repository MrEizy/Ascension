package net.zic.ascension.mob_cultivation.generation;

import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Mob;
import net.zic.ascension.mob_cultivation.MobCultivationData;
import net.zic.ascension.mob_cultivation.profile.MobCultivationEliteSettings;
import net.zic.ascension.mob_cultivation.profile.ResolvedMobCultivationProfile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class MobCultivationEliteGenerator {
    private MobCultivationEliteGenerator() {
    }

    public static MobCultivationEliteTier rollTier(Mob mob, ResolvedMobCultivationProfile profile) {
        MobCultivationEliteSettings settings = profile.eliteSettings();
        if (!settings.enabled() || mob.getRandom().nextDouble() >= settings.chance()) {
            return MobCultivationEliteTier.NORMAL;
        }
        return mob.getRandom().nextDouble() < settings.ancientChance() ? MobCultivationEliteTier.ANCIENT : MobCultivationEliteTier.ELITE;
    }

    public static void addEliteTraits(Mob mob, MobCultivationData data, ResolvedMobCultivationProfile profile) {
        if (!data.getEliteTier().isElite() || profile.eliteTraitWeights().isEmpty()) {
            return;
        }
        int rolls = profile.eliteSettings().extraTraitRolls();
        if (data.getEliteTier() == MobCultivationEliteTier.ANCIENT) {
            rolls++;
        }
        Set<Identifier> current = data.getTraits();
        List<Map.Entry<Identifier, Double>> candidates = new ArrayList<>();
        for (Map.Entry<Identifier, Double> entry : profile.eliteTraitWeights().entrySet()) {
            if (entry.getValue() > 0.0D && !current.contains(entry.getKey())) {
                candidates.add(entry);
            }
        }
        for (int i = 0; i < rolls && !candidates.isEmpty(); i++) {
            Identifier chosen = chooseWeighted(mob.getRandom(), candidates);
            data.addTrait(chosen);
            candidates.removeIf(entry -> entry.getKey().equals(chosen));
        }
    }

    private static Identifier chooseWeighted(RandomSource random, List<Map.Entry<Identifier, Double>> entries) {
        double total = entries.stream().mapToDouble(Map.Entry::getValue).sum();
        double roll = random.nextDouble() * total;
        for (Map.Entry<Identifier, Double> entry : entries) {
            roll -= entry.getValue();
            if (roll <= 0.0D) return entry.getKey();
        }
        return entries.getLast().getKey();
    }
}
