package net.thejadeproject.ascension.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemKilledByPlayerCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.neoforged.neoforge.common.data.GlobalLootModifierProvider;
import net.neoforged.neoforge.common.loot.LootTableIdCondition;
import net.thejadeproject.ascension.AscensionCraft;
import net.thejadeproject.ascension.common.items.ModItems;
import net.thejadeproject.ascension.datagen.loot.AddItemModifier;
import net.thejadeproject.ascension.datagen.loot.AddPhysiqueRandomPurityModifier;
import net.thejadeproject.ascension.datagen.loot.AddTechniquePageModifier;
import net.thejadeproject.ascension.datagen.loot.conditions.AddTechniqueManualModifier;
import net.thejadeproject.ascension.datagen.loot.conditions.MobRankLootCondition;

import java.util.concurrent.CompletableFuture;

public class ModGlobalLootModifierProvider extends GlobalLootModifierProvider {
    public ModGlobalLootModifierProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, AscensionCraft.MOD_ID);
    }
    @Override
    protected void start() {




        //Technique Pages

        add("bloodfeast_ch5_from_ender_dragon", new AddTechniquePageModifier(
                new LootItemCondition[] {
                        LootTableIdCondition.builder(ResourceLocation.parse("minecraft:entities/ender_dragon")).build(),
                        LootItemRandomChanceCondition.randomChance(0.03f).build()
                },
                ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "bloodfeast_soul_refining_scripture"),
                5
        ));

        //Techniques
        add("basic_cultivation_technique_from_village", new AddTechniqueManualModifier(
                new LootItemCondition[] {
                        LootTableIdCondition.builder(ResourceLocation.withDefaultNamespace("chests/village/village_plains_house")).build(),
                        LootItemRandomChanceCondition.randomChance(0.05f).build()
                },
                ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "basic_cultivation_technique")
        ));

        add("advanced_cultivation_technique_from_zombie_villager", new AddTechniqueManualModifier(
                new LootItemCondition[] {
                        LootTableIdCondition.builder(ResourceLocation.withDefaultNamespace("entities/zombie_villager")).build(),
                        LootItemKilledByPlayerCondition.killedByPlayer().build(),
                        LootItemRandomChanceCondition.randomChance(0.04f).build(),
                        MobRankLootCondition.between("qi_gathering", 5, "formation_establishment", 9).build()
                },
                ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "advanced_cultivation_technique")
        ));

        add("advanced_cultivation_technique_from_stronghold_library", new AddTechniqueManualModifier(
                new LootItemCondition[] {
                        LootTableIdCondition.builder(ResourceLocation.withDefaultNamespace("chests/stronghold_library")).build(),
                        LootItemRandomChanceCondition.randomChance(0.06f).build()
                },
                ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "advanced_cultivation_technique")
        ));

        add("fire_essence_technique_from_blaze", new AddTechniqueManualModifier(
                new LootItemCondition[] {
                        LootTableIdCondition.builder(ResourceLocation.withDefaultNamespace("entities/blaze")).build(),
                        LootItemKilledByPlayerCondition.killedByPlayer().build(),
                        LootItemRandomChanceCondition.randomChance(0.04f).build(),
                        MobRankLootCondition.between("formation_establishment", 5, "formation_establishment", 9).build()
                },
                ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "fire_essence_technique")
        ));

        add("water_essence_technique_from_elder_guardian", new AddTechniqueManualModifier(
                new LootItemCondition[] {
                        LootTableIdCondition.builder(ResourceLocation.withDefaultNamespace("entities/elder_guardian")).build(),
                        LootItemKilledByPlayerCondition.killedByPlayer().build(),
                        LootItemRandomChanceCondition.randomChance(0.05f).build(),
                        MobRankLootCondition.between("formation_establishment", 6, "formation_establishment", 9).build()
                },
                ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "water_essence_technique")
        ));

        add("wood_essence_technique_from_jungle_temple", new AddTechniqueManualModifier(
                new LootItemCondition[] {
                        LootTableIdCondition.builder(ResourceLocation.withDefaultNamespace("chests/jungle_temple")).build(),
                        LootItemRandomChanceCondition.randomChance(0.05f).build()
                },
                ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "wood_essence_technique")
        ));

        add("earth_essence_technique_from_trail_ruins", new AddTechniqueManualModifier(
                new LootItemCondition[] {
                        LootTableIdCondition.builder(ResourceLocation.withDefaultNamespace("archaeology/trail_ruins_rare")).build(),
                        LootItemRandomChanceCondition.randomChance(0.04f).build()
                },
                ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "earth_essence_technique")
        ));

        add("metal_essence_technique_from_iron_golem", new AddTechniqueManualModifier(
                new LootItemCondition[] {
                        LootTableIdCondition.builder(ResourceLocation.withDefaultNamespace("entities/iron_golem")).build(),
                        LootItemKilledByPlayerCondition.killedByPlayer().build(),
                        LootItemRandomChanceCondition.randomChance(0.04f).build(),
                        MobRankLootCondition.between("formation_establishment", 4, "formation_establishment", 9).build()
                },
                ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "metal_essence_technique")
        ));

        add("lightning_essence_technique_from_creeper", new AddTechniqueManualModifier(
                new LootItemCondition[] {
                        LootTableIdCondition.builder(ResourceLocation.withDefaultNamespace("entities/creeper")).build(),
                        LootItemKilledByPlayerCondition.killedByPlayer().build(),
                        LootItemRandomChanceCondition.randomChance(0.04f).build(),
                        MobRankLootCondition.between("formation_establishment", 6, "formation_establishment", 9).build()
                },
                ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "lightning_essence_technique")
        ));

        add("wind_essence_technique_from_phantom", new AddTechniqueManualModifier(
                new LootItemCondition[] {
                        LootTableIdCondition.builder(ResourceLocation.withDefaultNamespace("entities/phantom")).build(),
                        LootItemKilledByPlayerCondition.killedByPlayer().build(),
                        LootItemRandomChanceCondition.randomChance(0.04f).build(),
                        MobRankLootCondition.between("formation_establishment", 7, "formation_establishment", 9).build()
                },
                ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "wind_essence_technique")
        ));

        add("five_element_cultivation_technique_from_end_city", new AddTechniqueManualModifier(
                new LootItemCondition[] {
                        LootTableIdCondition.builder(ResourceLocation.withDefaultNamespace("chests/end_city_treasure")).build(),
                        LootItemRandomChanceCondition.randomChance(0.03f).build()
                },
                ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "five_element_cultivation_technique")
        ));

        add("heart_fire_technique_from_nether_fortress", new AddTechniqueManualModifier(
                new LootItemCondition[] {
                        LootTableIdCondition.builder(ResourceLocation.withDefaultNamespace("chests/nether_bridge")).build(),
                        LootItemRandomChanceCondition.randomChance(0.05f).build()
                },
                ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "heart_fire_technique")
        ));

        add("heart_fire_technique_from_wither_skeleton", new AddTechniqueManualModifier(
                new LootItemCondition[] {
                        LootTableIdCondition.builder(ResourceLocation.withDefaultNamespace("entities/wither_skeleton")).build(),
                        LootItemKilledByPlayerCondition.killedByPlayer().build(),
                        LootItemRandomChanceCondition.randomChance(0.03f).build(),
                        MobRankLootCondition.between("qi_gathering", 1, "formation_establishment", 9).build()
                },
                ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "heart_fire_technique")
        ));

        add("kidney_water_technique_from_drowned", new AddTechniqueManualModifier(
                new LootItemCondition[] {
                        LootTableIdCondition.builder(ResourceLocation.withDefaultNamespace("entities/drowned")).build(),
                        LootItemKilledByPlayerCondition.killedByPlayer().build(),
                        LootItemRandomChanceCondition.randomChance(0.03f).build(),
                        MobRankLootCondition.between("qi_gathering", 1, "formation_establishment", 9).build()
                },
                ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "kidney_water_technique")
        ));

        add("kidney_water_technique_from_ocean_ruin", new AddTechniqueManualModifier(
                new LootItemCondition[] {
                        LootTableIdCondition.builder(ResourceLocation.withDefaultNamespace("chests/underwater_ruin_big")).build(),
                        LootItemRandomChanceCondition.randomChance(0.05f).build()
                },
                ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "kidney_water_technique")
        ));

        add("liver_wood_technique_from_jungle_temple", new AddTechniqueManualModifier(
                new LootItemCondition[] {
                        LootTableIdCondition.builder(ResourceLocation.withDefaultNamespace("chests/jungle_temple")).build(),
                        LootItemRandomChanceCondition.randomChance(0.05f).build()
                },
                ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "liver_wood_technique")
        ));

        add("spleen_earth_technique_from_desert_pyramid", new AddTechniqueManualModifier(
                new LootItemCondition[] {
                        LootTableIdCondition.builder(ResourceLocation.withDefaultNamespace("chests/desert_pyramid")).build(),
                        LootItemRandomChanceCondition.randomChance(0.05f).build()
                },
                ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "spleen_earth_technique")
        ));

        add("lung_metal_technique_from_mineshaft", new AddTechniqueManualModifier(
                new LootItemCondition[] {
                        LootTableIdCondition.builder(ResourceLocation.withDefaultNamespace("chests/abandoned_mineshaft")).build(),
                        LootItemRandomChanceCondition.randomChance(0.05f).build()
                },
                ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "lung_metal_technique")
        ));

        add("lung_metal_technique_from_iron_golem", new AddTechniqueManualModifier(
                new LootItemCondition[] {
                        LootTableIdCondition.builder(ResourceLocation.withDefaultNamespace("entities/iron_golem")).build(),
                        LootItemKilledByPlayerCondition.killedByPlayer().build(),
                        LootItemRandomChanceCondition.randomChance(0.03f).build(),
                        MobRankLootCondition.between("qi_gathering", 1, "formation_establishment", 9).build()
                },
                ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "lung_metal_technique")
        ));

        add("wood_fire_body_technique_from_mansion", new AddTechniqueManualModifier(
                new LootItemCondition[] {
                        LootTableIdCondition.builder(ResourceLocation.withDefaultNamespace("chests/woodland_mansion")).build(),
                        LootItemRandomChanceCondition.randomChance(0.04f).build()
                },
                ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "wood_fire_body_technique")
        ));

        add("fire_earth_body_technique_from_bastion", new AddTechniqueManualModifier(
                new LootItemCondition[] {
                        LootTableIdCondition.builder(ResourceLocation.withDefaultNamespace("chests/bastion_treasure")).build(),
                        LootItemRandomChanceCondition.randomChance(0.04f).build()
                },
                ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "fire_earth_body_technique")
        ));

        add("earth_metal_body_technique_from_ancient_city", new AddTechniqueManualModifier(
                new LootItemCondition[] {
                        LootTableIdCondition.builder(ResourceLocation.withDefaultNamespace("chests/ancient_city")).build(),
                        LootItemRandomChanceCondition.randomChance(0.04f).build()
                },
                ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "earth_metal_body_technique")
        ));

        add("metal_water_body_technique_from_shipwreck", new AddTechniqueManualModifier(
                new LootItemCondition[] {
                        LootTableIdCondition.builder(ResourceLocation.withDefaultNamespace("chests/shipwreck_treasure")).build(),
                        LootItemRandomChanceCondition.randomChance(0.04f).build()
                },
                ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "metal_water_body_technique")
        ));

        add("water_wood_body_technique_from_dungeon", new AddTechniqueManualModifier(
                new LootItemCondition[] {
                        LootTableIdCondition.builder(ResourceLocation.withDefaultNamespace("chests/simple_dungeon")).build(),
                        LootItemRandomChanceCondition.randomChance(0.04f).build()
                },
                ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "water_wood_body_technique")
        ));

        add("wood_fire_earth_body_technique_from_mansion", new AddTechniqueManualModifier(
                new LootItemCondition[] {
                        LootTableIdCondition.builder(ResourceLocation.withDefaultNamespace("chests/woodland_mansion")).build(),
                        LootItemRandomChanceCondition.randomChance(0.03f).build()
                },
                ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "wood_fire_earth_body_technique")
        ));

        add("fire_earth_metal_body_technique_from_bastion", new AddTechniqueManualModifier(
                new LootItemCondition[] {
                        LootTableIdCondition.builder(ResourceLocation.withDefaultNamespace("chests/bastion_treasure")).build(),
                        LootItemRandomChanceCondition.randomChance(0.03f).build()
                },
                ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "fire_earth_metal_body_technique")
        ));

        add("earth_metal_water_body_technique_from_ancient_city", new AddTechniqueManualModifier(
                new LootItemCondition[] {
                        LootTableIdCondition.builder(ResourceLocation.withDefaultNamespace("chests/ancient_city")).build(),
                        LootItemRandomChanceCondition.randomChance(0.03f).build()
                },
                ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "earth_metal_water_body_technique")
        ));

        add("metal_water_wood_body_technique_from_end_city", new AddTechniqueManualModifier(
                new LootItemCondition[] {
                        LootTableIdCondition.builder(ResourceLocation.withDefaultNamespace("chests/end_city_treasure")).build(),
                        LootItemRandomChanceCondition.randomChance(0.03f).build()
                },
                ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "metal_water_wood_body_technique")
        ));

        add("water_wood_fire_body_technique_from_stronghold", new AddTechniqueManualModifier(
                new LootItemCondition[] {
                        LootTableIdCondition.builder(ResourceLocation.withDefaultNamespace("chests/stronghold_library")).build(),
                        LootItemRandomChanceCondition.randomChance(0.03f).build()
                },
                ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "water_wood_fire_body_technique")
        ));

        add("wood_fire_earth_metal_body_technique_from_ancient_city", new AddTechniqueManualModifier(
                new LootItemCondition[] {
                        LootTableIdCondition.builder(ResourceLocation.withDefaultNamespace("chests/ancient_city")).build(),
                        LootItemRandomChanceCondition.randomChance(0.02f).build()
                },
                ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "wood_fire_earth_metal_body_technique")
        ));

        add("fire_earth_metal_water_body_technique_from_bastion", new AddTechniqueManualModifier(
                new LootItemCondition[] {
                        LootTableIdCondition.builder(ResourceLocation.withDefaultNamespace("chests/bastion_treasure")).build(),
                        LootItemRandomChanceCondition.randomChance(0.02f).build()
                },
                ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "fire_earth_metal_water_body_technique")
        ));

        add("earth_metal_water_wood_body_technique_from_end_city", new AddTechniqueManualModifier(
                new LootItemCondition[] {
                        LootTableIdCondition.builder(ResourceLocation.withDefaultNamespace("chests/end_city_treasure")).build(),
                        LootItemRandomChanceCondition.randomChance(0.02f).build()
                },
                ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "earth_metal_water_wood_body_technique")
        ));

        add("metal_water_wood_fire_body_technique_from_mansion", new AddTechniqueManualModifier(
                new LootItemCondition[] {
                        LootTableIdCondition.builder(ResourceLocation.withDefaultNamespace("chests/woodland_mansion")).build(),
                        LootItemRandomChanceCondition.randomChance(0.02f).build()
                },
                ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "metal_water_wood_fire_body_technique")
        ));

        add("water_wood_fire_earth_body_technique_from_stronghold", new AddTechniqueManualModifier(
                new LootItemCondition[] {
                        LootTableIdCondition.builder(ResourceLocation.withDefaultNamespace("chests/stronghold_library")).build(),
                        LootItemRandomChanceCondition.randomChance(0.02f).build()
                },
                ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "water_wood_fire_earth_body_technique")
        ));

        add("scholarly_soul_technique_from_stronghold_library", new AddTechniqueManualModifier(
                new LootItemCondition[] {
                        LootTableIdCondition.builder(ResourceLocation.withDefaultNamespace("chests/stronghold_library")).build(),
                        LootItemRandomChanceCondition.randomChance(0.04f).build()
                },
                ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "scholarly_soul_technique")
        ));

        add("scholarly_soul_technique_from_zombie_villager", new AddTechniqueManualModifier(
                new LootItemCondition[] {
                        LootTableIdCondition.builder(ResourceLocation.withDefaultNamespace("entities/zombie_villager")).build(),
                        LootItemKilledByPlayerCondition.killedByPlayer().build(),
                        LootItemRandomChanceCondition.randomChance(0.03f).build(),
                        MobRankLootCondition.between("formation_establishment", 6, "formation_establishment", 9).build()
                },
                ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "scholarly_soul_technique")
        ));

        add("pale_moon_scripture_from_ancient_city", new AddTechniqueManualModifier(
                new LootItemCondition[] {
                        LootTableIdCondition.builder(ResourceLocation.withDefaultNamespace("chests/ancient_city")).build(),
                        LootItemRandomChanceCondition.randomChance(0.04f).build()
                },
                ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "pale_moon_scripture")
        ));

        add("pale_moon_scripture_from_phantom", new AddTechniqueManualModifier(
                new LootItemCondition[] {
                        LootTableIdCondition.builder(ResourceLocation.withDefaultNamespace("entities/phantom")).build(),
                        LootItemKilledByPlayerCondition.killedByPlayer().build(),
                        LootItemRandomChanceCondition.randomChance(0.03f).build(),
                        MobRankLootCondition.between("formation_establishment", 8, "formation_establishment", 9).build()
                },
                ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "pale_moon_scripture")
        ));

        add("gibbous_moon_scripture_from_end_city", new AddTechniqueManualModifier(
                new LootItemCondition[] {
                        LootTableIdCondition.builder(ResourceLocation.withDefaultNamespace("chests/end_city_treasure")).build(),
                        LootItemRandomChanceCondition.randomChance(0.03f).build()
                },
                ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "gibbous_moon_scripture")
        ));

        add("gibbous_moon_scripture_from_enderman", new AddTechniqueManualModifier(
                new LootItemCondition[] {
                        LootTableIdCondition.builder(ResourceLocation.withDefaultNamespace("entities/enderman")).build(),
                        LootItemKilledByPlayerCondition.killedByPlayer().build(),
                        LootItemRandomChanceCondition.randomChance(0.02f).build(),
                        MobRankLootCondition.between("formation_establishment", 8, "formation_establishment", 9).build()
                },
                ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "gibbous_moon_scripture")
        ));

        add("sword_comprehension_technique_from_pillager_outpost", new AddTechniqueManualModifier(
                new LootItemCondition[] {
                        LootTableIdCondition.builder(ResourceLocation.withDefaultNamespace("chests/pillager_outpost")).build(),
                        LootItemRandomChanceCondition.randomChance(0.05f).build()
                },
                ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "sword_comprehension_technique")
        ));

        add("sword_comprehension_technique_from_vindicator", new AddTechniqueManualModifier(
                new LootItemCondition[] {
                        LootTableIdCondition.builder(ResourceLocation.withDefaultNamespace("entities/vindicator")).build(),
                        LootItemKilledByPlayerCondition.killedByPlayer().build(),
                        LootItemRandomChanceCondition.randomChance(0.03f).build(),
                        MobRankLootCondition.between("formation_establishment", 4, "formation_establishment", 9).build()
                },
                ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "sword_comprehension_technique")
        ));

        add("sword_comprehension_technique_from_mansion", new AddTechniqueManualModifier(
                new LootItemCondition[] {
                        LootTableIdCondition.builder(ResourceLocation.withDefaultNamespace("chests/woodland_mansion")).build(),
                        LootItemRandomChanceCondition.randomChance(0.04f).build()
                },
                ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "sword_comprehension_technique")
        ));

        add("dawning_sun_scripture_from_nether_fortress", new AddTechniqueManualModifier(
                new LootItemCondition[] {
                        LootTableIdCondition.builder(ResourceLocation.withDefaultNamespace("chests/nether_bridge")).build(),
                        LootItemRandomChanceCondition.randomChance(0.07f).build()
                },
                ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "dawning_sun_scripture")
        ));

        add("dawning_sun_scripture_from_blaze", new AddTechniqueManualModifier(
                new LootItemCondition[] {
                        LootTableIdCondition.builder(ResourceLocation.withDefaultNamespace("entities/blaze")).build(),
                        LootItemKilledByPlayerCondition.killedByPlayer().build(),
                        LootItemRandomChanceCondition.randomChance(0.03f).build(),
                        MobRankLootCondition.between("formation_establishment", 6, "formation_establishment", 9).build()
                },
                ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "dawning_sun_scripture")
        ));

        add("zenith_sun_scripture_from_bastion", new AddTechniqueManualModifier(
                new LootItemCondition[] {
                        LootTableIdCondition.builder(ResourceLocation.withDefaultNamespace("chests/bastion_treasure")).build(),
                        LootItemRandomChanceCondition.randomChance(0.03f).build()
                },
                ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "zenith_sun_scripture")
        ));

        add("zenith_sun_scripture_from_wither", new AddTechniqueManualModifier(
                new LootItemCondition[] {
                        LootTableIdCondition.builder(ResourceLocation.withDefaultNamespace("entities/wither")).build(),
                        LootItemKilledByPlayerCondition.killedByPlayer().build(),
                        LootItemRandomChanceCondition.randomChance(0.05f).build()
                },
                ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "zenith_sun_scripture")
        ));



        //Physiques

        // Elemental Bodies

        add("fire_body_from_blaze", new AddPhysiqueRandomPurityModifier(
                new LootItemCondition[] {
                        LootTableIdCondition.builder(ResourceLocation.withDefaultNamespace("entities/blaze")).build(),
                        LootItemRandomChanceCondition.randomChance(0.08f).build()
                },
                ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "fire_body"),
                8,
                18
        ));

        add("fire_body_from_nether_fortress", new AddPhysiqueRandomPurityModifier(
                new LootItemCondition[] {
                        LootTableIdCondition.builder(ResourceLocation.withDefaultNamespace("chests/nether_bridge")).build(),
                        LootItemRandomChanceCondition.randomChance(0.05f).build()
                },
                ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "fire_body"),
                10,
                20
        ));

        add("water_body_from_elder_guardian", new AddPhysiqueRandomPurityModifier(
                new LootItemCondition[] {
                        LootTableIdCondition.builder(ResourceLocation.withDefaultNamespace("entities/elder_guardian")).build(),
                        LootItemRandomChanceCondition.randomChance(0.07f).build()
                },
                ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "water_body"),
                8,
                18
        ));

        add("water_body_from_ocean_ruin", new AddPhysiqueRandomPurityModifier(
                new LootItemCondition[] {
                        LootTableIdCondition.builder(ResourceLocation.withDefaultNamespace("chests/underwater_ruin_big")).build(),
                        LootItemRandomChanceCondition.randomChance(0.05f).build()
                },
                ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "water_body"),
                10,
                20
        ));

        add("wood_body_from_jungle_temple", new AddPhysiqueRandomPurityModifier(
                new LootItemCondition[] {
                        LootTableIdCondition.builder(ResourceLocation.withDefaultNamespace("chests/jungle_temple")).build(),
                        LootItemRandomChanceCondition.randomChance(0.05f).build()
                },
                ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "wood_body"),
                10,
                20
        ));

        add("wood_body_from_woodland_mansion", new AddPhysiqueRandomPurityModifier(
                new LootItemCondition[] {
                        LootTableIdCondition.builder(ResourceLocation.withDefaultNamespace("chests/woodland_mansion")).build(),
                        LootItemRandomChanceCondition.randomChance(0.04f).build()
                },
                ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "wood_body"),
                10,
                20
        ));

        add("earth_body_from_desert_pyramid", new AddPhysiqueRandomPurityModifier(
                new LootItemCondition[] {
                        LootTableIdCondition.builder(ResourceLocation.withDefaultNamespace("chests/desert_pyramid")).build(),
                        LootItemRandomChanceCondition.randomChance(0.05f).build()
                },
                ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "earth_body"),
                10,
                20
        ));

        add("earth_body_from_ancient_city", new AddPhysiqueRandomPurityModifier(
                new LootItemCondition[] {
                        LootTableIdCondition.builder(ResourceLocation.withDefaultNamespace("chests/ancient_city")).build(),
                        LootItemRandomChanceCondition.randomChance(0.04f).build()
                },
                ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "earth_body"),
                12,
                22
        ));

        add("metal_body_from_iron_golem", new AddPhysiqueRandomPurityModifier(
                new LootItemCondition[] {
                        LootTableIdCondition.builder(ResourceLocation.withDefaultNamespace("entities/iron_golem")).build(),
                        LootItemRandomChanceCondition.randomChance(0.06f).build()
                },
                ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "metal_body"),
                8,
                18
        ));

        add("metal_body_from_mineshaft", new AddPhysiqueRandomPurityModifier(
                new LootItemCondition[] {
                        LootTableIdCondition.builder(ResourceLocation.withDefaultNamespace("chests/abandoned_mineshaft")).build(),
                        LootItemRandomChanceCondition.randomChance(0.05f).build()
                },
                ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "metal_body"),
                10,
                20
        ));

        add("severed_meridians_from_witch", new AddPhysiqueRandomPurityModifier(
                new LootItemCondition[] {
                        LootTableIdCondition.builder(ResourceLocation.parse("minecraft:entities/witch")).build(),
                        LootItemRandomChanceCondition.randomChance(0.20f).build()
                },
                ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "severed_meridians"),
                5,
                12
        ));

        add("sword_bone_from_skeleton", new AddPhysiqueRandomPurityModifier(
                new LootItemCondition[] {
                        LootTableIdCondition.builder(ResourceLocation.parse("minecraft:entities/skeleton")).build(),
                        LootItemRandomChanceCondition.randomChance(0.16f).build()
                },
                ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "sword_bone"),
                5,
                15
        ));

        add("flame_touched_from_magma_cube", new AddPhysiqueRandomPurityModifier(
                new LootItemCondition[] {
                        LootTableIdCondition.builder(ResourceLocation.parse("minecraft:entities/magma_cube")).build(),
                        LootItemRandomChanceCondition.randomChance(0.17f).build()
                },
                ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "flame_touched"),
                5,
                15
        ));

        add("fire_attuned_from_piglin_barter", new AddPhysiqueRandomPurityModifier(
                new LootItemCondition[] {
                        LootTableIdCondition.builder(ResourceLocation.parse("minecraft:gameplay/piglin_bartering")).build(),
                        LootItemRandomChanceCondition.randomChance(0.16f).build()
                },
                ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "fire_attuned"),
                5,
                12
        ));

        add("water_attuned_from_fishing", new AddPhysiqueRandomPurityModifier(
                new LootItemCondition[] {
                        LootTableIdCondition.builder(ResourceLocation.parse("minecraft:gameplay/fishing")).build(),
                        LootItemRandomChanceCondition.randomChance(0.21f).build()
                },
                ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "water_attuned"),
                5,
                12
        ));

        add("earth_attuned_from_trail_ruins", new AddPhysiqueRandomPurityModifier(
                new LootItemCondition[] {
                        LootTableIdCondition.builder(ResourceLocation.parse("minecraft:archaeology/trail_ruins_common")).build(),
                        LootItemRandomChanceCondition.randomChance(0.23f).build()
                },
                ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "earth_attuned"),
                5,
                12
        ));

        add("wood_attuned_from_jungle_temple", new AddPhysiqueRandomPurityModifier(
                new LootItemCondition[] {
                        LootTableIdCondition.builder(ResourceLocation.parse("minecraft:chests/jungle_temple")).build(),
                        LootItemRandomChanceCondition.randomChance(0.21f).build()
                },
                ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "wood_attuned"),
                5,
                12
        ));

        add("metal_attuned_from_mineshaft", new AddPhysiqueRandomPurityModifier(
                new LootItemCondition[] {
                        LootTableIdCondition.builder(ResourceLocation.parse("minecraft:chests/abandoned_mineshaft")).build(),
                        LootItemRandomChanceCondition.randomChance(0.15f).build()
                },
                ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "metal_attuned"),
                5,
                15
        ));

        add("lightning_attuned_from_creeper", new AddPhysiqueRandomPurityModifier(
                new LootItemCondition[] {
                        LootTableIdCondition.builder(ResourceLocation.parse("minecraft:entities/creeper")).build(),
                        LootItemRandomChanceCondition.randomChance(0.16f).build()
                },
                ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "lightning_attuned"),
                5,
                15
        ));

        add("wind_attuned_from_phantom", new AddPhysiqueRandomPurityModifier(
                new LootItemCondition[] {
                        LootTableIdCondition.builder(ResourceLocation.parse("minecraft:entities/phantom")).build(),
                        LootItemRandomChanceCondition.randomChance(0.20f).build()
                },
                ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "wind_attuned"),
                5,
                15
        ));

        add("mortal_essence_bone_from_dungeon", new AddPhysiqueRandomPurityModifier(
                new LootItemCondition[] {
                        LootTableIdCondition.builder(ResourceLocation.parse("minecraft:chests/simple_dungeon")).build(),
                        LootItemRandomChanceCondition.randomChance(0.16f).build()
                },
                ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "mortal_essence_bone"),
                11,
                24
        ));

