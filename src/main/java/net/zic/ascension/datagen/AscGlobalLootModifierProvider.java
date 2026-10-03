package net.zic.ascension.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.GlobalLootModifierProvider;
import net.zic.ascension.AscensionCraft;

import java.util.concurrent.CompletableFuture;

public class AscGlobalLootModifierProvider extends GlobalLootModifierProvider {
    public AscGlobalLootModifierProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, AscensionCraft.MOD_ID);
    }

    @Override
    protected void start() {

    }
}
