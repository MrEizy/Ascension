package net.zic.ascension.api.core.progression;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.Identifier;
import net.zic.ascension.api.core.CoreRegistries;
import net.zic.ascension.api.datapack.progresison.ProgressActionType;

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
