package net.zic.ascension.api.rpg_engine.source;

import com.mojang.datafixers.util.Pair;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.Identifier;
import net.zic.ascension.api.rpg_engine.source.data_source.DataSource;
import net.zic.ascension.api.rpg_engine.source.data_source.DataSourceInstance;
import net.zic.zenithlib.network.ByteBufHelpers;
import net.zic.zenithlib.stats.StatInstance;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

public record OriginSourcePatch(
        Map<Identifier, DataSourceInstance> dirtyDataSources,
        Collection<Identifier> toRemoveDataSources,
        Collection<StatInstance> dirtyStats){

    public static void encode(OriginSourcePatch patch, ByteBuf buf,RegistryAccess access){
        ByteBufHelpers.encodeCollection(patch.dirtyDataSources().entrySet(), buf, (pair, byteBuf) -> {
            ByteBufHelpers.encodeIdentifier(pair.getKey(), byteBuf);
            pair.getValue().getDataSource().encodeInstance(pair.getValue(),byteBuf,access);
        });
        ByteBufHelpers.encodeCollection(patch.toRemoveDataSources(), buf, ByteBufHelpers::encodeIdentifier);

        ByteBufHelpers.encodeCollection(patch.dirtyStats(), buf, StatInstance::encode);
    }

    public static OriginSourcePatch decode(ByteBuf buf, RegistryAccess access){

        return new OriginSourcePatch(
                ByteBufHelpers.decodeArray(buf, byteBuf -> {
                    Identifier identifier = ByteBufHelpers.decodeIdentifier(byteBuf);
                    DataSourceInstance data = DataSource.getInstance(identifier).loadInstance(byteBuf,access);
                    return new Pair<>(identifier, data);
                }).stream().collect(Collectors.toMap(
                        Pair::getFirst,
                        Pair::getSecond
                )),
                ByteBufHelpers.decodeArray(buf, ByteBufHelpers::decodeIdentifier),
                ByteBufHelpers.decodeArray(buf, StatInstance::decode)
        );
    }
}
