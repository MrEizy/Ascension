package net.zic.ascension.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.EntityTypeTagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.util.ModTags;

import javax.annotation.Nullable;
import java.util.concurrent.CompletableFuture;

public class ModEntityTypeTagProvider extends EntityTypeTagsProvider {

    public ModEntityTypeTagProvider(PackOutput output,
                                    CompletableFuture<HolderLookup.Provider> lookupProvider,
                                    @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, AscensionCraft.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        // Spirit Sealing Ring Blacklist - Cannot capture these mobs
        tag(ModTags.EntityTypes.SEALING_BLACKLIST)
                .add(EntityType.WITHER)
                .add(EntityType.ENDER_DRAGON)
                .add(EntityType.WARDEN)
                .add(EntityType.ELDER_GUARDIAN)
                .add(EntityType.PLAYER);

        // Optional: Whitelist approach (if you want to restrict to specific mobs)
        // Leaving empty means all non-blacklisted mobs can be captured
        tag(ModTags.EntityTypes.CAPTURABLE);
    }

    private ResourceLocation modTag(String path) {
        return ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, path);
    }
}