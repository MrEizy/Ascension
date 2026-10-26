package net.zic.ascension.api.ascension.core.golden_core.data_source;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.Identifier;
import net.zic.ascension.api.ascension.core.CoreDataSources;
import net.zic.ascension.api.ascension.core.golden_core.GoldenCoreInstance;
import net.zic.ascension.api.rpg_engine.source.data_source.DataSourceInstance;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

//an origin can hold any number of golden cores, each one unique by its instance id
public class GoldenCoreHolder implements DataSourceInstance<GoldenCoreDataSource> {

    private final Map<UUID, GoldenCoreInstance<?>> cores = new HashMap<>();
    private final Map<UUID, Set<Identifier>> coreOwners = new HashMap<>();
    //path -> cores tracking that path
    private final Map<Identifier, Set<UUID>> coresByPath = new HashMap<>();

    //sync state, only the dirty and removed cores are sent in a partial patch
    private final Set<UUID> dirtyCores = new HashSet<>();
    private final Set<UUID> removedCores = new HashSet<>();

    //cores read from save, held until an owner adds the core again
    private final Map<UUID, GoldenCoreInstance<?>> cachedCores = new HashMap<>();

    /**
     * adds a core, if the core is already held only the owner is added.
     * if the core is cached the cached instance is used instead
     * @param instance the core we are adding
     * @param owner the source of this core
     * @return true -> added, false -> not added
     */
    public boolean addGoldenCore(GoldenCoreInstance<?> instance, Identifier owner) {
        if (instance == null) return false;
        UUID id = instance.getId();
        if (hasGoldenCore(id)) return addOwner(id, owner);

        GoldenCoreInstance<?> cached = takeCachedGoldenCore(id);
        put(cached != null ? cached : instance);
        coreOwners.computeIfAbsent(id, key -> new HashSet<>()).add(owner);

        removedCores.remove(id);
        dirtyCores.add(id);
        return true;
    }

    //adds an owner to a core that is already held
    public boolean addOwner(UUID id, Identifier owner) {
        if (!hasGoldenCore(id)) return false;
        coreOwners.computeIfAbsent(id, key -> new HashSet<>()).add(owner);
        return true;
    }

    /**
     * replaces a held core with an instance of the same id, keeps the owners. runs no core logic
     * @return false if no core with that id is held
     */
    public boolean replaceGoldenCore(GoldenCoreInstance<?> instance) {
        GoldenCoreInstance<?> old = cores.get(instance.getId());
        if (old == null) return false;
        unindex(old);
        put(instance);
        dirtyCores.add(instance.getId());
        return true;
    }

    /**
     * removes a core only if it has no owners left
     * @param id the core we want to remove
     * @param owner the source of the removal
     * @return true->removed, false -> not removed
     */
    public boolean removeGoldenCore(UUID id, Identifier owner) {
        Set<Identifier> owners = coreOwners.get(id);
        if (owners == null) return false;
        owners.remove(owner);

        if (!owners.isEmpty()) return false;

        GoldenCoreInstance<?> instance = cores.remove(id);
        if (instance != null) unindex(instance);
        coreOwners.remove(id);

        dirtyCores.remove(id);
        removedCores.add(id);
        return true;
    }

    public Collection<UUID> getGoldenCores() {return cores.keySet();}

    public Collection<GoldenCoreInstance<?>> getGoldenCoreInstances() {return cores.values();}

    public boolean hasGoldenCore(UUID id) {
        return cores.containsKey(id);
    }

    public @Nullable GoldenCoreInstance<?> getGoldenCore(UUID id) {
        return cores.get(id);
    }

    public Collection<Identifier> getOwners(UUID id) {
        Set<Identifier> owners = coreOwners.get(id);
        return owners == null ? Set.of() : Set.copyOf(owners);
    }

    //all held cores that track the given path
    public List<GoldenCoreInstance<?>> getGoldenCoresForPath(Identifier path) {
        Set<UUID> ids = coresByPath.get(path);
        if (ids == null) return List.of();
        List<GoldenCoreInstance<?>> result = new ArrayList<>(ids.size());
        for (UUID id : ids) {
            GoldenCoreInstance<?> instance = cores.get(id);
            if (instance != null) result.add(instance);
        }
        return result;
    }

