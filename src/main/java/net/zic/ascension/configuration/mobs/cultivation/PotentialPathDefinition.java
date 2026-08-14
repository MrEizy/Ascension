package net.zic.ascension.configuration.mobs.cultivation;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import net.zic.ascension.configuration.mobs.MobTierDefinition;

import java.util.List;
import java.util.stream.Collectors;

//TODO figure out how dafuc im going to handle tribulations, could add potential tribulation results, which provides pre made completed tribulation
public record PotentialPathDefinition(Identifier path, List<PotentialRealmDefinition> potentialRealms,double chance){

    private record IntermediaryPathDefinition(List<PotentialRealmDefinition> potentialRealms,double chance){
        public static final Codec<IntermediaryPathDefinition> CODEC = RecordCodecBuilder.create(
                instance->instance.group(
                        PotentialRealmDefinition.CODEC.listOf().fieldOf("realms").forGetter(IntermediaryPathDefinition::potentialRealms),
                        Codec.DOUBLE.fieldOf("chance").forGetter(IntermediaryPathDefinition::chance)
                ).apply(instance,IntermediaryPathDefinition::new)
        );
    }
    private IntermediaryPathDefinition intermediaryPathDefinition(){
        return new IntermediaryPathDefinition(potentialRealms,chance);
    }
    public static final Codec<List<PotentialPathDefinition>> MAP_CODEC = Codec.unboundedMap(
            Identifier.CODEC,
            IntermediaryPathDefinition.CODEC
    ).xmap(
            rawInput-> rawInput.entrySet().stream()
            .map(entry -> new PotentialPathDefinition(entry.getKey(), entry.getValue().potentialRealms(),entry.getValue().chance()))
            .collect(Collectors.toList()),
            rawOutput-> rawOutput.stream().collect(Collectors.toMap(PotentialPathDefinition::path,PotentialPathDefinition::intermediaryPathDefinition))

    );
}
