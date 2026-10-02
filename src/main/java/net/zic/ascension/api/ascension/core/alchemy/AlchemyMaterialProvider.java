package net.zic.ascension.api.ascension.core.alchemy;

import com.mojang.serialization.Codec;
import net.minecraft.world.item.ItemStack;
import net.zic.ascension.api.ascension.datapack.CodecType;

public interface AlchemyMaterialProvider {
    Codec<AlchemyMaterialProvider> CODEC = AlchemyMaterials.PROVIDER_TYPE_REGISTRY.byNameCodec()
            .dispatch(AlchemyMaterialProvider::getType, CodecType<AlchemyMaterialProvider>::codec);

    CodecType<AlchemyMaterialProvider> getType();

    AlchemyMaterial resolve(ItemStack stack, AlchemyContext context);
}
