package net.zic.ascension.api.ascension.core.path;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.ascension.core.CoreHolderProviders;
import net.zic.ascension.api.ascension.core.CoreRegistries;
import net.zic.ascension.api.ascension.core.bloodline.Bloodline;
import net.zic.ascension.api.ascension.core.bloodline.BloodlineData;
import net.zic.ascension.api.ascension.core.physique.PhysiqueData;
import net.zic.ascension.api.rpg_engine.source.data_source.DataSource;
import net.zic.ascension.api.rpg_engine.source.data_source.DataSourceInstance;
import net.zic.zenithlib.nbt.NbtHelpers;
import net.zic.zenithlib.network.ByteBufHelpers;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class PathHolder implements DataSourceInstance {

    private final HashMap<Identifier, PathInstance> paths = new HashMap<>();
    private final HashMap<Identifier, HashSet<Identifier>> pathOwners = new HashMap<>();

    private final HashMap<Identifier,PathInstance>  cachedPaths = new HashMap<>();

    private final HashSet<Identifier> dirtyPaths = new HashSet<>();
    private final HashSet<Identifier> toRemovePaths = new HashSet<>();


    @Override
    public DataSource getDataSource() {
        return CoreHolderProviders.PATH_HOLDER_PROVIDER.get();
    }

    //──Path Data────────────────────────────────────────────────────────

    public boolean addPath(Identifier path,PathInstance PathInstance,Identifier owner){
        return addPath(path,PathInstance,owner,false);
    }
    public boolean addPath(Identifier path,PathInstance PathInstance,Identifier owner,boolean overwrite){

        if((hasPath(path) && overwrite) || !hasPath(path)) {
            if(hasCachedPath(path)) PathInstance = removeCachedPath(path);
            paths.put(path,PathInstance);
            pathOwners.computeIfAbsent(path,key->new HashSet<>());
        }
        pathOwners.get(path).add(owner);
        return true;
    }
    public PathInstance getPath(Identifier path){
        return paths.get(path);
    }
    public Path getPath(Identifier path,RegistryAccess access){
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

    //──Data────────────────────────────────────────────────────────

    public void write(ValueOutput output, RegistryAccess access){

        ValueOutput.ValueOutputList paths = output.childrenList("paths");
        for(Identifier path : getPaths()){
            AscensionCraft.LOGGER.debug("Saving Path {}",path);
            try {
                ValueOutput pathOutput = paths.addChild();
                NbtHelpers.writeIdentifier(pathOutput,"path",path);
                ValueOutput pathInstance = pathOutput.child("data");
                if(getPath(path) != null) getPath(path).write(pathInstance,access);
                else throw new Exception("no path data for path "+path);
            }catch (Exception e){
                AscensionCraft.LOGGER.debug("Error writing path {}",path);
                AscensionCraft.LOGGER.debug("stacktrace",e);
            }
        }
    }
    public void read(ValueInput input, RegistryAccess access){

        clearCache();
        ValueInput.ValueInputList pathsInput = input.childrenListOrEmpty("paths");
        for(ValueInput pathInput : pathsInput){
            try {
                Identifier pathId = NbtHelpers.readIdentifier(pathInput,"path");
                AscensionCraft.LOGGER.debug("Reading Path {}",pathId);
                ValueInput PathInstance = pathInput.childOrEmpty("data");

                Path path = CoreRegistries.safeAccess(CoreRegistries.PATH_REGISTRY,pathId,access);
                if(path == null) continue;

                PathInstance data = path.loadInstance(PathInstance,access);
                addCachedPath(pathId,data);
            }catch (Exception e){
                AscensionCraft.LOGGER.debug("Error loading path");
                AscensionCraft.LOGGER.debug("stacktrace: ",e);
            }
        }
    }
    public void encode(ByteBuf buf, RegistryAccess access,boolean fullPatch){
        buf.writeBoolean(fullPatch);
        if(fullPatch) encodeFullPatch(buf,access);
        else encodePartialPatch(buf,access);

        dirtyPaths.clear();
        toRemovePaths.clear();
    }

    protected void encodeFullPatch(ByteBuf buf, RegistryAccess access){
        buf.writeInt(paths.size());
        for(Identifier path : paths.keySet()){
            ByteBufHelpers.encodeIdentifier(path,buf);
            getPath(path).encode(buf,access);
        }
    }
    protected void encodePartialPatch(ByteBuf buf, RegistryAccess access){
        buf.writeInt(dirtyPaths.size());
        for(Identifier dirtyPath : dirtyPaths){
            ByteBufHelpers.encodeIdentifier(dirtyPath,buf);
            getPath(dirtyPath).encode(buf,access);
        }
        ByteBufHelpers.encodeCollection(toRemovePaths,buf,ByteBufHelpers::encodeIdentifier);
    }
    public void decode(ByteBuf buf,RegistryAccess access){


        if(buf.readBoolean()) decodeFullPatch(buf,access);
        else decodePartialPatch(buf,access);

        dirtyPaths.clear();
        toRemovePaths.clear();
    }

    private void decodeFullPatch(ByteBuf buf,RegistryAccess access){
        paths.clear();
        int size = buf.readInt();
        for(int i = 0;i<size;i++){
            Identifier pathId = ByteBufHelpers.decodeIdentifier(buf);
            Path path = CoreRegistries.safeAccess(CoreRegistries.PATH_REGISTRY,pathId,access);
            PathInstance data = path.loadInstance(buf,access);
            paths.put(pathId,data);
        }
    }

    protected void decodePartialPatch(ByteBuf buf,RegistryAccess access){
        int size = buf.readInt();
        for(int i = 0;i<size;i++){
            Identifier pathId = ByteBufHelpers.decodeIdentifier(buf);
            Path path = CoreRegistries.safeAccess(CoreRegistries.PATH_REGISTRY,pathId,access);
            PathInstance data = path.loadInstance(buf,access);
            paths.put(pathId,data);
        }
        ByteBufHelpers.decodeArray(buf,ByteBufHelpers::decodeIdentifier).forEach(paths::remove);

    }
}
