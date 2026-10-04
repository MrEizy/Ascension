package net.zic.ascension.worldgen;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.worldgen.Pools;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.heightproviders.ConstantHeight;
import net.minecraft.world.level.levelgen.heightproviders.UniformHeight;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.TerrainAdjustment;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadType;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.structures.JigsawStructure;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;
import net.zic.ascension.AscensionCraft;

import java.util.List;
import java.util.Map;

public class AscStructures {

    private static final ResourceKey<StructureProcessorList> EMPTY_PROCESSORS =
            ResourceKey.create(Registries.PROCESSOR_LIST, Identifier.withDefaultNamespace("empty"));


    public static final ResourceKey<Structure> SPIRITUAL_CAVERN = structureKey("spiritual_cavern");
    public static final ResourceKey<Structure> SWORD_TOMB1 = structureKey("sword_tomb1");
    public static final ResourceKey<Structure> SWORD_TOMB2 = structureKey("sword_tomb2");
    public static final ResourceKey<Structure> SWORD_TOMB3 = structureKey("sword_tomb3");
    public static final ResourceKey<Structure> FLOATING_IRONWOOD_ISLAND = structureKey("floating_ironwood_island");


    public static final ResourceKey<StructureSet> SPIRITUAL_CAVERN_SET = structureSetKey("spiritual_cavern_set");
    public static final ResourceKey<StructureSet> SWORD_TOMB1_SET = structureSetKey("sword_tomb1_set");
    public static final ResourceKey<StructureSet> SWORD_TOMB2_SET = structureSetKey("sword_tomb2_set");
    public static final ResourceKey<StructureSet> SWORD_TOMB3_SET = structureSetKey("sword_tomb3_set");
    public static final ResourceKey<StructureSet> FLOATING_IRONWOOD_ISLAND_SET = structureSetKey("floating_ironwood_island_set");


    public static final ResourceKey<StructureTemplatePool> SPIRITUAL_CAVERN_POOL = poolKey("spiritual_caverns/spiritual_cavern_pool");
    public static final ResourceKey<StructureTemplatePool> SWORD_TOMB1_POOL = poolKey("sword_tombs/sword_tomb1_pool");
    public static final ResourceKey<StructureTemplatePool> SWORD_TOMB2_POOL = poolKey("sword_tombs/sword_tomb2_pool");
    public static final ResourceKey<StructureTemplatePool> SWORD_TOMB3_POOL = poolKey("sword_tombs/sword_tomb3_pool");
    public static final ResourceKey<StructureTemplatePool> FLOATING_IRONWOOD_ISLAND_POOL = poolKey("floating_ironwood_island/floating_ironwood_island_pool");

