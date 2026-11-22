package net.zic.ascension.mob_cultivation.oliver_rewrite;

import net.minecraft.world.entity.Mob;
import net.zic.ascension.common.data_attachements.AscensionAttachments;
import net.zic.ascension.configuration.ConfigurationDataMaps;
import net.zic.ascension.configuration.mob_traits.MobTraitReference;
import net.zic.ascension.configuration.mobs.MobConfiguration;
import net.zic.ascension.configuration.mobs.PotentialTier;
import net.zic.ascension.configuration.mobs.PotentialTrait;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class MobSpawnHelper {

    public static boolean hasConfig(Mob mob) {
        return mob.getData(ConfigurationDataMaps.MOB_CONFIGURATION) != null;
    }

    public static MobConfiguration getConfig(Mob mob) {
        return mob.getData(ConfigurationDataMaps.MOB_CONFIGURATION);
    }

    public static boolean hasConfigHolder(Mob mob) {
        return mob.hasData(AscensionAttachments.MOB_CONFIG_HOLDER);
    }

    public static MobConfigurationHolder getConfigHolder(Mob mob) {
        return mob.getData(AscensionAttachments.MOB_CONFIG_HOLDER);
    }

    public static void buildConfigurationInstance(Mob mob) {
        if (mob.level().isClientSide() || !hasConfig(mob) || hasConfigHolder(mob)) return;
        MobConfiguration configuration = getConfig(mob);
        getConfigHolder(mob).setMobConfigurationInstance(generateInstance(configuration, mob));
    }

    public static void applyConfigurationInstance(Mob mob) {
        if (mob.level().isClientSide() || !hasConfigHolder(mob)) return;
        getConfigHolder(mob).applyConfigurationToEntity();
    }

    public static MobCultivationEliteTier rollTier(Mob mob, MobConfiguration configuration) {
        long totalWeight = 0L;
        List<Long> weights = new ArrayList<>();
        for (PotentialTier definition : configuration.potentialTiers()) {
            long weight = definition.getWeight(mob);
            weights.add(weight);
            totalWeight += weight;
        }
        if (totalWeight <= 0L) return MobCultivationEliteTier.NORMAL;

        long roll = ThreadLocalRandom.current().nextLong(totalWeight);
        long cumulativeWeight = 0L;
        for (int index = 0; index < configuration.potentialTiers().size(); index++) {
            cumulativeWeight += weights.get(index);
            if (roll < cumulativeWeight) return configuration.potentialTiers().get(index).tier();
        }
        return MobCultivationEliteTier.NORMAL;
    }

    public static MobConfigurationInstance generateInstance(MobConfiguration configuration, Mob mob) {
        MobCultivationEliteTier tier = rollTier(mob, configuration);
        List<MobTraitReference> traits = new ArrayList<>();
        for (PotentialTrait potentialTrait : configuration.getPotentialTraits(tier)) {
            if (!potentialTrait.test(mob)) continue;
            if (ThreadLocalRandom.current().nextDouble() < potentialTrait.chance()
                    && !traits.contains(potentialTrait.trait())) {
                traits.add(potentialTrait.trait());
            }
        }
        return new MobConfigurationInstance(tier, traits);
    }
}
