package net.zic.ascension.api.ascension.core.path.bonus;

import io.netty.buffer.ByteBuf;
import net.minecraft.resources.Identifier;
import net.zic.ascension.api.ascension.core.CoreHolderProviders;
import net.zic.ascension.api.rpg_engine.source.data_source.DataSource;
import net.zic.ascension.api.rpg_engine.source.data_source.DataSourceInstance;
import net.zic.zenithlib.network.ByteBufHelpers;
import net.zic.zenithlib.value_containers.ValueContainer;
import net.zic.zenithlib.value_containers.ValueContainerModifier;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

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
    public void encode(ByteBuf buf){

        buf.writeInt(dirtyCategories.size());
        for(Identifier dirtyCategory : dirtyCategories){
            ByteBufHelpers.encodeIdentifier(dirtyCategory,buf);
            categories.get(dirtyCategory).encode(buf);
        }
        dirtyCategories.clear();

    }
    public void decode(ByteBuf buf){

        int size = buf.readInt();
        for(int i = 0;i<size; i++){
            Identifier category = ByteBufHelpers.decodeIdentifier(buf);
            PathBonusCategoryHolder holder = getCategoryHolder(category);
            holder.decode(buf);
        }
    }

    @Override
    public DataSource getDataSource() {
        return CoreHolderProviders.PATH_BONUS_HOLDER_PROVIDER.get();
    }
}
