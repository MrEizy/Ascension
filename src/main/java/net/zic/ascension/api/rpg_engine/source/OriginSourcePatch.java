package net.zic.ascension.api.rpg_engine.source;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.Identifier;
import net.zic.ascension.api.rpg_engine.source.data_source.DataSourceInstance;
import net.zic.zenithlib.network.ByteBufHelpers;
import net.zic.zenithlib.stats.StatInstance;

import java.util.Collection;
import java.util.Map;

public record OriginSourcePatch(
        Map<Identifier, DataSourceInstance> dirtyDataSources,
        Collection<Identifier> toRemoveDataSources,
        Collection<StatInstance> dirtyStats){


    protected static void encode(OriginSourcePatch patch,ByteBuf buf,RegistryAccess access,boolean fullPatch){
        ByteBufHelpers.encodeCollection(patch.dirtyDataSources().entrySet(), buf, (pair, byteBuf) -> {
            ByteBufHelpers.encodeIdentifier(pair.getKey(), byteBuf);
            pair.getValue().getDataSource().encodeInstance(pair.getValue(),byteBuf,access,fullPatch);
        });
        ByteBufHelpers.encodeCollection(patch.toRemoveDataSources(), buf, ByteBufHelpers::encodeIdentifier);

        ByteBufHelpers.encodeCollection(patch.dirtyStats(), buf, StatInstance::encode);
    }

    public static void fullEncode(OriginSourcePatch patch,ByteBuf buf,RegistryAccess access){
        encode(patch,buf,access,true);
    }

    public static void encodePatch(OriginSourcePatch patch, ByteBuf buf,RegistryAccess access){
        encode(patch,buf,access,false);
    }

}
