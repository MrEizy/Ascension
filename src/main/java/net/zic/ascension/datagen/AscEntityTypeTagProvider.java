package net.zic.ascension.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.EntityTypeTagsProvider;
import net.minecraft.world.entity.EntityType;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.common.util.ModTags;

import java.util.concurrent.CompletableFuture;

public class AscEntityTypeTagProvider extends EntityTypeTagsProvider {
    public AscEntityTypeTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, lookupProvider, AscensionCraft.MOD_ID);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        // Effect Resistances
        tag(ModTags.EntityTypes.FROZEN_IMMUNE)
                .add(EntityType.BLAZE, EntityType.MAGMA_CUBE, EntityType.STRIDER);

        tag(ModTags.EntityTypes.FROZEN_RESISTANT)
                .add(EntityType.POLAR_BEAR, EntityType.SNOW_GOLEM);

        tag(ModTags.EntityTypes.FROZEN_BOSS_PROFILE)
                .add(EntityType.ENDER_DRAGON, EntityType.WITHER, EntityType.WARDEN);


        // Ranged Projectiles
        tag(ModTags.EntityTypes.RANGED_PROJECTILE)
                .add(EntityType.ARROW, EntityType.SPECTRAL_ARROW, EntityType.TRIDENT);


        // Mob Cultivation Stuff
        tag(ModTags.EntityTypes.MOB_CULTIVATION_PASSIVE);
        tag(ModTags.EntityTypes.MOB_CULTIVATION_HOSTILE);


        tag(ModTags.EntityTypes.MOB_CULTIVATION_BOSSES)
                .add(
                        EntityType.ENDER_DRAGON,
                        EntityType.WITHER,
                        EntityType.WARDEN,
                        EntityType.ELDER_GUARDIAN,
                        EntityType.EVOKER);
    }
}
