package net.zic.ascension.common.item.components;

import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.zic.ascension.AscensionCraft;

import java.util.function.Supplier;

public class AscensionComponents {
    public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENTS =
            DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, AscensionCraft.MOD_ID);


    public static final Supplier<DataComponentType<Identifier>> REGISTRY_ID_HOLDER = DATA_COMPONENTS.register(
            "registry_id_holer",
            ()->DataComponentType.<Identifier>builder()
                    .persistent(Identifier.CODEC)
                    .networkSynchronized(Identifier.STREAM_CODEC)
                    .build()
    );
    public static final Supplier<DataComponentType<Integer>> PURITY = DATA_COMPONENTS.register(
            "purity",
            ()->DataComponentType.<Integer>builder()
                    .persistent(Codec.INT)
                    .networkSynchronized(ByteBufCodecs.INT)
                    .build()
    );

    public static final Supplier<DataComponentType<Identifier>> PATH_DAMAGE_TYPE = DATA_COMPONENTS.register(
            "path_damage_type",
            ()->DataComponentType.<Identifier>builder()
                    .persistent(Identifier.CODEC)
                    .networkSynchronized(Identifier.STREAM_CODEC)
                    .build()
    );
    public static void register(IEventBus eventBus) {
        DATA_COMPONENTS.register(eventBus);
    }
}
