package net.zic.ascension.api.ascension.event.data_source;

import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.Event;
import net.zic.ascension.api.ascension.core.CoreRegistries;
import net.zic.ascension.api.ascension.core.source.AscensionOriginSource;
import net.zic.ascension.api.ascension.core.data_source.DataSource;
import net.zic.ascension.api.ascension.core.data_source.DataSourceInstance;

public abstract class DataSourceEvent extends Event {

    private final Identifier dataSource;
    private final DataSourceInstance data;
    private final AscensionOriginSource source;

    protected DataSourceEvent(Identifier dataSource, DataSourceInstance data, AscensionOriginSource source) {
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
    public AscensionOriginSource getSource(){
        return source;
    }
}
