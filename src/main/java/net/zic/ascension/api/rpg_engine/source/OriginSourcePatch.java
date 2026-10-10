package net.zic.ascension.api.rpg_engine.source;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.Identifier;
import net.zic.ascension.api.rpg_engine.source.data_source.DataSourceHolder;
import net.zic.ascension.api.rpg_engine.source.data_source.DataSourceInstance;
import net.zic.zenithlib.network.ByteBufHelpers;
import net.zic.zenithlib.value_containers.typed.ValueContainer;

import java.util.Collection;
import java.util.Map;

public record OriginSourcePatch(
        Map<Identifier, DataSourceHolder<? extends DataSourceInstance>> dirtyDataSources,
        Collection<Identifier> toRemoveDataSources,
        Collection<ValueContainer<Double>> dirtyStats){


    private static void encode(OriginSourcePatch patch,ByteBuf buf,RegistryAccess access,boolean fullPatch){
        buf.writeBoolean(fullPatch);
        ByteBufHelpers.encodeCollection(patch.dirtyDataSources().entrySet(), buf, (pair, byteBuf) -> {
            buf.writeBoolean(pair.getValue().dataSource().syncHandler(fullPatch) != null);
            if(pair.getValue().dataSource().syncHandler(fullPatch) == null) return;
            ByteBufHelpers.encodeIdentifier(pair.getKey(), byteBuf);
            pair.getValue().encode(byteBuf,access,fullPatch);
        });
        ByteBufHelpers.encodeCollection(patch.toRemoveDataSources(), buf, ByteBufHelpers::encodeIdentifier);

        ByteBufHelpers.encodeCollection(patch.dirtyStats(), buf, (container,byteBuf)->
                ValueContainer.encode(container,byteBuf, Codec.DOUBLE)
        );
    }

    public static void fullEncode(OriginSourcePatch patch,ByteBuf buf,RegistryAccess access){
        encode(patch,buf,access,true);
    }

    public static void encodePatch(OriginSourcePatch patch, ByteBuf buf,RegistryAccess access){
        encode(patch,buf,access,false);
    }

}
