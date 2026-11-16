package net.zic.ascension.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.StructureTagsProvider;
import net.minecraft.tags.StructureTags;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.common.util.ModTags;
import net.zic.ascension.worldgen.AscStructures;

import java.util.concurrent.CompletableFuture;

/**
 * Generates the mod's structure tags. Registered in {@link AscDataGen}; reads the structures that
 * {@link AscStructures} bootstraps, so it takes the datapack provider's registry lookup.
 */
public class AscStructureTagProvider extends StructureTagsProvider {
    public AscStructureTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, lookupProvider, AscensionCraft.MOD_ID);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        // Used by /locate
        tag(ModTags.Structures.PAGODAS)
                .add(AscStructures.PAGODA_LARGE);

        // Lets NpcVillageSpawning populate the pagoda like a village (its lecterns are village POIs)
        tag(StructureTags.VILLAGE)
                .add(AscStructures.PAGODA_LARGE);
    }
}
