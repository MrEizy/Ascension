package net.zic.ascension.datagen;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.zic.ascension.worldgen.dimension.AscWorldPresets;

import java.util.concurrent.CompletableFuture;

public final class AscWorldPresetTagProvider implements DataProvider {
    private final PackOutput.PathProvider tags;

    public AscWorldPresetTagProvider(PackOutput output) {
        this.tags = output.createPathProvider(PackOutput.Target.DATA_PACK, "tags/worldgen/world_preset");
    }

    @Override
    public CompletableFuture<?> run(CachedOutput output) {
        JsonObject root = new JsonObject();
        root.addProperty("replace", false);

        JsonArray values = new JsonArray();
        values.add(AscWorldPresets.ASCENSION.identifier().toString());
        root.add("values", values);

        return DataProvider.saveStable(
                output,
                root,
                tags.json(Identifier.fromNamespaceAndPath("minecraft", "normal"))
        );
    }

    @Override
    public String getName() {
        return "Ascension world preset tags";
    }
}
