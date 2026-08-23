package net.zic.ascension.configuration.mobs;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.entity.Mob;
import net.zic.ascension.configuration.mob_traits.MobTraitReference;
import net.zic.ascension.configuration.mobs.condition.MobConfigurationCondition;
import net.zic.ascension.configuration.mobs.condition.MobConfigurationConditionType;
import net.zic.ascension.configuration.mobs.condition.TierCondition;
import net.zic.ascension.mob_cultivation.generation.MobCultivationEliteTier;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public record PotentialTier(MobCultivationEliteTier tier, List<TierCondition> conditions, int baseWeight) {
    public static final Codec<MobCultivationEliteTier> TIER_CODEC =  Codec.STRING.comapFlatMap(
            tier -> {
                try {
                    return DataResult.success(MobCultivationEliteTier.valueOf(tier.toUpperCase()));
                } catch (IllegalArgumentException e) {
                    return DataResult.<MobCultivationEliteTier>error(() -> "Unknown target type: " + tier);
                }
            },
            MobCultivationEliteTier::name
    );
    public static final Codec<Pair<List<TierCondition>,Integer>> INTERMEDIATE_CODEC = RecordCodecBuilder.<Pair<List<TierCondition>,Integer>>create(
            instance->instance.group(
                    TierCondition.CODEC.listOf().optionalFieldOf("conditions",List.of()).forGetter(Pair<List<TierCondition>,Integer>::getFirst),
                    Codec.INT.fieldOf("base_weight").forGetter(Pair<List<TierCondition>,Integer>::getSecond)
            ).apply(instance, Pair<List<TierCondition>,Integer>::new)
    );
    public static final Codec<List<PotentialTier>> CODEC = Codec.unboundedMap(
            TIER_CODEC,
            INTERMEDIATE_CODEC
    ).xmap(raw->
        raw.entrySet().stream().map(entry->new PotentialTier(entry.getKey(),entry.getValue().getFirst(),entry.getValue().getSecond())).toList(),
            list->list.stream().collect(Collectors.toMap(PotentialTier::tier,val->new Pair<>(val.conditions,val.baseWeight)))
    );

    public int getWeight(Mob mob){
        int finalWeight = baseWeight;
        for(TierCondition condition : conditions){
            if(condition.condition().test(mob)) finalWeight += baseWeight;
        }
        return finalWeight;
    }
}
