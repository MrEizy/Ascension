package net.zic.ascension.datagen;

import net.minecraft.core.registries.Registries;
import net.minecraft.data.loot.LootTableSubProvider;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.EmptyLootItem;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.functions.SetComponentsFunction;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.common.item.ModItems;
import net.zic.ascension.common.item.components.AscensionComponents;

import java.util.function.BiConsumer;

/**
 * Loot tables that are rolled into vanilla tables by {@link AscGlobalLootModifierProvider}.
 * Drop chances and other conditions live on the loot modifier, not here.
 */
public class AscAddedLootTableProvider implements LootTableSubProvider {
    // Bloodlines
    public static final ResourceKey<LootTable> VINDICATOR_BARBARIAN_BLOODLINE = key("added_loot/bloodlines/vindicator_barbarian_bloodline");
    public static final ResourceKey<LootTable> PANDA_BEASTKIN_BLOODLINE = key("added_loot/bloodlines/panda_beastkin_bloodline");
    public static final ResourceKey<LootTable> SILVERFISH_BLACK_IRON_ANT_BLOODLINE = key("added_loot/bloodlines/silverfish_black_iron_ant_bloodline");
    public static final ResourceKey<LootTable> MAGMA_CUBE_EMBER_SPIRIT_BLOODLINE = key("added_loot/bloodlines/magma_cube_ember_spirit_bloodline");
    public static final ResourceKey<LootTable> DUNGEON_FALLING_STAR_BLOODLINE = key("added_loot/bloodlines/dungeon_falling_star_bloodline");
    public static final ResourceKey<LootTable> IGLOO_FROSTKIN_BLOODLINE = key("added_loot/bloodlines/igloo_frostkin_bloodline");
    public static final ResourceKey<LootTable> SNOW_FOX_MOONVEIL_FOX_BLOODLINE = key("added_loot/bloodlines/snow_fox_moonveil_fox_bloodline");
    public static final ResourceKey<LootTable> NITWIT_HUMAN_BLOODLINE = key("added_loot/bloodlines/nitwit_human_bloodline");
    public static final ResourceKey<LootTable> ARMADILLO_SCALEKIN_BLOODLINE = key("added_loot/bloodlines/armadillo_scalekin_bloodline");
    public static final ResourceKey<LootTable> WITCH_VERDANTBLOOD_BLOODLINE = key("added_loot/bloodlines/witch_verdantblood_bloodline");
    public static final ResourceKey<LootTable> OCELOT_WIND_CHASING_LEOPARD_BLOODLINE = key("added_loot/bloodlines/ocelot_wind_chasing_leopard_bloodline");
    public static final ResourceKey<LootTable> ALLAY_DEEP_SPRING_SPIRIT_BLOODLINE = key("added_loot/bloodlines/allay_deep_spring_spirit_bloodline");
    public static final ResourceKey<LootTable> DESERT_PYRAMID_GOLDEN_MANED_LION_BLOODLINE = key("added_loot/bloodlines/desert_pyramid_golden_maned_lion_bloodline");
    public static final ResourceKey<LootTable> SNOWY_VILLAGE_MOON_WHITE_CRANE_BLOODLINE = key("added_loot/bloodlines/snowy_village_moon_white_crane_bloodline");
    public static final ResourceKey<LootTable> WOLF_BEAST_BLOODLINE = key("added_loot/bloodlines/wolf_beast_bloodline");
    public static final ResourceKey<LootTable> COLD_CHICKEN_RAVEN_BLOODLINE = key("added_loot/bloodlines/cold_chicken_raven_bloodline");
    public static final ResourceKey<LootTable> TEMPERATE_CHICKEN_CRANE_BLOODLINE = key("added_loot/bloodlines/temperate_chicken_crane_bloodline");
    public static final ResourceKey<LootTable> WARM_CHICKEN_PHOENIX_BLOODLINE = key("added_loot/bloodlines/warm_chicken_phoenix_bloodline");
    public static final ResourceKey<LootTable> SHIPWRECK_GREED_DEMON_BLOODLINE = key("added_loot/bloodlines/shipwreck_greed_demon_bloodline");
    public static final ResourceKey<LootTable> BABY_ZOMBIE_WRATH_DEMON_BLOODLINE = key("added_loot/bloodlines/baby_zombie_wrath_demon_bloodline");

