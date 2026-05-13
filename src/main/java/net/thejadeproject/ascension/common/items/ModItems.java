package net.thejadeproject.ascension.common.items;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.*;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.thejadeproject.ascension.AscensionCraft;
import net.thejadeproject.ascension.common.blocks.ModBlocks;

import net.thejadeproject.ascension.common.items.artifacts.talismans.*;
import net.thejadeproject.ascension.common.items.techniques.TechniqueBinderItem;
import net.thejadeproject.ascension.common.items.techniques.TechniquePageItem;
import net.thejadeproject.ascension.common.items.tools.herbs.MortarAndPestle;
import net.thejadeproject.ascension.common.items.tools.herbs.SpiritualMeal;
import net.thejadeproject.ascension.common.items.tools.hidden_weapons.NeedleItem;
import net.thejadeproject.ascension.entity.ModEntities;
import net.thejadeproject.ascension.common.items.artifacts.*;
import net.thejadeproject.ascension.common.items.data_components.ModDataComponents;

import net.thejadeproject.ascension.common.items.herbs.*;
import net.thejadeproject.ascension.common.items.physiques.PhysiqueTransferItem;
import net.thejadeproject.ascension.common.items.pills.*;
import net.thejadeproject.ascension.common.items.stones.SpatialStoneItem;
import net.thejadeproject.ascension.common.items.techniques.ScholarlySoulChapterItem;
import net.thejadeproject.ascension.common.items.techniques.TechniqueTransferItem;
import net.thejadeproject.ascension.common.items.tools.BladeItem;
import net.thejadeproject.ascension.common.items.tools.FanItem;
import net.thejadeproject.ascension.common.items.tools.SpearItem;
import net.thejadeproject.ascension.common.items.tools.SpiritualStoneTools;
import net.thejadeproject.ascension.refactor_packages.alchemy.ModPillEffects;
import net.thejadeproject.ascension.refactor_packages.techniques.custom.technique_data.ScholarlySoulTechniqueData;
import net.thejadeproject.ascension.util.ItemUtil;

import java.util.List;


