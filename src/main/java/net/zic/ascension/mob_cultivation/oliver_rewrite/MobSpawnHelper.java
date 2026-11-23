package net.zic.ascension.mob_cultivation.oliver_rewrite;

import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Mob;
import net.zic.ascension.common.data_attachements.AscensionAttachments;
import net.zic.ascension.configuration.ConfigurationDataMaps;
import net.zic.ascension.configuration.mob_traits.MobTraitReference;
import net.zic.ascension.configuration.mob_traits.MobTraitDefinition;
import net.zic.ascension.configuration.mob_traits.MobTraitRoll;
import net.zic.ascension.configuration.mob_traits.RollableMobTraitDefinition;
import net.zic.ascension.configuration.mobs.MobConfiguration;
import net.zic.ascension.configuration.mobs.MobCultivationRules;
import net.zic.ascension.configuration.mobs.MobFallbackConfigurations;
import net.zic.ascension.configuration.mobs.MobTraitPool;
import net.zic.ascension.configuration.mobs.MobTraitPoolEntry;
import net.zic.ascension.configuration.mobs.PotentialTrait;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MobSpawnHelper {
    public static boolean hasConfig(Mob mob) {
        return getConfig(mob) != null;
    }

    public static MobConfiguration getConfig(Mob mob) {
        MobConfiguration explicit = mob.getData(ConfigurationDataMaps.MOB_CONFIGURATION);
        return explicit != null ? explicit : MobFallbackConfigurations.get(mob);
    }

    public static boolean hasExplicitConfig(Mob mob) {
        return mob.getData(ConfigurationDataMaps.MOB_CONFIGURATION) != null;
    }

    public static boolean hasConfigHolder(Mob mob) {
        return mob.hasData(AscensionAttachments.MOB_CONFIG_HOLDER);
    }

    public static MobConfigurationHolder getConfigHolder(Mob mob) {
        return mob.getData(AscensionAttachments.MOB_CONFIG_HOLDER);
    }

    public static void buildConfigurationInstance(Mob mob) {
        if (mob.level().isClientSide() || hasConfigHolder(mob)) return;
        MobConfiguration configuration = getConfig(mob);
        if (configuration == null) return;
        getConfigHolder(mob).setMobConfigurationInstance(generateInstance(configuration, mob));
    }

    public static void applyConfigurationInstance(Mob mob) {
        if (mob.level().isClientSide() || !hasConfigHolder(mob)) return;
        getConfigHolder(mob).applyConfigurationToEntity();
    }

    public static MobConfigurationInstance generateInstance(MobConfiguration configuration, Mob mob) {
        return generateInstance(configuration, mob, mob.getRandom(), Map.of());
    }

    public static MobConfigurationInstance generateInstance(MobConfiguration configuration, Mob mob,
                                                             RandomSource random, Map<Identifier, String> forcedEntries) {
        List<MobTraitReference> traits = new ArrayList<>();
        List<MobConfigurationInstance.PoolSelection> selections = new ArrayList<>();
        addTraits(configuration.traits(), mob, random, traits);

        for (MobTraitPool pool : configuration.pools()) {
            Set<String> selected = new HashSet<>();
            String forced = forcedEntries.get(pool.id());
            if (forced != null && pool.rolls() > 0) {
                for (MobTraitPoolEntry entry : pool.entries()) {
                    if (forced.equals(entry.id())) {
                        addEntry(pool, entry, mob, random, traits, selections);
                        selected.add(entry.id());
                        break;
                    }
                }
            }
            for (int roll = selected.isEmpty() ? 0 : 1; roll < pool.rolls(); roll++) {
                MobTraitPoolEntry entry = rollEntry(pool, mob, random, selected);
                if (entry == null) break;
                addEntry(pool, entry, mob, random, traits, selections);
                selected.add(entry.id());
            }
        }
        List<ResolvedMobTrait> rolledTraits = new ArrayList<>();
        for (MobTraitReference trait : traits) {
            MobTraitDefinition definition = trait.getTrait(mob.registryAccess());
            if (definition instanceof RollableMobTraitDefinition rollable) {
                MobTraitRoll roll = rollable.roll(random);
                if (!roll.isEmpty()) rolledTraits.add(new ResolvedMobTrait(trait, roll));
            }
        }
        MobCultivationRules rules = configuration.cultivation();
        for (MobTraitPool pool : configuration.pools()) {
            for (MobConfigurationInstance.PoolSelection selection : selections) {
                if (!selection.pool().equals(pool.id())) continue;
                for (MobTraitPoolEntry entry : pool.entries()) {
                    if (selection.entry().equals(entry.id()) && entry.cultivation().isPresent()) {
                        rules = entry.cultivation().get();
                    }
                }
            }
        }
        MobTraitRoll cultivation = rules.roll(random);
        return new MobConfigurationInstance(selections, traits, rolledTraits, cultivation, configuration.statBiases());
    }

    private static void addEntry(MobTraitPool pool, MobTraitPoolEntry entry, Mob mob, RandomSource random,
                                 List<MobTraitReference> traits,
                                 List<MobConfigurationInstance.PoolSelection> selections) {
        MobConfigurationInstance.PoolSelection selection = new MobConfigurationInstance.PoolSelection(pool.id(), entry.id());
        if (!selections.contains(selection)) selections.add(selection);
        addTraits(entry.traits(), mob, random, traits);
    }

    private static void addTraits(List<PotentialTrait> candidates, Mob mob, RandomSource random,
                                  List<MobTraitReference> traits) {
        for (PotentialTrait candidate : candidates) {
            if (candidate.test(mob) && random.nextDouble() < candidate.chance()
                    && !traits.contains(candidate.trait())) {
                traits.add(candidate.trait());
            }
        }
    }

    private static MobTraitPoolEntry rollEntry(MobTraitPool pool, Mob mob, RandomSource random,
                                                Set<String> selected) {
        long totalWeight = 0L;
        List<Long> weights = new ArrayList<>(pool.entries().size());
        for (MobTraitPoolEntry entry : pool.entries()) {
            long weight = pool.exclusive() && selected.contains(entry.id()) ? 0L : entry.getWeight(mob);
            weights.add(weight);
            totalWeight = totalWeight > Long.MAX_VALUE - weight ? Long.MAX_VALUE : totalWeight + weight;
        }
        if (totalWeight <= 0L) return null;

        long roll = boundedLong(random, totalWeight);
        long cumulativeWeight = 0L;
        for (int index = 0; index < pool.entries().size(); index++) {
            long weight = weights.get(index);
            cumulativeWeight = cumulativeWeight > Long.MAX_VALUE - weight ? Long.MAX_VALUE : cumulativeWeight + weight;
            if (weight > 0L && roll < cumulativeWeight) return pool.entries().get(index);
        }
        return null;
    }

    private static long boundedLong(RandomSource random, long bound) {
        long bits;
        long result;
        do {
            bits = random.nextLong() >>> 1;
            result = bits % bound;
        } while (bits - result + bound - 1L < 0L);
        return result;
    }
}
