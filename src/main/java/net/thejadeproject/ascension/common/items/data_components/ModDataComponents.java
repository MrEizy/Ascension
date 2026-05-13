package net.thejadeproject.ascension.common.items.data_components;

import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.thejadeproject.ascension.AscensionCraft;
import net.thejadeproject.ascension.common.items.data_components.herb_pouch.HerbPouchComponent;
import net.thejadeproject.ascension.events.SealedEntityData;
import net.thejadeproject.ascension.common.items.data_components.spatial_ring.SpatialRingComponent;
import net.thejadeproject.ascension.refactor_packages.util.ByteBufUtil;

import java.util.List;
import java.util.function.Supplier;

public class ModDataComponents {
    public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENTS =
            DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, AscensionCraft.MOD_ID);











    public static final Supplier<DataComponentType<String>> NEEDLE_EFFECT = DATA_COMPONENTS.register(
            "needle_effect",
            () -> DataComponentType.<String>builder()
                    .persistent(Codec.STRING)
                    .networkSynchronized(ByteBufCodecs.STRING_UTF8)
                    .build()
    );



    public static final Supplier<DataComponentType<HerbPouchComponent>> HERB_POUCH_DATA =
            DATA_COMPONENTS.register(
                    "herb_pouch_data",
                    () -> DataComponentType.<HerbPouchComponent>builder()
                            .persistent(HerbPouchComponent.CODEC)
                            .networkSynchronized(HerbPouchComponent.STREAM_CODEC)
                            .build()
            );




    public static final Supplier<DataComponentType<String>> PHYSIQUE_ID = DATA_COMPONENTS.register(
            "physique_id",
            () -> DataComponentType.<String>builder()
                    .persistent(Codec.STRING)
                    .networkSynchronized(ByteBufCodecs.STRING_UTF8)
                    .build()
    );
    public static final Supplier<DataComponentType<String>> TECHNIQUE_ID = DATA_COMPONENTS.register(
            "technique_id",
            ()->DataComponentType.<String>builder()
                    .persistent(Codec.STRING)
                    .networkSynchronized(ByteBufCodecs.STRING_UTF8)
                    .build()
    );
    public static final Supplier<DataComponentType<Integer>> COLLECTED_PAGES = DATA_COMPONENTS.register(
            "collected_pages",
            () -> DataComponentType.<Integer>builder()
                    .persistent(Codec.INT)
                    .networkSynchronized(ByteBufCodecs.VAR_INT)
                    .build()
    );
    public static final Supplier<DataComponentType<Integer>> PAGE_INDEX = DATA_COMPONENTS.register(
            "page_index",
            () -> DataComponentType.<Integer>builder()
                    .persistent(Codec.INT)
                    .networkSynchronized(ByteBufCodecs.VAR_INT)
                    .build()
    );
    public static final Supplier<DataComponentType<List<Integer>>> COLLECTED_PAGE_INDICES = DATA_COMPONENTS.register(
            "collected_page_indices",
            () -> DataComponentType.<List<Integer>>builder()
                    .persistent(Codec.INT.listOf())
                    .networkSynchronized(ByteBufCodecs.fromCodec(Codec.INT.listOf()))
                    .build()
    );

    public static final Supplier<DataComponentType<Integer>> PURITY = DATA_COMPONENTS.register(
            "purity",
            () -> DataComponentType.<Integer>builder()
                    .persistent(Codec.INT)
                    .networkSynchronized(ByteBufCodecs.VAR_INT)
                    .build()
    );

    public static final Supplier<DataComponentType<Boolean>> PERMANENT = DATA_COMPONENTS.register(
            "permanent",
            () -> DataComponentType.<Boolean>builder()
                    .persistent(Codec.BOOL)
                    .networkSynchronized(ByteBufCodecs.BOOL)
                    .build()
    );

    public static final Supplier<DataComponentType<Integer>> RECHARGE_PROGRESS = DATA_COMPONENTS.register(
            "recharge_progress",
            () -> DataComponentType.<Integer>builder()
                    .persistent(Codec.INT)
                    .networkSynchronized(ByteBufCodecs.VAR_INT)
                    .build()
    );


    //Spirit Sealing Ring - Stores captured entity data
    public static final Supplier<DataComponentType<SealedEntityData>> SEALED_ENTITY = DATA_COMPONENTS.register(
            "sealed_entity",
            () -> DataComponentType.<SealedEntityData>builder()
                    .persistent(SealedEntityData.CODEC)
                    .networkSynchronized(SealedEntityData.STREAM_CODEC)
                    .cacheEncoding()
                    .build()
    );



    public static final DataComponentType<Integer> EXTRA_SLOTS = DataComponentType.<Integer>builder()
            .persistent(Codec.INT)
            .networkSynchronized(ByteBufCodecs.VAR_INT)
            .cacheEncoding()
            .build();
    //Spatial Ring Stuff

    public static final DataComponentType<Integer> STACK_MULTIPLIER = DataComponentType.<Integer>builder()
            .persistent(Codec.INT)
            .networkSynchronized(ByteBufCodecs.VAR_INT)
            .cacheEncoding()
            .build();

    public static final DataComponentType<Boolean> AUTO_PICKUP = DataComponentType.<Boolean>builder()
            .persistent(Codec.BOOL)
            .networkSynchronized(ByteBufCodecs.BOOL)
            .cacheEncoding()
            .build();

    public static final Supplier<DataComponentType<List<ItemStack>>> UPGRADE_ITEMS = DATA_COMPONENTS.register(
            "upgrade_items",
            () -> DataComponentType.<List<ItemStack>>builder()
                    .persistent(ItemStack.CODEC.listOf())
                    .networkSynchronized(ByteBufCodecs.fromCodec(ItemStack.CODEC.listOf()))
                    .build()
    );




    public static final Supplier<DataComponentType<SpatialRingComponent>> SPIRIT_RING_DATA = DATA_COMPONENTS.register(
            "spirit_ring_data",
            ()->DataComponentType.<SpatialRingComponent>builder()
                    .persistent(SpatialRingComponent.CODEC)
                    .networkSynchronized(SpatialRingComponent.STREAM_CODEC)
                    .build()

    );




    // ── Pill: purity (1-100, used for grade names in tooltip) ────
    public static final Supplier<DataComponentType<Integer>> PILL_PURITY = DATA_COMPONENTS.register(
            "pill_purity",
            () -> DataComponentType.<Integer>builder()
                    .persistent(Codec.INT)
                    .networkSynchronized(ByteBufCodecs.VAR_INT)
                    .build()
    );

    // ── Pill: major realm (1-9) ───────────────────────────────────
    public static final Supplier<DataComponentType<Integer>> PILL_MAJOR_REALM = DATA_COMPONENTS.register(
            "pill_major_realm",
            () -> DataComponentType.<Integer>builder()
                    .persistent(Codec.INT)
                    .networkSynchronized(ByteBufCodecs.VAR_INT)
                    .build()
    );

    public static final Supplier<DataComponentType<List<String>>> PILL_EFFECTS = DATA_COMPONENTS.register(
            "pill_effects",
            () -> DataComponentType.<List<String>>builder()
                    .persistent(Codec.STRING.listOf())
                    .networkSynchronized(ByteBufUtil.STRING_LIST)
                    .build()
    );

    // ── Pill: bonus herb effect ID ────────────────────────────────
    public static final Supplier<DataComponentType<String>> PILL_BONUS_EFFECT = DATA_COMPONENTS.register(
            "pill_bonus_effect",
            () -> DataComponentType.<String>builder()
                    .persistent(Codec.STRING)
                    .networkSynchronized(ByteBufCodecs.STRING_UTF8)
                    .build()
    );

    public static final Supplier<DataComponentType<Long>> HERB_AGE_TICKS = DATA_COMPONENTS.register(
            "herb_age_ticks",
            () -> DataComponentType.<Long>builder()
                    .persistent(Codec.LONG)
                    .networkSynchronized(ByteBufCodecs.VAR_LONG)
                    .build()
    );

    // ── Herb: quality tier (0=Basic 1=Average 2=Advanced 3=Peak) ─
    // Rolled randomly when the crop first reaches max age.
    // Higher quality = larger purity contribution when used in cauldron.
    public static final Supplier<DataComponentType<Integer>> HERB_QUALITY = DATA_COMPONENTS.register(
            "herb_quality",
            () -> DataComponentType.<Integer>builder()
                    .persistent(Codec.INT)
                    .networkSynchronized(ByteBufCodecs.VAR_INT)
                    .build()
    );




    public static void register(IEventBus eventBus) {
        DATA_COMPONENTS.register(eventBus);
    }
}