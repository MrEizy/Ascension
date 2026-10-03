package net.zic.ascension.api.ascension.core.path.bonus.data_source;

import io.netty.buffer.ByteBuf;
import net.minecraft.resources.Identifier;
import net.zic.ascension.api.ascension.core.CoreHolderProviders;
import net.zic.ascension.api.ascension.core.path.bonus.PathBonus;
import net.zic.ascension.api.ascension.core.path.bonus.PathBonusCategoryHolder;
import net.zic.ascension.api.rpg_engine.source.data_source.DataSource;
import net.zic.ascension.api.rpg_engine.source.data_source.DataSourceInstance;
import net.zic.zenithlib.network.ByteBufHelpers;
import net.zic.zenithlib.value_containers.typed.Modifier;
import net.zic.zenithlib.value_containers.typed.RangedValueContainer;
import net.zic.zenithlib.value_containers.typed.ValueContainer;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

public class DataSourcePathBonusHolder implements DataSourceInstance {


    //maps a category -> category bonus holder
    private final HashMap<Identifier, PathBonusCategoryHolder> categories = new HashMap<>();

    private final HashSet<Identifier> dirtyCategories = new HashSet<>();

    protected PathBonusCategoryHolder getCategoryHolder(Identifier category){
        categories.computeIfAbsent(category,key->new PathBonusCategoryHolder());
        return categories.get(category);
    }

    public void addFlatModifier(Identifier category,Identifier path, Modifier<Double> modifier){
        getCategoryHolder(category).addFlatModifier(path,modifier);
        dirtyCategories.add(category);
    }
    public void addMultiplierModifier(Identifier category,Identifier path,Modifier<Double> modifier){
        getCategoryHolder(category).addMultiplierModifier(path,modifier);
        dirtyCategories.add(category);
    }
    public void removeModifier(Identifier category,Identifier path,Identifier modifier){
        if(!categories.containsKey(category)) return;

        getCategoryHolder(category).removeModifier(path,modifier);
        dirtyCategories.add(category);
    }



    public double getBonus(Identifier category,Identifier path){
        return categories.containsKey(category) ? getCategoryHolder(category).getBonus(path) : 0;
    }

    public void setPathBonusContainer(Identifier category, Identifier path, ValueContainer<Double> container){
        getCategoryHolder(category).setPathBonusContainer(path,container);
    }


    public ValueContainer<Double> getPathBonusContainer(Identifier category, Identifier path){
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
    public void encode(ByteBuf buf, boolean fullPatch){

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
