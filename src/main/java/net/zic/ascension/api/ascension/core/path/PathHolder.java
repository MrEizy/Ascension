package net.zic.ascension.api.ascension.core.path;

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

public class PathHolder implements DataSourceInstance<PathDataSource> {
    private final Map<Identifier, PathInstance<?>> paths = new HashMap<>();
    private final Map<Identifier, Set<Identifier>> pathOwners = new HashMap<>();

    //paths read from save, held until an owner adds the path again
    private final Map<Identifier, PathInstance<?>> cachedPaths = new HashMap<>();

    //sync state, only the dirty and removed paths are sent in a partial patch
    private final Set<Identifier> dirtyPaths = new HashSet<>();
    private final Set<Identifier> removedPaths = new HashSet<>();
    //──Path Data────────────────────────────────────────────────────────

    public boolean addPath(PathInstance<?> pathInstance, Identifier owner){
        return addPath(pathInstance,owner,false);
    }

    /**
     * adds a path, if the path is already held only the owner is added unless overwrite is set.
     * if the path is cached the cached instance is used instead
     */
    public boolean addPath(PathInstance<?> pathInstance, Identifier owner, boolean overwrite){
        Identifier path = pathInstance.getPathId();
        if((hasPath(path) && overwrite) || !hasPath(path)) {
            if(hasCachedPath(path)) pathInstance = removeCachedPath(path);
            paths.put(path,pathInstance);
            pathOwners.computeIfAbsent(path,key->new HashSet<>());
            removedPaths.remove(path);
            markPathDirty(path);
        }
        pathOwners.get(path).add(owner);
        return true;
    }
    public @Nullable PathInstance<?> getPath(Identifier path){
        return paths.get(path);
    }
    public boolean hasPath(Identifier path){
        return paths.containsKey(path);
    }

    /**
     * removes a path only if it has no owners left
     * @return true->removed, false -> not removed
     */
    public boolean removePath(Identifier path,Identifier owner){
        Set<Identifier> owners = pathOwners.get(path);
        if (owners == null) return false;
        owners.remove(owner);
        if(!owners.isEmpty()) return false;
        paths.remove(path);
        pathOwners.remove(path);
        removedPaths.add(path);
        dirtyPaths.remove(path);
        return true;
    }

    public Collection<Identifier> getPaths(){
        return paths.keySet();
    }
    public Collection<PathInstance<?>> getPathInstances(){
        return paths.values();
    }
    public Collection<Identifier> getOwners(Identifier path){
        return pathOwners.get(path);
    }

    //should be used if you changed a paths PathData
    public void markPathDirty(Identifier path){
        if(hasPath(path)) dirtyPaths.add(path);
    }

    //──Cached Data────────────────────────────────────────────────────────
    public void addCachedPath(PathInstance<?> pathInstance){
        if(pathInstance == null) return;
        cachedPaths.put(pathInstance.getPathId(),pathInstance);
    }
    public boolean hasCachedPath(Identifier path){
        return cachedPaths.containsKey(path);
    }
    public @Nullable PathInstance<?> removeCachedPath(Identifier path){
        return cachedPaths.remove(path);
    }
    public void clearCache(){
        cachedPaths.clear();
    }

    //──Raw Manipulation────────────────────────────────────────────────────────
    public Map<Identifier,PathInstance<?>> getPathSnapshot(){
        return Map.copyOf(paths);
    }
    public Map<Identifier,Set<Identifier>> getOwnerSnapshot(){
        Map<Identifier, Set<Identifier>> owners = new HashMap<>();
        pathOwners.forEach((path, pathOwner) -> owners.put(path, Set.copyOf(pathOwner)));
        return owners;
    }

    //restores paths without running any path logic
    public void restore(Map<Identifier,PathInstance<?>> rawPaths, Map<Identifier,Set<Identifier>> rawOwners){
        paths.clear();
        pathOwners.clear();
        paths.putAll(rawPaths);
        rawOwners.forEach((path, owners) -> pathOwners.put(path, new HashSet<>(owners)));
        dirtyPaths.addAll(rawPaths.keySet());
    }
    public void clearContainer(){
        paths.clear();
        pathOwners.clear();
        dirtyPaths.clear();
        removedPaths.clear();
    }

    //──Sync────────────────────────────────────────────────────────

    void encodeFull(ByteBuf buf, RegistryAccess access) {
        buf.writeInt(paths.size());
        for (PathInstance<?> instance : paths.values()) {
            instance.encode(buf, access);
        }
        clearSyncState();
    }

    void encodePartial(ByteBuf buf, RegistryAccess access) {
        buf.writeInt(dirtyPaths.size());
        for (Identifier path : dirtyPaths) {
            paths.get(path).encode(buf, access);
        }
        ByteBufHelpers.encodeCollection(removedPaths, buf, ByteBufHelpers::encodeIdentifier);
        clearSyncState();
    }

    void decodeFull(ByteBuf buf, RegistryAccess access) {
        paths.clear();
        decodeInstances(buf, access);
        clearSyncState();
    }

    void decodePartial(ByteBuf buf, RegistryAccess access) {
        decodeInstances(buf, access);
        ByteBufHelpers.decodeArray(buf, ByteBufHelpers::decodeIdentifier).forEach(paths::remove);
        clearSyncState();
    }

    private void decodeInstances(ByteBuf buf, RegistryAccess access) {
        int size = buf.readInt();
        for (int i = 0; i < size; i++) {
            PathInstance<?> instance = PathInstance.decode(buf, access);
            if (instance != null) paths.put(instance.getPathId(), instance);
        }
    }

    void clearSyncState() {
        dirtyPaths.clear();
        removedPaths.clear();
    }

    @Override
    public PathDataSource getDataSource() {
        return CoreDataSources.PATH_DATA_SOURCE.get();
    }
}
