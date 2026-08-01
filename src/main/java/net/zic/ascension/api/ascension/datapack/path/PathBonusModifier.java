package net.zic.ascension.api.ascension.datapack.path;

import com.mojang.serialization.Codec;
import net.minecraft.resources.Identifier;
import net.zic.zenithlib.value_containers.ValueContainerModifier;

import java.util.*;

public record PathBonusModifier(Identifier category,Identifier path,ValueContainerModifier modifier){


    public static Codec<List<PathBonusModifier>> CODEC =    Codec.unboundedMap(
            Identifier.CODEC,
            Codec.unboundedMap(Identifier.CODEC,ValueContainerModifier.CODEC.listOf())
    ).xmap(
            raw->{
                List<PathBonusModifier> modifiers = new ArrayList<>();
                for(Identifier category : raw.keySet()){
                    for(Identifier path : raw.get(category).keySet()){
                        for(ValueContainerModifier modifier : raw.get(category).get(path)){
                            modifiers.add(new PathBonusModifier(category,path,modifier));
                        }
                    }
                }
                return modifiers;
            },
            rawInput->{
                Map<Identifier,Map<Identifier,List<ValueContainerModifier>>> outputMap = new HashMap<>();

                for(PathBonusModifier modifier : rawInput){
                    outputMap.computeIfAbsent(modifier.category,key->new HashMap<>())
                            .computeIfAbsent(modifier.category,key->new ArrayList<>())
                            .add(modifier.modifier);
                }
                return outputMap;
            }
    );


}