    public static void bootstrapStructures(BootstrapContext<Structure> context) {
        HolderGetter<StructureTemplatePool> pools = context.lookup(Registries.TEMPLATE_POOL);
        HolderGetter<Biome> biomes = context.lookup(Registries.BIOME);


        context.register(SWORD_TOMB1, surfaceTomb(pools.getOrThrow(SWORD_TOMB1_POOL), HolderSet.direct(biomes::getOrThrow,
                Biomes.SAVANNA, Biomes.SAVANNA_PLATEAU, Biomes.WINDSWEPT_SAVANNA,
                Biomes.BADLANDS, Biomes.ERODED_BADLANDS, Biomes.WOODED_BADLANDS)));

        context.register(SWORD_TOMB2, surfaceTomb(pools.getOrThrow(SWORD_TOMB2_POOL), HolderSet.direct(biomes::getOrThrow,
                Biomes.PLAINS, Biomes.SNOWY_PLAINS, Biomes.SUNFLOWER_PLAINS, Biomes.CHERRY_GROVE,
                Biomes.GROVE, Biomes.FLOWER_FOREST, Biomes.OLD_GROWTH_PINE_TAIGA)));

        context.register(SWORD_TOMB3, surfaceTomb(pools.getOrThrow(SWORD_TOMB3_POOL), HolderSet.direct(biomes::getOrThrow,
                Biomes.WINDSWEPT_FOREST, Biomes.WINDSWEPT_HILLS, Biomes.WINDSWEPT_GRAVELLY_HILLS)));


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

        context.register(FLOATING_IRONWOOD_ISLAND, new JigsawStructure(
                new Structure.StructureSettings(
                        HolderSet.direct(biomes::getOrThrow,
                                Biomes.PLAINS, Biomes.SUNFLOWER_PLAINS, Biomes.FOREST, Biomes.BIRCH_FOREST,
                                Biomes.OLD_GROWTH_BIRCH_FOREST, Biomes.DARK_FOREST, Biomes.FLOWER_FOREST,
                                Biomes.TAIGA, Biomes.OLD_GROWTH_PINE_TAIGA, Biomes.OLD_GROWTH_SPRUCE_TAIGA,
                                Biomes.WINDSWEPT_FOREST, Biomes.CHERRY_GROVE, Biomes.GROVE, Biomes.MEADOW),
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

        context.register(SPIRITUAL_CAVERN_SET, new StructureSet(
                structures.getOrThrow(SPIRITUAL_CAVERN),
                new RandomSpreadStructurePlacement(20, 12, RandomSpreadType.LINEAR, 1234567890)
        ));
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
        context.register(FLOATING_IRONWOOD_ISLAND_SET, new StructureSet(
                structures.getOrThrow(FLOATING_IRONWOOD_ISLAND),
                new RandomSpreadStructurePlacement(40, 16, RandomSpreadType.LINEAR, 12244329)
        ));
    }

    public static void bootstrapPools(BootstrapContext<StructureTemplatePool> context) {
        HolderGetter<StructureTemplatePool> pools = context.lookup(Registries.TEMPLATE_POOL);
        HolderGetter<StructureProcessorList> processors = context.lookup(Registries.PROCESSOR_LIST);
        var emptyPool = pools.getOrThrow(Pools.EMPTY);
        var emptyProcessors = processors.getOrThrow(EMPTY_PROCESSORS);

        context.register(SPIRITUAL_CAVERN_POOL, singleTemplatePool(emptyPool, "spiritual_cavern", emptyProcessors));
        context.register(SWORD_TOMB1_POOL, singleTemplatePool(emptyPool, "sword_tomb1", emptyProcessors));
        context.register(SWORD_TOMB2_POOL, singleTemplatePool(emptyPool, "sword_tomb2", emptyProcessors));
        context.register(SWORD_TOMB3_POOL, singleTemplatePool(emptyPool, "sword_tomb3", emptyProcessors));
        context.register(FLOATING_IRONWOOD_ISLAND_POOL, singleTemplatePool(emptyPool, "floating_ironwood_island", emptyProcessors));
    }

    private static JigsawStructure surfaceTomb(Holder<StructureTemplatePool> pool, HolderSet<Biome> biomes) {
        return new JigsawStructure(
                new Structure.StructureSettings(
                        biomes,
                        Map.of(),
                        GenerationStep.Decoration.SURFACE_STRUCTURES,
                        TerrainAdjustment.BEARD_THIN
                ),
                pool,
                1,
                ConstantHeight.of(VerticalAnchor.absolute(1)),
                false,
                Heightmap.Types.WORLD_SURFACE_WG
        );
    }

    private static StructureTemplatePool singleTemplatePool(Holder<StructureTemplatePool> fallback,
                                                            String template,
                                                            Holder<StructureProcessorList> processors) {
        return new StructureTemplatePool(
                fallback,
                List.of(Pair.of(StructurePoolElement.single(AscensionCraft.prefix(template).toString(), processors), 1)),
                StructureTemplatePool.Projection.RIGID
        );
    }

    private static ResourceKey<Structure> structureKey(String name) {
        return ResourceKey.create(Registries.STRUCTURE, AscensionCraft.prefix(name));
    }

    private static ResourceKey<StructureSet> structureSetKey(String name) {
        return ResourceKey.create(Registries.STRUCTURE_SET, AscensionCraft.prefix(name));
    }

    private static ResourceKey<StructureTemplatePool> poolKey(String name) {
        return ResourceKey.create(Registries.TEMPLATE_POOL, AscensionCraft.prefix(name));
    }
}
