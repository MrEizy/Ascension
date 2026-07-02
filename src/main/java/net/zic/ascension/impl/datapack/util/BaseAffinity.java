package net.zic.ascension.impl.datapack.util;

import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.core.path.PathEffectValueUtil;
import net.zic.zenithlib.value_containers.ValueContainer;
import net.zic.zenithlib.value_containers.ValueContainerModifier;

import java.util.*;
import java.util.stream.Collectors;

public record BaseAffinity(Identifier path, Identifier category,double value){


    public static final Codec<List<BaseAffinity>> CODEC =Codec.unboundedMap(
            Identifier.CODEC,
            Codec.either(
                    Codec.unboundedMap(Identifier.CODEC,Codec.DOUBLE).xmap(
                            rawMap->rawMap.entrySet().stream().map(entry->new Pair<>(entry.getKey(),entry.getValue())).toList(),
                            entries->entries.stream().collect(Collectors.toMap(
                                    Pair::getFirst,
                                    Pair::getSecond
                            ))
                    ),
                    Codec.DOUBLE
            ).xmap(raw->raw.map(multiple->multiple,single->List.of(new Pair<>(PathEffectValueUtil.NO_CATEGORY,single))),
                    Either::left)

    ).xmap(
            raw->{
                ArrayList<BaseAffinity> affinities = new ArrayList<>();
                for(Identifier path : raw.keySet()){
                    List<Pair<Identifier,Double>> val = raw.get(path);
                    affinities.addAll(val.stream().map(pair->new BaseAffinity(path,pair.getFirst(),pair.getSecond())).toList());
                }
                return affinities;
            },
            raw->{
                HashMap<Identifier,List<Pair<Identifier,Double>>> finalMap = new HashMap<>();
                for(BaseAffinity affinity : raw){
                    finalMap.computeIfAbsent(affinity.path,key->new ArrayList<>());

                    finalMap.get(affinity.path).add(new Pair<>(affinity.category, affinity.value));
                }
                return finalMap;
            }
    );

}
