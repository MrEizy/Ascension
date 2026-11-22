package net.zic.ascension.configuration.mobs;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.entity.Mob;
import net.zic.ascension.configuration.mobs.condition.WeightedCondition;

import java.util.List;

public record MobTraitPoolEntry(String id, int baseWeight, List<WeightedCondition> conditions,
                                List<PotentialTrait> traits) {
    public static final Codec<MobTraitPoolEntry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("id").forGetter(MobTraitPoolEntry::id),
            Codec.INT.fieldOf("base_weight").forGetter(MobTraitPoolEntry::baseWeight),
            WeightedCondition.CODEC.listOf().optionalFieldOf("conditions", List.of()).forGetter(MobTraitPoolEntry::conditions),
            PotentialTrait.CODEC.listOf().optionalFieldOf("traits", List.of()).forGetter(MobTraitPoolEntry::traits)
    ).apply(instance, MobTraitPoolEntry::new));

    public long getWeight(Mob mob) {
        long weight = baseWeight;
        for (WeightedCondition condition : conditions) {
            if (condition.condition().test(mob)) weight += condition.weight();
        }
        return Math.max(0L, weight);
    }
}
