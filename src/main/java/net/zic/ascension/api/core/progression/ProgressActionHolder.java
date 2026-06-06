package net.zic.ascension.api.core.progression;

import com.mojang.serialization.Codec;
import net.minecraft.resources.Identifier;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.core.CoreRegistries;
import net.zic.ascension.api.core.RegistryObjectData;
import net.zic.ascension.api.core.source.OriginSource;

import java.util.*;

/**
 * Holds a map of testConditions -> list of actions as well as a unique holder ID that can be used
 * in modifier Ids if needed
 *
 * note, the same action should NEVER be used TWICE inside the same context with the same progression, as this will cause
 * duplicate keys, as long as internal context (e.g different realms) is different it is fine
 */
public record ProgressActionHolder(UUID holderId, Map<Identifier, List<Identifier>> listeners) {
    public static final Codec<Map<Identifier, List<Identifier>>> CODEC = Codec.unboundedMap(Identifier.CODEC, Identifier.CODEC.listOf());
    public static ProgressActionHolder fromMap(Map<Identifier, List<Identifier>> listeners){
        return new ProgressActionHolder(UUID.randomUUID(),listeners);
    }

    public void run(OriginSource source, Identifier contextIdentifier, RegistryObjectData contextData, ProgressDirection direction){
        for(Identifier condition:listeners.keySet()){
            ProgressActionCondition conditionInstance = CoreRegistries.safeAccess(CoreRegistries.PROGRESS_ACTION_CONDITION_REGISTRY,condition,source.getRegistryAccess());
            AscensionCraft.LOGGER.debug("Running condition : {}", condition);
            if(conditionInstance == null || !conditionInstance.test(source,contextIdentifier,contextData,direction)) continue;
            AscensionCraft.LOGGER.debug("Running actions");
            for(Identifier action : listeners.get(condition)){
                AscensionCraft.LOGGER.debug("Running action {}",action);
                ProgressAction actionInstance = CoreRegistries.safeAccess(CoreRegistries.PROGRESS_ACTION_REGISTRY,action,source.getRegistryAccess());
                if(actionInstance == null) continue;
                actionInstance.run(holderId,source,contextIdentifier,contextData,direction);
            }
        }
    }
}
