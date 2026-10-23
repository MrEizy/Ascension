package net.zic.ascension.api.ascension.core.technique;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.Identifier;
import net.zic.ascension.api.ascension.core.CoreDataSources;
import net.zic.ascension.api.rpg_engine.source.data_source.DataSourceInstance;
import net.zic.zenithlib.network.ByteBufHelpers;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class TechniqueHolder implements DataSourceInstance<TechniqueDataSource> {
    private final Map<Identifier, TechniqueInstance<?>> techniques = new HashMap<>();

    //sync state, only the dirty and removed techniques are sent in a partial patch
    private final Set<Identifier> dirtyTechniques = new HashSet<>();
    private final Set<Identifier> removedTechniques = new HashSet<>();

    public boolean addTechnique(TechniqueInstance<?> instance) {
        if (instance == null || hasTechnique(instance.getTechniqueId())) {
            return false;
        }
        Identifier technique = instance.getTechniqueId();
        techniques.put(technique, instance);
        removedTechniques.remove(technique);
        dirtyTechniques.add(technique);
        return true;
    }

    public boolean removeTechnique(Identifier technique) {
        if (!hasTechnique(technique)) {
            return false;
        }
        techniques.remove(technique);
        dirtyTechniques.remove(technique);
        removedTechniques.add(technique);
        return true;
    }

    public boolean hasTechnique(Identifier technique) {
        return techniques.containsKey(technique);
    }

    public @Nullable TechniqueInstance<?> getTechnique(Identifier technique) {
        return techniques.get(technique);
    }

    public Collection<Identifier> getTechniques() {
        return techniques.keySet();
    }

    public Collection<TechniqueInstance<?>> getTechniqueInstances() {
        return techniques.values();
    }

    //should be used if you changed a techniques TechniqueData
    public void markTechniqueDirty(Identifier technique) {
        if (hasTechnique(technique)) {
            dirtyTechniques.add(technique);
        }
    }

    //──Raw Manipulation────────────────────────────────────────────────────────
    public Map<Identifier, TechniqueInstance<?>> getTechniqueSnapshot() {
        return Map.copyOf(techniques);
    }

    //restores techniques without running any technique logic
    public void restore(Map<Identifier, TechniqueInstance<?>> rawTechniques) {
        techniques.clear();
        techniques.putAll(rawTechniques);
        dirtyTechniques.addAll(rawTechniques.keySet());
    }

    public void clearContainer() {
        techniques.clear();
        dirtyTechniques.clear();
        removedTechniques.clear();
    }

    //──Sync────────────────────────────────────────────────────────

    void encodeFull(ByteBuf buf, RegistryAccess access) {
        buf.writeInt(techniques.size());
        for (TechniqueInstance<?> instance : techniques.values()) {
            instance.encode(buf, access);
        }
        clearSyncState();
    }

    void encodePartial(ByteBuf buf, RegistryAccess access) {
        buf.writeInt(dirtyTechniques.size());
        for (Identifier technique : dirtyTechniques) {
            techniques.get(technique).encode(buf, access);
        }
        ByteBufHelpers.encodeCollection(removedTechniques, buf, ByteBufHelpers::encodeIdentifier);
        clearSyncState();
    }

    void decodeFull(ByteBuf buf, RegistryAccess access) {
        techniques.clear();
        decodeInstances(buf, access);
        clearSyncState();
    }

    void decodePartial(ByteBuf buf, RegistryAccess access) {
        decodeInstances(buf, access);
        ByteBufHelpers.decodeArray(buf, ByteBufHelpers::decodeIdentifier).forEach(techniques::remove);
        clearSyncState();
    }

    private void decodeInstances(ByteBuf buf, RegistryAccess access) {
        int size = buf.readInt();
        for (int i = 0; i < size; i++) {
            TechniqueInstance<?> instance = TechniqueInstance.decode(buf, access);
            if (instance != null) techniques.put(instance.getTechniqueId(), instance);
        }
    }

    void clearSyncState() {
        dirtyTechniques.clear();
        removedTechniques.clear();
    }

    @Override
    public TechniqueDataSource getDataSource() {
        return CoreDataSources.TECHNIQUE_DATA_SOURCE.get();
    }
}
