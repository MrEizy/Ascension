package net.zic.ascension.api.ascension.core.path.bonus.data_source;

import io.netty.buffer.ByteBuf;
import net.minecraft.resources.Identifier;
import net.zic.ascension.api.ascension.core.CoreHolderProviders;
import net.zic.ascension.api.ascension.core.path.bonus.PathBonus;
import net.zic.ascension.api.ascension.core.path.bonus.PathBonusCategoryHolder;
import net.zic.ascension.api.ascension.core.path.bonus.PathBonusProvider;
import net.zic.ascension.api.rpg_engine.source.data_source.DataSource;
import net.zic.ascension.api.rpg_engine.source.data_source.DataSourceInstance;
import net.zic.zenithlib.network.ByteBufHelpers;
import net.zic.zenithlib.util.Processable;
import net.zic.zenithlib.value_containers.typed.Modifier;
import net.zic.zenithlib.value_containers.typed.RangedValueContainer;
import net.zic.zenithlib.value_containers.typed.ValueContainer;

import java.util.*;

//by istelsf the processable wont do anything, only when accessed through
//AscensionOriginSourceHelper does it properly update the path bonus
//TODO for now this will not sync, primarily because it is not "needed" on the client(it is all linked to entity bonus holder)
public class DataSourcePathBonusHolder extends Processable implements DataSourceInstance, PathBonusProvider {



    @Override
    public DataSource getDataSource() {
        return CoreHolderProviders.PATH_BONUS_HOLDER_PROVIDER.get();
    }

    @Override
    public ValueContainer<Double> getPathBonusContainer(Identifier category, Identifier path) {
        return null;
    }

    @Override
    public double getPathBonus(Identifier category, Identifier path) {
        return 0;
    }

    @Override
    public Collection<PathBonus> getAllPathBonuses() {
        return List.of();
    }

    @Override
    public Collection<Identifier> getAllPathBonusesInCategory(Identifier category) {
        return List.of();
    }
}
