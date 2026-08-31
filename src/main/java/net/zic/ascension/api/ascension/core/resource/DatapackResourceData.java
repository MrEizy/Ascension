package net.zic.ascension.api.ascension.core.resource;

import com.mojang.serialization.Codec;
import net.minecraft.resources.Identifier;

import java.util.HashMap;
import java.util.Map;

public final class DatapackResourceData {
    public static final Codec<DatapackResourceData> CODEC = Codec.unboundedMap(Identifier.CODEC, Codec.DOUBLE)
            .xmap(DatapackResourceData::new, DatapackResourceData::values);

    private final Map<Identifier, Double> values;

    public DatapackResourceData() {
        this(Map.of());
    }

    public DatapackResourceData(Map<Identifier, Double> values) {
        this.values = new HashMap<>(values == null ? Map.of() : values);
    }

    public boolean contains(Identifier resource) {
        return values.containsKey(resource);
    }

    public double get(Identifier resource) {
        return values.getOrDefault(resource, 0.0D);
    }

    public void set(Identifier resource, double amount) {
        values.put(resource, amount);
    }

    public Map<Identifier, Double> values() {
        return Map.copyOf(values);
    }
}
