package net.zic.ascension.datagen.mob_cultivation;

import com.google.gson.JsonObject;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.zic.ascension.AscensionCraft;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

abstract class MobCultivationJsonProvider implements DataProvider {
    private final PackOutput.PathProvider pathProvider;

    protected MobCultivationJsonProvider(PackOutput output, String folder) {
        this.pathProvider = output.createPathProvider(PackOutput.Target.DATA_PACK, "mob_cultivation/" + folder);
    }

    protected abstract void addEntries(Map<Identifier, JsonObject> entries);

    @Override
    public CompletableFuture<?> run(CachedOutput output) {
        Map<Identifier, JsonObject> entries = new LinkedHashMap<>();
        addEntries(entries);

        return CompletableFuture.allOf(entries.entrySet().stream()
                .map(entry -> DataProvider.saveStable(output, entry.getValue(), pathProvider.json(entry.getKey())))
                .toArray(CompletableFuture[]::new));
    }

    protected static Identifier id(String path) {
        return AscensionCraft.prefix(path);
    }
}
