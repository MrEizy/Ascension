package net.zic.ascension.api.ascension.core.progression;

import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.Identifier;
import net.zic.ascension.api.ascension.core.CoreRegistries;

public sealed interface ProgressActionReference
        permits ProgressActionReference.RegistryReference,
        ProgressActionReference.InPlace {

    ProgressAction resolve(RegistryAccess access);

    record RegistryReference(Identifier id) implements ProgressActionReference {
        @Override
        public ProgressAction resolve(RegistryAccess access) {
            return CoreRegistries.safeAccess(
                    CoreRegistries.PROGRESS_ACTION_REGISTRY,
                    id,
                    access
            );
        }
    }

    record InPlace(ProgressAction action) implements ProgressActionReference {
        @Override
        public ProgressAction resolve(RegistryAccess access) {
            return action;
        }
    }


}
