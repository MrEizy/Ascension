package net.zic.ascension.datagen;

import net.minecraft.advancements.criterion.DataComponentMatchers;
import net.minecraft.advancements.criterion.EntityFlagsPredicate;
import net.minecraft.advancements.criterion.EntityPredicate;
import net.minecraft.advancements.criterion.LocationPredicate;
import net.minecraft.advancements.criterion.NbtPredicate;
import net.minecraft.advancements.criterion.PlayerPredicate;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentExactPredicate;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.animal.chicken.ChickenVariant;
import net.minecraft.world.entity.animal.chicken.ChickenVariants;
import net.minecraft.world.entity.animal.fox.Fox;
import net.minecraft.world.level.levelgen.structure.BuiltinStructures;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.AllOfCondition;
import net.minecraft.world.level.storage.loot.predicates.AnyOfCondition;
import net.minecraft.world.level.storage.loot.predicates.InvertedLootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LocationCheck;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemEntityPropertyCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemKilledByPlayerCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.neoforged.neoforge.common.data.GlobalLootModifierProvider;
import net.neoforged.neoforge.common.loot.AddTableLootModifier;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootTableIdCondition;
import net.zic.ascension.AscensionCraft;

import java.util.concurrent.CompletableFuture;

public class AscGlobalLootModifierProvider extends GlobalLootModifierProvider {
    public AscGlobalLootModifierProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, AscensionCraft.MOD_ID);
    }

    @Override
    protected void start() {
        add("bloodlines/barbarian_bloodline_from_vindicator", new AddTableLootModifier(
                new LootItemCondition[] {
                        LootTableIdCondition.builder(Identifier.withDefaultNamespace("entities/vindicator")).build(),
                        killedPersonallyByPlayer().build(),
                        LootItemRandomChanceCondition.randomChance(0.125f).build()
                },
                IGlobalLootModifier.DEFAULT_PRIORITY,
                AscAddedLootTableProvider.VINDICATOR_BARBARIAN_BLOODLINE
        ));
        add("bloodlines/beastkin_bloodline_from_panda", new AddTableLootModifier(
                new LootItemCondition[] {
                        LootTableIdCondition.builder(Identifier.withDefaultNamespace("entities/panda")).build(),
                        killedPersonallyByPlayer().build(),
                        LootItemRandomChanceCondition.randomChance(0.125f).build()
                },
                IGlobalLootModifier.DEFAULT_PRIORITY,
                AscAddedLootTableProvider.PANDA_BEASTKIN_BLOODLINE
        ));
        add("bloodlines/black_iron_ant_bloodline_from_silverfish", new AddTableLootModifier(
                new LootItemCondition[] {
                        LootTableIdCondition.builder(Identifier.withDefaultNamespace("entities/silverfish")).build(),
                        killedPersonallyByPlayer().build(),
                        LootItemRandomChanceCondition.randomChance(0.05f).build()
                },
                IGlobalLootModifier.DEFAULT_PRIORITY,
                AscAddedLootTableProvider.SILVERFISH_BLACK_IRON_ANT_BLOODLINE
        ));
        add("bloodlines/ember_spirit_bloodline_from_magma_cube", new AddTableLootModifier(
                new LootItemCondition[] {
                        LootTableIdCondition.builder(Identifier.withDefaultNamespace("entities/magma_cube")).build(),
                        killedPersonallyByPlayer().build(),
                        LootItemRandomChanceCondition.randomChance(0.005f).build()
                },
                IGlobalLootModifier.DEFAULT_PRIORITY,
                AscAddedLootTableProvider.MAGMA_CUBE_EMBER_SPIRIT_BLOODLINE
        ));
        add("bloodlines/moonveil_fox_bloodline_from_snow_fox", new AddTableLootModifier(
                new LootItemCondition[] {
                        LootTableIdCondition.builder(Identifier.withDefaultNamespace("entities/fox")).build(),
                        LootItemEntityPropertyCondition.hasProperties(LootContext.EntityTarget.THIS,
                                EntityPredicate.Builder.entity().components(DataComponentMatchers.Builder.components()
                                        .exact(DataComponentExactPredicate.expect(DataComponents.FOX_VARIANT, Fox.Variant.SNOW))
                                        .build())).build(),
                        killedPersonallyByPlayer().build(),
                        LootItemRandomChanceCondition.randomChance(0.2f).build()
                },
                IGlobalLootModifier.DEFAULT_PRIORITY,
                AscAddedLootTableProvider.SNOW_FOX_MOONVEIL_FOX_BLOODLINE
        ));
        add("bloodlines/human_bloodline_from_nitwit", new AddTableLootModifier(
                new LootItemCondition[] {
                        LootTableIdCondition.builder(Identifier.withDefaultNamespace("entities/villager")).build(),
                        LootItemEntityPropertyCondition.hasProperties(LootContext.EntityTarget.THIS,
                                EntityPredicate.Builder.entity().nbt(villagerProfession("minecraft:nitwit"))).build(),
                        killedPersonallyByPlayer().build(),
                        LootItemRandomChanceCondition.randomChance(0.125f).build()
                },
                IGlobalLootModifier.DEFAULT_PRIORITY,
                AscAddedLootTableProvider.NITWIT_HUMAN_BLOODLINE
        ));
        add("bloodlines/scalekin_bloodline_from_armadillo", new AddTableLootModifier(
                new LootItemCondition[] {
                        LootTableIdCondition.builder(Identifier.withDefaultNamespace("entities/armadillo")).build(),
                        killedPersonallyByPlayer().build(),
                        LootItemRandomChanceCondition.randomChance(0.05f).build()
                },
                IGlobalLootModifier.DEFAULT_PRIORITY,
                AscAddedLootTableProvider.ARMADILLO_SCALEKIN_BLOODLINE
        ));
        add("bloodlines/verdantblood_bloodline_from_witch", new AddTableLootModifier(
                new LootItemCondition[] {
                        LootTableIdCondition.builder(Identifier.withDefaultNamespace("entities/witch")).build(),
                        killedPersonallyByPlayer().build(),
                        LootItemRandomChanceCondition.randomChance(0.01f).build()
                },
                IGlobalLootModifier.DEFAULT_PRIORITY,
                AscAddedLootTableProvider.WITCH_VERDANTBLOOD_BLOODLINE
        ));
        add("bloodlines/wind_chasing_leopard_bloodline_from_ocelot", new AddTableLootModifier(
                new LootItemCondition[] {
                        LootTableIdCondition.builder(Identifier.withDefaultNamespace("entities/ocelot")).build(),
                        killedPersonallyByPlayer().build(),
                        LootItemRandomChanceCondition.randomChance(0.25f).build()
                },
                IGlobalLootModifier.DEFAULT_PRIORITY,
                AscAddedLootTableProvider.OCELOT_WIND_CHASING_LEOPARD_BLOODLINE
        ));
        add("bloodlines/deep_spring_spirit_bloodline_from_allay", new AddTableLootModifier(
                new LootItemCondition[] {
                        LootTableIdCondition.builder(Identifier.withDefaultNamespace("entities/allay")).build(),
                        killedPersonallyByPlayer().build(),
                        LootItemRandomChanceCondition.randomChance(0.2f).build()
                },
                IGlobalLootModifier.DEFAULT_PRIORITY,
                AscAddedLootTableProvider.ALLAY_DEEP_SPRING_SPIRIT_BLOODLINE
        ));
        add("bloodlines/beast_bloodline_from_wolf", new AddTableLootModifier(
                new LootItemCondition[] {
                        LootTableIdCondition.builder(Identifier.withDefaultNamespace("entities/wolf")).build(),
                        killedPersonallyByPlayer().build(),
                        LootItemRandomChanceCondition.randomChance(0.01f).build()
                },
                IGlobalLootModifier.DEFAULT_PRIORITY,
                AscAddedLootTableProvider.WOLF_BEAST_BLOODLINE
        ));

        // Chickens, by temperature variant
        add("bloodlines/raven_bloodline_from_cold_chicken", new AddTableLootModifier(
                new LootItemCondition[] {
                        LootTableIdCondition.builder(Identifier.withDefaultNamespace("entities/chicken")).build(),
                        chickenVariant(ChickenVariants.COLD).build(),
                        killedPersonallyByPlayer().build(),
                        LootItemRandomChanceCondition.randomChance(0.003f).build()
                },
                IGlobalLootModifier.DEFAULT_PRIORITY,
                AscAddedLootTableProvider.COLD_CHICKEN_RAVEN_BLOODLINE
        ));
        add("bloodlines/crane_bloodline_from_temperate_chicken", new AddTableLootModifier(
                new LootItemCondition[] {
                        LootTableIdCondition.builder(Identifier.withDefaultNamespace("entities/chicken")).build(),
                        chickenVariant(ChickenVariants.TEMPERATE).build(),
                        killedPersonallyByPlayer().build(),
                        LootItemRandomChanceCondition.randomChance(0.001f).build()
                },
                IGlobalLootModifier.DEFAULT_PRIORITY,
                AscAddedLootTableProvider.TEMPERATE_CHICKEN_CRANE_BLOODLINE
        ));
        add("bloodlines/phoenix_bloodline_from_warm_chicken", new AddTableLootModifier(
                new LootItemCondition[] {
                        LootTableIdCondition.builder(Identifier.withDefaultNamespace("entities/chicken")).build(),
                        chickenVariant(ChickenVariants.WARM).build(),
                        killedPersonallyByPlayer().build(),
                        LootItemRandomChanceCondition.randomChance(0.001f).build()
                },
                IGlobalLootModifier.DEFAULT_PRIORITY,
                AscAddedLootTableProvider.WARM_CHICKEN_PHOENIX_BLOODLINE
        ));
        add("physiques/ember_touched_meridians_physique_from_magma_cube", new AddTableLootModifier(
                new LootItemCondition[] {
                        LootTableIdCondition.builder(Identifier.withDefaultNamespace("entities/magma_cube")).build(),
                        killedPersonallyByPlayer().build(),
                        LootItemRandomChanceCondition.randomChance(0.0005f).build()
                },
                IGlobalLootModifier.DEFAULT_PRIORITY,
                AscAddedLootTableProvider.MAGMA_CUBE_EMBER_TOUCHED_MERIDIANS_PHYSIQUE
        ));

        // Dungeon chests
        add("bloodlines/falling_star_bloodline_from_dungeon", new AddTableLootModifier(
                new LootItemCondition[] {
                        LootTableIdCondition.builder(Identifier.withDefaultNamespace("chests/simple_dungeon")).build(),
                        LootItemRandomChanceCondition.randomChance(0.5f).build()
                },
                IGlobalLootModifier.DEFAULT_PRIORITY,
                AscAddedLootTableProvider.DUNGEON_FALLING_STAR_BLOODLINE
        ));
        add("physiques/starlit_meridians_physique_from_dungeon", new AddTableLootModifier(
                new LootItemCondition[] {
                        LootTableIdCondition.builder(Identifier.withDefaultNamespace("chests/simple_dungeon")).build(),
                        LootItemRandomChanceCondition.randomChance(0.2f).build()
                },
                IGlobalLootModifier.DEFAULT_PRIORITY,
                AscAddedLootTableProvider.DUNGEON_STARLIT_MERIDIANS_PHYSIQUE
        ));

        // Igloo chests
        add("bloodlines/frostkin_bloodline_from_igloo", new AddTableLootModifier(
                new LootItemCondition[] {
                        LootTableIdCondition.builder(Identifier.withDefaultNamespace("chests/igloo_chest")).build(),
                        LootItemRandomChanceCondition.randomChance(0.5f).build()
                },
                IGlobalLootModifier.DEFAULT_PRIORITY,
                AscAddedLootTableProvider.IGLOO_FROSTKIN_BLOODLINE
        ));

        // Desert pyramid chests
        add("bloodlines/golden_maned_lion_bloodline_from_desert_pyramid", new AddTableLootModifier(
                new LootItemCondition[] {
                        LootTableIdCondition.builder(Identifier.withDefaultNamespace("chests/desert_pyramid")).build(),
                        LootItemRandomChanceCondition.randomChance(0.18f).build()
                },
                IGlobalLootModifier.DEFAULT_PRIORITY,
                AscAddedLootTableProvider.DESERT_PYRAMID_GOLDEN_MANED_LION_BLOODLINE
        ));

        // Snowy village house chests
        add("bloodlines/moon_white_crane_bloodline_from_snowy_village", new AddTableLootModifier(
                new LootItemCondition[] {
                        LootTableIdCondition.builder(Identifier.withDefaultNamespace("chests/village/village_snowy_house")).build(),
                        LootItemRandomChanceCondition.randomChance(0.04f).build()
                },
                IGlobalLootModifier.DEFAULT_PRIORITY,
                AscAddedLootTableProvider.SNOWY_VILLAGE_MOON_WHITE_CRANE_BLOODLINE
        ));

        // Shipwreck treasure chests
        add("bloodlines/greed_demon_bloodline_from_shipwreck", new AddTableLootModifier(
                new LootItemCondition[] {
                        LootTableIdCondition.builder(Identifier.withDefaultNamespace("chests/shipwreck_treasure")).build(),
                        LootItemRandomChanceCondition.randomChance(0.6f).build()
                },
                IGlobalLootModifier.DEFAULT_PRIORITY,
                AscAddedLootTableProvider.SHIPWRECK_GREED_DEMON_BLOODLINE
        ));

        // Physiques from mobs
        add("physiques/brittle_bone_body_physique_from_phantom", new AddTableLootModifier(
                new LootItemCondition[] {
                        LootTableIdCondition.builder(Identifier.withDefaultNamespace("entities/phantom")).build(),
                        killedPersonallyByPlayer().build(),
                        LootItemRandomChanceCondition.randomChance(0.005f).build()
                },
                IGlobalLootModifier.DEFAULT_PRIORITY,
                AscAddedLootTableProvider.PHANTOM_BRITTLE_BONE_BODY_PHYSIQUE
        ));
        add("physiques/cursed_body_physique_from_zombified_piglin", new AddTableLootModifier(
                new LootItemCondition[] {
                        LootTableIdCondition.builder(Identifier.withDefaultNamespace("entities/zombified_piglin")).build(),
                        killedPersonallyByPlayer().build(),
                        LootItemRandomChanceCondition.randomChance(0.01f).build()
                },
                IGlobalLootModifier.DEFAULT_PRIORITY,
                AscAddedLootTableProvider.ZOMBIFIED_PIGLIN_CURSED_BODY_PHYSIQUE
        ));
        add("physiques/dampened_fire_body_physique_from_slime", new AddTableLootModifier(
                new LootItemCondition[] {
                        LootTableIdCondition.builder(Identifier.withDefaultNamespace("entities/slime")).build(),
                        killedPersonallyByPlayer().build(),
                        LootItemRandomChanceCondition.randomChance(0.0001f).build()
                },
                IGlobalLootModifier.DEFAULT_PRIORITY,
                AscAddedLootTableProvider.SLIME_DAMPENED_FIRE_BODY_PHYSIQUE
        ));
        add("physiques/frostbitten_vein_body_physique_from_stray", new AddTableLootModifier(
                new LootItemCondition[] {
                        LootTableIdCondition.builder(Identifier.withDefaultNamespace("entities/stray")).build(),
                        killedPersonallyByPlayer().build(),
                        LootItemRandomChanceCondition.randomChance(0.005f).build()
                },
                IGlobalLootModifier.DEFAULT_PRIORITY,
                AscAddedLootTableProvider.STRAY_FROSTBITTEN_VEIN_BODY_PHYSIQUE
        ));
        add("physiques/heavy_bone_physique_from_polar_bear", new AddTableLootModifier(
                new LootItemCondition[] {
                        LootTableIdCondition.builder(Identifier.withDefaultNamespace("entities/polar_bear")).build(),
                        killedPersonallyByPlayer().build(),
                        LootItemRandomChanceCondition.randomChance(0.05f).build()
                },
                IGlobalLootModifier.DEFAULT_PRIORITY,
                AscAddedLootTableProvider.POLAR_BEAR_HEAVY_BONE_PHYSIQUE
        ));
        add("physiques/iron_ant_bones_physique_from_silverfish", new AddTableLootModifier(
                new LootItemCondition[] {
                        LootTableIdCondition.builder(Identifier.withDefaultNamespace("entities/silverfish")).build(),
                        killedPersonallyByPlayer().build(),
                        LootItemRandomChanceCondition.randomChance(0.0005f).build()
                },
                IGlobalLootModifier.DEFAULT_PRIORITY,
                AscAddedLootTableProvider.SILVERFISH_IRON_ANT_BONES_PHYSIQUE
        ));
        add("physiques/iron_blossom_physique_from_iron_golem", new AddTableLootModifier(
                new LootItemCondition[] {
                        LootTableIdCondition.builder(Identifier.withDefaultNamespace("entities/iron_golem")).build(),
                        // Naturally spawned only (villages); player-built golems save PlayerCreated: true
                        LootItemEntityPropertyCondition.hasProperties(LootContext.EntityTarget.THIS,
                                EntityPredicate.Builder.entity().nbt(notPlayerCreated())).build(),
                        killedPersonallyByPlayer().build(),
                        LootItemRandomChanceCondition.randomChance(0.05f).build()
                },
                IGlobalLootModifier.DEFAULT_PRIORITY,
                AscAddedLootTableProvider.IRON_GOLEM_IRON_BLOSSOM_PHYSIQUE
        ));

        // Physiques from chests
        add("physiques/five_elements_imbalance_physique_from_beached_shipwreck", new AddTableLootModifier(
                new LootItemCondition[] {
                        LootTableIdCondition.builder(Identifier.withDefaultNamespace("chests/shipwreck_supply")).build(),
                        // Beached shipwrecks share loot tables with ocean ones, so check the structure itself
                        LocationCheck.checkLocation(LocationPredicate.Builder.inStructure(
                                this.registries.lookupOrThrow(Registries.STRUCTURE).getOrThrow(BuiltinStructures.SHIPWRECK_BEACHED))).build(),
                        LootItemRandomChanceCondition.randomChance(0.2f).build()
                },
                IGlobalLootModifier.DEFAULT_PRIORITY,
                AscAddedLootTableProvider.BEACHED_SHIPWRECK_FIVE_ELEMENTS_IMBALANCE_PHYSIQUE
        ));
        add("physiques/miasma_lungs_body_physique_from_mineshaft", new AddTableLootModifier(
                new LootItemCondition[] {
                        LootTableIdCondition.builder(Identifier.withDefaultNamespace("chests/abandoned_mineshaft")).build(),
                        LootItemRandomChanceCondition.randomChance(0.05f).build()
                },
                IGlobalLootModifier.DEFAULT_PRIORITY,
                AscAddedLootTableProvider.MINESHAFT_MIASMA_LUNGS_BODY_PHYSIQUE
        ));
        add("physiques/golden_sun_heart_physique_from_desert_pyramid", new AddTableLootModifier(
                new LootItemCondition[] {
                        LootTableIdCondition.builder(Identifier.withDefaultNamespace("chests/desert_pyramid")).build(),
                        LootItemRandomChanceCondition.randomChance(0.04f).build()
                },
                IGlobalLootModifier.DEFAULT_PRIORITY,
                AscAddedLootTableProvider.DESERT_PYRAMID_GOLDEN_SUN_HEART_PHYSIQUE
        ));
        add("physiques/hundred_venom_dantian_physique_from_jungle_temple", new AddTableLootModifier(
                new LootItemCondition[] {
                        LootTableIdCondition.builder(Identifier.withDefaultNamespace("chests/jungle_temple")).build(),
                        LootItemRandomChanceCondition.randomChance(0.15f).build()
                },
                IGlobalLootModifier.DEFAULT_PRIORITY,
                AscAddedLootTableProvider.JUNGLE_TEMPLE_HUNDRED_VENOM_DANTIAN_PHYSIQUE
        ));
        add("physiques/ice_soul_body_flawed_physique_from_igloo", new AddTableLootModifier(
                new LootItemCondition[] {
                        LootTableIdCondition.builder(Identifier.withDefaultNamespace("chests/igloo_chest")).build(),
                        LootItemRandomChanceCondition.randomChance(0.2f).build()
                },
                IGlobalLootModifier.DEFAULT_PRIORITY,
                AscAddedLootTableProvider.IGLOO_ICE_SOUL_BODY_FLAWED_PHYSIQUE
        ));
        add("physiques/jade_furnace_physique_from_nether_fortress", new AddTableLootModifier(
                new LootItemCondition[] {
                        LootTableIdCondition.builder(Identifier.withDefaultNamespace("chests/nether_bridge")).build(),
                        LootItemRandomChanceCondition.randomChance(0.05f).build()
                },
                IGlobalLootModifier.DEFAULT_PRIORITY,
                AscAddedLootTableProvider.NETHER_FORTRESS_JADE_FURNACE_PHYSIQUE
        ));
        add("physiques/shallow_core_body_physique_from_dungeon", new AddTableLootModifier(
                new LootItemCondition[] {
                        LootTableIdCondition.builder(Identifier.withDefaultNamespace("chests/simple_dungeon")).build(),
                        LootItemRandomChanceCondition.randomChance(0.1f).build()
                },
                IGlobalLootModifier.DEFAULT_PRIORITY,
                AscAddedLootTableProvider.DUNGEON_SHALLOW_CORE_BODY_PHYSIQUE
        ));

        // Physiques from mob gifts (every generated item is dropped, so adding works)
        add("physiques/tiger_marrow_physique_from_cat_gift", new AddTableLootModifier(
                new LootItemCondition[] {
                        LootTableIdCondition.builder(Identifier.withDefaultNamespace("gameplay/cat_morning_gift")).build(),
                        LootItemRandomChanceCondition.randomChance(0.1f).build()
                },
                IGlobalLootModifier.DEFAULT_PRIORITY,
                AscAddedLootTableProvider.CAT_GIFT_TIGER_MARROW_PHYSIQUE
        ));
        add("physiques/trembling_earth_body_unstable_physique_from_sniffer_digging", new AddTableLootModifier(
                new LootItemCondition[] {
                        LootTableIdCondition.builder(Identifier.withDefaultNamespace("gameplay/sniffer_digging")).build(),
                        LootItemRandomChanceCondition.randomChance(0.01f).build()
                },
                IGlobalLootModifier.DEFAULT_PRIORITY,
                AscAddedLootTableProvider.SNIFFER_TREMBLING_EARTH_BODY_UNSTABLE_PHYSIQUE
        ));

        // More physiques from mobs
        add("physiques/fire_spirit_body_unstable_physique_from_blaze", new AddTableLootModifier(
                new LootItemCondition[] {
                        LootTableIdCondition.builder(Identifier.withDefaultNamespace("entities/blaze")).build(),
                        killedPersonallyByPlayer().build(),
                        LootItemRandomChanceCondition.randomChance(0.0005f).build()
                },
                IGlobalLootModifier.DEFAULT_PRIORITY,
                AscAddedLootTableProvider.BLAZE_FIRE_SPIRIT_BODY_UNSTABLE_PHYSIQUE
        ));
        add("physiques/firefly_spirit_eyes_physique_from_frog", new AddTableLootModifier(
                new LootItemCondition[] {
                        LootTableIdCondition.builder(Identifier.withDefaultNamespace("entities/frog")).build(),
                        killedPersonallyByPlayer().build(),
                        LootItemRandomChanceCondition.randomChance(0.0005f).build()
                },
                IGlobalLootModifier.DEFAULT_PRIORITY,
                AscAddedLootTableProvider.FROG_FIREFLY_SPIRIT_EYES_PHYSIQUE
        ));
        add("physiques/gale_cheetah_meridians_physique_from_ocelot", new AddTableLootModifier(
                new LootItemCondition[] {
                        LootTableIdCondition.builder(Identifier.withDefaultNamespace("entities/ocelot")).build(),
                        killedPersonallyByPlayer().build(),
                        // Half the Wind-Chasing Leopard bloodline chance (25%)
                        LootItemRandomChanceCondition.randomChance(0.125f).build()
                },
                IGlobalLootModifier.DEFAULT_PRIORITY,
                AscAddedLootTableProvider.OCELOT_GALE_CHEETAH_MERIDIANS_PHYSIQUE
        ));
        add("physiques/lion_roar_chest_physique_from_llama", new AddTableLootModifier(
                new LootItemCondition[] {
                        LootTableIdCondition.builder(Identifier.withDefaultNamespace("entities/llama")).build(),
                        killedPersonallyByPlayer().build(),
                        LootItemRandomChanceCondition.randomChance(0.05f).build()
                },
                IGlobalLootModifier.DEFAULT_PRIORITY,
                AscAddedLootTableProvider.LLAMA_LION_ROAR_CHEST_PHYSIQUE
        ));
        add("physiques/thunder_physiques_from_charged_creeper", new AddTableLootModifier(
                new LootItemCondition[] {
                        LootTableIdCondition.builder(Identifier.withDefaultNamespace("entities/creeper")).build(),
                        LootItemEntityPropertyCondition.hasProperties(LootContext.EntityTarget.THIS,
                                EntityPredicate.Builder.entity().nbt(booleanNbt("powered", true))).build(),
                        killedPersonallyByPlayer().build()
                        // The 10% / 10% split lives in the table so only one can drop
                },
                IGlobalLootModifier.DEFAULT_PRIORITY,
                AscAddedLootTableProvider.CHARGED_CREEPER_THUNDER_PHYSIQUES
        ));
        add("physiques/abyss_drifter_physique_from_enderman", new AddTableLootModifier(
                new LootItemCondition[] {
                        LootTableIdCondition.builder(Identifier.withDefaultNamespace("entities/enderman")).build(),
                        killedPersonallyByPlayer().build(),
                        LootItemRandomChanceCondition.randomChance(0.001f).build()
                },
                IGlobalLootModifier.DEFAULT_PRIORITY,
                AscAddedLootTableProvider.ENDERMAN_ABYSS_DRIFTER_PHYSIQUE
        ));
        add("physiques/weak_spirit_body_physique_from_wandering_trader", new AddTableLootModifier(
                new LootItemCondition[] {
                        LootTableIdCondition.builder(Identifier.withDefaultNamespace("entities/wandering_trader")).build(),
                        killedPersonallyByPlayer().build(),
                        LootItemRandomChanceCondition.randomChance(0.05f).build()
                },
                IGlobalLootModifier.DEFAULT_PRIORITY,
                AscAddedLootTableProvider.WANDERING_TRADER_WEAK_SPIRIT_BODY_PHYSIQUE
        ));

        add("bloodlines/wrath_demon_bloodline_from_baby_zombie", new AddTableLootModifier(
                new LootItemCondition[] {
                        AnyOfCondition.anyOf(
                                LootTableIdCondition.builder(Identifier.withDefaultNamespace("entities/zombie")),
                                LootTableIdCondition.builder(Identifier.withDefaultNamespace("entities/husk")),
                                LootTableIdCondition.builder(Identifier.withDefaultNamespace("entities/drowned")),
                                LootTableIdCondition.builder(Identifier.withDefaultNamespace("entities/zombie_villager"))
                        ).build(),
                        LootItemEntityPropertyCondition.hasProperties(LootContext.EntityTarget.THIS,
                                EntityPredicate.Builder.entity().flags(EntityFlagsPredicate.Builder.flags().setIsBaby(true))).build(),
                        killedPersonallyByPlayer().build(),
                        LootItemRandomChanceCondition.randomChance(0.03f).build()
                },
                IGlobalLootModifier.DEFAULT_PRIORITY,
                AscAddedLootTableProvider.BABY_ZOMBIE_WRATH_DEMON_BLOODLINE
        ));
    }

    private LootItemCondition.Builder chickenVariant(ResourceKey<ChickenVariant> variant) {
        return LootItemEntityPropertyCondition.hasProperties(LootContext.EntityTarget.THIS,
                EntityPredicate.Builder.entity().components(DataComponentMatchers.Builder.components()
                        .exact(DataComponentExactPredicate.expect(DataComponents.CHICKEN_VARIANT,
                                this.registries.lookupOrThrow(Registries.CHICKEN_VARIANT).getOrThrow(variant)))
                        .build()));
    }

    /** Villager profession isn't exposed as a data component, so match the saved {@code VillagerData} instead. */
    @SuppressWarnings("SameParameterValue") // reusable for other professions
    private static NbtPredicate villagerProfession(String profession) {
        CompoundTag villagerData = new CompoundTag();
        villagerData.putString("profession", profession);
        CompoundTag tag = new CompoundTag();
        tag.put("VillagerData", villagerData);
        return new NbtPredicate(tag);
    }

    private static NbtPredicate notPlayerCreated() {
        return booleanNbt("PlayerCreated", false);
    }

    private static NbtPredicate booleanNbt(String key, boolean value) {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean(key, value);
        return new NbtPredicate(tag);
    }

    /**
     * A player recently hit the mob, and no other mob landed the killing blow.
     * Fire ticks, falls etc. after a player's hit still count; pets, golems and mob farms do not.
     */
    private static LootItemCondition.Builder killedPersonallyByPlayer() {
        return AllOfCondition.allOf(
                LootItemKilledByPlayerCondition.killedByPlayer(),
                AnyOfCondition.anyOf(
                        LootItemEntityPropertyCondition.hasProperties(LootContext.EntityTarget.ATTACKER,
                                EntityPredicate.Builder.entity().subPredicate(PlayerPredicate.Builder.player().build())),
                        InvertedLootItemCondition.invert(LootItemEntityPropertyCondition.entityPresent(LootContext.EntityTarget.ATTACKER))));
    }
}