//        add("special_essence_bone_from_desert_pyramid", new AddPhysiqueRandomPurityModifier(
//                new LootItemCondition[] {
//                        LootTableIdCondition.builder(ResourceLocation.parse("minecraft:chests/desert_pyramid")).build(),
//                        LootItemRandomChanceCondition.randomChance(0.19f).build()
//                },
//                ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "special_essence_bone"),
//                10,
//                22
//        ));
//
//        add("heavenly_essence_bone_from_end_city", new AddPhysiqueRandomPurityModifier(
//                new LootItemCondition[] {
//                        LootTableIdCondition.builder(ResourceLocation.parse("minecraft:chests/end_city_treasure")).build(),
//                        LootItemRandomChanceCondition.randomChance(0.15f).build()
//                },
//                ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "heavenly_essence_bone"),
//                12,
//                28
//        ));
//
//        add("divine_essence_bone_from_ancient_city", new AddPhysiqueRandomPurityModifier(
//                new LootItemCondition[] {
//                        LootTableIdCondition.builder(ResourceLocation.parse("minecraft:chests/ancient_city")).build(),
//                        LootItemRandomChanceCondition.randomChance(0.17f).build()
//                },
//                ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "divine_essence_bone"),
//                15,
//                35
//        ));


        add("flow_severing_eyes_from_enderman", new AddPhysiqueRandomPurityModifier(
                new LootItemCondition[] {
                        LootTableIdCondition.builder(ResourceLocation.parse("minecraft:entities/enderman")).build(),
                        LootItemRandomChanceCondition.randomChance(0.21f).build()
                },
                ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "flow_severing_eyes"),
                8,
                18
        ));

        add("thin_sword_pulse_from_stray", new AddPhysiqueRandomPurityModifier(
                new LootItemCondition[] {
                        LootTableIdCondition.builder(ResourceLocation.parse("minecraft:entities/stray")).build(),
                        LootItemRandomChanceCondition.randomChance(0.17f).build()
                },
                ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "thin_sword_pulse"),
                5,
                15
        ));

        add("wild_cleaver_veteran_from_vindicator", new AddPhysiqueRandomPurityModifier(
                new LootItemCondition[] {
                        LootTableIdCondition.builder(ResourceLocation.parse("minecraft:entities/vindicator")).build(),
                        LootItemRandomChanceCondition.randomChance(0.22f).build()
                },
                ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "wild_cleaver_veteran"),
                5,
                15
        ));

        add("arrow_blessed_from_pillager", new AddPhysiqueRandomPurityModifier(
                new LootItemCondition[] {
                        LootTableIdCondition.builder(ResourceLocation.parse("minecraft:entities/pillager")).build(),
                        LootItemRandomChanceCondition.randomChance(0.19f).build()
                },
                ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "arrow_blessed"),
                5,
                15
        ));

        add("iron_bulwark_spine_from_mansion", new AddPhysiqueRandomPurityModifier(
                new LootItemCondition[] {
                        LootTableIdCondition.builder(ResourceLocation.parse("minecraft:chests/woodland_mansion")).build(),
                        LootItemRandomChanceCondition.randomChance(0.23f).build()
                },
                ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "iron_bulwark_spine"),
                8,
                18
        ));

        add("bruised_knuckle_body_from_ravager", new AddPhysiqueRandomPurityModifier(
                new LootItemCondition[] {
                        LootTableIdCondition.builder(ResourceLocation.parse("minecraft:entities/ravager")).build(),
                        LootItemRandomChanceCondition.randomChance(0.21f).build()
                },
                ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "bruised_knuckle_body"),
                8,
                18
        ));

        add("pointed_eyes_from_guardian", new AddPhysiqueRandomPurityModifier(
                new LootItemCondition[] {
                        LootTableIdCondition.builder(ResourceLocation.parse("minecraft:entities/guardian")).build(),
                        LootItemRandomChanceCondition.randomChance(0.19f).build()
                },
                ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "pointed_eyes"),
                5,
                15
        ));

