package net.zic.ascension.configuration.mob_traits;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.Identifier;
import net.zic.ascension.api.ascension.core.CoreRegistries;
import net.zic.ascension.api.ascension.core.tribulation.TribulationDefinition;
import net.zic.ascension.api.ascension.core.tribulation.TribulationDefinitionReference;
import net.zic.ascension.api.ascension.datapack.tribulation.TribulationType;
import net.zic.ascension.configuration.ConfigurationRegistries;

public sealed interface MobTraitReference
        permits MobTraitReference.RegistryReference,
        MobTraitReference.InPlace {


        MobTraitDefinition resolve(RegistryAccess access);

        record RegistryReference(
                Identifier id) implements MobTraitReference {
            @Override
            public MobTraitDefinition resolve(RegistryAccess access) {
                return ConfigurationRegistries.MOB_TRAIT_DEFINITION_REGISTRY.get(access).getValue(id);
            }
        }

        record InPlace(MobTraitDefinition traitDefinition) implements MobTraitReference {
            @Override
            public MobTraitDefinition resolve(RegistryAccess access) {
                return traitDefinition;
            }
        }

        public static final Codec<MobTraitReference> CODEC =
                Codec.either(
                        Identifier.CODEC,
                        MobTraitDefinitionType.MOB_TRAIT_CODEC
                ).xmap(
                        either-> either.map(
                                MobTraitReference.RegistryReference::new,
                                MobTraitReference.InPlace::new
                        ),
                        wrapper -> {
                            if (wrapper instanceof MobTraitReference.RegistryReference(Identifier id)) {
                                return Either.left(id);
                            }

                            if (wrapper instanceof MobTraitReference.InPlace(
                                    MobTraitDefinition definition)) {
                                return Either.right(definition);
                            }

                            throw new IllegalStateException("Unknown Trait Reference type");
                        }
                );

}
