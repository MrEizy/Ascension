package net.zic.ascension.api.core.progression;

import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.Identifier;
import net.zic.ascension.api.core.CoreRegistries;

public sealed interface ProgressActionConditionReference
        permits ProgressActionConditionReference.RegistryReference,
        ProgressActionConditionReference.InPlace {

    ProgressActionCondition resolve(RegistryAccess access);

    record RegistryReference(Identifier id) implements ProgressActionConditionReference {
        @Override
        public ProgressActionCondition resolve(RegistryAccess access) {
            return CoreRegistries.safeAccess(
                    CoreRegistries.PROGRESS_ACTION_CONDITION_REGISTRY,
                    id,
                    access
            );
        }
    }

    record InPlace(ProgressActionCondition condition) implements ProgressActionConditionReference {
        @Override
        public ProgressActionCondition resolve(RegistryAccess access) {
            return condition;
        }
    }


}
