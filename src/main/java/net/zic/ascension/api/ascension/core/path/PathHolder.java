package net.zic.ascension.api.ascension.core.path;

import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.Identifier;
import net.zic.ascension.api.ascension.core.CoreDataSources;
import net.zic.ascension.api.ascension.core.CoreRegistries;
import net.zic.ascension.api.rpg_engine.source.data_source.DataSourceInstance;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class PathHolder implements DataSourceInstance<PathDataSource> {
    final HashMap<Identifier, PathInstance> paths = new HashMap<>();
    final HashMap<Identifier, HashSet<Identifier>> pathOwners = new HashMap<>();

    final HashMap<Identifier,PathInstance>  cachedPaths = new HashMap<>();

    final HashSet<Identifier> dirtyPaths = new HashSet<>();
    final HashSet<Identifier> toRemovePaths = new HashSet<>();
    //──Path Data────────────────────────────────────────────────────────

    public boolean addPath(Identifier path,PathInstance PathInstance,Identifier owner){
        return addPath(path,PathInstance,owner,false);
    }
    public boolean addPath(Identifier path,PathInstance pathInstance,Identifier owner,boolean overwrite){

        if((hasPath(path) && overwrite) || !hasPath(path)) {
            if(hasCachedPath(path)) pathInstance = removeCachedPath(path);
            paths.put(path,pathInstance);
            pathOwners.computeIfAbsent(path,key->new HashSet<>());
            markPathDirty(path);
        }
        pathOwners.get(path).add(owner);
        return true;
    }
    public PathInstance getPath(Identifier path){
        return paths.get(path);
    }
    public Path getPath(Identifier path, RegistryAccess access){
        return CoreRegistries.safeAccess(CoreRegistries.PATH_REGISTRY,path,access);
    }
    public boolean hasPath(Identifier path){
        return paths.containsKey(path);
    }
    public boolean removePath(Identifier path,Identifier owner){
        if (!pathOwners.containsKey(path)) return false;
        pathOwners.get(path).remove(owner);
        if(!pathOwners.get(path).isEmpty()) return false;
        paths.remove(path);
        pathOwners.remove(path);
        toRemovePaths.add(path);
        dirtyPaths.remove(path);
        return true;
    }

    public Collection<Identifier> getPaths(){
        return paths.keySet();
    }
    public Collection<Identifier> getOwners(Identifier path){
        return pathOwners.get(path);
    }

    public void markPathDirty(Identifier path){
        if(hasPath(path)) dirtyPaths.add(path);
    }

    //──Cached Data────────────────────────────────────────────────────────
    public void addCachedPath(Identifier path,PathInstance pathInstance){
        if(pathInstance == null || path == null) return;
        cachedPaths.put(path,pathInstance);
    }
    public boolean hasCachedPath(Identifier path){
        return cachedPaths.containsKey(path);
    }
    public PathInstance removeCachedPath(Identifier path){
        return cachedPaths.remove(path);
    }
    public void clearCache(){
        cachedPaths.clear();
    }

    //──Raw Manipulation────────────────────────────────────────────────────────
    public Map<Identifier,PathInstance> getRawPathInstance(){
        return Map.copyOf(paths);
    }
    public Map<Identifier,HashSet<Identifier>> getRawPathOwnerData(){
        return Map.copyOf(pathOwners);
    }

    public void setRawData(Map<Identifier,PathInstance> rawPaths,Map<Identifier,HashSet<Identifier>> rawOwnerData){
        paths.clear();
        pathOwners.clear();
        paths.putAll(rawPaths);
        pathOwners.putAll(rawOwnerData);
        dirtyPaths.addAll(rawPaths.keySet());
    }
    public void clearContainer(){
        paths.clear();
        pathOwners.clear();
        dirtyPaths.clear();
        toRemovePaths.clear();
    }
    public void clearDirty(){
        dirtyPaths.clear();
        toRemovePaths.clear();
    }
    @Override
    public PathDataSource getDataSource() {
        return CoreDataSources.PATH_DATA_SOURCE.get();
    }
}
