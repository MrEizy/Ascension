package net.zic.ascension.common.item.components;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.zic.ascension.AscensionCraft;

import java.util.function.Supplier;

public class AscensionComponents {
    public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENTS =
            DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, AscensionCraft.MOD_ID);


    public static final Supplier<DataComponentType<Identifier>> REGISTRY_ID_HOLDER = DATA_COMPONENTS.register(
            "registry_id_holder",
            ()->DataComponentType.<Identifier>builder()
                    .persistent(Identifier.CODEC)
                    .networkSynchronized(Identifier.STREAM_CODEC)
                    .build()
    );
    //for now only used by qi items like spirit stones, but could be repurposed for a general use tier
    //TODO consider expecting them to be translatable keys? or expand the tierCapacity definition to be a general
    //TODO tier definition which includes a translatable key?
    public static final Supplier<DataComponentType<String>> ITEM_TIER = DATA_COMPONENTS.register(
            "spirit_stone_tier",
            ()->DataComponentType.<String>builder()
                    .persistent(Codec.STRING)
                    .networkSynchronized(ByteBufCodecs.STRING_UTF8)
                    .build()
    );
    public static final Supplier<DataComponentType<Long>> ITEM_QI = DATA_COMPONENTS.register(
            "item_qi",
            ()->DataComponentType.<Long>builder()
                    .persistent(Codec.LONG)
                    .networkSynchronized(ByteBufCodecs.LONG)
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

    public static final Supplier<DataComponentType<HerbData>> HERB_DATA = DATA_COMPONENTS.register(
            "herb_data",
            () -> DataComponentType.<HerbData>builder()
                    .persistent(HerbData.CODEC)
                    .networkSynchronized(HerbData.STREAM_CODEC)
                    .build()
    );

    public record HerbData(int ageTier, int qualityTier, boolean wild) {
        public static final HerbData DEFAULT = new HerbData(0, 1, false);

        public static final Codec<HerbData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.intRange(0, 15).fieldOf("age_tier").forGetter(HerbData::ageTier),
                Codec.intRange(0, 4).fieldOf("quality_tier").forGetter(HerbData::qualityTier),
                Codec.BOOL.fieldOf("wild").forGetter(HerbData::wild)
        ).apply(instance, HerbData::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, HerbData> STREAM_CODEC = new StreamCodec<>() {
            @Override
            public HerbData decode(RegistryFriendlyByteBuf buf) {
                return new HerbData(buf.readVarInt(), buf.readVarInt(), buf.readBoolean());
            }

            @Override
            public void encode(RegistryFriendlyByteBuf buf, HerbData data) {
                buf.writeVarInt(data.ageTier());
                buf.writeVarInt(data.qualityTier());
                buf.writeBoolean(data.wild());
            }
        };
    }

    public static void register(IEventBus eventBus) {
        DATA_COMPONENTS.register(eventBus);
    }
}