    //should be used if you changed a cores GoldenCoreData
    public void markGoldenCoreDirty(UUID id) {
        if (hasGoldenCore(id)) dirtyCores.add(id);
    }

    private void put(GoldenCoreInstance<?> instance) {
        cores.put(instance.getId(), instance);
        coresByPath.computeIfAbsent(instance.getPath(), key -> new HashSet<>()).add(instance.getId());
    }

    private void unindex(GoldenCoreInstance<?> instance) {
        Set<UUID> ids = coresByPath.get(instance.getPath());
        if (ids == null) return;
        ids.remove(instance.getId());
        if (ids.isEmpty()) coresByPath.remove(instance.getPath());
    }

    //──Cached Data────────────────────────────────────────────────────────
    public void addCachedGoldenCore(GoldenCoreInstance<?> instance) {
        cachedCores.put(instance.getId(), instance);
    }

    public boolean hasCachedGoldenCore(UUID id) {
        return cachedCores.containsKey(id);
    }

    public @Nullable GoldenCoreInstance<?> takeCachedGoldenCore(UUID id) {
        return cachedCores.remove(id);
    }

    public void clearCache() {
        cachedCores.clear();
    }

    //──Raw Manipulation────────────────────────────────────────────────────────
    public Map<UUID, GoldenCoreInstance<?>> getGoldenCoreSnapshot() {
        return Map.copyOf(cores);
    }

    public Map<UUID, Set<Identifier>> getOwnerSnapshot() {
        Map<UUID, Set<Identifier>> owners = new HashMap<>();
        coreOwners.forEach((id, coreOwner) -> owners.put(id, Set.copyOf(coreOwner)));
        return owners;
    }

    //restores cores without running any core logic
    public void restore(Map<UUID, GoldenCoreInstance<?>> rawCores, Map<UUID, Set<Identifier>> rawOwners) {
        cores.clear();
        coresByPath.clear();
        coreOwners.clear();
        rawCores.values().forEach(this::put);
        rawOwners.forEach((id, owners) -> coreOwners.put(id, new HashSet<>(owners)));
        dirtyCores.addAll(rawCores.keySet());
    }

    public void clearContainer() {
        cores.clear();
        coresByPath.clear();
        coreOwners.clear();
        dirtyCores.clear();
        removedCores.clear();
    }

    //──Sync────────────────────────────────────────────────────────

    void encodeFull(ByteBuf buf, RegistryAccess access) {
        buf.writeInt(cores.size());
        for (GoldenCoreInstance<?> instance : cores.values()) {
            instance.encode(buf, access);
        }
        clearSyncState();
    }

    void encodePartial(ByteBuf buf, RegistryAccess access) {
        buf.writeInt(dirtyCores.size());
        for (UUID id : dirtyCores) {
            cores.get(id).encode(buf, access);
        }
        buf.writeInt(removedCores.size());
        for (UUID id : removedCores) {
            GoldenCoreInstance.encodeId(id, buf);
        }
        clearSyncState();
    }

    void decodeFull(ByteBuf buf, RegistryAccess access) {
        cores.clear();
        coresByPath.clear();
        decodeInstances(buf, access);
        clearSyncState();
    }

    void decodePartial(ByteBuf buf, RegistryAccess access) {
        decodeInstances(buf, access);
        int removed = buf.readInt();
        for (int i = 0; i < removed; i++) {
            GoldenCoreInstance<?> instance = cores.remove(GoldenCoreInstance.decodeId(buf));
            if (instance != null) unindex(instance);
        }
        clearSyncState();
    }

    private void decodeInstances(ByteBuf buf, RegistryAccess access) {
        int size = buf.readInt();
        for (int i = 0; i < size; i++) {
            GoldenCoreInstance<?> instance = GoldenCoreInstance.decode(buf, access);
            if (instance == null) continue;
            GoldenCoreInstance<?> old = cores.get(instance.getId());
            if (old != null) unindex(old);
            put(instance);
        }
    }

    void clearSyncState() {
        dirtyCores.clear();
        removedCores.clear();
    }

    @Override
    public GoldenCoreDataSource getDataSource() {
        return CoreDataSources.GOLDEN_CORE_DATA_SOURCE.get();
    }
}
