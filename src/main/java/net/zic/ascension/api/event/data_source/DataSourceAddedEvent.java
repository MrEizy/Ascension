package net.zic.ascension.api.event.data_source;

import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.ICancellableEvent;
import net.zic.ascension.api.core.OriginSource;
import net.zic.ascension.api.core.data_source.DataSourceInstance;

public abstract class DataSourceAddedEvent extends DataSourceEvent{
    protected DataSourceAddedEvent(Identifier dataSource, DataSourceInstance data, OriginSource source) {
        super(dataSource, data, source);
    }
    public static class Pre extends DataSourceAddedEvent implements ICancellableEvent{

        public Pre(Identifier dataSource, DataSourceInstance data, OriginSource source) {
            super(dataSource, data, source);
        }
    }
    public static class Post extends DataSourceAddedEvent{

        public Post(Identifier dataSource, DataSourceInstance data, OriginSource source) {
            super(dataSource, data, source);
        }
    }
}
