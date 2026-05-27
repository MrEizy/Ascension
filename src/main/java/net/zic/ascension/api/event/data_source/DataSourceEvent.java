package net.zic.ascension.api.event.data_source;

import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.Event;
import net.zic.ascension.api.core.CoreRegistries;
import net.zic.ascension.api.core.OriginSource;
import net.zic.ascension.api.core.data_source.DataSource;
import net.zic.ascension.api.core.data_source.DataSourceInstance;
import net.zic.ascension.api.core.path.Path;
import net.zic.ascension.api.core.path.PathData;

public abstract class DataSourceEvent extends Event {

    private final Identifier dataSource;
    private final DataSourceInstance data;
    private final OriginSource source;

    protected DataSourceEvent(Identifier dataSource, DataSourceInstance data, OriginSource source) {
        this.dataSource = dataSource;
        this.data = data;
        this.source = source;
    }

    public Identifier getDataSourceIdentifier() {
        return dataSource;
    }
    public DataSource getDataSource(RegistryAccess access){
        return CoreRegistries.DATA_SOURCE_REGISTRY.get(access).getValue(dataSource);
    }

    public DataSourceInstance getInstance(){
        return data;
    }
    public OriginSource getSource(){
        return source;
    }
}
