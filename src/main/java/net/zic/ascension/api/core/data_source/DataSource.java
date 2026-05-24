package net.zic.ascension.api.core.data_source;

import net.zic.ascension.api.datapack.data_source.DataSourceType;

public interface DataSource {


    DataSourceType getType();

    DataSourceInstance newInstance();
}
