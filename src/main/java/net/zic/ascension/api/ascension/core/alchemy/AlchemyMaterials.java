package net.zic.ascension.api.ascension.core.alchemy;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import net.neoforged.neoforge.registries.datamaps.DataMapType;
import net.neoforged.neoforge.registries.datamaps.RegisterDataMapTypesEvent;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.ascension.datapack.CodecType;
import net.zic.zenithlib.registry.RegistryHelper;

import java.util.Optional;

@EventBusSubscriber(modid = AscensionCraft.MOD_ID)
public final class AlchemyMaterials {
    public static final Registry<CodecType<AlchemyMaterialProvider>> PROVIDER_TYPE_REGISTRY =
            RegistryHelper.registry(AscensionCraft.MOD_ID, "alchemy_material_provider_type");
    public static final DataMapType<Item, AlchemyMaterialProvider> MATERIAL = DataMapType.builder(
            AscensionCraft.prefix("alchemy_material"),
            Registries.ITEM,
            AlchemyMaterialProvider.CODEC
    ).synced(AlchemyMaterialProvider.CODEC, false).build();

    private AlchemyMaterials() {
    }

    public static Optional<AlchemyMaterial> resolve(ItemStack stack, AlchemyContext context) {
        if (stack == null || stack.isEmpty()) {
            return Optional.empty();
        }

        AlchemyMaterialProvider provider = stack.getData(MATERIAL);
        if (provider == null) {
            return Optional.empty();
        }

        AlchemyMaterial material = provider.resolve(stack, context == null ? AlchemyContext.EMPTY : context);
        return material == null || material.isEmpty() ? Optional.empty() : Optional.of(material);
    }

    @SubscribeEvent
    public static void registerRegistries(NewRegistryEvent event) {
        event.register(PROVIDER_TYPE_REGISTRY);
    }

    @SubscribeEvent
    public static void registerDataMapTypes(RegisterDataMapTypesEvent event) {
        event.register(MATERIAL);
    }
}