public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(AscensionCraft.MOD_ID);

    public static final DeferredItem<Item> ANCESTOR_JOURNAL = ITEMS.register("ancestor_journal",
            () -> new AncestorJournalItem(ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "ancestor_journal")));

    public static final DeferredItem<Item> FAN = ITEMS.register("fan",
            () -> new FanItem(new Item.Properties()));



    // ── Silver Needles ──────────────────────────────────────────────────────────
    public static final DeferredItem<NeedleItem> SILVER_NEEDLE = ITEMS.register("silver_needle",
            () -> new NeedleItem(new Item.Properties().stacksTo(64)));




    public static final DeferredItem<Item> RIFT_SUMMONER_DEBUG_STICK = ITEMS.register("rift_debug_stick",
            () -> new ShaderSummonerItem(new Item.Properties()));



    //Spatial Ring Stuff

    public static final DeferredItem<Item> SPATIAL_RING = ITEMS.register("spatial_ring",
            ()->new SpatialRing(new Item.Properties()
                    .stacksTo(1)));

    public static final DeferredItem<Item> SPATIAL_STONE_TIER_1 = ITEMS.register("spatial_stone_tier_1",
            () -> new SpatialStoneItem(1, "I", Rarity.COMMON));

    // Stack Upgrades Todo Fix upgrades
    /*public static final DeferredItem<Item> STACK_UPGRADE_T1 = ITEMS.register("stack_upgrade_t1",
            () -> new UpgradeItem("stack_upgrade_t1", Rarity.UNCOMMON));
    public static final DeferredItem<Item> STACK_UPGRADE_T2 = ITEMS.register("stack_upgrade_t2",
            () -> new UpgradeItem("stack_upgrade_t2", Rarity.RARE));

    // Pickup Upgrade
    public static final DeferredItem<Item> PICKUP_UPGRADE = ITEMS.register("pickup_upgrade",
            () -> new UpgradeItem("pickup_upgrade", Rarity.RARE));*/



















    public static final DeferredItem<Item> RAW_BLACK_IRON = ITEMS.register("raw_black_iron",
            () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> BLACK_IRON_NUGGET = ITEMS.register("black_iron_nugget",
            () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> BLACK_IRON_INGOT = ITEMS.register("black_iron_ingot",
            () -> new Item(new Item.Properties()));

    public static final DeferredItem<Item> RAW_FROST_SILVER = ITEMS.register("raw_frost_silver",
            () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> FROST_SILVER_NUGGET = ITEMS.register("frost_silver_nugget",
            () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> FROST_SILVER_INGOT = ITEMS.register("frost_silver_ingot",
            () -> new Item(new Item.Properties()));

    public static final DeferredItem<Item> JADE = ITEMS.register("jade",
            () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> JADE_NUGGET = ITEMS.register("jade_nugget",
            () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> SPIRITUAL_STONE = ITEMS.register("spiritual_stone",
            () -> new Item(new Item.Properties().rarity(Rarity.EPIC)));


    //Weapons & Tools

    public static final DeferredItem<Item> MORTAR_PESTLE = ITEMS.register("mortar_pestle",
            () -> new MortarAndPestle(new Item.Properties()));
    public static final DeferredItem<Item> SPIRITUAL_MEAL = ITEMS.register("spiritual_meal",
            () -> new SpiritualMeal(new Item.Properties()));



    public static final DeferredItem<BladeItem> WOODEN_BLADE = ITEMS.register("wooden_blade",
            () -> new BladeItem(Tiers.WOOD, new Item.Properties().durability(69).attributes(BladeItem.createAttributes((Tier) Tiers.WOOD, 2, (float) -2))));
    public static final DeferredItem<BladeItem> STONE_BLADE = ITEMS.register("stone_blade",
            () -> new BladeItem(Tiers.STONE, new Item.Properties().durability(141).attributes(BladeItem.createAttributes((Tier) Tiers.STONE, 2, (float) -2))));
    public static final DeferredItem<BladeItem> IRON_BLADE = ITEMS.register("iron_blade",
            () -> new BladeItem(Tiers.IRON, new Item.Properties().durability(260).attributes(BladeItem.createAttributes((Tier) Tiers.IRON, 2, (float) -2))));
    public static final DeferredItem<BladeItem> GOLD_BLADE = ITEMS.register("gold_blade",
            () -> new BladeItem(Tiers.GOLD, new Item.Properties().durability(42).attributes(BladeItem.createAttributes((Tier) Tiers.GOLD, 2f, (float) -2))));
    public static final DeferredItem<BladeItem> DIAMOND_BLADE = ITEMS.register("diamond_blade",
            () -> new BladeItem(Tiers.DIAMOND, new Item.Properties().durability(1661).attributes(BladeItem.createAttributes((Tier) Tiers.DIAMOND, 2, (float) -2))));
    public static final DeferredItem<BladeItem> NETHERITE_BLADE = ITEMS.register("netherite_blade",
            () -> new BladeItem(Tiers.NETHERITE, new Item.Properties().durability(2131).attributes(BladeItem.createAttributes((Tier) Tiers.NETHERITE, 2, (float) -2))));


    public static final DeferredItem<SpearItem> WOODEN_SPEAR = ITEMS.register("wooden_spear",
            () -> new SpearItem(Tiers.WOOD, new Item.Properties().durability(49).attributes(ItemUtil.createAscensionItemAttributes(Tiers.WOOD, 3, -2.4f,0.5))));
    public static final DeferredItem<SpearItem> STONE_SPEAR = ITEMS.register("stone_spear",
            () -> new SpearItem(Tiers.STONE, new Item.Properties().durability(121).attributes(ItemUtil.createAscensionItemAttributes(Tiers.STONE, 3, -2.4f,1))));
    public static final DeferredItem<SpearItem> IRON_SPEAR = ITEMS.register("iron_spear",
            () -> new SpearItem(Tiers.IRON, new Item.Properties().durability(240).attributes(ItemUtil.createAscensionItemAttributes(Tiers.IRON, 3, -2.4f,1.5))));
    public static final DeferredItem<SpearItem> GOLD_SPEAR = ITEMS.register("gold_spear",
            () -> new SpearItem(Tiers.GOLD, new Item.Properties().durability(22).attributes(ItemUtil.createAscensionItemAttributes(Tiers.GOLD, 3, -2.4f,1.5))));
    public static final DeferredItem<SpearItem> DIAMOND_SPEAR = ITEMS.register("diamond_spear",
            () -> new SpearItem(Tiers.DIAMOND, new Item.Properties().durability(1461).attributes(ItemUtil.createAscensionItemAttributes(Tiers.DIAMOND, 3, -2.4f,2))));
    public static final DeferredItem<SpearItem> NETHERITE_SPEAR = ITEMS.register("netherite_spear",
            () -> new SpearItem(Tiers.NETHERITE, new Item.Properties().durability(1931).attributes(ItemUtil.createAscensionItemAttributes(Tiers.NETHERITE, 3, -2.4f,2))));


    //Custom Tools
    public static final DeferredItem<Item> SPIRITUAL_STONE_PICKAXE = ITEMS.register("spiritual_stone_pickaxe",
            () -> new SpiritualStoneTools.Pickaxe(ModToolTiers.SPIRITUAL_STONE,
                    new Item.Properties().attributes(PickaxeItem.createAttributes(ModToolTiers.SPIRITUAL_STONE, 1.0F, -2.8F))));

    public static final DeferredItem<Item> SPIRITUAL_STONE_AXE = ITEMS.register("spiritual_stone_axe",
            () -> new SpiritualStoneTools.Axe(ModToolTiers.SPIRITUAL_STONE,
                    new Item.Properties().attributes(AxeItem.createAttributes(ModToolTiers.SPIRITUAL_STONE, 6.0F, -3.1F))));

    public static final DeferredItem<Item> SPIRITUAL_STONE_SHOVEL = ITEMS.register("spiritual_stone_shovel",
            () -> new SpiritualStoneTools.Shovel(ModToolTiers.SPIRITUAL_STONE,
                    new Item.Properties().attributes(ShovelItem.createAttributes(ModToolTiers.SPIRITUAL_STONE, 1.5F, -3.0F))));

    public static final DeferredItem<Item> SPIRITUAL_STONE_HOE = ITEMS.register("spiritual_stone_hoe",
            () -> new SpiritualStoneTools.Hoe(ModToolTiers.SPIRITUAL_STONE,
                    new Item.Properties().attributes(HoeItem.createAttributes(ModToolTiers.SPIRITUAL_STONE, -2.0F, -1.0F))));






    //Artifacts


    public static final DeferredItem<Item> SOUL_ANCHOR_TALISMAN = ITEMS.register("soul_anchor_talisman",
            () -> new SoulAnchorTalisman(new Item.Properties()));



    public static final DeferredItem<Item> SPATIAL_RUPTURE_TALISMAN_T1 = ITEMS.register("spatial_rupture_talisman_t1",
            () -> new SpatialRuptureTalismanT1(new Item.Properties()));
    public static final DeferredItem<Item> SPATIAL_RUPTURE_TALISMAN_T2 = ITEMS.register("spatial_rupture_talisman_t2",
            () -> new SpatialRuptureTalismanT2(new Item.Properties()));
    public static final DeferredItem<Item> SPATIAL_RUPTURE_TALISMAN_T3 = ITEMS.register("spatial_rupture_talisman_t3",
            () -> new SpatialRuptureTalismanT3(new Item.Properties()));
    public static final DeferredItem<Item> SOULSTEAD_RETURN_TALISMAN = ITEMS.register("soulstead_return_talisman",
            () -> new SoulsteadReturnTalisman(new Item.Properties()));
    public static final DeferredItem<Item> WORLD_AXIS_TALISMAN = ITEMS.register("world_axis_talisman",
            () -> new WorldAxisTalisman(new Item.Properties()));
    public static final DeferredItem<Item> VOID_MARKING_TALISMAN = ITEMS.register("void_marking_talisman",
            () -> new VoidMarkingTalisman(new Item.Properties()));

    public static final DeferredItem<Item> DEATH_RECALL_TALISMAN = ITEMS.register("death_recall_talisman",
            () -> new DeathRecallTalisman(new Item.Properties()));


    public static final DeferredItem<Item> TABLET_OF_DESTRUCTION_HUMAN = ITEMS.register("todh",
            () -> new TabletOfDestructionHuman(new Item.Properties()));
    public static final DeferredItem<Item> TABLET_OF_DESTRUCTION_EARTH = ITEMS.register("tode",
            () -> new TabletOfDestructionEarth(new Item.Properties()));
    public static final DeferredItem<Item> TABLET_OF_DESTRUCTION_HEAVEN = ITEMS.register("todhe",
            () -> new TabletOfDestructionHeaven(new Item.Properties()));

    public static final DeferredItem<Item> FIRE_GOURD = ITEMS.register("fire_gourd",
            () -> new FlameGourd(new Item.Properties()));

    public static final DeferredItem<Item> REPAIR_SLIP = ITEMS.register("repair_slip",
            () -> new RepairSlip(new Item.Properties()));
    public static final DeferredItem<Item> ENDER_POUCH = ITEMS.register("ender_pouch",
            () -> new EnderPouch(new Item.Properties()));


    //Drops
    public static final DeferredItem<Item> LIVING_CORE = ITEMS.register("living_core",
            () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> UNDEAD_CORE = ITEMS.register("undead_core",
            () -> new Item(new Item.Properties()));

    //Spiritual Fires
    public static final DeferredItem<Item> CRIMSON_LOTUS_FLAME = ITEMS.register("crimson_lotus_flame",
            () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> FLAME = ITEMS.register("flame",
            () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> SOUL_FLAME = ITEMS.register("soul_flame",
            () -> new Item(new Item.Properties()));

    //Crafting Ingredients
    public static final DeferredItem<Item> TALISMAN_PAPER = ITEMS.register("talisman_paper",
            () -> new Item(new Item.Properties()));


    //Pills

    public static final DeferredItem<Item> NEUTRALITY_PILL = ITEMS.register("neutrality_pill",
            () -> new PillCooldownItem(new Item.Properties().food(ModFoodProperties.NEUTRALITY_PILL), 400));
    public static final DeferredItem<Item> CLEANSING_PILL_T1 = ITEMS.register("cleansing_pill_t1",
            () -> new PillCooldownItem(new Item.Properties().food(ModFoodProperties.CLEANSING_PILL_T1), 400));
    public static final DeferredItem<Item> CLEANSING_PILL_T2 = ITEMS.register("cleansing_pill_t2",
            () -> new PillCooldownItem(new Item.Properties().food(ModFoodProperties.CLEANSING_PILL_T2), 400));
    public static final DeferredItem<Item> CLEANSING_PILL_T3 = ITEMS.register("cleansing_pill_t3",
            () -> new PillCooldownItem(new Item.Properties().food(ModFoodProperties.CLEANSING_PILL_T3), 400));
    public static final DeferredItem<Item> CLEANSING_PILL_T4 = ITEMS.register("cleansing_pill_t4",
            () -> new PillCooldownItem(new Item.Properties().food(ModFoodProperties.CLEANSING_PILL_T4), 400));

    public static final DeferredItem<Item> PILL_RESIDUE = ITEMS.register("pill_residue",
            () -> new PillResidue(new Item.Properties().food(ModFoodProperties.PILL_RESIDUE)));
    public static final DeferredItem<Item> FASTING_PILL_T1 = ITEMS.register("fasting_pill_t1",
            () -> new Item(new Item.Properties().food(ModFoodProperties.FASTING_PILL_T1)));
    public static final DeferredItem<Item> FASTING_PILL_T2 = ITEMS.register("fasting_pill_t2",
            () -> new Item(new Item.Properties().food(ModFoodProperties.FASTING_PILL_T2)));
    public static final DeferredItem<Item> FASTING_PILL_T3 = ITEMS.register("fasting_pill_t3",
            () -> new Item(new Item.Properties().food(ModFoodProperties.FASTING_PILL_T3)));


    //Important Pills
    // ── Cultivation Pills ─────────────────────────────────────────
    public static final DeferredItem<PillItem> ESSENCE_GATHERING_PILL = ITEMS.register("essence_gathering_pill",
            () -> new PillItem(new Item.Properties()
                    .food(ModFoodProperties.CULT_PILL)
                    .component(ModDataComponents.PILL_EFFECTS, List.of(ModPillEffects.ESSENCE_EFFECT.getId().toString()))
                    , 200, false));
    public static final DeferredItem<PillItem> INNER_REINFORCEMENT_PILL = ITEMS.register("inner_reinforcement_pill",
            () -> new PillItem(new Item.Properties()
                    .food(ModFoodProperties.CULT_PILL)
                    .component(ModDataComponents.PILL_EFFECTS, List.of(ModPillEffects.BODY_EFFECT.getId().toString()))
                    , 200, false));
    public static final DeferredItem<PillItem> SOUL_FOCUS_PILL = ITEMS.register("soul_focus_pill",
            () -> new PillItem(new Item.Properties()
                    .food(ModFoodProperties.CULT_PILL)
                    .component(ModDataComponents.PILL_EFFECTS, List.of(ModPillEffects.SOUL_EFFECT.getId().toString()))
                    , 200, false));





    // ── Poison Pills ──────────────────────────────────────────────
    public static final DeferredItem<PillItem> QI_DEVOURING_PARASITE_PILL = ITEMS.register("qi_devouring_parasite_pill",
            () -> new PillItem(new Item.Properties()
                    .food(ModFoodProperties.POISON_PILL)
                    .component(ModDataComponents.PILL_EFFECTS, List.of(ModPillEffects.PARASITE_PILL_EFFECT.getId().toString()))
                    , 0, true));
    public static final DeferredItem<Item> QI_DEVOURING_POWDER = ITEMS.register("qi_devouring_powder",
            () -> new Item(new Item.Properties()));


    // ── Positive Pills ──────────────────────────────────────────────
    public static final DeferredItem<PillItem> QI_ENHANCED_REGEN_PILL = ITEMS.register("regeneration_pill",
            () -> new PillItem(new Item.Properties()
                    .food(ModFoodProperties.MEDICINAL_PILL)
                    .component(ModDataComponents.PILL_EFFECTS, List.of(ModPillEffects.QI_ENHANCED_REGEN_EFFECT.getId().toString()))
                    , 0, false));

    public static final DeferredItem<PillItem> QI_REPLENISHING_PILL = ITEMS.register("qi_replenishing_pill",
            () -> new PillItem(new Item.Properties()
                    .food(ModFoodProperties.CULT_PILL)
                    .component(ModDataComponents.PILL_EFFECTS, List.of(ModPillEffects.QI_REPLENISHING_EFFECT.getId().toString()))
                    , 100, false));

    // ── Antidote Pills ────────────────────────────────────────────
    public static final DeferredItem<PillItem> ANTIDOTE_PILL_QDP = ITEMS.register("antidote_qdp_pill",
            () -> new PillItem(new Item.Properties()
                    .food(ModFoodProperties.ANTIDOTE_PILL)
                    .component(ModDataComponents.PILL_EFFECTS, List.of(ModPillEffects.ANTIDOTE_PILL_EFFECT.getId().toString()))
                    , 0, false));


    // ── Physique Changing Pills ────────────────────────────────────────────
    public static final DeferredItem<PillItem> MARROW_CLEANSE_PILL = ITEMS.register("marrow_cleanse_pill",
            () -> new PillItem(new Item.Properties()
                    .food(ModFoodProperties.MARROW_PILL)
                    .component(ModDataComponents.PILL_EFFECTS, List.of(ModPillEffects.MARROW_CLEANSE_PILL_EFFECT.getId().toString()))
                    , 0, false));
    public static final DeferredItem<PillItem> CRIMSON_LOTUS_BONE_PILL =
            ITEMS.register("crimson_lotus_bone_pill",
                    () -> new PillItem(new Item.Properties()
                            .food(ModFoodProperties.RITUAL_PILL)
                            .component(ModDataComponents.PILL_EFFECTS, List.of(ModPillEffects.CRIMSON_LOTUS_BONE_EFFECT.getId().toString()))
                            , 0, false));


    //Phys Stuff
    //Todo
    //Change it into a Blood Essence texture and also make it so blood essence drop then you can combine blood essences to make the purity 100% and when its 100% only then can you use it.
    public static final DeferredItem<Item> PHYSIQUE_ESSENCE = ITEMS.register("physique_essence",
            () -> new PhysiqueTransferItem(new Item.Properties().stacksTo(1)));

    public static final DeferredItem<TechniqueTransferItem> TECHNIQUE_MANUAL = ITEMS.register("technique_manual",
            () -> new TechniqueTransferItem(new Item.Properties().stacksTo(1)));

    public static final DeferredItem<Item> TECHNIQUE_PAGE = ITEMS.register("technique_page",
            () -> new TechniquePageItem(new Item.Properties().stacksTo(16)));

    public static final DeferredItem<Item> TECHNIQUE_BINDER = ITEMS.register("technique_binder",
            () -> new TechniqueBinderItem(new Item.Properties().stacksTo(1)));



    // ── Herbs ─────────────────────────────────────────────────────────────────
    public static final DeferredItem<Item> GOLDEN_SUN_LEAF = ITEMS.register("golden_sun_leaf",
            () -> new HerbItem(new Item.Properties().food(ModFoodProperties.GOLDEN_SUN_LEAF)));
    public static final DeferredItem<Item> JADE_BAMBOO_OF_SERENITY = ITEMS.register("jade_bamboo_of_serenity",
            () -> new HerbItem(new Item.Properties().food(ModFoodProperties.JADE_BAMBOO_OF_SERENITY)));

    public static final DeferredItem<Item> JADE_DEW_GRASS = ITEMS.register("jade_dew_grass",
            () -> new HerbItem(new Item.Properties().food(ModFoodProperties.JADE_DEW_GRASS)));
    public static final DeferredItem<Item> JADE_DEW_GRASS_SEEDS = ITEMS.register("jade_dew_grass_seeds",
            () -> new ItemNameBlockItem(ModBlocks.JADE_DEW_GRASS_CROP.get(), new Item.Properties())); // safe: inside lambda, resolves at registration time

    public static final DeferredItem<Item> IRONWOOD_SPROUT = ITEMS.register("ironwood_sprout",
            () -> new HerbItem(new Item.Properties().food(ModFoodProperties.IRONWOOD_SPROUT)));

    public static final DeferredItem<Item> WHITE_JADE_ORCHID = ITEMS.register("white_jade_orchid",
            () -> new HerbBlockItem(() -> ModBlocks.WHITE_JADE_ORCHID_CROP.get(),
                    new Item.Properties().food(ModFoodProperties.WHITE_JADE_ORCHID)));

    public static final DeferredItem<Item> HUNDRED_YEAR_GINSENG = ITEMS.register("hundred_year_ginseng",
            () -> new HerbBlockItem(() -> ModBlocks.HUNDRED_YEAR_GINSENG_CROP.get(),
                    new Item.Properties().food(ModFoodProperties.HUNDRED_YEAR_GINSENG)));

    public static final DeferredItem<Item> HUNDRED_YEAR_SNOW_GINSENG = ITEMS.register("hundred_year_snow_ginseng",
            () -> new HerbBlockItem(() -> ModBlocks.HUNDRED_YEAR_SNOW_GINSENG_CROP.get(),
                    new Item.Properties().food(ModFoodProperties.HUNDRED_YEAR_SNOW_GINSENG),
                    (stack, level, entity) -> {
                        if (entity instanceof net.minecraft.world.entity.player.Player player) {
                            player.setTicksFrozen(300);
                            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 2));
                            player.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 100, 2));
                        }
                    }));

    public static final DeferredItem<Item> HUNDRED_YEAR_FIRE_GINSENG = ITEMS.register("hundred_year_fire_ginseng",
            () -> new HerbBlockItem(() -> ModBlocks.HUNDRED_YEAR_FIRE_GINSENG_CROP.get(),
                    new Item.Properties().food(ModFoodProperties.HUNDRED_YEAR_FIRE_GINSENG),
                    (stack, level, entity) -> {
                        if (entity instanceof net.minecraft.world.entity.player.Player player) {
                            player.setRemainingFireTicks(300);
                            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                                    SoundEvents.BLAZE_SHOOT, SoundSource.PLAYERS, 0.8F, 1.0F);
                            for (int i = 0; i < 15; i++) {
                                double x = player.getX() + (level.random.nextDouble() - 0.5) * 3;
                                double y = player.getY() + level.random.nextDouble() * 2;
                                double z = player.getZ() + (level.random.nextDouble() - 0.5) * 3;
                                level.addParticle(ParticleTypes.FLAME, x, y, z, 0, 0.05, 0);
                            }
                        }
                    }));



    public static final DeferredItem<Item> PEACH = ITEMS.register("peach",
            () -> new Item(new Item.Properties().food(ModFoodProperties.PEACH)));

    public static final DeferredItem<Item> HERB_POUCH = ITEMS.register("herb_pouch",
            () -> new HerbPouchItem(new Item.Properties().stacksTo(1)));


    //MobEggs
    public static final DeferredItem<Item> RAT_SPAWN_EGG = ITEMS.register("rat_spawn_egg",
            () -> new DeferredSpawnEggItem(ModEntities.RAT, 0x4F2242, 0x703240,
                    new Item.Properties()));


    // ── Scholarly Soul Technique Chapters ──────────────────────────────────────────────
    public static final DeferredItem<ScholarlySoulChapterItem> SCHOLARLY_SOUL_RECTIFICATION_OF_NAMES =
            registerScholarlySoulChapter(
                    "rectification_of_names",
                    ScholarlySoulTechniqueData.RECTIFICATION_OF_NAMES
            );

    public static final DeferredItem<ScholarlySoulChapterItem> SCHOLARLY_SOUL_GREAT_LEARNING =
            registerScholarlySoulChapter(
                    "great_learning",
                    ScholarlySoulTechniqueData.GREAT_LEARNING
            );

    public static final DeferredItem<ScholarlySoulChapterItem> SCHOLARLY_SOUL_THOUSAND_COMMENTARIES =
            registerScholarlySoulChapter(
                    "thousand_commentaries",
                    ScholarlySoulTechniqueData.THOUSAND_COMMENTARIES
            );

    public static final DeferredItem<ScholarlySoulChapterItem> SCHOLARLY_SOUL_SAGE_MANDATE =
            registerScholarlySoulChapter(
                    "sage_mandate",
                    ScholarlySoulTechniqueData.SAGE_MANDATE
            );

    private static DeferredItem<ScholarlySoulChapterItem> registerScholarlySoulChapter(
            String name,
            ResourceLocation chapter
    ) {
        return ITEMS.register("scholarly_soul_" + name,
                () -> new ScholarlySoulChapterItem(
                        new Item.Properties(),
                        chapter
                ));
    }




    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
