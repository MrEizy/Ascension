package net.zic.ascension.impl.datapack.util;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import net.zic.ascension.AscensionCraft;
import net.zic.zenithlib.value_containers.ValueContainer;
import net.zic.zenithlib.value_containers.ValueContainerModifier;

public record BaseAffinity(ValueContainer.BaseModifier base, Identifier category){
    public static final Codec<BaseAffinity> CODEC = Codec.either(
            RecordCodecBuilder.<BaseAffinity>create(instance ->
                    instance.group(
                            ValueContainer.BASE_MODIFIER_CODEC.fieldOf("modifier")
                                    .forGetter(BaseAffinity::base),

                            Identifier.CODEC.optionalFieldOf(
                                    "category",
                                    Identifier.fromNamespaceAndPath(
                                            AscensionCraft.MOD_ID,
                                            "none"
                                    )
                            ).forGetter(BaseAffinity::category)
                    ).apply(instance, BaseAffinity::new)
            ),

            ValueContainer.BASE_MODIFIER_CODEC
    ).xmap(
            either -> either.map(
                    full -> full,
                    modifier -> new BaseAffinity(
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
                        ? Either.right(affinity.base())
                        : Either.left(affinity);
            }
    );
}
