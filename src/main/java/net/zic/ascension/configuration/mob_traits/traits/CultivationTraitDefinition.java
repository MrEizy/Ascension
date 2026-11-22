package net.zic.ascension.configuration.mob_traits.traits;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Mob;
import net.zic.ascension.api.rpg_engine.source.OriginSource;
import net.zic.ascension.configuration.mob_traits.MobTraitDefinition;
import net.zic.ascension.configuration.mob_traits.MobTraitDefinitionType;
import net.zic.ascension.configuration.mob_traits.MobTraitRoll;
import net.zic.ascension.configuration.mob_traits.RollableMobTraitDefinition;
import net.zic.ascension.configuration.mob_traits.traits.cultivation_traits.PotentialRealmDefinition;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.ToLongFunction;

public record CultivationTraitDefinition(Component name, List<WeightedPath> foundationPaths,
                                         List<WeightedPath> subpaths, RollRange subpathCount,
                                         List<SkillPool> skillPools) implements MobTraitDefinition, RollableMobTraitDefinition {

    public record RollRange(int min, int max) {
        public static final Codec<RollRange> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.intRange(0, 32).fieldOf("min").forGetter(RollRange::min),
                Codec.intRange(0, 32).fieldOf("max").forGetter(RollRange::max)
        ).apply(instance, RollRange::new));

        public int roll(RandomSource random) {
            int minimum = Math.min(min, max);
            int maximum = Math.max(min, max);
            return minimum == maximum ? minimum : minimum + random.nextInt(maximum - minimum + 1);
        }
    }

    public record WeightedPath(Identifier path, int weight, List<PotentialRealmDefinition> realms) {
        public static final Codec<WeightedPath> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Identifier.CODEC.fieldOf("path").forGetter(WeightedPath::path),
                Codec.intRange(0, Integer.MAX_VALUE).optionalFieldOf("weight", 1).forGetter(WeightedPath::weight),
                PotentialRealmDefinition.CODEC.listOf().optionalFieldOf("realms", List.of()).forGetter(WeightedPath::realms)
        ).apply(instance, WeightedPath::new));
    }

    public record WeightedSkill(Identifier skill, int weight, int minimumRealmScore, Optional<Identifier> requiredPath) {
        public static final Codec<WeightedSkill> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Identifier.CODEC.fieldOf("skill").forGetter(WeightedSkill::skill),
                Codec.intRange(0, Integer.MAX_VALUE).optionalFieldOf("weight", 1).forGetter(WeightedSkill::weight),
                Codec.intRange(0, Integer.MAX_VALUE).optionalFieldOf("minimum_realm_score", 0).forGetter(WeightedSkill::minimumRealmScore),
                Identifier.CODEC.optionalFieldOf("requires_path").forGetter(WeightedSkill::requiredPath)
        ).apply(instance, WeightedSkill::new));
    }

    public record SkillPool(Identifier id, RollRange rolls, List<WeightedSkill> entries) {
        public static final Codec<SkillPool> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Identifier.CODEC.fieldOf("id").forGetter(SkillPool::id),
                RollRange.CODEC.fieldOf("rolls").forGetter(SkillPool::rolls),
                WeightedSkill.CODEC.listOf().fieldOf("entries").forGetter(SkillPool::entries)
        ).apply(instance, SkillPool::new));
    }

    @Override
    public MobTraitDefinitionType getType() {
        return AscensionTraitTypes.CULTIVATION_TRAIT_TYPE.get();
    }

    @Override
    public void applyToSource(OriginSource source, Identifier owner) {
    }

    @Override
    public void applyToMob(Mob mob) {
    }

    @Override
    public void removeFromSource(OriginSource source, Identifier owner) {
    }

    @Override
    public void removeFromMob(Mob mob) {
    }

    @Override
    public MobTraitRoll roll(RandomSource random) {
        return roll(random, foundationPaths, subpaths, subpathCount, skillPools);
    }

    public static MobTraitRoll roll(RandomSource random, List<WeightedPath> foundationPaths,
                                    List<WeightedPath> subpaths, RollRange subpathCount,
                                    List<SkillPool> skillPools) {
        Map<Identifier, MobTraitRoll.PathRoll> paths = new LinkedHashMap<>();
        WeightedPath foundation = select(foundationPaths, random, WeightedPath::weight);
        if (foundation != null) addPath(paths, foundation, random);

        List<WeightedPath> available = new ArrayList<>(subpaths);
        int count = subpathCount.roll(random);
        for (int i = 0; i < count; i++) {
            available.removeIf(path -> paths.containsKey(path.path()));
            WeightedPath selected = select(available, random, WeightedPath::weight);
            if (selected == null) break;
            addPath(paths, selected, random);
            available.removeIf(path -> path.path().equals(selected.path()));
        }

        List<Identifier> skills = new ArrayList<>();
        for (SkillPool pool : skillPools) {
            List<WeightedSkill> availableSkills = new ArrayList<>(pool.entries());
            int rolls = pool.rolls().roll(random);
            for (int i = 0; i < rolls; i++) {
                availableSkills.removeIf(entry -> skills.contains(entry.skill())
                        || entry.requiredPath().isPresent() && !paths.containsKey(entry.requiredPath().get())
                        || entry.minimumRealmScore() > realmScore(paths, entry.requiredPath()));
                WeightedSkill selected = select(availableSkills, random, WeightedSkill::weight);
                if (selected == null) break;
                skills.add(selected.skill());
                availableSkills.removeIf(entry -> entry.skill().equals(selected.skill()));
            }
        }
        return new MobTraitRoll(new ArrayList<>(paths.values()), skills);
    }

    private static int realmScore(Map<Identifier, MobTraitRoll.PathRoll> paths, Optional<Identifier> requiredPath) {
        if (requiredPath.isPresent()) {
            MobTraitRoll.PathRoll path = paths.get(requiredPath.get());
            return path == null ? -1 : path.majorRealm() * 3 + path.minorRealm();
        }
        return paths.values().stream().mapToInt(path -> path.majorRealm() * 3 + path.minorRealm()).max().orElse(0);
    }

    private static void addPath(Map<Identifier, MobTraitRoll.PathRoll> selected, WeightedPath option, RandomSource random) {
        List<PotentialRealmDefinition> validRealms = option.realms().stream()
                .filter(realm -> realm.weight() > 0 && realm.realm() >= 0
                        && realm.minMinorRealm() >= 0 && realm.maxMinorRealm() >= realm.minMinorRealm())
                .toList();
        PotentialRealmDefinition realm = select(validRealms, random, PotentialRealmDefinition::weight);
        int major = 0;
        int minor = 0;
        if (realm != null && realm.realm() >= 0 && realm.minMinorRealm() >= 0
                && realm.maxMinorRealm() >= realm.minMinorRealm()) {
            major = realm.realm();
            minor = realm.minMinorRealm() + (int) boundedLong(random,
                    (long) realm.maxMinorRealm() - realm.minMinorRealm() + 1);
        }
        selected.put(option.path(), new MobTraitRoll.PathRoll(option.path(), major, minor));
    }

    private static <T> T select(List<T> entries, RandomSource random, ToLongFunction<T> weight) {
        long total = 0;
        for (T entry : entries) {
            long value = Math.max(0, weight.applyAsLong(entry));
            total = total > Long.MAX_VALUE - value ? Long.MAX_VALUE : total + value;
        }
        if (total == 0) return null;
        long roll = boundedLong(random, total);
        long cumulative = 0;
        for (T entry : entries) {
            long value = Math.max(0, weight.applyAsLong(entry));
            cumulative = cumulative > Long.MAX_VALUE - value ? Long.MAX_VALUE : cumulative + value;
            if (value > 0 && roll < cumulative) return entry;
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