    // Physiques
    public static final ResourceKey<LootTable> MAGMA_CUBE_EMBER_TOUCHED_MERIDIANS_PHYSIQUE = key("added_loot/physiques/magma_cube_ember_touched_meridians_physique");
    public static final ResourceKey<LootTable> DUNGEON_STARLIT_MERIDIANS_PHYSIQUE = key("added_loot/physiques/dungeon_starlit_meridians_physique");
    public static final ResourceKey<LootTable> PHANTOM_BRITTLE_BONE_BODY_PHYSIQUE = key("added_loot/physiques/phantom_brittle_bone_body_physique");
    public static final ResourceKey<LootTable> ZOMBIFIED_PIGLIN_CURSED_BODY_PHYSIQUE = key("added_loot/physiques/zombified_piglin_cursed_body_physique");
    public static final ResourceKey<LootTable> SLIME_DAMPENED_FIRE_BODY_PHYSIQUE = key("added_loot/physiques/slime_dampened_fire_body_physique");
    public static final ResourceKey<LootTable> BEACHED_SHIPWRECK_FIVE_ELEMENTS_IMBALANCE_PHYSIQUE = key("added_loot/physiques/beached_shipwreck_five_elements_imbalance_physique");
    public static final ResourceKey<LootTable> STRAY_FROSTBITTEN_VEIN_BODY_PHYSIQUE = key("added_loot/physiques/stray_frostbitten_vein_body_physique");
    public static final ResourceKey<LootTable> POLAR_BEAR_HEAVY_BONE_PHYSIQUE = key("added_loot/physiques/polar_bear_heavy_bone_physique");
    public static final ResourceKey<LootTable> SILVERFISH_IRON_ANT_BONES_PHYSIQUE = key("added_loot/physiques/silverfish_iron_ant_bones_physique");
    public static final ResourceKey<LootTable> IRON_GOLEM_IRON_BLOSSOM_PHYSIQUE = key("added_loot/physiques/iron_golem_iron_blossom_physique");
    public static final ResourceKey<LootTable> MINESHAFT_MIASMA_LUNGS_BODY_PHYSIQUE = key("added_loot/physiques/mineshaft_miasma_lungs_body_physique");
    public static final ResourceKey<LootTable> CAT_GIFT_TIGER_MARROW_PHYSIQUE = key("added_loot/physiques/cat_gift_tiger_marrow_physique");
    public static final ResourceKey<LootTable> SNIFFER_TREMBLING_EARTH_BODY_UNSTABLE_PHYSIQUE = key("added_loot/physiques/sniffer_trembling_earth_body_unstable_physique");
    public static final ResourceKey<LootTable> BLAZE_FIRE_SPIRIT_BODY_UNSTABLE_PHYSIQUE = key("added_loot/physiques/blaze_fire_spirit_body_unstable_physique");
    public static final ResourceKey<LootTable> FROG_FIREFLY_SPIRIT_EYES_PHYSIQUE = key("added_loot/physiques/frog_firefly_spirit_eyes_physique");
    public static final ResourceKey<LootTable> OCELOT_GALE_CHEETAH_MERIDIANS_PHYSIQUE = key("added_loot/physiques/ocelot_gale_cheetah_meridians_physique");
    public static final ResourceKey<LootTable> DESERT_PYRAMID_GOLDEN_SUN_HEART_PHYSIQUE = key("added_loot/physiques/desert_pyramid_golden_sun_heart_physique");
    public static final ResourceKey<LootTable> JUNGLE_TEMPLE_HUNDRED_VENOM_DANTIAN_PHYSIQUE = key("added_loot/physiques/jungle_temple_hundred_venom_dantian_physique");
    public static final ResourceKey<LootTable> IGLOO_ICE_SOUL_BODY_FLAWED_PHYSIQUE = key("added_loot/physiques/igloo_ice_soul_body_flawed_physique");
    public static final ResourceKey<LootTable> NETHER_FORTRESS_JADE_FURNACE_PHYSIQUE = key("added_loot/physiques/nether_fortress_jade_furnace_physique");
    public static final ResourceKey<LootTable> LLAMA_LION_ROAR_CHEST_PHYSIQUE = key("added_loot/physiques/llama_lion_roar_chest_physique");
    public static final ResourceKey<LootTable> CHARGED_CREEPER_THUNDER_PHYSIQUES = key("added_loot/physiques/charged_creeper_thunder_physiques");
    public static final ResourceKey<LootTable> ENDERMAN_ABYSS_DRIFTER_PHYSIQUE = key("added_loot/physiques/enderman_abyss_drifter_physique");
    public static final ResourceKey<LootTable> DUNGEON_SHALLOW_CORE_BODY_PHYSIQUE = key("added_loot/physiques/dungeon_shallow_core_body_physique");
    public static final ResourceKey<LootTable> WANDERING_TRADER_WEAK_SPIRIT_BODY_PHYSIQUE = key("added_loot/physiques/wandering_trader_weak_spirit_body_physique");

