package net.zic.ascension.impl.datapack.util;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import net.zic.ascension.AscensionCraft;
import net.zic.zenithlib.value_containers.ValueContainerModifier;

public record AffinityModifier(ValueContainerModifier modifier,Identifier category) {

    public static final Codec<AffinityModifier> CODEC = Codec.either(
            RecordCodecBuilder.<AffinityModifier>create(instance ->
                    instance.group(
                            ValueContainerModifier.CODEC.fieldOf("modifier")
                                    .forGetter(AffinityModifier::modifier),

                            Identifier.CODEC.optionalFieldOf(
                                    "category",
                                    Identifier.fromNamespaceAndPath(
                                            AscensionCraft.MOD_ID,
                                            "none"
                                    )
                            ).forGetter(AffinityModifier::category)
                    ).apply(instance, AffinityModifier::new)
            ),
            ValueContainerModifier.CODEC
    ).xmap(
            either -> either.map(
                    full -> full,
                    modifier -> new AffinityModifier(
                            modifier,
                            Identifier.fromNamespaceAndPath(
                                    AscensionCraft.MOD_ID,
                                    "none"
                            )
                    )
            ),

            affinity -> {
                boolean isDefaultCategory =
                        affinity.category().equals(
                                Identifier.fromNamespaceAndPath(
                                        AscensionCraft.MOD_ID,
                                        "none"
                                )
                        );

                return isDefaultCategory
                        ? Either.right(affinity.modifier())
                        : Either.left(affinity);
            }
    );
}
