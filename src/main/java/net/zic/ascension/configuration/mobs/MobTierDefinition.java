package net.zic.ascension.configuration.mobs;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.zic.ascension.api.ascension.core.path.interaction.PathInteractionType;
import net.zic.ascension.mob_cultivation.generation.MobCultivationEliteTier;

import java.util.List;
import java.util.stream.Collectors;

public record MobTierDefinition(MobCultivationEliteTier tier, int weight){

    public static final Codec<List<MobTierDefinition>> CODEC = Codec.unboundedMap(
            Codec.STRING.comapFlatMap(
                    tier -> {
                        try {
                            return DataResult.success(MobCultivationEliteTier.valueOf(tier.toUpperCase()));
                        } catch (IllegalArgumentException e) {
                            return DataResult.<MobCultivationEliteTier>error(() -> "Unknown target type: " + tier);
                        }
                    },
                    MobCultivationEliteTier::name
            ),Codec.INT).xmap(
            rawInput ->
                    rawInput.entrySet().stream()
                        .map(entry -> new MobTierDefinition(entry.getKey(), entry.getValue()))
                        .collect(Collectors.toList())
            ,
            rawOutput-> rawOutput.stream().collect(Collectors.toMap(MobTierDefinition::tier,MobTierDefinition::weight))
    );
}
