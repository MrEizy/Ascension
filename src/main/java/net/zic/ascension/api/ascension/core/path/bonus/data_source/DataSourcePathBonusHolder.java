package net.zic.ascension.api.ascension.core.path.bonus.data_source;

import io.netty.buffer.ByteBuf;
import net.minecraft.resources.Identifier;
import net.zic.ascension.api.ascension.core.CoreHolderProviders;
import net.zic.ascension.api.ascension.core.path.bonus.PathBonus;
import net.zic.ascension.api.ascension.core.path.bonus.PathBonusCategoryHolder;
import net.zic.ascension.api.ascension.core.path.bonus.PathBonusHolder;
import net.zic.ascension.api.ascension.core.path.bonus.PathBonusProvider;
import net.zic.ascension.api.rpg_engine.source.data_source.DataSource;
import net.zic.ascension.api.rpg_engine.source.data_source.DataSourceInstance;
import net.zic.zenithlib.network.ByteBufHelpers;
import net.zic.zenithlib.util.Processable;
import net.zic.zenithlib.value_containers.typed.Modifier;
import net.zic.zenithlib.value_containers.typed.RangedValueContainer;
import net.zic.zenithlib.value_containers.typed.ValueContainer;

import java.util.*;
import java.util.stream.Collectors;

//by istelsf the processable wont do anything, only when accessed through
//AscensionOriginSourceHelper does it properly update the path bonus
//TODO for now this will not sync, primarily because it is not "needed" on the client(it is all linked to entity bonus holder)
public class DataSourcePathBonusHolder extends Processable implements DataSourceInstance, PathBonusProvider {

    private final PathBonusHolder internalHolder = new PathBonusHolder();

    public void addFlatModifier(Identifier category, Identifier path, Modifier<Double> modifier){
        internalHolder.addFlatModifier(category,path,modifier);
        startAndResolveProcess();
    }
    public void addMultiplierModifier(Identifier category,Identifier path,Modifier<Double> modifier){
        internalHolder.addMultiplierModifier(category,path,modifier);
        startAndResolveProcess();

    }
    public void removeModifier(Identifier category,Identifier path,Identifier modifier){
        internalHolder.removeModifier(category,path,modifier);
        startAndResolveProcess();

    }

    @Override
    public DataSource getDataSource() {
        return CoreHolderProviders.PATH_BONUS_HOLDER_PROVIDER.get();
    }

    @Override
    public ValueContainer<Double> getPathBonusContainer(Identifier category, Identifier path) {
        return internalHolder.getContainer(category,path);
    }

    @Override
    public double getPathBonus(Identifier category, Identifier path) {
        return internalHolder.getPathBonus(category,path);
    }

    @Override
    public Collection<PathBonus> getAllPathBonuses() {
        return internalHolder.getPathBonuses();
    }

    @Override
    public Collection<Identifier> getAllPathBonusesInCategory(Identifier category) {
        return getAllPathBonuses().stream().filter(bonus->bonus.category().equals(category)).map(PathBonus::path).collect(Collectors.toSet());
    }
}
