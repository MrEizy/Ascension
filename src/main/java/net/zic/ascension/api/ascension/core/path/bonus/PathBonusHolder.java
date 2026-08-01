package net.zic.ascension.api.ascension.core.path.bonus;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.Identifier;
import net.zic.ascension.api.ascension.core.CoreHolderProviders;
import net.zic.ascension.api.ascension.core.CoreRegistries;
import net.zic.ascension.api.ascension.core.path.Path;
import net.zic.ascension.api.ascension.core.path.PathData;
import net.zic.ascension.api.rpg_engine.source.data_source.DataSource;
import net.zic.ascension.api.rpg_engine.source.data_source.DataSourceInstance;
import net.zic.zenithlib.network.ByteBufHelpers;
import net.zic.zenithlib.value_containers.ValueContainer;
import net.zic.zenithlib.value_containers.ValueContainerModifier;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
//TODO:
// set up such that on changes we notify attached entities
// then those entities trigger an update, and use a combined value container
public class PathBonusHolder implements DataSourceInstance {


    //maps a category -> category bonus holder
    private final HashMap<Identifier,PathBonusCategoryHolder> categories = new HashMap<>();

    private final HashSet<Identifier> dirtyCategories = new HashSet<>();

    protected PathBonusCategoryHolder getCategoryHolder(Identifier category){
        categories.computeIfAbsent(category,key->new PathBonusCategoryHolder());
        return categories.get(category);
    }

    public void addBonus(Identifier category,Identifier path,double val){
        getCategoryHolder(category).addBonus(path,val);
        dirtyCategories.add(category);
    }
    public void addBonusModifier(Identifier category, Identifier path, ValueContainerModifier modifier){
        getCategoryHolder(category).addBonusModifier(path,modifier);
        dirtyCategories.add(category);
    }

    public void removeBonus(Identifier category,Identifier path,double val){
        if(!categories.containsKey(category)) return;
        getCategoryHolder(category).removeBonus(path,val);
        dirtyCategories.add(category);
    }

    public void removeBonusModifier(Identifier category,Identifier path,Identifier modifier){
        if(!categories.containsKey(category)) return;
        getCategoryHolder(category).removeBonusModifier(path,modifier);
        dirtyCategories.add(category);
    }


    public double getBonus(Identifier category,Identifier path){
        return categories.containsKey(category) ? getCategoryHolder(category).getBonus(path) : 0;
    }

    public void setPathBonusContainer(Identifier category,Identifier path,ValueContainer container){
        getCategoryHolder(category).setPathBonusContainer(path,container);
    }


    public ValueContainer getPathBonusContainer(Identifier category,Identifier path){
        return getCategoryHolder(category).getPathBonusContainer(path);
    }

    public Collection<PathBonus> getAllPathBonuses(){
        HashSet<PathBonus> pathBonuses = new HashSet<>();
        for(Identifier category:categories.keySet()){
            getCategoryHolder(category).getAllPaths().forEach(
                    path->pathBonuses.add(new PathBonus(category,path))
            );
        }
        return pathBonuses;
    }
    public Collection<Identifier> getAllPathBonusesInCategory(Identifier category){
        return getCategoryHolder(category).getAllPaths();
    }
    public Collection<PathBonus> getDirtyPathBonuses(){
        HashSet<PathBonus> pathBonuses = new HashSet<>();
        for(Identifier category:categories.keySet()){
            getCategoryHolder(category).getDirtyPaths().forEach(
                    path->pathBonuses.add(new PathBonus(category,path))
            );
        }
        return pathBonuses;
    }
    public void encode(ByteBuf buf,boolean fullPatch){

        buf.writeBoolean(fullPatch);
        if(fullPatch) encodeFullPatch(buf);
        else encodePartialPatch(buf);

        dirtyCategories.clear();

    }
    protected void encodeFullPatch(ByteBuf buf){
        buf.writeInt(categories.size());
        for(Identifier category : categories.keySet()){
            ByteBufHelpers.encodeIdentifier(category,buf);
            categories.get(category).encode(buf,true);
        }
    }
    protected void encodePartialPatch(ByteBuf buf){
        buf.writeInt(dirtyCategories.size());
        for(Identifier dirtyCategory : dirtyCategories){
            ByteBufHelpers.encodeIdentifier(dirtyCategory,buf);
            categories.get(dirtyCategory).encode(buf,false);
        }
    }
    public void decode(ByteBuf buf){

        if(buf.readBoolean()) categories.clear();
        int size = buf.readInt();
        for(int i = 0;i<size; i++){
            Identifier category = ByteBufHelpers.decodeIdentifier(buf);
            PathBonusCategoryHolder holder = getCategoryHolder(category);
            holder.decode(buf);
        }
        dirtyCategories.clear();

    }

    @Override
    public DataSource getDataSource() {
        return CoreHolderProviders.PATH_BONUS_HOLDER_PROVIDER.get();
    }
}
