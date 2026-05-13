package net.zic.ascension.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.world.damagesource.DamageEffects;
import net.minecraft.world.damagesource.DamageScaling;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.damagesource.DeathMessageType;
import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.util.ModTags;
import net.zic.ascension.worldgen.*;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class ModDatapackProvider extends DatapackBuiltinEntriesProvider {
    public static final RegistrySetBuilder BUILDER = new RegistrySetBuilder()
            .add(Registries.CONFIGURED_FEATURE, ModConfiguredFeatures::bootstrap)
            .add(Registries.PLACED_FEATURE, ModPlacedFeatures::bootstrap)
            .add(NeoForgeRegistries.Keys.BIOME_MODIFIERS, ModBiomeModifiers::bootstrap)
            .add(Registries.STRUCTURE, ModStructureGen::bootstrapStructures)
            .add(Registries.STRUCTURE_SET, ModStructureGen::bootstrapStructureSets)
            .add(Registries.TEMPLATE_POOL, ModStructureGen::bootstrapPools)
            .add(Registries.DAMAGE_TYPE, bootstrap -> {
                bootstrap.register(ModTags.DamageTypes.DAO_DAMAGE_KEY, new DamageType(ModTags.DamageTypes.DAO_DAMAGE_KEY.location().toString(),
                        DamageScaling.WHEN_CAUSED_BY_LIVING_NON_PLAYER,
                        0.1f,
                        DamageEffects.HURT,
                        DeathMessageType.DEFAULT));
            });


    public ModDatapackProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, BUILDER, Set.of(AscensionCraft.MOD_ID));
    }
}
