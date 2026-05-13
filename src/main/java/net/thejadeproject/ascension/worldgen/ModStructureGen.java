package net.thejadeproject.ascension.worldgen;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.heightproviders.ConstantHeight;
import net.minecraft.world.level.levelgen.heightproviders.UniformHeight;
import net.minecraft.world.level.levelgen.structure.*;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadType;
import net.minecraft.world.level.levelgen.structure.pools.*;
import net.minecraft.world.level.levelgen.structure.pools.alias.PoolAliasBindings;
import net.minecraft.world.level.levelgen.structure.structures.JigsawStructure;
import net.minecraft.world.level.levelgen.structure.templatesystem.LiquidSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;
import net.thejadeproject.ascension.AscensionCraft;
import net.neoforged.fml.ModList;
import net.minecraft.tags.TagKey;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ModStructureGen {

    // Structure Keys
    public static final ResourceKey<Structure> CRIMSON_LOTUS_STRUCTURE = createStructureKey("crimson_lotus_structure");
    public static final ResourceKey<Structure> SPIRITUAL_CAVERN = createStructureKey("spiritual_cavern");
    public static final ResourceKey<Structure> SWORD_TOMB1 = createStructureKey("sword_tomb1");
    public static final ResourceKey<Structure> SWORD_TOMB2 = createStructureKey("sword_tomb2");
    public static final ResourceKey<Structure> SWORD_TOMB3 = createStructureKey("sword_tomb3");
    public static final ResourceKey<Structure> FLOATING_IRONWOOD_ISLAND = createStructureKey("floating_ironwood_island");

    // Structure Set Keys
    public static final ResourceKey<StructureSet> CRIMSON_LOTUS_STRUCTURE_SET = createStructureSetKey("crimson_lotus_structure_set");
    public static final ResourceKey<StructureSet> SPIRITUAL_CAVERN_SET = createStructureSetKey("spiritual_cavern_set");
    public static final ResourceKey<StructureSet> SWORD_TOMB1_SET = createStructureSetKey("sword_tomb1_set");
    public static final ResourceKey<StructureSet> SWORD_TOMB2_SET = createStructureSetKey("sword_tomb2_set");
    public static final ResourceKey<StructureSet> SWORD_TOMB3_SET = createStructureSetKey("sword_tomb3_set");
    public static final ResourceKey<StructureSet> FLOATING_IRONWOOD_ISLAND_SET = createStructureSetKey("floating_ironwood_island_set");

    // Template Pool Keys (must match the paths in your JSONs)
    public static final ResourceKey<StructureTemplatePool> CRIMSON_LOTUS_POOL = createPoolKey("crimson_lotus_structures/crimson_lotus_structure_pool");
    public static final ResourceKey<StructureTemplatePool> SPIRITUAL_CAVERN_POOL = createPoolKey("spiritual_caverns/spiritual_cavern_pool");
    public static final ResourceKey<StructureTemplatePool> SWORD_TOMB1_POOL = createPoolKey("sword_tombs/sword_tomb1_pool");
    public static final ResourceKey<StructureTemplatePool> SWORD_TOMB2_POOL = createPoolKey("sword_tombs/sword_tomb2_pool");
    public static final ResourceKey<StructureTemplatePool> SWORD_TOMB3_POOL = createPoolKey("sword_tombs/sword_tomb3_pool");
    public static final ResourceKey<StructureTemplatePool> FLOATING_IRONWOOD_ISLAND_POOL = createPoolKey("floating_ironwood_island/floating_ironwood_island_pool");

    // Empty processor reference
    private static final ResourceKey<StructureProcessorList> EMPTY_PROCESSORS = ResourceKey.create(
            Registries.PROCESSOR_LIST,
            ResourceLocation.withDefaultNamespace("empty")
    );

    // Mod compatibility flags - checked once during bootstrap
    private static final boolean BOP_LOADED = ModList.get().isLoaded("biomesoplenty");
    private static final boolean BWG_LOADED = ModList.get().isLoaded("biomeswevegone");
    private static final boolean ATMOSPHERIC_LOADED = ModList.get().isLoaded("atmospheric");
    private static final boolean ENVIRONMENTAL_LOADED = ModList.get().isLoaded("environmental");
    private static final boolean MORE_ORN_PLANTS = ModList.get().isLoaded("more_orn_plants");

    private static ResourceKey<Structure> createStructureKey(String name) {
        return ResourceKey.create(Registries.STRUCTURE, ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, name));
    }

    private static ResourceKey<StructureSet> createStructureSetKey(String name) {
        return ResourceKey.create(Registries.STRUCTURE_SET, ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, name));
    }

    private static ResourceKey<StructureTemplatePool> createPoolKey(String name) {
        return ResourceKey.create(Registries.TEMPLATE_POOL, ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, name));
    }

    /**
     * Helper to build a HolderSet from a list of biome holders, with optional modded biomes.
     * If a modded biome isn't present, it is silently skipped.
     */
    private static HolderSet<Biome> buildBiomeSet(HolderGetter<Biome> biomes,
                                                  List<ResourceKey<Biome>> vanillaBiomes,
                                                  List<ModdedBiomeEntry> moddedBiomes) {
        List<Holder<Biome>> holders = new ArrayList<>();

        // Add vanilla biomes (always present)
        for (ResourceKey<Biome> key : vanillaBiomes) {
            biomes.get(key).ifPresent(holders::add);
        }

        // Add modded biomes only if their mod is loaded
        for (ModdedBiomeEntry entry : moddedBiomes) {
            if (entry.modLoaded()) {
                biomes.get(entry.biomeKey()).ifPresent(holders::add);
            }
        }

        return HolderSet.direct(holders.toArray(new Holder[0]));
    }

    /**
     * Simple record for modded biome entries with modid check
     */
    private record ModdedBiomeEntry(ResourceKey<Biome> biomeKey, String modid) {
        boolean modLoaded() {
            return ModList.get().isLoaded(modid);
        }
    }

    public static void bootstrapStructures(BootstrapContext<Structure> context) {
        HolderGetter<StructureTemplatePool> pools = context.lookup(Registries.TEMPLATE_POOL);
        HolderGetter<Biome> biomes = context.lookup(Registries.BIOME);

        // ============================================
        // SWORD TOMB 1 - Savanna & Badlands
        // ============================================
        List<ResourceKey<Biome>> swordTomb1Vanilla = List.of(
                Biomes.SAVANNA,
                Biomes.SAVANNA_PLATEAU,
                Biomes.WINDSWEPT_SAVANNA,
                Biomes.BADLANDS,
                Biomes.ERODED_BADLANDS,
                Biomes.WOODED_BADLANDS
        );
        List<ModdedBiomeEntry> swordTomb1Modded = List.of(
                new ModdedBiomeEntry(ResourceKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath("biomesoplenty", "mediterranean_forest")), "biomesoplenty"),
                new ModdedBiomeEntry(ResourceKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath("biomesoplenty", "scrubland")), "biomesoplenty"),
                new ModdedBiomeEntry(ResourceKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath("biomesoplenty", "lush_desert")), "biomesoplenty"),
                new ModdedBiomeEntry(ResourceKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath("biomesoplenty", "rocky_badlands")), "biomesoplenty"),
                new ModdedBiomeEntry(ResourceKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath("biomesoplenty", "wooded_badlands")), "biomesoplenty"),
                new ModdedBiomeEntry(ResourceKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath("biomesoplenty", "wasteland")), "biomesoplenty"),
                new ModdedBiomeEntry(ResourceKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath("biomeswevegone", "atacama_outback")), "biomeswevegone"),
                new ModdedBiomeEntry(ResourceKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath("biomeswevegone", "baobab_savanna")), "biomeswevegone"),
                new ModdedBiomeEntry(ResourceKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath("biomeswevegone", "firecracker_chaparral")), "biomeswevegone"),
                new ModdedBiomeEntry(ResourceKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath("biomeswevegone", "mojave_desert")), "biomeswevegone"),
                new ModdedBiomeEntry(ResourceKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath("biomeswevegone", "red_rock_valley")), "biomeswevegone"),
                new ModdedBiomeEntry(ResourceKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath("biomeswevegone", "rugged_badlands")), "biomeswevegone"),
                new ModdedBiomeEntry(ResourceKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath("biomeswevegone", "sierra_badlands")), "biomeswevegone"),
                new ModdedBiomeEntry(ResourceKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath("biomeswevegone", "windswept_desert")), "biomeswevegone"),
                new ModdedBiomeEntry(ResourceKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath("atmospheric", "dunes")), "atmospheric"),
                new ModdedBiomeEntry(ResourceKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath("atmospheric", "flourishing_dunes")), "atmospheric"),
                new ModdedBiomeEntry(ResourceKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath("atmospheric", "rocky_dunes")), "atmospheric"),
                new ModdedBiomeEntry(ResourceKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath("atmospheric", "petrified_dunes")), "atmospheric")
        );

        context.register(SWORD_TOMB1, new JigsawStructure(
                new Structure.StructureSettings(
                        buildBiomeSet(biomes, swordTomb1Vanilla, swordTomb1Modded),
                        Map.of(),
                        GenerationStep.Decoration.SURFACE_STRUCTURES,
                        TerrainAdjustment.BEARD_THIN
                ),
                pools.getOrThrow(SWORD_TOMB1_POOL),
                1,
                ConstantHeight.of(VerticalAnchor.absolute(1)),
                false,
                Heightmap.Types.WORLD_SURFACE_WG
        ));

        // ============================================
        // SWORD TOMB 2 - Plains & Forests
        // ============================================
        List<ResourceKey<Biome>> swordTomb2Vanilla = List.of(
                Biomes.PLAINS,
                Biomes.SNOWY_PLAINS,
                Biomes.SUNFLOWER_PLAINS,
                Biomes.CHERRY_GROVE,
                Biomes.GROVE,
                Biomes.FLOWER_FOREST,
                Biomes.OLD_GROWTH_PINE_TAIGA
        );
        List<ModdedBiomeEntry> swordTomb2Modded = List.of(
                new ModdedBiomeEntry(ResourceKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath("biomesoplenty", "boreal_forest")), "biomesoplenty"),
                new ModdedBiomeEntry(ResourceKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath("biomesoplenty", "cherry_blossom_grove")), "biomesoplenty"),
                new ModdedBiomeEntry(ResourceKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath("biomesoplenty", "coniferous_forest")), "biomesoplenty"),
                new ModdedBiomeEntry(ResourceKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath("biomesoplenty", "field")), "biomesoplenty"),
                new ModdedBiomeEntry(ResourceKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath("biomesoplenty", "grassland")), "biomesoplenty"),
                new ModdedBiomeEntry(ResourceKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath("biomesoplenty", "grove")), "biomesoplenty"),
                new ModdedBiomeEntry(ResourceKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath("biomesoplenty", "highland")), "biomesoplenty"),
                new ModdedBiomeEntry(ResourceKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath("biomesoplenty", "meadow")), "biomesoplenty"),
                new ModdedBiomeEntry(ResourceKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath("biomesoplenty", "orchard")), "biomesoplenty"),
                new ModdedBiomeEntry(ResourceKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath("biomesoplenty", "prairie")), "biomesoplenty"),
                new ModdedBiomeEntry(ResourceKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath("biomesoplenty", "seasonal_forest")), "biomesoplenty"),
                new ModdedBiomeEntry(ResourceKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath("biomesoplenty", "woodland")), "biomesoplenty"),
                new ModdedBiomeEntry(ResourceKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath("biomeswevegone", "aspen_boreal")), "biomeswevegone"),
                new ModdedBiomeEntry(ResourceKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath("biomeswevegone", "autumnal_valley")), "biomeswevegone"),
                new ModdedBiomeEntry(ResourceKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath("biomeswevegone", "borealis_grove")), "biomeswevegone"),
                new ModdedBiomeEntry(ResourceKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath("biomeswevegone", "cherry_blossom_forest")), "biomeswevegone"),
                new ModdedBiomeEntry(ResourceKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath("biomeswevegone", "cika_woods")), "biomeswevegone"),
                new ModdedBiomeEntry(ResourceKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath("biomeswevegone", "coniferous_forest")), "biomeswevegone"),
                new ModdedBiomeEntry(ResourceKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath("biomeswevegone", "dacite_ridges")), "biomeswevegone"),
                new ModdedBiomeEntry(ResourceKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath("biomeswevegone", "ebony_woods")), "biomeswevegone"),
                new ModdedBiomeEntry(ResourceKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath("biomeswevegone", "enchanted_tangle")), "biomeswevegone"),
                new ModdedBiomeEntry(ResourceKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath("biomeswevegone", "forgotten_forest")), "biomeswevegone"),
                new ModdedBiomeEntry(ResourceKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath("biomeswevegone", "maple_taiga")), "biomeswevegone"),
                new ModdedBiomeEntry(ResourceKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath("biomeswevegone", "orchard")), "biomeswevegone"),
                new ModdedBiomeEntry(ResourceKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath("biomeswevegone", "red_oak_forest")), "biomeswevegone"),
                new ModdedBiomeEntry(ResourceKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath("biomeswevegone", "rose_fields")), "biomeswevegone"),
                new ModdedBiomeEntry(ResourceKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath("biomeswevegone", "zelkova_forest")), "biomeswevegone"),
                new ModdedBiomeEntry(ResourceKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath("atmospheric", "rainforest")), "atmospheric"),
                new ModdedBiomeEntry(ResourceKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath("atmospheric", "sparse_rainforest")), "atmospheric"),
                new ModdedBiomeEntry(ResourceKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath("environmental", "blossom_valleys")), "environmental"),
                new ModdedBiomeEntry(ResourceKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath("environmental", "marsh")), "environmental"),
                new ModdedBiomeEntry(ResourceKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath("more_orn_plants", "fragrant_snow_sea")), "more_orn_plants"),
                new ModdedBiomeEntry(ResourceKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath("more_orn_plants", "sweetgum_woods")), "more_orn_plants"),
                new ModdedBiomeEntry(ResourceKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath("more_orn_plants", "ginkgo_forest")), "more_orn_plants"),
                new ModdedBiomeEntry(ResourceKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath("more_orn_plants", "flowers_grove")), "more_orn_plants")
        );

        context.register(SWORD_TOMB2, new JigsawStructure(
                new Structure.StructureSettings(
                        buildBiomeSet(biomes, swordTomb2Vanilla, swordTomb2Modded),
                        Map.of(),
                        GenerationStep.Decoration.SURFACE_STRUCTURES,
                        TerrainAdjustment.BEARD_THIN
                ),
                pools.getOrThrow(SWORD_TOMB2_POOL),
                1,
                ConstantHeight.of(VerticalAnchor.absolute(1)),
                false,
                Heightmap.Types.WORLD_SURFACE_WG
        ));

        // ============================================
        // SWORD TOMB 3 - Windswept
        // ============================================
        List<ResourceKey<Biome>> swordTomb3Vanilla = List.of(
                Biomes.WINDSWEPT_FOREST,
                Biomes.WINDSWEPT_HILLS,
                Biomes.WINDSWEPT_GRAVELLY_HILLS
        );
        List<ModdedBiomeEntry> swordTomb3Modded = List.of(
                new ModdedBiomeEntry(ResourceKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath("biomesoplenty", "crag")), "biomesoplenty"),
                new ModdedBiomeEntry(ResourceKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath("biomesoplenty", "highland")), "biomesoplenty"),
                new ModdedBiomeEntry(ResourceKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath("biomeswevegone", "canadian_shield")), "biomeswevegone"),
                new ModdedBiomeEntry(ResourceKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath("biomeswevegone", "howling_peaks")), "biomeswevegone"),
                new ModdedBiomeEntry(ResourceKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath("biomeswevegone", "shattered_glacier")), "biomeswevegone")
        );

        context.register(SWORD_TOMB3, new JigsawStructure(
                new Structure.StructureSettings(
                        buildBiomeSet(biomes, swordTomb3Vanilla, swordTomb3Modded),
                        Map.of(),
                        GenerationStep.Decoration.SURFACE_STRUCTURES,
                        TerrainAdjustment.BEARD_THIN
                ),
                pools.getOrThrow(SWORD_TOMB3_POOL),
                1,
                ConstantHeight.of(VerticalAnchor.absolute(1)),
                false,
                Heightmap.Types.WORLD_SURFACE_WG
        ));

        // ============================================
        // CRIMSON LOTUS - Mountains
        // ============================================
        // For tag-based biomes (like IS_MOUNTAIN), we can't easily add modded biomes to the tag in code.
        // Instead, we use the vanilla tag and rely on datapack biome tags for modded biomes.
        // If you want full modded mountain support in code, you'd need to build a custom HolderSet.
        context.register(CRIMSON_LOTUS_STRUCTURE, new JigsawStructure(
                new Structure.StructureSettings(
                        biomes.getOrThrow(BiomeTags.IS_MOUNTAIN),
                        Map.of(),
                        GenerationStep.Decoration.SURFACE_STRUCTURES,
                        TerrainAdjustment.BEARD_THIN
                ),
                pools.getOrThrow(CRIMSON_LOTUS_POOL),
                1,
                ConstantHeight.of(VerticalAnchor.absolute(1)),
                false,
                Heightmap.Types.WORLD_SURFACE_WG
        ));

        // ============================================
        // SPIRITUAL CAVERN - Mineshaft biomes
        // ============================================
        context.register(SPIRITUAL_CAVERN, new JigsawStructure(
                new Structure.StructureSettings(
                        biomes.getOrThrow(BiomeTags.HAS_MINESHAFT),
                        Map.of(),
                        GenerationStep.Decoration.UNDERGROUND_STRUCTURES,
                        TerrainAdjustment.NONE
                ),
                pools.getOrThrow(SPIRITUAL_CAVERN_POOL),
                1,
                UniformHeight.of(VerticalAnchor.absolute(-58), VerticalAnchor.absolute(0)),
                false
        ));

        // ============================================
        // FLOATING IRONWOOD ISLAND - Forests & Plains
        //  ============================================
        List<ResourceKey<Biome>> ironwoodVanilla = List.of(
                Biomes.PLAINS,
                Biomes.SUNFLOWER_PLAINS,
                Biomes.FOREST,
                Biomes.BIRCH_FOREST,
                Biomes.OLD_GROWTH_BIRCH_FOREST,
                Biomes.DARK_FOREST,
                Biomes.FLOWER_FOREST,
                Biomes.TAIGA,
                Biomes.OLD_GROWTH_PINE_TAIGA,
                Biomes.OLD_GROWTH_SPRUCE_TAIGA,
                Biomes.WINDSWEPT_FOREST,
                Biomes.CHERRY_GROVE,
                Biomes.GROVE,
                Biomes.MEADOW
        );
        List<ModdedBiomeEntry> ironwoodModded = List.of(
                new ModdedBiomeEntry(ResourceKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath("biomesoplenty", "boreal_forest")), "biomesoplenty"),
                new ModdedBiomeEntry(ResourceKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath("biomesoplenty", "cherry_blossom_grove")), "biomesoplenty"),
                new ModdedBiomeEntry(ResourceKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath("biomesoplenty", "coniferous_forest")), "biomesoplenty"),
                new ModdedBiomeEntry(ResourceKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath("biomesoplenty", "field")), "biomesoplenty"),
                new ModdedBiomeEntry(ResourceKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath("biomesoplenty", "grassland")), "biomesoplenty"),
                new ModdedBiomeEntry(ResourceKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath("biomesoplenty", "grove")), "biomesoplenty"),
                new ModdedBiomeEntry(ResourceKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath("biomesoplenty", "highland")), "biomesoplenty"),
                new ModdedBiomeEntry(ResourceKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath("biomesoplenty", "meadow")), "biomesoplenty"),
                new ModdedBiomeEntry(ResourceKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath("biomesoplenty", "orchard")), "biomesoplenty"),
                new ModdedBiomeEntry(ResourceKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath("biomesoplenty", "prairie")), "biomesoplenty"),
                new ModdedBiomeEntry(ResourceKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath("biomesoplenty", "seasonal_forest")), "biomesoplenty"),
                new ModdedBiomeEntry(ResourceKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath("biomesoplenty", "woodland")), "biomesoplenty"),
                new ModdedBiomeEntry(ResourceKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath("biomeswevegone", "aspen_boreal")), "biomeswevegone"),
                new ModdedBiomeEntry(ResourceKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath("biomeswevegone", "borealis_grove")), "biomeswevegone"),
                new ModdedBiomeEntry(ResourceKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath("biomeswevegone", "cherry_blossom_forest")), "biomeswevegone"),
                new ModdedBiomeEntry(ResourceKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath("biomeswevegone", "cika_woods")), "biomeswevegone"),
                new ModdedBiomeEntry(ResourceKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath("biomeswevegone", "coniferous_forest")), "biomeswevegone"),
                new ModdedBiomeEntry(ResourceKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath("biomeswevegone", "ebony_woods")), "biomeswevegone"),
                new ModdedBiomeEntry(ResourceKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath("biomeswevegone", "enchanted_tangle")), "biomeswevegone"),
                new ModdedBiomeEntry(ResourceKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath("biomeswevegone", "forgotten_forest")), "biomeswevegone"),
                new ModdedBiomeEntry(ResourceKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath("biomeswevegone", "maple_taiga")), "biomeswevegone"),
                new ModdedBiomeEntry(ResourceKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath("biomeswevegone", "orchard")), "biomeswevegone"),
                new ModdedBiomeEntry(ResourceKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath("biomeswevegone", "red_oak_forest")), "biomeswevegone"),
                new ModdedBiomeEntry(ResourceKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath("biomeswevegone", "zelkova_forest")), "biomeswevegone"),
                new ModdedBiomeEntry(ResourceKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath("atmospheric", "rainforest")), "atmospheric"),
                new ModdedBiomeEntry(ResourceKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath("atmospheric", "sparse_rainforest")), "atmospheric"),
                new ModdedBiomeEntry(ResourceKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath("environmental", "blossom_valleys")), "environmental"),
                new ModdedBiomeEntry(ResourceKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath("environmental", "marsh")), "environmental"),
                new ModdedBiomeEntry(ResourceKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath("more_orn_plants", "fragrant_snow_sea")), "more_orn_plants"),
                new ModdedBiomeEntry(ResourceKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath("more_orn_plants", "sweetgum_woods")), "more_orn_plants"),
                new ModdedBiomeEntry(ResourceKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath("more_orn_plants", "ginkgo_forest")), "more_orn_plants"),
                new ModdedBiomeEntry(ResourceKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath("more_orn_plants", "flowers_grove")), "more_orn_plants")
        );

        context.register(FLOATING_IRONWOOD_ISLAND, new JigsawStructure(
                new Structure.StructureSettings(
                        buildBiomeSet(biomes, ironwoodVanilla, ironwoodModded),
                        Map.of(),
                        GenerationStep.Decoration.SURFACE_STRUCTURES,
                        TerrainAdjustment.NONE
                ),
                pools.getOrThrow(FLOATING_IRONWOOD_ISLAND_POOL),
                1,
                UniformHeight.of(VerticalAnchor.absolute(90), VerticalAnchor.absolute(250)),
                false
        ));
    }

    public static void bootstrapStructureSets(BootstrapContext<StructureSet> context) {
        HolderGetter<Structure> structures = context.lookup(Registries.STRUCTURE);
        context.register(SWORD_TOMB1_SET, new StructureSet(
                structures.getOrThrow(SWORD_TOMB1),
                new RandomSpreadStructurePlacement(20, 12, RandomSpreadType.LINEAR, 12244326)
        ));
        context.register(SWORD_TOMB2_SET, new StructureSet(
                structures.getOrThrow(SWORD_TOMB2),
                new RandomSpreadStructurePlacement(20, 12, RandomSpreadType.LINEAR, 12244327)
        ));
        context.register(SWORD_TOMB3_SET, new StructureSet(
                structures.getOrThrow(SWORD_TOMB3),
                new RandomSpreadStructurePlacement(20, 12, RandomSpreadType.LINEAR, 12244328)
        ));

        context.register(CRIMSON_LOTUS_STRUCTURE_SET, new StructureSet(
                structures.getOrThrow(CRIMSON_LOTUS_STRUCTURE),
                new RandomSpreadStructurePlacement(12, 6, RandomSpreadType.LINEAR, 12244325)
        ));

        context.register(SPIRITUAL_CAVERN_SET, new StructureSet(
                structures.getOrThrow(SPIRITUAL_CAVERN),
                new RandomSpreadStructurePlacement(20, 12, RandomSpreadType.LINEAR, 1234567890)
        ));

        context.register(FLOATING_IRONWOOD_ISLAND_SET, new StructureSet(
                structures.getOrThrow(FLOATING_IRONWOOD_ISLAND),
                new RandomSpreadStructurePlacement(40, 16, RandomSpreadType.LINEAR, 12244329)
        ));
    }

    public static void bootstrapPools(BootstrapContext<StructureTemplatePool> context) {
        HolderGetter<StructureTemplatePool> pools = context.lookup(Registries.TEMPLATE_POOL);
        HolderGetter<StructureProcessorList> processors = context.lookup(Registries.PROCESSOR_LIST);
        var emptyPool = pools.getOrThrow(ResourceKey.create(Registries.TEMPLATE_POOL, ResourceLocation.withDefaultNamespace("empty")));
        var emptyProcessorList = processors.getOrThrow(EMPTY_PROCESSORS);

        context.register(SWORD_TOMB1_POOL, new StructureTemplatePool(
                emptyPool,
                List.of(
                        Pair.of(
                                StructurePoolElement.single("ascension:sword_tomb1", emptyProcessorList),
                                1
                        )
                ),
                StructureTemplatePool.Projection.RIGID
        ));
        context.register(SWORD_TOMB2_POOL, new StructureTemplatePool(
                emptyPool,
                List.of(
                        Pair.of(
                                StructurePoolElement.single("ascension:sword_tomb2", emptyProcessorList),
                                1
                        )
                ),
                StructureTemplatePool.Projection.RIGID
        ));
        context.register(SWORD_TOMB3_POOL, new StructureTemplatePool(
                emptyPool,
                List.of(
                        Pair.of(
                                StructurePoolElement.single("ascension:sword_tomb3", emptyProcessorList),
                                1
                        )
                ),
                StructureTemplatePool.Projection.RIGID
        ));

        context.register(CRIMSON_LOTUS_POOL, new StructureTemplatePool(
                emptyPool,
                List.of(
                        Pair.of(
                                StructurePoolElement.single("ascension:crimson_lotus_structure", emptyProcessorList),
                                1
                        )
                ),
                StructureTemplatePool.Projection.RIGID
        ));

        context.register(SPIRITUAL_CAVERN_POOL, new StructureTemplatePool(
                emptyPool,
                List.of(
                        Pair.of(
                                StructurePoolElement.single("ascension:spiritual_cavern", emptyProcessorList),
                                1
                        )
                ),
                StructureTemplatePool.Projection.RIGID
        ));

        context.register(FLOATING_IRONWOOD_ISLAND_POOL, new StructureTemplatePool(
                emptyPool,
                List.of(
                        Pair.of(
                                StructurePoolElement.single("ascension:floating_ironwood_island", emptyProcessorList),
                                1
                        )
                ),
                StructureTemplatePool.Projection.RIGID
        ));
    }
}