//        add("blood_fiend_from_bastion", new AddPhysiqueRandomPurityModifier(
//                new LootItemCondition[] {
//                        LootTableIdCondition.builder(ResourceLocation.parse("minecraft:chests/bastion_treasure")).build(),
//                        LootItemRandomChanceCondition.randomChance(0.30f).build()
//                },
//                ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "blood_fiend"),
//                8,
//                20
//        ));

        add("stone_monkey_from_desert_archaeology", new AddPhysiqueRandomPurityModifier(
                new LootItemCondition[] {
                        LootTableIdCondition.builder(ResourceLocation.parse("minecraft:archaeology/desert_pyramid")).build(),
                        LootItemRandomChanceCondition.randomChance(0.23f).build()
                },
                ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "stone_monkey"),
                8,
                18
        ));

        add("tyrant_body_from_evoker", new AddPhysiqueRandomPurityModifier(
                new LootItemCondition[] {
                        LootTableIdCondition.builder(ResourceLocation.parse("minecraft:entities/evoker")).build(),
                        LootItemRandomChanceCondition.randomChance(0.20f).build()
                },
                ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "tyrant_body"),
                10,
                22
        ));

        add("academic_spirit_from_library", new AddPhysiqueRandomPurityModifier(
                new LootItemCondition[] {
                        LootTableIdCondition.builder(ResourceLocation.parse("minecraft:chests/stronghold_library")).build(),
                        LootItemRandomChanceCondition.randomChance(0.19f).build()
                },
                ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "academic_spirit"),
                8,
                18
        ));

        add("world_dominator_from_warden", new AddPhysiqueRandomPurityModifier(
                new LootItemCondition[] {
                        LootTableIdCondition.builder(ResourceLocation.parse("minecraft:entities/warden")).build(),
                        LootItemRandomChanceCondition.randomChance(0.04f).build()
                },
                ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "world_dominator"),
                15,
                30
        ));

        add("dreaming_soul_from_phantom", new AddPhysiqueRandomPurityModifier(
                new LootItemCondition[] {
                        LootTableIdCondition.builder(ResourceLocation.parse("minecraft:entities/phantom")).build(),
                        LootItemKilledByPlayerCondition.killedByPlayer().build(),
                        LootItemRandomChanceCondition.randomChance(0.14f).build(),
                        MobRankLootCondition.atLeast("formation_establishment", 5).build()

                },
                ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "dreaming_soul"),
                8,
                18
        ));

        add("soul_gaze_from_ancient_city", new AddPhysiqueRandomPurityModifier(
                new LootItemCondition[] {
                        LootTableIdCondition.builder(ResourceLocation.parse("minecraft:chests/ancient_city")).build(),
                        LootItemRandomChanceCondition.randomChance(0.10f).build()
                },
                ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "soul_gaze"),
                12,
                24
        ));

        add("ashen_soul_flame_from_nether_fortress", new AddPhysiqueRandomPurityModifier(
                new LootItemCondition[] {
                        LootTableIdCondition.builder(ResourceLocation.parse("minecraft:chests/nether_bridge")).build(),
                        LootItemRandomChanceCondition.randomChance(0.08f).build()
                },
                ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "ashen_soul_flame"),
                14,
                30
        ));

        add("thundering_soul_core_from_end_city", new AddPhysiqueRandomPurityModifier(
                new LootItemCondition[] {
                        LootTableIdCondition.builder(ResourceLocation.parse("minecraft:chests/end_city_treasure")).build(),
                        LootItemRandomChanceCondition.randomChance(0.07f).build()
                },
                ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "thundering_soul_core"),
                16,
                34
        ));

        add("soul_sword_heart_from_vindicator", new AddPhysiqueRandomPurityModifier(
                new LootItemCondition[] {
                        LootTableIdCondition.builder(ResourceLocation.parse("minecraft:entities/vindicator")).build(),
                        LootItemKilledByPlayerCondition.killedByPlayer().build(),
                        LootItemRandomChanceCondition.randomChance(0.09f).build(),
                        MobRankLootCondition.atLeast("formation_establishment", 8).build()
                },
                ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "soul_sword_heart"),
                12,
                28
        ));

        add("spear_soul_mark_from_pillager", new AddPhysiqueRandomPurityModifier(
                new LootItemCondition[] {
                        LootTableIdCondition.builder(ResourceLocation.parse("minecraft:entities/pillager")).build(),
                        LootItemKilledByPlayerCondition.killedByPlayer().build(),
                        LootItemRandomChanceCondition.randomChance(0.11f).build(),
                        MobRankLootCondition.atLeast("formation_establishment", 6).build()
                },
                ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "spear_soul_mark"),
                8,
                20
        ));


        //Herbs

        this.add("jade_dew_grass_from_village", new AddItemModifier(new LootItemCondition[] {
                LootTableIdCondition.builder(ResourceLocation.withDefaultNamespace("chests/village/village_plains_house")).build(),
                LootItemRandomChanceCondition.randomChance(0.27F).build()
        }, ModItems.JADE_DEW_GRASS_SEEDS.get()));
        this.add("hundred_year_ginseng_from_village", new AddItemModifier(new LootItemCondition[] {
                LootTableIdCondition.builder(ResourceLocation.withDefaultNamespace("chests/village/village_plains_house")).build(),
                LootItemRandomChanceCondition.randomChance(0.16F).build()
        }, ModItems.HUNDRED_YEAR_GINSENG.get()));
        this.add("hundred_year_ginseng_from_dungeon", new AddItemModifier(new LootItemCondition[] {
                LootTableIdCondition.builder(ResourceLocation.withDefaultNamespace("chests/simple_dungeon")).build(),
                LootItemRandomChanceCondition.randomChance(0.19F).build()
        }, ModItems.HUNDRED_YEAR_GINSENG.get()));
        this.add("hundred_year_snow_ginseng_from_igloo", new AddItemModifier(new LootItemCondition[] {
                LootTableIdCondition.builder(ResourceLocation.withDefaultNamespace("chests/igloo_chest")).build(),
                LootItemRandomChanceCondition.randomChance(0.18F).build()
        }, ModItems.HUNDRED_YEAR_SNOW_GINSENG.get()));
        this.add("hundred_year_snow_ginseng_from_snowy_village", new AddItemModifier(new LootItemCondition[] {
                LootTableIdCondition.builder(ResourceLocation.withDefaultNamespace("chests/village/village_snowy_house")).build(),
                LootItemRandomChanceCondition.randomChance(0.19F).build()
        }, ModItems.HUNDRED_YEAR_SNOW_GINSENG.get()));
        this.add("hundred_year_fire_ginseng_from_desert_temple", new AddItemModifier(new LootItemCondition[] {
                LootTableIdCondition.builder(ResourceLocation.withDefaultNamespace("chests/desert_pyramid")).build(),
                LootItemRandomChanceCondition.randomChance(0.14F).build()
        }, ModItems.HUNDRED_YEAR_FIRE_GINSENG.get()));
        this.add("hundred_year_fire_ginseng_from_bastion", new AddItemModifier(new LootItemCondition[] {
                LootTableIdCondition.builder(ResourceLocation.withDefaultNamespace("chests/bastion_treasure")).build(),
                LootItemRandomChanceCondition.randomChance(0.19F).build()
        }, ModItems.HUNDRED_YEAR_FIRE_GINSENG.get()));
        this.add("white_jade_orchid_from_mineshaft", new AddItemModifier(new LootItemCondition[] {
                LootTableIdCondition.builder(ResourceLocation.withDefaultNamespace("chests/abandoned_mineshaft")).build(),
                LootItemRandomChanceCondition.randomChance(0.20F).build()
        }, ModItems.WHITE_JADE_ORCHID.get()));
        this.add("white_jade_orchid_from_stronghold", new AddItemModifier(new LootItemCondition[] {
                LootTableIdCondition.builder(ResourceLocation.withDefaultNamespace("chests/stronghold_corridor")).build(),
                LootItemRandomChanceCondition.randomChance(0.18F).build()
        }, ModItems.WHITE_JADE_ORCHID.get()));



        this.add("jade_bamboo_of_serenity_from_bamboo", new AddItemModifier(new LootItemCondition[] {
                new LootTableIdCondition.Builder(ResourceLocation.withDefaultNamespace("blocks/bamboo")).build(),
                LootItemRandomChanceCondition.randomChance(0.12F).build()}, ModItems.JADE_BAMBOO_OF_SERENITY.get()));

        //Living Core Drop Regular Mobs
        this.add("living_core_from_strider", new AddItemModifier(new LootItemCondition[] {
                new LootTableIdCondition.Builder(ResourceLocation.withDefaultNamespace("entities/strider")).build(),
                LootItemRandomChanceCondition.randomChance(0.15F).build()}, ModItems.LIVING_CORE.get()));
        this.add("living_core_from_piglin", new AddItemModifier(new LootItemCondition[] {
                new LootTableIdCondition.Builder(ResourceLocation.withDefaultNamespace("entities/piglin")).build(),
                LootItemRandomChanceCondition.randomChance(0.15F).build()}, ModItems.LIVING_CORE.get()));
        this.add("living_core_from_hoglin", new AddItemModifier(new LootItemCondition[] {
                new LootTableIdCondition.Builder(ResourceLocation.withDefaultNamespace("entities/hoglin")).build(),
                LootItemRandomChanceCondition.randomChance(0.15F).build()}, ModItems.LIVING_CORE.get()));
        this.add("living_core_from_zoglin", new AddItemModifier(new LootItemCondition[] {
                new LootTableIdCondition.Builder(ResourceLocation.withDefaultNamespace("entities/zoglin")).build(),
                LootItemRandomChanceCondition.randomChance(0.15F).build()}, ModItems.LIVING_CORE.get()));
        this.add("living_core_from_vex", new AddItemModifier(new LootItemCondition[] {
                new LootTableIdCondition.Builder(ResourceLocation.withDefaultNamespace("entities/vex")).build(),
                LootItemRandomChanceCondition.randomChance(0.15F).build()}, ModItems.LIVING_CORE.get()));
        this.add("living_core_from_allay", new AddItemModifier(new LootItemCondition[] {
                new LootTableIdCondition.Builder(ResourceLocation.withDefaultNamespace("entities/allay")).build(),
                LootItemRandomChanceCondition.randomChance(0.15F).build()}, ModItems.LIVING_CORE.get()));
        this.add("living_core_from_golem", new AddItemModifier(new LootItemCondition[] {
                new LootTableIdCondition.Builder(ResourceLocation.withDefaultNamespace("entities/iron_golem")).build(),
                LootItemRandomChanceCondition.randomChance(0.15F).build()}, ModItems.LIVING_CORE.get()));

        //Living Core Drop Boss Undead
        this.add("living_core_from_ender_dragon", new AddItemModifier(new LootItemCondition[] {
                new LootTableIdCondition.Builder(ResourceLocation.withDefaultNamespace("entities/ender_dragon")).build(),
                LootItemRandomChanceCondition.randomChance(0.29F).build()}, ModItems.LIVING_CORE.get()));







        //Undead Core Drop Regular Undead Mobs
        this.add("undead_core_from_zombie", new AddItemModifier(new LootItemCondition[] {
                new LootTableIdCondition.Builder(ResourceLocation.withDefaultNamespace("entities/zombie")).build(),
                LootItemRandomChanceCondition.randomChance(0.15F).build()}, ModItems.UNDEAD_CORE.get()));
        this.add("undead_core_from_skeleton", new AddItemModifier(new LootItemCondition[] {
                new LootTableIdCondition.Builder(ResourceLocation.withDefaultNamespace("entities/skeleton")).build(),
                LootItemRandomChanceCondition.randomChance(0.15F).build()}, ModItems.UNDEAD_CORE.get()));
        this.add("undead_core_from_wither_skeleton", new AddItemModifier(new LootItemCondition[] {
                new LootTableIdCondition.Builder(ResourceLocation.withDefaultNamespace("entities/wither_skeleton")).build(),
                LootItemRandomChanceCondition.randomChance(0.15F).build()}, ModItems.UNDEAD_CORE.get()));
        this.add("undead_core_from_stray", new AddItemModifier(new LootItemCondition[] {
                new LootTableIdCondition.Builder(ResourceLocation.withDefaultNamespace("entities/stray")).build(),
                LootItemRandomChanceCondition.randomChance(0.15F).build()}, ModItems.UNDEAD_CORE.get()));
        this.add("undead_core_from_husk", new AddItemModifier(new LootItemCondition[] {
                new LootTableIdCondition.Builder(ResourceLocation.withDefaultNamespace("entities/husk")).build(),
                LootItemRandomChanceCondition.randomChance(0.15F).build()}, ModItems.UNDEAD_CORE.get()));
        this.add("undead_core_from_drowned", new AddItemModifier(new LootItemCondition[] {
                new LootTableIdCondition.Builder(ResourceLocation.withDefaultNamespace("entities/drowned")).build(),
                LootItemRandomChanceCondition.randomChance(0.15F).build()}, ModItems.UNDEAD_CORE.get()));
        this.add("undead_core_from_zombie_villager", new AddItemModifier(new LootItemCondition[] {
                new LootTableIdCondition.Builder(ResourceLocation.withDefaultNamespace("entities/zombie_villager")).build(),
                LootItemRandomChanceCondition.randomChance(0.15F).build()}, ModItems.UNDEAD_CORE.get()));
        this.add("undead_core_from_zombified_piglin", new AddItemModifier(new LootItemCondition[] {
                new LootTableIdCondition.Builder(ResourceLocation.withDefaultNamespace("entities/zombified_piglin")).build(),
                LootItemRandomChanceCondition.randomChance(0.15F).build()}, ModItems.UNDEAD_CORE.get()));
        this.add("undead_core_from_zombie_horse", new AddItemModifier(new LootItemCondition[] {
                new LootTableIdCondition.Builder(ResourceLocation.withDefaultNamespace("entities/zombie_horse")).build(),
                LootItemRandomChanceCondition.randomChance(0.15F).build()}, ModItems.UNDEAD_CORE.get()));
        this.add("undead_core_from_phantom", new AddItemModifier(new LootItemCondition[] {
                new LootTableIdCondition.Builder(ResourceLocation.withDefaultNamespace("entities/phantom")).build(),
                LootItemRandomChanceCondition.randomChance(0.15F).build()}, ModItems.UNDEAD_CORE.get()));
        this.add("undead_core_from_zoglin", new AddItemModifier(new LootItemCondition[] {
                new LootTableIdCondition.Builder(ResourceLocation.withDefaultNamespace("entities/zoglin")).build(),
                LootItemRandomChanceCondition.randomChance(0.15F).build()}, ModItems.UNDEAD_CORE.get()));
        //Undead Core Drop Boss Undead
        this.add("undead_core_from_wither", new AddItemModifier(new LootItemCondition[] {
                new LootTableIdCondition.Builder(ResourceLocation.withDefaultNamespace("entities/wither")).build(),
                LootItemRandomChanceCondition.randomChance(0.29F).build()}, ModItems.UNDEAD_CORE.get()));



        // Example implementation of rank conditional drops (P.S. this can also be used in ModEntityLootTables (different pattern))

        /*
                Example of at least (if golden core or greater, drops a hundred-year ginseng when killed by a player)
        this.add("hundred_year_ginseng_from_ranked_zombie", new AddItemModifier(new LootItemCondition[] {
                LootTableIdCondition.builder(ResourceLocation.withDefaultNamespace("entities/zombie")).build(),
                LootItemKilledByPlayerCondition.killedByPlayer().build(),
                LootItemRandomChanceCondition.randomChance(0.10F).build(),
                MobRankLootCondition.atLeast("golden_core", 1).build()
        }, ModItems.HUNDRED_YEAR_GINSENG.get()));

                Example of exact (if exactly nascent soul, drops a white jade orchid when killed by a player)
        this.add("white_jade_orchid_from_ranked_zombie", new AddItemModifier(new LootItemCondition[] {
                LootTableIdCondition.builder(ResourceLocation.withDefaultNamespace("entities/zombie")).build(),
                LootItemKilledByPlayerCondition.killedByPlayer().build(),
                LootItemRandomChanceCondition.randomChance(0.25F).build(),
                MobRankLootCondition.exact("nascent_soul", 2).build()
        }, ModItems.WHITE_JADE_ORCHID.get()));

                Example of between (if between soul formation and tribulation transcendence, drops a hundred-year fire ginseng when killed by a player)
        this.add("hundred_year_fire_ginseng_from_ranked_zombie", new AddItemModifier(new LootItemCondition[] {
                LootTableIdCondition.builder(ResourceLocation.withDefaultNamespace("entities/zombie")).build(),
                LootItemKilledByPlayerCondition.killedByPlayer().build(),
                LootItemRandomChanceCondition.randomChance(0.18F).build(),
                MobRankLootCondition.between("soul_formation", 1, "tribulation_transcendence", 3).build()
        }, ModItems.HUNDRED_YEAR_FIRE_GINSENG.get()));
        */



    }
}
