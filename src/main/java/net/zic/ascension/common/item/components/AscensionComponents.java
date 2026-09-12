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
    public static final Supplier<DataComponentType<Integer>> PURITY = DATA_COMPONENTS.register(
            "purity",
            ()->DataComponentType.<Integer>builder()
                    .persistent(Codec.INT)
                    .networkSynchronized(ByteBufCodecs.INT)
                    .build()
    );

    public static final Supplier<DataComponentType<PillData>> PILL_DATA = DATA_COMPONENTS.register(
            "pill_data",
            () -> DataComponentType.<PillData>builder()
                    .persistent(PillData.CODEC)
                    .networkSynchronized(PillData.STREAM_CODEC)
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

    public record PillData(Identifier rank, int majorRealm, int minorRealm, int purity, double potency) {
        public static final Identifier ORDINARY = AscensionCraft.prefix("ordinary");
        public static final Identifier PROFOUND = AscensionCraft.prefix("profound");
        public static final Identifier HEAVEN = AscensionCraft.prefix("heaven");
        public static final Identifier SAINT = AscensionCraft.prefix("saint");
        public static final Identifier GOD = AscensionCraft.prefix("god");
        public static final Identifier HEAVENS_PATH = AscensionCraft.prefix("heavens_path");

        public static final PillData DEFAULT = new PillData(ORDINARY, 0, 0, 75, 2.5D);
        public static final PillData MAXIMUM = new PillData(HEAVENS_PATH, 11, 2, 100, 100.0D);

        public static final Codec<PillData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Identifier.CODEC.fieldOf("rank").forGetter(PillData::rank),
                Codec.INT.fieldOf("major_realm").forGetter(PillData::majorRealm),
                Codec.INT.fieldOf("minor_realm").forGetter(PillData::minorRealm),
                Codec.intRange(0, 100).fieldOf("purity").forGetter(PillData::purity),
                Codec.DOUBLE.fieldOf("potency").forGetter(PillData::potency)
        ).apply(instance, PillData::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, PillData> STREAM_CODEC = new StreamCodec<>() {
            @Override
            public PillData decode(RegistryFriendlyByteBuf buf) {
                return new PillData(
                        Identifier.STREAM_CODEC.decode(buf),
                        buf.readVarInt(),
                        buf.readVarInt(),
                        buf.readVarInt(),
                        buf.readDouble()
                );
            }

            @Override
            public void encode(RegistryFriendlyByteBuf buf, PillData data) {
                Identifier.STREAM_CODEC.encode(buf, data.rank());
                buf.writeVarInt(data.majorRealm());
                buf.writeVarInt(data.minorRealm());
                buf.writeVarInt(data.purity());
                buf.writeDouble(data.potency());
            }
        };

        public PillData {
            rank = rank == null ? ORDINARY : rank;
            majorRealm = Math.max(0, majorRealm);
            minorRealm = Math.max(0, minorRealm);
            purity = Math.max(0, Math.min(100, purity));
            potency = Double.isFinite(potency) ? Math.max(0.0D, potency) : 0.0D;
        }

        public static Identifier rankForRealm(int majorRealm) {
            if (majorRealm >= 10) return HEAVENS_PATH;
            if (majorRealm >= 8) return GOD;
            if (majorRealm >= 6) return SAINT;
            if (majorRealm >= 4) return HEAVEN;
            if (majorRealm >= 2) return PROFOUND;
            return ORDINARY;
        }

    }

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
