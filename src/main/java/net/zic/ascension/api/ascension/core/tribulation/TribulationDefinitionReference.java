package net.zic.ascension.api.ascension.core.tribulation;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.Identifier;
import net.zic.ascension.api.ascension.core.CoreRegistries;
import net.zic.ascension.api.ascension.datapack.tribulation.TribulationType;

public sealed interface TribulationDefinitionReference
    permits TribulationDefinitionReference.RegistryReference,
            TribulationDefinitionReference.InPlace {

    TribulationDefinition resolve(RegistryAccess access);

    record RegistryReference(Identifier id) implements TribulationDefinitionReference {
        @Override
        public TribulationDefinition resolve(RegistryAccess access) {
            return CoreRegistries.safeAccess(
                    CoreRegistries.TRIBULATION_DEFINITION_REGISTRY,
                    id,
                    access
            );
        }
    }

    record InPlace(TribulationDefinition tribulation) implements TribulationDefinitionReference {
        @Override
        public TribulationDefinition resolve(RegistryAccess access) {
            return tribulation;
        }
    }

    public static final Codec<TribulationDefinitionReference> CODEC =
            Codec.either(
                    Identifier.CODEC,
                    TribulationType.TRIBULATION_CODEC
            ).xmap(
                    either-> either.map(
                            RegistryReference::new,
                            InPlace::new
                    ),
                    wrapper -> {
                        if (wrapper instanceof RegistryReference(Identifier id)) {
                            return Either.left(id);
                        }

                        if (wrapper instanceof InPlace(
                                TribulationDefinition tribulation)) {
                            return Either.right(tribulation);
                        }

                        throw new IllegalStateException("Unknown Tribulation Reference type");
                    }
            );
}