    /** Physiques don't use purity yet, so every physique essence is generated at full purity. */
    static final int PHYSIQUE_PURITY = 100;

    private static ResourceKey<LootTable> key(String path) {
        return ResourceKey.create(Registries.LOOT_TABLE, Identifier.fromNamespaceAndPath(AscensionCraft.MOD_ID, path));
    }

    @Override
    public void generate(BiConsumer<ResourceKey<LootTable>, LootTable.Builder> consumer) {
        consumer.accept(VINDICATOR_BARBARIAN_BLOODLINE, LootTable.lootTable()
                .withPool(bloodlinePool(AscensionCraft.prefix("1_ordinary/barbarian_bloodline"), 12, 16)));
        consumer.accept(PANDA_BEASTKIN_BLOODLINE, LootTable.lootTable()
                .withPool(bloodlinePool(AscensionCraft.prefix("1_ordinary/beastkin_bloodline"), 12, 16)));
        consumer.accept(SILVERFISH_BLACK_IRON_ANT_BLOODLINE, LootTable.lootTable()
                .withPool(bloodlinePool(AscensionCraft.prefix("1_ordinary/black_iron_ant_bloodline"), 7, 11)));
        consumer.accept(MAGMA_CUBE_EMBER_SPIRIT_BLOODLINE, LootTable.lootTable()
                .withPool(bloodlinePool(AscensionCraft.prefix("1_ordinary/ember_spirit_bloodline"), 2, 5)));
        consumer.accept(MAGMA_CUBE_EMBER_TOUCHED_MERIDIANS_PHYSIQUE, LootTable.lootTable()
                .withPool(physiquePool(AscensionCraft.prefix("1_ordinary/ember_touched_meridians_physique"))));
        consumer.accept(DUNGEON_FALLING_STAR_BLOODLINE, LootTable.lootTable()
                .withPool(bloodlinePool(AscensionCraft.prefix("1_ordinary/falling_star_bloodline"), 50, 50)));
        consumer.accept(IGLOO_FROSTKIN_BLOODLINE, LootTable.lootTable()
                .withPool(bloodlinePool(AscensionCraft.prefix("1_ordinary/frostkin_bloodline"), 45, 51)));
        consumer.accept(SNOW_FOX_MOONVEIL_FOX_BLOODLINE, LootTable.lootTable()
                .withPool(bloodlinePool(AscensionCraft.prefix("2_profound/moonveil_fox_bloodline"), 13, 21)));
        consumer.accept(NITWIT_HUMAN_BLOODLINE, LootTable.lootTable()
                .withPool(bloodlinePool(AscensionCraft.prefix("1_ordinary/human_bloodline"), 12, 16)));
        consumer.accept(ARMADILLO_SCALEKIN_BLOODLINE, LootTable.lootTable()
                .withPool(bloodlinePool(AscensionCraft.prefix("1_ordinary/scalekin_bloodline"), 8, 13)));
        consumer.accept(WITCH_VERDANTBLOOD_BLOODLINE, LootTable.lootTable()
                .withPool(bloodlinePool(AscensionCraft.prefix("1_ordinary/verdantblood_bloodline"), 3, 6)));
        consumer.accept(OCELOT_WIND_CHASING_LEOPARD_BLOODLINE, LootTable.lootTable()
                .withPool(bloodlinePool(AscensionCraft.prefix("1_ordinary/wind_chasing_leopard_bloodline"), 23, 32)));
        consumer.accept(ALLAY_DEEP_SPRING_SPIRIT_BLOODLINE, LootTable.lootTable()
                .withPool(bloodlinePool(AscensionCraft.prefix("2_profound/deep_spring_spirit_bloodline"), 13, 21)));
        consumer.accept(DESERT_PYRAMID_GOLDEN_MANED_LION_BLOODLINE, LootTable.lootTable()
                .withPool(bloodlinePool(AscensionCraft.prefix("2_profound/golden_maned_lion_bloodline"), 11, 25)));
        consumer.accept(SNOWY_VILLAGE_MOON_WHITE_CRANE_BLOODLINE, LootTable.lootTable()
                .withPool(bloodlinePool(AscensionCraft.prefix("2_profound/moon_white_crane_bloodline"), 8, 10)));
        consumer.accept(WOLF_BEAST_BLOODLINE, LootTable.lootTable()
                .withPool(bloodlinePool(AscensionCraft.prefix("1_ordinary/beast_bloodline"), 20, 29)));
        consumer.accept(COLD_CHICKEN_RAVEN_BLOODLINE, LootTable.lootTable()
                .withPool(bloodlinePool(AscensionCraft.prefix("1_ordinary/raven_bloodline"), 18, 39)));
        consumer.accept(TEMPERATE_CHICKEN_CRANE_BLOODLINE, LootTable.lootTable()
                .withPool(bloodlinePool(AscensionCraft.prefix("2_profound/crane_bloodline"), 18, 39)));
        consumer.accept(WARM_CHICKEN_PHOENIX_BLOODLINE, LootTable.lootTable()
                .withPool(bloodlinePool(AscensionCraft.prefix("2_profound/phoenix_bloodline"), 5, 11)));
        consumer.accept(SHIPWRECK_GREED_DEMON_BLOODLINE, LootTable.lootTable()
                .withPool(bloodlinePool(AscensionCraft.prefix("1_ordinary/greed_demon_bloodline"), 19, 48)));
        consumer.accept(BABY_ZOMBIE_WRATH_DEMON_BLOODLINE, LootTable.lootTable()
                .withPool(bloodlinePool(AscensionCraft.prefix("2_profound/wrath_demon_bloodline"), 13, 21)));
        consumer.accept(DUNGEON_STARLIT_MERIDIANS_PHYSIQUE, LootTable.lootTable()
                .withPool(physiquePool(AscensionCraft.prefix("1_ordinary/starlit_meridians_physique"))));
        consumer.accept(PHANTOM_BRITTLE_BONE_BODY_PHYSIQUE, LootTable.lootTable()
                .withPool(physiquePool(AscensionCraft.prefix("1_ordinary/brittle_bone_body_physique"))));
        consumer.accept(ZOMBIFIED_PIGLIN_CURSED_BODY_PHYSIQUE, LootTable.lootTable()
                .withPool(physiquePool(AscensionCraft.prefix("1_ordinary/cursed_body_physique"))));
        consumer.accept(SLIME_DAMPENED_FIRE_BODY_PHYSIQUE, LootTable.lootTable()
                .withPool(physiquePool(AscensionCraft.prefix("1_ordinary/dampened_fire_body_physique"))));
        consumer.accept(BEACHED_SHIPWRECK_FIVE_ELEMENTS_IMBALANCE_PHYSIQUE, LootTable.lootTable()
                .withPool(physiquePool(AscensionCraft.prefix("1_ordinary/five_elements_imbalance"))));
        consumer.accept(STRAY_FROSTBITTEN_VEIN_BODY_PHYSIQUE, LootTable.lootTable()
                .withPool(physiquePool(AscensionCraft.prefix("1_ordinary/frostbitten_vein_body_physique"))));
        consumer.accept(POLAR_BEAR_HEAVY_BONE_PHYSIQUE, LootTable.lootTable()
                .withPool(physiquePool(AscensionCraft.prefix("1_ordinary/heavy_bone_physique"))));
        consumer.accept(SILVERFISH_IRON_ANT_BONES_PHYSIQUE, LootTable.lootTable()
                .withPool(physiquePool(AscensionCraft.prefix("1_ordinary/iron_ant_bones_physique"))));
        consumer.accept(IRON_GOLEM_IRON_BLOSSOM_PHYSIQUE, LootTable.lootTable()
                .withPool(physiquePool(AscensionCraft.prefix("1_ordinary/iron_blossom_physique"))));
        consumer.accept(MINESHAFT_MIASMA_LUNGS_BODY_PHYSIQUE, LootTable.lootTable()
                .withPool(physiquePool(AscensionCraft.prefix("1_ordinary/miasma_lungs_body_physique"))));
        consumer.accept(CAT_GIFT_TIGER_MARROW_PHYSIQUE, LootTable.lootTable()
                .withPool(physiquePool(AscensionCraft.prefix("1_ordinary/tiger_marrow_physique"))));
        consumer.accept(SNIFFER_TREMBLING_EARTH_BODY_UNSTABLE_PHYSIQUE, LootTable.lootTable()
                .withPool(physiquePool(AscensionCraft.prefix("2_profound/trembling_earth_body_unstable_physique"))));
        consumer.accept(BLAZE_FIRE_SPIRIT_BODY_UNSTABLE_PHYSIQUE, LootTable.lootTable()
                .withPool(physiquePool(AscensionCraft.prefix("2_profound/fire_spirit_body_unstable_physique"))));
        consumer.accept(FROG_FIREFLY_SPIRIT_EYES_PHYSIQUE, LootTable.lootTable()
                .withPool(physiquePool(AscensionCraft.prefix("2_profound/firefly_spirit_eyes_physique"))));
        consumer.accept(OCELOT_GALE_CHEETAH_MERIDIANS_PHYSIQUE, LootTable.lootTable()
                .withPool(physiquePool(AscensionCraft.prefix("2_profound/gale_cheetah_meridians_physique"))));
        consumer.accept(DESERT_PYRAMID_GOLDEN_SUN_HEART_PHYSIQUE, LootTable.lootTable()
                .withPool(physiquePool(AscensionCraft.prefix("2_profound/golden_sun_heart_physique"))));
        consumer.accept(JUNGLE_TEMPLE_HUNDRED_VENOM_DANTIAN_PHYSIQUE, LootTable.lootTable()
                .withPool(physiquePool(AscensionCraft.prefix("2_profound/hundred_venom_dantian_physique"))));
        consumer.accept(IGLOO_ICE_SOUL_BODY_FLAWED_PHYSIQUE, LootTable.lootTable()
                .withPool(physiquePool(AscensionCraft.prefix("2_profound/ice_soul_body_flawed_physique"))));
        consumer.accept(NETHER_FORTRESS_JADE_FURNACE_PHYSIQUE, LootTable.lootTable()
                .withPool(physiquePool(AscensionCraft.prefix("2_profound/jade_furnace_physique"))));
        consumer.accept(LLAMA_LION_ROAR_CHEST_PHYSIQUE, LootTable.lootTable()
                .withPool(physiquePool(AscensionCraft.prefix("2_profound/lion_roar_chest_physique"))));
        // One roll: 10% Thunder Body, 10% Thunder Spirit Veins, 80% nothing, so both can never drop together
        consumer.accept(CHARGED_CREEPER_THUNDER_PHYSIQUES, LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1))
                        .add(physiqueEssence(AscensionCraft.prefix("2_profound/thunder_body_unstable_physique")).setWeight(10))
                        .add(physiqueEssence(AscensionCraft.prefix("2_profound/thunder_spirit_veins_physique")).setWeight(10))
                        .add(EmptyLootItem.emptyItem().setWeight(80))));
        consumer.accept(ENDERMAN_ABYSS_DRIFTER_PHYSIQUE, LootTable.lootTable()
                .withPool(physiquePool(AscensionCraft.prefix("abyss_drifter_physique"))));
        consumer.accept(DUNGEON_SHALLOW_CORE_BODY_PHYSIQUE, LootTable.lootTable()
                .withPool(physiquePool(AscensionCraft.prefix("1_ordinary/shallow_core_body_physique"))));
        consumer.accept(WANDERING_TRADER_WEAK_SPIRIT_BODY_PHYSIQUE, LootTable.lootTable()
                .withPool(physiquePool(AscensionCraft.prefix("1_ordinary/weak_spirit_body_physique"))));
    }

    /**
     * One bloodline essence with a uniformly random purity in [minPurity, maxPurity].
     * Vanilla can only set constant components, so each purity value is its own equally weighted entry.
     */
    static LootPool.Builder bloodlinePool(Identifier bloodline, int minPurity, int maxPurity) {
        LootPool.Builder pool = LootPool.lootPool().setRolls(ConstantValue.exactly(1));
        for (int purity = minPurity; purity <= maxPurity; purity++) {
            pool.add(LootItem.lootTableItem(ModItems.BLOODLINE_ESSENCE.get())
                    .apply(SetComponentsFunction.setComponent(AscensionComponents.REGISTRY_ID_HOLDER.get(), bloodline))
                    .apply(SetComponentsFunction.setComponent(AscensionComponents.PURITY.get(), purity)));
        }
        return pool;
    }

    static LootPool.Builder physiquePool(Identifier physique) {
        return LootPool.lootPool()
                .setRolls(ConstantValue.exactly(1))
                .add(physiqueEssence(physique));
    }

    static LootPoolSingletonContainer.Builder<?> physiqueEssence(Identifier physique) {
        return LootItem.lootTableItem(ModItems.PHYSIQUE_ESSENCE.get())
                .apply(SetComponentsFunction.setComponent(AscensionComponents.REGISTRY_ID_HOLDER.get(), physique))
                .apply(SetComponentsFunction.setComponent(AscensionComponents.PURITY.get(), PHYSIQUE_PURITY));
    }
}
