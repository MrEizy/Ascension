package net.zic.ascension.api.ascension.core.path.bonus.data_source;

import net.minecraft.resources.Identifier;
import net.zic.ascension.api.ascension.core.CoreDataSources;
import net.zic.ascension.api.ascension.core.path.bonus.PathBonus;
import net.zic.ascension.api.ascension.core.path.bonus.PathBonusHolder;
import net.zic.ascension.api.ascension.core.path.bonus.PathBonusProvider;
import net.zic.ascension.api.rpg_engine.source.data_source.DataSourceInstance;
import net.zic.zenithlib.util.Processable;
import net.zic.zenithlib.value_containers.typed.Modifier;
import net.zic.zenithlib.value_containers.typed.ValueContainer;

import java.util.Collection;
import java.util.stream.Collectors;

public class DataSourcePathBonusHolder extends Processable implements DataSourceInstance<PathBonusDataSource>, PathBonusProvider {
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
    public PathBonusDataSource getDataSource() {
        return CoreDataSources.PATH_BONUS_DATA_SOURCE.get();
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
