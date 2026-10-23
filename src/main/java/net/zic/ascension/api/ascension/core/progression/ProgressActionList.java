package net.zic.ascension.api.ascension.core.progression;

import com.mojang.serialization.Codec;
import net.minecraft.resources.Identifier;
import net.zic.ascension.api.rpg_engine.source.OriginSource;

import java.util.List;
import java.util.UUID;

/**
 * Holds a list of actions that run without a condition
 */
public record ProgressActionList(UUID holderId, List<ProgressActionReference> actions) {
    public static final ProgressActionList EMPTY = from(List.of());

    public static final Codec<ProgressActionList> CODEC = ProgressActionHolder.ACTION_REFERENCE_CODEC.listOf()
            .xmap(ProgressActionList::from, ProgressActionList::actions);

    public static ProgressActionList from(List<ProgressActionReference> actions) {
        return new ProgressActionList(UUID.randomUUID(), actions == null ? List.of() : List.copyOf(actions));
    }

    public void run(OriginSource source, Identifier contextIdentifier, Object contextData, ProgressDirection direction, String salt) {
        for (ProgressActionReference reference : actions) {
            ProgressAction action = reference.resolve(source.getRegistryAccess());
            if (action == null) continue;
            action.run(holderId, source, contextIdentifier, contextData, direction, salt);
        }
    }
}
