package net.zic.ascension.api.ascension.core.progression;

import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import net.minecraft.resources.Identifier;
import net.zic.ascension.api.ascension.core.RegistryObjectData;
import net.zic.ascension.api.ascension.core.source.AscensionOriginSource;
import net.zic.ascension.api.ascension.datapack.progresison.ProgressActionConditionType;
import net.zic.ascension.api.ascension.datapack.progresison.ProgressActionType;

import java.util.*;

/**
 * Holds a list of conditions + list of actions for that condition
 */
public record ProgressActionHolder(UUID holderId, List<Pair<ProgressActionConditionReference, List<ProgressActionReference>>> listeners) {


    public static ProgressActionHolder from(List<Pair<ProgressActionConditionReference, List<ProgressActionReference>>> listeners){
        return new ProgressActionHolder(UUID.randomUUID(),listeners);
    }

    public void run(AscensionOriginSource source, Identifier contextIdentifier, RegistryObjectData contextData, ProgressDirection direction){
        for(Pair<ProgressActionConditionReference, List<ProgressActionReference>> listener : listeners){
            ProgressActionConditionReference condition = listener.getFirst();
            if(condition.resolve(source.getRegistryAccess()) == null) continue;
            if(!condition.resolve(source.getRegistryAccess()).test(
                    source,
                    contextIdentifier,
                    contextData,
                    direction
            )) continue;
            for(ProgressActionReference action : listener.getSecond()){
                if(action.resolve(source.getRegistryAccess()) == null) continue;

                action.resolve(source.getRegistryAccess()).run(
                        holderId,
                        source,
                        contextIdentifier,
                        contextData,
                        direction
                );
            }
        }
    }
    public static final Codec<ProgressActionReference> ACTION_REFERENCE_CODEC =
            Codec.either(
                    Identifier.CODEC,
                    ProgressActionType.PROGRESS_ACTION_CODEC
            ).xmap(
                    either -> either.map(
                            ProgressActionReference.RegistryReference::new,
                            ProgressActionReference.InPlace::new
                    ),
                    wrapper -> {
                        if (wrapper instanceof ProgressActionReference.RegistryReference(Identifier id)) {
                            return Either.left(id);
                        }

                        if (wrapper instanceof ProgressActionReference.InPlace(ProgressAction action)) {
                            return Either.right(action);
                        }

                        throw new IllegalStateException("Unknown ProgressActionReference type");
                    }
            );
    public static final Codec<ProgressActionConditionReference> CONDITION_REFERENCE_CODEC =
            Codec.either(
                    Identifier.CODEC,
                    ProgressActionConditionType.PROGRESS_ACTION_CONDITION_CODEC
            ).xmap(
                    either-> either.map(
                            ProgressActionConditionReference.RegistryReference::new,
                            ProgressActionConditionReference.InPlace::new
                    ),
                    wrapper -> {
                        if (wrapper instanceof ProgressActionConditionReference.RegistryReference(Identifier id)) {
                            return Either.left(id);
                        }

                        if (wrapper instanceof ProgressActionConditionReference.InPlace(ProgressActionCondition condition)) {
                            return Either.right(condition);
                        }

                        throw new IllegalStateException("Unknown ProgressActionConditionReference type");
                    }
            );
    public static final Codec<ProgressActionHolder>
            PROGRESS_HOLDER_CODEC = Codec.pair(
                CONDITION_REFERENCE_CODEC.fieldOf("condition").codec(),
                ACTION_REFERENCE_CODEC.listOf().fieldOf("actions").codec()
            ).listOf()
            .xmap(
                    ProgressActionHolder::from,
                    ProgressActionHolder::listeners
            );;
}
