package net.thejadeproject.ascension.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemKilledByPlayerCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.neoforged.neoforge.common.data.GlobalLootModifierProvider;
import net.neoforged.neoforge.common.loot.LootTableIdCondition;
import net.thejadeproject.ascension.AscensionCraft;
import net.thejadeproject.ascension.common.blocks.ModBlocks;
import net.thejadeproject.ascension.common.items.ModItems;
import net.thejadeproject.ascension.datagen.loot.*;
import net.thejadeproject.ascension.datagen.loot.conditions.AddTechniqueManualModifier;
import net.thejadeproject.ascension.datagen.loot.conditions.MobRankLootCondition;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ModGlobalLootModifierProvider extends GlobalLootModifierProvider {
    public ModGlobalLootModifierProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, AscensionCraft.MOD_ID);
    }
    @Override
    protected void start() {

        // ── Bloodline Essences ────────────────────────────────────────────────

        add("human_bloodline_from_village_house", new AddBloodlineRandomPurityModifier(
                new LootItemCondition[]{
                        LootTableIdCondition.builder(ResourceLocation.withDefaultNamespace("chests/village/village_plains_house")).build(),
                        LootItemRandomChanceCondition.randomChance(0.25f).build()
                },
                ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "human_bloodline"), 10, 60
        ));
        add("human_bloodline_from_village_temple", new AddBloodlineRandomPurityModifier(
                new LootItemCondition[]{
                        LootTableIdCondition.builder(ResourceLocation.withDefaultNamespace("chests/village/village_desert_house")).build(),
                        LootItemRandomChanceCondition.randomChance(0.25f).build()
                },
                ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "human_bloodline"), 10, 60
        ));

        add("beast_bloodline_from_wolf", new AddBloodlineRandomPurityModifier(
                new LootItemCondition[]{
                        LootTableIdCondition.builder(ResourceLocation.withDefaultNamespace("entities/wolf")).build(),
                        LootItemKilledByPlayerCondition.killedByPlayer().build(),
                        LootItemRandomChanceCondition.randomChance(0.12f).build()
                },
                ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "beast_bloodline"), 5, 40
        ));
        add("beast_bloodline_from_spider", new AddBloodlineRandomPurityModifier(
                new LootItemCondition[]{
                        LootTableIdCondition.builder(ResourceLocation.withDefaultNamespace("entities/spider")).build(),
                        LootItemKilledByPlayerCondition.killedByPlayer().build(),
                        LootItemRandomChanceCondition.randomChance(0.12f).build()
                },
                ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "beast_bloodline"), 5, 40
        ));
        add("beast_bloodline_from_cave_spider", new AddBloodlineRandomPurityModifier(
                new LootItemCondition[]{
                        LootTableIdCondition.builder(ResourceLocation.withDefaultNamespace("entities/cave_spider")).build(),
                        LootItemKilledByPlayerCondition.killedByPlayer().build(),
                        LootItemRandomChanceCondition.randomChance(0.12f).build()
                },
                ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "beast_bloodline"), 5, 45
        ));

        add("phoenix_bloodline_from_nether_fortress", new AddBloodlineRandomPurityModifier(
                new LootItemCondition[]{
                        LootTableIdCondition.builder(ResourceLocation.withDefaultNamespace("chests/nether_bridge")).build(),
                        LootItemRandomChanceCondition.randomChance(0.15f).build()
                },
                ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "phoenix_bloodline"), 5, 35
        ));

        add("dragon_bloodline_from_ender_dragon", new AddBloodlineRandomPurityModifier(
                new LootItemCondition[]{
                        LootTableIdCondition.builder(ResourceLocation.withDefaultNamespace("entities/ender_dragon")).build(),
                        LootItemRandomChanceCondition.randomChance(0.11f).build()
                },
                ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "dragon_bloodline"), 5, 35
        ));
        add("dragon_bloodline_from_end_city", new AddBloodlineRandomPurityModifier(
                new LootItemCondition[]{
                        LootTableIdCondition.builder(ResourceLocation.withDefaultNamespace("chests/end_city_treasure")).build(),
                        LootItemRandomChanceCondition.randomChance(0.015f).build()
                },
                ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "dragon_bloodline"), 5, 35
        ));

        add("astral_bloodline_from_end_city", new AddBloodlineRandomPurityModifier(
                new LootItemCondition[]{
                        LootTableIdCondition.builder(ResourceLocation.withDefaultNamespace("chests/end_city_treasure")).build(),
                        LootItemRandomChanceCondition.randomChance(0.015f).build()
                },
                ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "astral_bloodline"), 5, 35
        ));
        add("astral_bloodline_from_shulker", new AddBloodlineRandomPurityModifier(
                new LootItemCondition[]{
                        LootTableIdCondition.builder(ResourceLocation.withDefaultNamespace("entities/shulker")).build(),
                        LootItemKilledByPlayerCondition.killedByPlayer().build(),
                        LootItemRandomChanceCondition.randomChance(0.015f).build()
                },
                ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "astral_bloodline"), 5, 30
        ));

        add("raven_bloodline_from_pillager", new AddBloodlineRandomPurityModifier(
                new LootItemCondition[]{
                        LootTableIdCondition.builder(ResourceLocation.withDefaultNamespace("entities/pillager")).build(),
                        LootItemKilledByPlayerCondition.killedByPlayer().build(),
                        LootItemRandomChanceCondition.randomChance(0.12f).build()
                },
                ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "raven_bloodline"), 5, 40
        ));
        add("raven_bloodline_from_phantom", new AddBloodlineRandomPurityModifier(
                new LootItemCondition[]{
                        LootTableIdCondition.builder(ResourceLocation.withDefaultNamespace("entities/phantom")).build(),
                        LootItemKilledByPlayerCondition.killedByPlayer().build(),
                        LootItemRandomChanceCondition.randomChance(0.12f).build()
                },
                ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "raven_bloodline"), 5, 40
        ));
        add("raven_bloodline_from_woodland_mansion", new AddBloodlineRandomPurityModifier(
                new LootItemCondition[]{
                        LootTableIdCondition.builder(ResourceLocation.withDefaultNamespace("chests/woodland_mansion")).build(),
                        LootItemRandomChanceCondition.randomChance(0.12f).build()
                },
                ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "raven_bloodline"), 10, 45
        ));

        add("nine_tailed_fox_bloodline_from_fox", new AddBloodlineRandomPurityModifier(
                new LootItemCondition[]{
                        LootTableIdCondition.builder(ResourceLocation.withDefaultNamespace("entities/fox")).build(),
                        LootItemKilledByPlayerCondition.killedByPlayer().build(),
                        LootItemRandomChanceCondition.randomChance(0.10f).build()
                },
                ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "nine_tailed_fox_bloodline"), 5, 35
        ));

        add("nine_tailed_fox_bloodline_from_ancient_city", new AddBloodlineRandomPurityModifier(
                new LootItemCondition[]{
                        LootTableIdCondition.builder(ResourceLocation.withDefaultNamespace("chests/ancient_city")).build(),
                        LootItemRandomChanceCondition.randomChance(0.025f).build()
                },
                ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "nine_tailed_fox_bloodline"), 10, 45
        ));

        add("tiger_bloodline_from_cat", new AddBloodlineRandomPurityModifier(
                new LootItemCondition[]{
                        LootTableIdCondition.builder(ResourceLocation.withDefaultNamespace("entities/cat")).build(),
                        LootItemKilledByPlayerCondition.killedByPlayer().build(),
                        LootItemRandomChanceCondition.randomChance(0.08f).build()
                },
                ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "tiger_bloodline"), 5, 20
        ));

        add("tiger_bloodline_from_jungle_temple", new AddBloodlineRandomPurityModifier(
                new LootItemCondition[]{
                        LootTableIdCondition.builder(ResourceLocation.withDefaultNamespace("chests/jungle_temple")).build(),
                        LootItemRandomChanceCondition.randomChance(0.08f).build()
                },
                ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "tiger_bloodline"), 10, 45
        ));

        add("serpent_bloodline_from_cave_spider", new AddBloodlineRandomPurityModifier(
                new LootItemCondition[]{
                        LootTableIdCondition.builder(ResourceLocation.withDefaultNamespace("entities/cave_spider")).build(),
                        LootItemKilledByPlayerCondition.killedByPlayer().build(),
                        LootItemRandomChanceCondition.randomChance(0.10f).build()
                },
                ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "serpent_bloodline"), 5, 15
        ));

        add("serpent_bloodline_from_desert_pyramid", new AddBloodlineRandomPurityModifier(
                new LootItemCondition[]{
                        LootTableIdCondition.builder(ResourceLocation.withDefaultNamespace("chests/desert_pyramid")).build(),
                        LootItemRandomChanceCondition.randomChance(0.07f).build()
                },
                ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "serpent_bloodline"), 10, 45
        ));


        add("crane_bloodline_from_village_temple", new AddBloodlineRandomPurityModifier(
                new LootItemCondition[]{
                        LootTableIdCondition.builder(ResourceLocation.withDefaultNamespace("chests/village/village_temple")).build(),
                        LootItemRandomChanceCondition.randomChance(0.06f).build()
                },
                ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "crane_bloodline"), 10, 40
        ));

        add("panda_bloodline_from_panda", new AddBloodlineRandomPurityModifier(
                new LootItemCondition[]{
                        LootTableIdCondition.builder(ResourceLocation.withDefaultNamespace("entities/panda")).build(),
                        LootItemKilledByPlayerCondition.killedByPlayer().build(),
                        LootItemRandomChanceCondition.randomChance(0.12f).build()
                },
                ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "panda_bloodline"), 5, 40
        ));

        add("wrath_demon_bloodline_from_blaze", new AddBloodlineRandomPurityModifier(
                new LootItemCondition[]{
                        LootTableIdCondition.builder(ResourceLocation.withDefaultNamespace("entities/blaze")).build(),
                        LootItemKilledByPlayerCondition.killedByPlayer().build(),
                        LootItemRandomChanceCondition.randomChance(0.08f).build()
                },
                ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "wrath_demon_bloodline"), 5, 30
        ));

        add("wrath_demon_bloodline_from_bastion", new AddBloodlineRandomPurityModifier(
                new LootItemCondition[]{
                        LootTableIdCondition.builder(ResourceLocation.withDefaultNamespace("chests/bastion_treasure")).build(),
                        LootItemRandomChanceCondition.randomChance(0.05f).build()
                },
                ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "wrath_demon_bloodline"), 10, 40
        ));

        add("pride_demon_bloodline_from_wither_skeleton", new AddBloodlineRandomPurityModifier(
                new LootItemCondition[]{
                        LootTableIdCondition.builder(ResourceLocation.withDefaultNamespace("entities/wither_skeleton")).build(),
                        LootItemKilledByPlayerCondition.killedByPlayer().build(),
                        LootItemRandomChanceCondition.randomChance(0.07f).build()
                },
                ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "pride_demon_bloodline"), 5, 30
        ));

        add("pride_demon_bloodline_from_nether_fortress", new AddBloodlineRandomPurityModifier(
                new LootItemCondition[]{
                        LootTableIdCondition.builder(ResourceLocation.withDefaultNamespace("chests/nether_bridge")).build(),
                        LootItemRandomChanceCondition.randomChance(0.05f).build()
                },
                ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "pride_demon_bloodline"), 10, 40
        ));

        add("greed_demon_bloodline_from_piglin", new AddBloodlineRandomPurityModifier(
                new LootItemCondition[]{
                        LootTableIdCondition.builder(ResourceLocation.withDefaultNamespace("entities/piglin")).build(),
                        LootItemKilledByPlayerCondition.killedByPlayer().build(),
                        LootItemRandomChanceCondition.randomChance(0.08f).build()
                },
                ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "greed_demon_bloodline"), 5, 30
        ));

        add("greed_demon_bloodline_from_bastion", new AddBloodlineRandomPurityModifier(
                new LootItemCondition[]{
                        LootTableIdCondition.builder(ResourceLocation.withDefaultNamespace("chests/bastion_treasure")).build(),
                        LootItemRandomChanceCondition.randomChance(0.06f).build()
                },
                ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "greed_demon_bloodline"), 10, 45
        ));


        //Technique Pages

        add("bloodfeast_ch5_from_ender_dragon", new AddTechniquePageModifier(
                new LootItemCondition[]{
                        LootTableIdCondition.builder(ResourceLocation.parse("minecraft:entities/ender_dragon")).build(),
                        LootItemRandomChanceCondition.randomChance(0.15f).build()
                },
                ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "bloodfeast_soul_refining_scripture"),
                5
        ));

        add("soul_forged_weapon_manual_ch1_from_bastion", new AddTechniquePageModifier(
                new LootItemCondition[]{
                        LootTableIdCondition.builder(ResourceLocation.withDefaultNamespace("chests/bastion_treasure")).build(),
                        LootItemRandomChanceCondition.randomChance(0.17f).build()
                },
                ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "soul_forged_weapon_manual"),
                1
        ));

        add("soul_forged_weapon_manual_ch3_from_vindicator", new AddTechniquePageModifier(
                new LootItemCondition[]{
                        LootTableIdCondition.builder(ResourceLocation.withDefaultNamespace("entities/vindicator")).build(),
                        LootItemKilledByPlayerCondition.killedByPlayer().build(),
                        LootItemRandomChanceCondition.randomChance(0.15f).build(),
                        MobRankLootCondition.between("formation_establishment", 1, "formation_establishment", 3).build()
                },
                ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "soul_forged_weapon_manual"),
                3
        ));


        //Techniques
        add("fire_essence_technique_from_blaze", new AddTechniqueManualModifier(
                new LootItemCondition[]{
                        LootTableIdCondition.builder(ResourceLocation.withDefaultNamespace("entities/blaze")).build(),
                        LootItemKilledByPlayerCondition.killedByPlayer().build(),
                        LootItemRandomChanceCondition.randomChance(0.16f).build(),
                        MobRankLootCondition.between("formation_establishment", 2, "formation_establishment", 3).build()
                },
                ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "fire_essence_technique")
        ));

        add("water_essence_technique_from_elder_guardian", new AddTechniqueManualModifier(
                new LootItemCondition[]{
                        LootTableIdCondition.builder(ResourceLocation.withDefaultNamespace("entities/elder_guardian")).build(),
                        LootItemKilledByPlayerCondition.killedByPlayer().build(),
                        LootItemRandomChanceCondition.randomChance(0.17f).build(),
                        MobRankLootCondition.atLeast("formation_establishment", 3).build()
                },
                ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "water_essence_technique")
        ));

        this.add("water_essence_technique_from_fishing_treasure", new AddTechniqueManualModifier(
                new LootItemCondition[]{
                        LootTableIdCondition.builder(ResourceLocation.withDefaultNamespace("gameplay/fishing/treasure")).build(),
                        LootItemRandomChanceCondition.randomChance(0.04F).build()
                },
                ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "water_essence_technique")
        ));

        this.add("kidney_water_technique_from_fishing_treasure", new AddTechniqueManualModifier(
                new LootItemCondition[]{
                        LootTableIdCondition.builder(ResourceLocation.withDefaultNamespace("gameplay/fishing/treasure")).build(),
                        LootItemRandomChanceCondition.randomChance(0.04F).build()
                },
                ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "kidney_water_technique")
        ));

        add("wood_essence_technique_from_jungle_temple", new AddTechniqueManualModifier(
                new LootItemCondition[]{
                        LootTableIdCondition.builder(ResourceLocation.withDefaultNamespace("chests/jungle_temple")).build(),
                        LootItemRandomChanceCondition.randomChance(0.17f).build()
                },
                ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "wood_essence_technique")
        ));

        add("earth_essence_technique_from_trail_ruins", new AddTechniqueManualModifier(
                new LootItemCondition[]{
                        LootTableIdCondition.builder(ResourceLocation.withDefaultNamespace("archaeology/trail_ruins_rare")).build(),
                        LootItemRandomChanceCondition.randomChance(0.16f).build()
                },
                ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "earth_essence_technique")
        ));

        add("metal_essence_technique_from_iron_golem", new AddTechniqueManualModifier(
                new LootItemCondition[]{
                        LootTableIdCondition.builder(ResourceLocation.withDefaultNamespace("entities/iron_golem")).build(),
                        LootItemKilledByPlayerCondition.killedByPlayer().build(),
                        LootItemRandomChanceCondition.randomChance(0.16f).build(),
                        MobRankLootCondition.between("formation_establishment", 2, "formation_establishment", 3).build()
                },
                ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "metal_essence_technique")
        ));

        add("lightning_essence_technique_from_creeper", new AddTechniqueManualModifier(
                new LootItemCondition[]{
                        LootTableIdCondition.builder(ResourceLocation.withDefaultNamespace("entities/creeper")).build(),
                        LootItemKilledByPlayerCondition.killedByPlayer().build(),
                        LootItemRandomChanceCondition.randomChance(0.16f).build(),
                        MobRankLootCondition.between("formation_establishment", 1, "formation_establishment", 2).build()
                },
                ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "lightning_essence_technique")
        ));

        add("wind_essence_technique_from_phantom", new AddTechniqueManualModifier(
                new LootItemCondition[]{
                        LootTableIdCondition.builder(ResourceLocation.withDefaultNamespace("entities/phantom")).build(),
                        LootItemKilledByPlayerCondition.killedByPlayer().build(),
                        LootItemRandomChanceCondition.randomChance(0.16f).build(),
                        MobRankLootCondition.exact("formation_establishment", 1).build()
                },
                ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "wind_essence_technique")
        ));

        add("mortal_essence_scripture_from_village_plains_house", new AddTechniqueManualModifier(
                new LootItemCondition[]{
                        LootTableIdCondition.builder(ResourceLocation.withDefaultNamespace("chests/village/village_plains_house")).build(),
                        LootItemRandomChanceCondition.randomChance(0.15f).build(),
                },
                ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "mortal_essence_scripture")
        ));

        add("jade_meridians_technique_from_panda", new AddTechniqueManualModifier(
                new LootItemCondition[]{
                        LootTableIdCondition.builder(ResourceLocation.withDefaultNamespace("entities/panda")).build(),
                        LootItemKilledByPlayerCondition.killedByPlayer().build(),
                        LootItemRandomChanceCondition.randomChance(0.08f).build(),
                        MobRankLootCondition.exact("mortal", 2).build()
                },
                ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "jade_meridians_technique")
        ));

        add("heart_fire_technique_from_nether_fortress", new AddTechniqueManualModifier(
                new LootItemCondition[]{
                        LootTableIdCondition.builder(ResourceLocation.withDefaultNamespace("chests/nether_bridge")).build(),
                        LootItemRandomChanceCondition.randomChance(0.17f).build()
                },
                ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "heart_fire_technique")
        ));

        add("heart_fire_technique_from_wither_skeleton", new AddTechniqueManualModifier(
                new LootItemCondition[]{
                        LootTableIdCondition.builder(ResourceLocation.withDefaultNamespace("entities/wither_skeleton")).build(),
                        LootItemKilledByPlayerCondition.killedByPlayer().build(),
                        LootItemRandomChanceCondition.randomChance(0.15f).build(),
                        MobRankLootCondition.between("qi_gathering", 1, "formation_establishment", 3).build()
                },
                ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "heart_fire_technique")
        ));

        add("kidney_water_technique_from_drowned", new AddTechniqueManualModifier(
                new LootItemCondition[]{
                        LootTableIdCondition.builder(ResourceLocation.withDefaultNamespace("entities/drowned")).build(),
                        LootItemKilledByPlayerCondition.killedByPlayer().build(),
                        LootItemRandomChanceCondition.randomChance(0.15f).build(),
                        MobRankLootCondition.between("qi_gathering", 1, "formation_establishment", 3).build()
                },
                ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "kidney_water_technique")
        ));

        add("kidney_water_technique_from_ocean_ruin", new AddTechniqueManualModifier(
                new LootItemCondition[]{
                        LootTableIdCondition.builder(ResourceLocation.withDefaultNamespace("chests/underwater_ruin_big")).build(),
                        LootItemRandomChanceCondition.randomChance(0.17f).build()
                },
                ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "kidney_water_technique")
        ));

        add("liver_wood_technique_from_jungle_temple", new AddTechniqueManualModifier(
                new LootItemCondition[]{
                        LootTableIdCondition.builder(ResourceLocation.withDefaultNamespace("chests/jungle_temple")).build(),
                        LootItemRandomChanceCondition.randomChance(0.17f).build()
                },
                ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "liver_wood_technique")
        ));

        add("spleen_earth_technique_from_desert_pyramid", new AddTechniqueManualModifier(
                new LootItemCondition[]{
                        LootTableIdCondition.builder(ResourceLocation.withDefaultNamespace("chests/desert_pyramid")).build(),
                        LootItemRandomChanceCondition.randomChance(0.17f).build()
                },
                ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "spleen_earth_technique")
        ));

        add("myriad_venom_refinement_scripture_from_desert_pyramid", new AddTechniqueManualModifier(
                new LootItemCondition[]{
                        LootTableIdCondition.builder(ResourceLocation.withDefaultNamespace("chests/desert_pyramid")).build(),
                        LootItemRandomChanceCondition.randomChance(0.11f).build()
                },
                ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "myriad_venom_refinement_scripture")
        ));

        add("lung_metal_technique_from_mineshaft", new AddTechniqueManualModifier(
                new LootItemCondition[]{
                        LootTableIdCondition.builder(ResourceLocation.withDefaultNamespace("chests/abandoned_mineshaft")).build(),
                        LootItemRandomChanceCondition.randomChance(0.17f).build()
                },
                ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "lung_metal_technique")
        ));

        add("lung_metal_technique_from_iron_golem", new AddTechniqueManualModifier(
                new LootItemCondition[]{
                        LootTableIdCondition.builder(ResourceLocation.withDefaultNamespace("entities/iron_golem")).build(),
                        LootItemKilledByPlayerCondition.killedByPlayer().build(),
                        LootItemRandomChanceCondition.randomChance(0.15f).build(),
                        MobRankLootCondition.between("qi_gathering", 1, "formation_establishment", 3).build()
                },
                ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "lung_metal_technique")
        ));

        add("wood_fire_body_technique_from_mansion", new AddTechniqueManualModifier(
                new LootItemCondition[]{
                        LootTableIdCondition.builder(ResourceLocation.withDefaultNamespace("chests/woodland_mansion")).build(),
                        LootItemRandomChanceCondition.randomChance(0.16f).build()
                },
                ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "wood_fire_body_technique")
        ));

        add("fire_earth_body_technique_from_bastion", new AddTechniqueManualModifier(
                new LootItemCondition[]{
                        LootTableIdCondition.builder(ResourceLocation.withDefaultNamespace("chests/bastion_treasure")).build(),
                        LootItemRandomChanceCondition.randomChance(0.16f).build()
                },
                ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "fire_earth_body_technique")
        ));

        add("earth_metal_body_technique_from_ancient_city", new AddTechniqueManualModifier(
                new LootItemCondition[]{
                        LootTableIdCondition.builder(ResourceLocation.withDefaultNamespace("chests/ancient_city")).build(),
                        LootItemRandomChanceCondition.randomChance(0.16f).build()
                },
                ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "earth_metal_body_technique")
        ));

        add("metal_water_body_technique_from_shipwreck", new AddTechniqueManualModifier(
                new LootItemCondition[]{
                        LootTableIdCondition.builder(ResourceLocation.withDefaultNamespace("chests/shipwreck_treasure")).build(),
                        LootItemRandomChanceCondition.randomChance(0.16f).build()
                },
                ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "metal_water_body_technique")
        ));

        add("water_wood_body_technique_from_dungeon", new AddTechniqueManualModifier(
                new LootItemCondition[]{
                        LootTableIdCondition.builder(ResourceLocation.withDefaultNamespace("chests/simple_dungeon")).build(),
                        LootItemRandomChanceCondition.randomChance(0.16f).build()
                },
                ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "water_wood_body_technique")
        ));

        add("wood_fire_earth_body_technique_from_mansion", new AddTechniqueManualModifier(
                new LootItemCondition[]{
                        LootTableIdCondition.builder(ResourceLocation.withDefaultNamespace("chests/woodland_mansion")).build(),
                        LootItemRandomChanceCondition.randomChance(0.15f).build()
                },
                ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "wood_fire_earth_body_technique")
        ));

        add("fire_earth_metal_body_technique_from_bastion", new AddTechniqueManualModifier(
                new LootItemCondition[]{
                        LootTableIdCondition.builder(ResourceLocation.withDefaultNamespace("chests/bastion_treasure")).build(),
                        LootItemRandomChanceCondition.randomChance(0.15f).build()
                },
                ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "fire_earth_metal_body_technique")
        ));

        add("earth_metal_water_body_technique_from_ancient_city", new AddTechniqueManualModifier(
                new LootItemCondition[]{
                        LootTableIdCondition.builder(ResourceLocation.withDefaultNamespace("chests/ancient_city")).build(),
                        LootItemRandomChanceCondition.randomChance(0.15f).build()
                },
                ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "earth_metal_water_body_technique")
        ));

        add("metal_water_wood_body_technique_from_end_city", new AddTechniqueManualModifier(
                new LootItemCondition[]{
                        LootTableIdCondition.builder(ResourceLocation.withDefaultNamespace("chests/end_city_treasure")).build(),
                        LootItemRandomChanceCondition.randomChance(0.15f).build()
                },
                ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "metal_water_wood_body_technique")
        ));

        add("water_wood_fire_body_technique_from_stronghold", new AddTechniqueManualModifier(
                new LootItemCondition[]{
                        LootTableIdCondition.builder(ResourceLocation.withDefaultNamespace("chests/stronghold_library")).build(),
                        LootItemRandomChanceCondition.randomChance(0.15f).build()
                },
                ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "water_wood_fire_body_technique")
        ));

        add("wood_fire_earth_metal_body_technique_from_ancient_city", new AddTechniqueManualModifier(
                new LootItemCondition[]{
                        LootTableIdCondition.builder(ResourceLocation.withDefaultNamespace("chests/ancient_city")).build(),
                        LootItemRandomChanceCondition.randomChance(0.14f).build()
                },
                ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "wood_fire_earth_metal_body_technique")
        ));

        add("fire_earth_metal_water_body_technique_from_bastion", new AddTechniqueManualModifier(
                new LootItemCondition[]{
                        LootTableIdCondition.builder(ResourceLocation.withDefaultNamespace("chests/bastion_treasure")).build(),
                        LootItemRandomChanceCondition.randomChance(0.14f).build()
                },
                ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "fire_earth_metal_water_body_technique")
        ));

        add("earth_metal_water_wood_body_technique_from_end_city", new AddTechniqueManualModifier(
                new LootItemCondition[]{
                        LootTableIdCondition.builder(ResourceLocation.withDefaultNamespace("chests/end_city_treasure")).build(),
                        LootItemRandomChanceCondition.randomChance(0.14f).build()
                },
                ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "earth_metal_water_wood_body_technique")
        ));

        add("metal_water_wood_fire_body_technique_from_mansion", new AddTechniqueManualModifier(
                new LootItemCondition[]{
                        LootTableIdCondition.builder(ResourceLocation.withDefaultNamespace("chests/woodland_mansion")).build(),
                        LootItemRandomChanceCondition.randomChance(0.14f).build()
                },
                ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "metal_water_wood_fire_body_technique")
        ));

        add("water_wood_fire_earth_body_technique_from_stronghold", new AddTechniqueManualModifier(
                new LootItemCondition[]{
                        LootTableIdCondition.builder(ResourceLocation.withDefaultNamespace("chests/stronghold_library")).build(),
                        LootItemRandomChanceCondition.randomChance(0.14f).build()
                },
                ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "water_wood_fire_earth_body_technique")
        ));

        add("scholarly_soul_technique_from_stronghold_library", new AddTechniqueManualModifier(
                new LootItemCondition[]{
                        LootTableIdCondition.builder(ResourceLocation.withDefaultNamespace("chests/stronghold_library")).build(),
                        LootItemRandomChanceCondition.randomChance(0.16f).build()
                },
                ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "scholarly_soul_technique")
        ));

        add("scholarly_soul_technique_from_zombie_villager", new AddTechniqueManualModifier(
                new LootItemCondition[]{
                        LootTableIdCondition.builder(ResourceLocation.withDefaultNamespace("entities/zombie_villager")).build(),
                        LootItemKilledByPlayerCondition.killedByPlayer().build(),
                        LootItemRandomChanceCondition.randomChance(0.15f).build(),
                        MobRankLootCondition.exact("formation_establishment", 3).build()
                },
                ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "scholarly_soul_technique")
        ));

        add("pale_moon_scripture_from_ancient_city", new AddTechniqueManualModifier(
                new LootItemCondition[]{
                        LootTableIdCondition.builder(ResourceLocation.withDefaultNamespace("chests/ancient_city")).build(),
                        LootItemRandomChanceCondition.randomChance(0.16f).build()
                },
                ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "pale_moon_scripture")
        ));

        add("pale_moon_scripture_from_phantom", new AddTechniqueManualModifier(
                new LootItemCondition[]{
                        LootTableIdCondition.builder(ResourceLocation.withDefaultNamespace("entities/phantom")).build(),
                        LootItemKilledByPlayerCondition.killedByPlayer().build(),
                        LootItemRandomChanceCondition.randomChance(0.15f).build(),
                        MobRankLootCondition.atLeast("formation_establishment", 3).build()
                },
                ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "pale_moon_scripture")
        ));

        add("gibbous_moon_scripture_from_end_city", new AddTechniqueManualModifier(
                new LootItemCondition[]{
                        LootTableIdCondition.builder(ResourceLocation.withDefaultNamespace("chests/end_city_treasure")).build(),
                        LootItemRandomChanceCondition.randomChance(0.15f).build()
                },
                ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "gibbous_moon_scripture")
        ));

        add("gibbous_moon_scripture_from_enderman", new AddTechniqueManualModifier(
                new LootItemCondition[]{
                        LootTableIdCondition.builder(ResourceLocation.withDefaultNamespace("entities/enderman")).build(),
                        LootItemKilledByPlayerCondition.killedByPlayer().build(),
                        LootItemRandomChanceCondition.randomChance(0.14f).build(),
                        MobRankLootCondition.atLeast("formation_establishment", 3).build()
                },
                ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "gibbous_moon_scripture")
        ));

        add("sword_comprehension_technique_from_pillager_outpost", new AddTechniqueManualModifier(
                new LootItemCondition[]{
                        LootTableIdCondition.builder(ResourceLocation.withDefaultNamespace("chests/pillager_outpost")).build(),
                        LootItemRandomChanceCondition.randomChance(0.17f).build()
                },
                ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "sword_comprehension_technique")
        ));

        add("sword_comprehension_technique_from_vindicator", new AddTechniqueManualModifier(
                new LootItemCondition[]{
                        LootTableIdCondition.builder(ResourceLocation.withDefaultNamespace("entities/vindicator")).build(),
                        LootItemKilledByPlayerCondition.killedByPlayer().build(),
                        LootItemRandomChanceCondition.randomChance(0.15f).build(),
                        MobRankLootCondition.between("formation_establishment", 2, "formation_establishment", 3).build()
                },
                ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "sword_comprehension_technique")
        ));

        add("sword_comprehension_technique_from_mansion", new AddTechniqueManualModifier(
                new LootItemCondition[]{
                        LootTableIdCondition.builder(ResourceLocation.withDefaultNamespace("chests/woodland_mansion")).build(),
                        LootItemRandomChanceCondition.randomChance(0.16f).build()
                },
                ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "sword_comprehension_technique")
        ));

        add("spear_comprehension_technique_from_pillager_outpost", new AddTechniqueManualModifier(
                new LootItemCondition[]{
                        LootTableIdCondition.builder(ResourceLocation.withDefaultNamespace("chests/pillager_outpost")).build(),
                        LootItemRandomChanceCondition.randomChance(0.17f).build()
                },
                ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "spear_comprehension_technique")
        ));

        add("spear_comprehension_technique_from_ocean_ruin", new AddTechniqueManualModifier(
                new LootItemCondition[]{
                        LootTableIdCondition.builder(ResourceLocation.withDefaultNamespace("chests/underwater_ruin_big")).build(),
                        LootItemRandomChanceCondition.randomChance(0.16f).build()
                },
                ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "spear_comprehension_technique")
        ));

        add("blade_comprehension_technique_from_woodland_mansion", new AddTechniqueManualModifier(
                new LootItemCondition[]{
                        LootTableIdCondition.builder(ResourceLocation.withDefaultNamespace("chests/woodland_mansion")).build(),
                        LootItemRandomChanceCondition.randomChance(0.16f).build()
                },
                ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "blade_comprehension_technique")
        ));

        add("blade_comprehension_technique_from_bastion", new AddTechniqueManualModifier(
                new LootItemCondition[]{
                        LootTableIdCondition.builder(ResourceLocation.withDefaultNamespace("chests/bastion_treasure")).build(),
                        LootItemRandomChanceCondition.randomChance(0.16f).build()
                },
                ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "blade_comprehension_technique")
        ));


        add("dawning_sun_scripture_from_nether_fortress", new AddTechniqueManualModifier(
                new LootItemCondition[]{
                        LootTableIdCondition.builder(ResourceLocation.withDefaultNamespace("chests/nether_bridge")).build(),
                        LootItemRandomChanceCondition.randomChance(0.19f).build()
                },
                ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "dawning_sun_scripture")
        ));

        add("dawning_sun_scripture_from_blaze", new AddTechniqueManualModifier(
                new LootItemCondition[]{
                        LootTableIdCondition.builder(ResourceLocation.withDefaultNamespace("entities/blaze")).build(),
                        LootItemKilledByPlayerCondition.killedByPlayer().build(),
                        LootItemRandomChanceCondition.randomChance(0.15f).build(),
                        MobRankLootCondition.atLeast("formation_establishment", 3).build()
                },
                ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "dawning_sun_scripture")
        ));

        add("zenith_sun_scripture_from_bastion", new AddTechniqueManualModifier(
                new LootItemCondition[]{
                        LootTableIdCondition.builder(ResourceLocation.withDefaultNamespace("chests/bastion_treasure")).build(),
                        LootItemRandomChanceCondition.randomChance(0.15f).build()
                },
                ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "zenith_sun_scripture")
        ));

        add("zenith_sun_scripture_from_wither", new AddTechniqueManualModifier(
                new LootItemCondition[]{
                        LootTableIdCondition.builder(ResourceLocation.withDefaultNamespace("entities/wither")).build(),
                        LootItemKilledByPlayerCondition.killedByPlayer().build(),
                        LootItemRandomChanceCondition.randomChance(0.17f).build()
                },
                ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "zenith_sun_scripture")
        ));

        add("jade_spirit_scripture_from_allay", new AddTechniqueManualModifier(
                new LootItemCondition[]{
                        LootTableIdCondition.builder(ResourceLocation.withDefaultNamespace("entities/allay")).build(),
                        LootItemRandomChanceCondition.randomChance(0.16f).build()
                },
                ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "jade_spirit_scripture")
        ));

        add("open_sky_breathing_scripture_from_pillager_outpost", new AddTechniqueManualModifier(
                new LootItemCondition[]{
                        LootTableIdCondition.builder(ResourceLocation.withDefaultNamespace("chests/pillager_outpost")).build(),
                        LootItemRandomChanceCondition.randomChance(0.17f).build()
                },
                ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "open_sky_breathing_scripture")
        ));

        add("open_sky_breathing_scripture_from_chicken", new AddTechniqueManualModifier(
                new LootItemCondition[]{
                        LootTableIdCondition.builder(ResourceLocation.withDefaultNamespace("entities/chicken")).build(),
                        LootItemKilledByPlayerCondition.killedByPlayer().build(),
                        LootItemRandomChanceCondition.randomChance(0.15f).build(),
                        MobRankLootCondition.between("qi_gathering", 1, "qi_gathering", 3).build()
                },
                ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "open_sky_breathing_scripture")
        ));

        add("hellbound_marrow_scripture_from_piglin_brute", new AddTechniqueManualModifier(
                new LootItemCondition[]{
                        LootTableIdCondition.builder(ResourceLocation.withDefaultNamespace("entities/piglin_brute")).build(),
                        LootItemKilledByPlayerCondition.killedByPlayer().build(),
                        LootItemRandomChanceCondition.randomChance(0.14f).build(),
                        MobRankLootCondition.atLeast("formation_establishment", 2).build()},
                ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "hellbound_marrow_scripture")
        ));

        add("hellbound_marrow_scripture_from_bastion", new AddTechniqueManualModifier(
                        new LootItemCondition[]{
                                LootTableIdCondition.builder(ResourceLocation.withDefaultNamespace("chests/bastion_treasure")).build(),
                                LootItemRandomChanceCondition.randomChance(0.13f).build()},
                        ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "hellbound_marrow_scripture")
        ));

        add("astral_essence_technique_from_shulker", new AddTechniqueManualModifier(
                new LootItemCondition[]{
                        LootTableIdCondition.builder(ResourceLocation.withDefaultNamespace("entities/shulker")).build(),
                        LootItemKilledByPlayerCondition.killedByPlayer().build(),
                        LootItemRandomChanceCondition.randomChance(0.13f).build(),
                        MobRankLootCondition.atLeast("formation_establishment", 3).build()},
                ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "astral_essence_technique")
        ));

        add("astral_essence_technique_from_end_city", new AddTechniqueManualModifier(
                new LootItemCondition[]{
                        LootTableIdCondition.builder(ResourceLocation.withDefaultNamespace("chests/end_city_treasure")).build(),
                        LootItemRandomChanceCondition.randomChance(0.14f).build()},
                ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "astral_essence_technique")
        ));

        add("fire_soul_technique_from_blaze", new AddTechniqueManualModifier(
                new LootItemCondition[]{
                        LootTableIdCondition.builder(ResourceLocation.withDefaultNamespace("entities/blaze")).build(),
                        LootItemKilledByPlayerCondition.killedByPlayer().build(),
                        LootItemRandomChanceCondition.randomChance(0.12f).build(),
                        MobRankLootCondition.atLeast("golden_core", 1).build()},
                ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "fire_soul_technique")
        ));

        add("fire_soul_technique_from_nether_fortress", new AddTechniqueManualModifier(
                new LootItemCondition[]{
                        LootTableIdCondition.builder(ResourceLocation.withDefaultNamespace("chests/nether_bridge")).build(),
                        LootItemRandomChanceCondition.randomChance(0.11f).build()},
                ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "fire_soul_technique")
        ));

        add("water_soul_technique_from_elder_guardian", new AddTechniqueManualModifier(
                new LootItemCondition[]{
                        LootTableIdCondition.builder(ResourceLocation.withDefaultNamespace("entities/elder_guardian")).build(),
                        LootItemKilledByPlayerCondition.killedByPlayer().build(),
                        LootItemRandomChanceCondition.randomChance(0.17f).build()},
                ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "water_soul_technique")
                ));

        add("water_soul_technique_from_shipwreck", new AddTechniqueManualModifier(
                new LootItemCondition[]{
                        LootTableIdCondition.builder(ResourceLocation.withDefaultNamespace("chests/shipwreck_treasure")).build(),
                        LootItemRandomChanceCondition.randomChance(0.10f).build()},
                ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "water_soul_technique")
        ));

        add("wood_soul_technique_from_evoker", new AddTechniqueManualModifier(
                new LootItemCondition[]{
                        LootTableIdCondition.builder(ResourceLocation.withDefaultNamespace("entities/evoker")).build(),
                        LootItemKilledByPlayerCondition.killedByPlayer().build(),
                        LootItemRandomChanceCondition.randomChance(0.12f).build(),
                        MobRankLootCondition.atLeast("golden_core", 1).build()},
                ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "wood_soul_technique")
        ));

        add("wood_soul_technique_from_mansion", new AddTechniqueManualModifier(
                new LootItemCondition[]{
                        LootTableIdCondition.builder(ResourceLocation.withDefaultNamespace("chests/woodland_mansion")).build(),
                        LootItemRandomChanceCondition.randomChance(0.12f).build()},
                        ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "wood_soul_technique")
        ));

        add("earth_soul_technique_from_ravager", new AddTechniqueManualModifier(
                new LootItemCondition[]{
                        LootTableIdCondition.builder(ResourceLocation.withDefaultNamespace("entities/ravager")).build(),
                        LootItemKilledByPlayerCondition.killedByPlayer().build(),
                        LootItemRandomChanceCondition.randomChance(0.12f).build(),
                        MobRankLootCondition.atLeast("golden_core", 1).build()},
                ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "earth_soul_technique")
        ));

        add("earth_soul_technique_from_ancient_city", new AddTechniqueManualModifier(
                new LootItemCondition[]{
                        LootTableIdCondition.builder(ResourceLocation.withDefaultNamespace("chests/ancient_city")).build(),
                        LootItemRandomChanceCondition.randomChance(0.11f).build()},
                ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "earth_soul_technique")
        ));


        add("metal_soul_technique_from_iron_golem", new AddTechniqueManualModifier(
                new LootItemCondition[]{
                        LootTableIdCondition.builder(ResourceLocation.withDefaultNamespace("entities/iron_golem")).build(),
                        LootItemKilledByPlayerCondition.killedByPlayer().build(),
                        LootItemRandomChanceCondition.randomChance(0.10f).build(),
                        MobRankLootCondition.atLeast("golden_core", 1).build()},
                ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "metal_soul_technique")
        ));

        add("metal_soul_technique_from_mineshaft", new AddTechniqueManualModifier(
                new LootItemCondition[]{
                        LootTableIdCondition.builder(ResourceLocation.withDefaultNamespace("chests/abandoned_mineshaft")).build(),
                        LootItemRandomChanceCondition.randomChance(0.11f).build()},
                ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "metal_soul_technique")
        ));

        add("lightning_soul_technique_from_witch", new AddTechniqueManualModifier(
                new LootItemCondition[]{
                        LootTableIdCondition.builder(ResourceLocation.withDefaultNamespace("entities/witch")).build(),
                        LootItemKilledByPlayerCondition.killedByPlayer().build(),
                        LootItemRandomChanceCondition.randomChance(0.12f).build(),
                        MobRankLootCondition.atLeast("golden_core", 1).build()},
                ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "lightning_soul_technique")
        ));

        add("lightning_soul_technique_from_ancient_city", new AddTechniqueManualModifier(
                new LootItemCondition[]{
                        LootTableIdCondition.builder(ResourceLocation.withDefaultNamespace("chests/ancient_city")).build(),
                        LootItemRandomChanceCondition.randomChance(0.10f).build()},
                ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "lightning_soul_technique")
        ));

        add("wind_soul_technique_from_phantom", new AddTechniqueManualModifier(
                new LootItemCondition[]{LootTableIdCondition.builder(ResourceLocation.withDefaultNamespace("entities/phantom")).build(),
                        LootItemKilledByPlayerCondition.killedByPlayer().build(),
                        LootItemRandomChanceCondition.randomChance(0.12f).build(),
                        MobRankLootCondition.atLeast("golden_core", 1).build()},
                ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "wind_soul_technique")
        ));

        add("wind_soul_technique_from_end_city", new AddTechniqueManualModifier(
                new LootItemCondition[]{
                        LootTableIdCondition.builder(ResourceLocation.withDefaultNamespace("chests/end_city_treasure")).build(),
                        LootItemRandomChanceCondition.randomChance(0.11f).build()},
                ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "wind_soul_technique")
        ));


        // Physiques from Fishing

        add("water_attuned_from_fishing_treasure", new AddPhysiqueRandomPurityModifier(
                new LootItemCondition[]{
                        LootTableIdCondition.builder(ResourceLocation.withDefaultNamespace("gameplay/fishing/treasure")).build(),
                        LootItemRandomChanceCondition.randomChance(0.05F).build()
                },
                ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "water_attuned"),
                15, 55
        ));

        add("water_body_from_fishing_treasure", new AddPhysiqueRandomPurityModifier(
                new LootItemCondition[]{
                        LootTableIdCondition.builder(ResourceLocation.withDefaultNamespace("gameplay/fishing/treasure")).build(),
                        LootItemRandomChanceCondition.randomChance(0.05F).build()
                },
                ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "water_body"),
                15, 55
        ));

        //Herbs

        this.add("jade_dew_grass_from_village", new AddItemModifier(new LootItemCondition[]{
                LootTableIdCondition.builder(ResourceLocation.withDefaultNamespace("chests/village/village_plains_house")).build(),
                LootItemRandomChanceCondition.randomChance(0.27F).build()
        }, ModItems.JADE_DEW_GRASS_SEEDS.get()));
        this.add("hundred_year_ginseng_from_village", new AddItemModifier(new LootItemCondition[]{
                LootTableIdCondition.builder(ResourceLocation.withDefaultNamespace("chests/village/village_plains_house")).build(),
                LootItemRandomChanceCondition.randomChance(0.16F).build()
        }, ModItems.HUNDRED_YEAR_GINSENG.get()));
        this.add("hundred_year_ginseng_from_dungeon", new AddItemModifier(new LootItemCondition[]{
                LootTableIdCondition.builder(ResourceLocation.withDefaultNamespace("chests/simple_dungeon")).build(),
                LootItemRandomChanceCondition.randomChance(0.19F).build()
        }, ModItems.HUNDRED_YEAR_GINSENG.get()));
        this.add("hundred_year_snow_ginseng_from_igloo", new AddItemModifier(new LootItemCondition[]{
                LootTableIdCondition.builder(ResourceLocation.withDefaultNamespace("chests/igloo_chest")).build(),
                LootItemRandomChanceCondition.randomChance(0.18F).build()
        }, ModItems.HUNDRED_YEAR_SNOW_GINSENG.get()));
        this.add("hundred_year_snow_ginseng_from_snowy_village", new AddItemModifier(new LootItemCondition[]{
                LootTableIdCondition.builder(ResourceLocation.withDefaultNamespace("chests/village/village_snowy_house")).build(),
                LootItemRandomChanceCondition.randomChance(0.19F).build()
        }, ModItems.HUNDRED_YEAR_SNOW_GINSENG.get()));
        this.add("hundred_year_fire_ginseng_from_desert_temple", new AddItemModifier(new LootItemCondition[]{
                LootTableIdCondition.builder(ResourceLocation.withDefaultNamespace("chests/desert_pyramid")).build(),
                LootItemRandomChanceCondition.randomChance(0.14F).build()
        }, ModItems.HUNDRED_YEAR_FIRE_GINSENG.get()));
        this.add("hundred_year_fire_ginseng_from_bastion", new AddItemModifier(new LootItemCondition[]{
                LootTableIdCondition.builder(ResourceLocation.withDefaultNamespace("chests/bastion_treasure")).build(),
                LootItemRandomChanceCondition.randomChance(0.19F).build()
        }, ModItems.HUNDRED_YEAR_FIRE_GINSENG.get()));
        this.add("white_jade_orchid_from_mineshaft", new AddItemModifier(new LootItemCondition[]{
                LootTableIdCondition.builder(ResourceLocation.withDefaultNamespace("chests/abandoned_mineshaft")).build(),
                LootItemRandomChanceCondition.randomChance(0.20F).build()
        }, ModItems.WHITE_JADE_ORCHID.get()));
        this.add("white_jade_orchid_from_stronghold", new AddItemModifier(new LootItemCondition[]{
                LootTableIdCondition.builder(ResourceLocation.withDefaultNamespace("chests/stronghold_corridor")).build(),
                LootItemRandomChanceCondition.randomChance(0.18F).build()
        }, ModItems.WHITE_JADE_ORCHID.get()));

        this.add("jade_bamboo_of_serenity_from_bamboo", new AddItemModifier(new LootItemCondition[]{
                new LootTableIdCondition.Builder(ResourceLocation.withDefaultNamespace("blocks/bamboo")).build(),
                LootItemRandomChanceCondition.randomChance(0.12F).build()}, ModItems.JADE_BAMBOO_OF_SERENITY.get()));

        //Living Core Drop Regular Mobs
        this.add("living_core_from_strider", new AddItemModifier(new LootItemCondition[]{
                new LootTableIdCondition.Builder(ResourceLocation.withDefaultNamespace("entities/strider")).build(),
                LootItemRandomChanceCondition.randomChance(0.15F).build()}, ModItems.LIVING_CORE.get()));
        this.add("living_core_from_piglin", new AddItemModifier(new LootItemCondition[]{
                new LootTableIdCondition.Builder(ResourceLocation.withDefaultNamespace("entities/piglin")).build(),
                LootItemRandomChanceCondition.randomChance(0.15F).build()}, ModItems.LIVING_CORE.get()));
        this.add("living_core_from_hoglin", new AddItemModifier(new LootItemCondition[]{
                new LootTableIdCondition.Builder(ResourceLocation.withDefaultNamespace("entities/hoglin")).build(),
                LootItemRandomChanceCondition.randomChance(0.15F).build()}, ModItems.LIVING_CORE.get()));
        this.add("living_core_from_zoglin", new AddItemModifier(new LootItemCondition[]{
                new LootTableIdCondition.Builder(ResourceLocation.withDefaultNamespace("entities/zoglin")).build(),
                LootItemRandomChanceCondition.randomChance(0.15F).build()}, ModItems.LIVING_CORE.get()));
        this.add("living_core_from_vex", new AddItemModifier(new LootItemCondition[]{
                new LootTableIdCondition.Builder(ResourceLocation.withDefaultNamespace("entities/vex")).build(),
                LootItemRandomChanceCondition.randomChance(0.15F).build()}, ModItems.LIVING_CORE.get()));
        this.add("living_core_from_allay", new AddItemModifier(new LootItemCondition[]{
                new LootTableIdCondition.Builder(ResourceLocation.withDefaultNamespace("entities/allay")).build(),
                LootItemRandomChanceCondition.randomChance(0.15F).build()}, ModItems.LIVING_CORE.get()));
        this.add("living_core_from_golem", new AddItemModifier(new LootItemCondition[]{
                new LootTableIdCondition.Builder(ResourceLocation.withDefaultNamespace("entities/iron_golem")).build(),
                LootItemRandomChanceCondition.randomChance(0.15F).build()}, ModItems.LIVING_CORE.get()));

        //Living Core Drop Boss Undead
        this.add("living_core_from_ender_dragon", new AddItemModifier(new LootItemCondition[]{
                new LootTableIdCondition.Builder(ResourceLocation.withDefaultNamespace("entities/ender_dragon")).build(),
                LootItemRandomChanceCondition.randomChance(0.29F).build()}, ModItems.LIVING_CORE.get()));

        //Undead Core Drop Regular Undead Mobs
        this.add("undead_core_from_zombie", new AddItemModifier(new LootItemCondition[]{
                new LootTableIdCondition.Builder(ResourceLocation.withDefaultNamespace("entities/zombie")).build(),
                LootItemRandomChanceCondition.randomChance(0.15F).build()}, ModItems.UNDEAD_CORE.get()));
        this.add("undead_core_from_skeleton", new AddItemModifier(new LootItemCondition[]{
                new LootTableIdCondition.Builder(ResourceLocation.withDefaultNamespace("entities/skeleton")).build(),
                LootItemRandomChanceCondition.randomChance(0.15F).build()}, ModItems.UNDEAD_CORE.get()));
        this.add("undead_core_from_wither_skeleton", new AddItemModifier(new LootItemCondition[]{
                new LootTableIdCondition.Builder(ResourceLocation.withDefaultNamespace("entities/wither_skeleton")).build(),
                LootItemRandomChanceCondition.randomChance(0.15F).build()}, ModItems.UNDEAD_CORE.get()));
        this.add("undead_core_from_stray", new AddItemModifier(new LootItemCondition[]{
                new LootTableIdCondition.Builder(ResourceLocation.withDefaultNamespace("entities/stray")).build(),
                LootItemRandomChanceCondition.randomChance(0.15F).build()}, ModItems.UNDEAD_CORE.get()));
        this.add("undead_core_from_husk", new AddItemModifier(new LootItemCondition[]{
                new LootTableIdCondition.Builder(ResourceLocation.withDefaultNamespace("entities/husk")).build(),
                LootItemRandomChanceCondition.randomChance(0.15F).build()}, ModItems.UNDEAD_CORE.get()));
        this.add("undead_core_from_drowned", new AddItemModifier(new LootItemCondition[]{
                new LootTableIdCondition.Builder(ResourceLocation.withDefaultNamespace("entities/drowned")).build(),
                LootItemRandomChanceCondition.randomChance(0.15F).build()}, ModItems.UNDEAD_CORE.get()));
        this.add("undead_core_from_zombie_villager", new AddItemModifier(new LootItemCondition[]{
                new LootTableIdCondition.Builder(ResourceLocation.withDefaultNamespace("entities/zombie_villager")).build(),
                LootItemRandomChanceCondition.randomChance(0.15F).build()}, ModItems.UNDEAD_CORE.get()));
        this.add("undead_core_from_zombified_piglin", new AddItemModifier(new LootItemCondition[]{
                new LootTableIdCondition.Builder(ResourceLocation.withDefaultNamespace("entities/zombified_piglin")).build(),
                LootItemRandomChanceCondition.randomChance(0.15F).build()}, ModItems.UNDEAD_CORE.get()));
        this.add("undead_core_from_zombie_horse", new AddItemModifier(new LootItemCondition[]{
                new LootTableIdCondition.Builder(ResourceLocation.withDefaultNamespace("entities/zombie_horse")).build(),
                LootItemRandomChanceCondition.randomChance(0.15F).build()}, ModItems.UNDEAD_CORE.get()));
        this.add("undead_core_from_phantom", new AddItemModifier(new LootItemCondition[]{
                new LootTableIdCondition.Builder(ResourceLocation.withDefaultNamespace("entities/phantom")).build(),
                LootItemRandomChanceCondition.randomChance(0.15F).build()}, ModItems.UNDEAD_CORE.get()));
        this.add("undead_core_from_zoglin", new AddItemModifier(new LootItemCondition[]{
                new LootTableIdCondition.Builder(ResourceLocation.withDefaultNamespace("entities/zoglin")).build(),
                LootItemRandomChanceCondition.randomChance(0.15F).build()}, ModItems.UNDEAD_CORE.get()));
        //Undead Core Drop Boss Undead
        this.add("undead_core_from_wither", new AddItemModifier(new LootItemCondition[]{
                new LootTableIdCondition.Builder(ResourceLocation.withDefaultNamespace("entities/wither")).build(),
                LootItemRandomChanceCondition.randomChance(0.29F).build()}, ModItems.UNDEAD_CORE.get()));

        // Elemental Cores
        this.add("fire_core_from_blaze", new AddItemModifier(new LootItemCondition[]{
                new LootTableIdCondition.Builder(ResourceLocation.withDefaultNamespace("entities/blaze")).build(),
                LootItemKilledByPlayerCondition.killedByPlayer().build(),
                LootItemRandomChanceCondition.randomChance(0.12F).build()}, ModItems.FIRE_CORE.get()));
        this.add("water_core_from_drowned", new AddItemModifier(new LootItemCondition[]{
                new LootTableIdCondition.Builder(ResourceLocation.withDefaultNamespace("entities/drowned")).build(),
                LootItemKilledByPlayerCondition.killedByPlayer().build(),
                LootItemRandomChanceCondition.randomChance(0.12F).build()}, ModItems.WATER_CORE.get()));
        this.add("wood_core_from_creeper", new AddItemModifier(new LootItemCondition[]{
                new LootTableIdCondition.Builder(ResourceLocation.withDefaultNamespace("entities/creeper")).build(),
                LootItemKilledByPlayerCondition.killedByPlayer().build(),
                LootItemRandomChanceCondition.randomChance(0.12F).build()}, ModItems.WOOD_CORE.get()));
        this.add("earth_core_from_husk", new AddItemModifier(new LootItemCondition[]{
                new LootTableIdCondition.Builder(ResourceLocation.withDefaultNamespace("entities/husk")).build(),
                LootItemKilledByPlayerCondition.killedByPlayer().build(),
                LootItemRandomChanceCondition.randomChance(0.12F).build()}, ModItems.EARTH_CORE.get()));
        this.add("metal_core_from_skeleton", new AddItemModifier(new LootItemCondition[]{
                new LootTableIdCondition.Builder(ResourceLocation.withDefaultNamespace("entities/skeleton")).build(),
                LootItemKilledByPlayerCondition.killedByPlayer().build(),
                LootItemRandomChanceCondition.randomChance(0.12F).build()}, ModItems.METAL_CORE.get()));
        this.add("lightning_core_from_witch", new AddItemModifier(new LootItemCondition[]{
                new LootTableIdCondition.Builder(ResourceLocation.withDefaultNamespace("entities/witch")).build(),
                LootItemKilledByPlayerCondition.killedByPlayer().build(),
                LootItemRandomChanceCondition.randomChance(0.12F).build()}, ModItems.LIGHTNING_CORE.get()));
        this.add("wind_core_from_phantom", new AddItemModifier(new LootItemCondition[]{
                new LootTableIdCondition.Builder(ResourceLocation.withDefaultNamespace("entities/phantom")).build(),
                LootItemKilledByPlayerCondition.killedByPlayer().build(),
                LootItemRandomChanceCondition.randomChance(0.12F).build()}, ModItems.WIND_CORE.get()));


        this.add("rectification_of_names_from_stronghold_library", new AddItemModifier(new LootItemCondition[]{
                LootTableIdCondition.builder(ResourceLocation.withDefaultNamespace("chests/stronghold_library")).build(),
                LootItemRandomChanceCondition.randomChance(0.18F).build()}, ModItems.SCHOLARLY_SOUL_RECTIFICATION_OF_NAMES.get()));
        this.add("great_learning_from_woodland_mansion", new AddItemModifier(new LootItemCondition[]{
                LootTableIdCondition.builder(ResourceLocation.withDefaultNamespace("chests/woodland_mansion")).build(),
                LootItemRandomChanceCondition.randomChance(0.16F).build()}, ModItems.SCHOLARLY_SOUL_GREAT_LEARNING.get()));
        this.add("thousand_commentaries_from_ancient_city", new AddItemModifier(new LootItemCondition[]{
                LootTableIdCondition.builder(ResourceLocation.withDefaultNamespace("chests/ancient_city")).build(),
                LootItemRandomChanceCondition.randomChance(0.13F).build()}, ModItems.SCHOLARLY_SOUL_THOUSAND_COMMENTARIES.get()));
        this.add("sage_mandate_from_end_city", new AddItemModifier(new LootItemCondition[]{
                LootTableIdCondition.builder(ResourceLocation.withDefaultNamespace("chests/end_city_treasure")).build(),
                LootItemRandomChanceCondition.randomChance(0.10F).build()}, ModItems.SCHOLARLY_SOUL_SAGE_MANDATE.get()));


        add("disembodied_nether_soul_from_wither", new AddItemModifier(
                new LootItemCondition[]{
                        LootTableIdCondition.builder(ResourceLocation.withDefaultNamespace("entities/wither")).build(),
                        LootItemKilledByPlayerCondition.killedByPlayer().build(),
                        LootItemRandomChanceCondition.randomChance(0.33333334F).build()
                }, ModItems.DISEMBODIED_NETHER_SOUL.get()
        ));

        add("false_deified_orb_from_ender_dragon", new AddItemModifier(
                new LootItemCondition[]{
                        LootTableIdCondition.builder(ResourceLocation.withDefaultNamespace("entities/ender_dragon")).build(),
                        LootItemKilledByPlayerCondition.killedByPlayer().build(),
                        LootItemRandomChanceCondition.randomChance(0.33333334F).build()
                }, ModItems.FALSE_DEIFIED_ORB.get()
        ));

        add("sculk_enamored_eyes_from_ancient_city", new AddItemModifier(
                new LootItemCondition[]{
                        LootTableIdCondition.builder(ResourceLocation.withDefaultNamespace("chests/ancient_city")).build(),
                        LootItemRandomChanceCondition.randomChance(0.06666667F).build()
                }, ModItems.SCULK_ENAMORED_EYES.get()
        ));


        add("ranked_mob_loot", new AddRankedMobLootModifier(
                new LootItemCondition[]{
                        LootItemKilledByPlayerCondition.killedByPlayer().build()
                }
        ));

    }
}