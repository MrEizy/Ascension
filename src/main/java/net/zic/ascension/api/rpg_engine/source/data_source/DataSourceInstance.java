package net.zic.ascension.api.rpg_engine.source.data_source;

public interface DataSourceInstance<T extends DataSource<? extends DataSourceInstance<T>>> {
    T getDataSource();
}
