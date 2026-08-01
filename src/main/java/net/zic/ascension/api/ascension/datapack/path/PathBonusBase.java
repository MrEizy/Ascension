package net.zic.ascension.api.ascension.datapack.path;

import com.mojang.serialization.Codec;
import net.minecraft.resources.Identifier;
import net.zic.zenithlib.value_containers.ValueContainerModifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record PathBonusBase(Identifier category,Identifier path,double value) {
    public static Codec<List<PathBonusBase>> CODEC =
    Codec.unboundedMap(
            Identifier.CODEC,
            Codec.unboundedMap(Identifier.CODEC, Codec.DOUBLE)
    ).xmap(
            raw->{
                List<PathBonusBase> modifiers = new ArrayList<>();
                for(Identifier category : raw.keySet()){
                    for(Identifier path : raw.get(category).keySet()){
                        modifiers.add(new PathBonusBase(category,path,raw.get(category).get(path)));
                    }
                }
                return modifiers;
            },
            rawInput->{
                Map<Identifier, Map<Identifier,Double>> outputMap = new HashMap<>();

                for(PathBonusBase base : rawInput){
                    outputMap.computeIfAbsent(base.category,key->new HashMap<>())
                            .put(base.path,base.value);
                }
                return outputMap;
            }
    );

}
