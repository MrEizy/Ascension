package net.zic.ascension.api.ascension.core.technique;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.Identifier;
import net.zic.ascension.api.rpg_engine.source.data_source.util.SyncHandler;
import net.zic.zenithlib.network.ByteBufHelpers;

public abstract class TechniqueSyncHandlers {
    public static class FullPatchSyncHandler implements SyncHandler<TechniqueHolder> {
        @Override
        public void decode(TechniqueHolder existingEncodable, ByteBuf buf, RegistryAccess access) {
            existingEncodable.techniques.clear();
            int size = buf.readInt();
            for (int index = 0; index < size; index++) {
                Identifier techniqueId = ByteBufHelpers.decodeIdentifier(buf);
                Technique technique = existingEncodable.getTechnique(techniqueId, access);
                if (technique == null) {
                    throw new IllegalStateException("unknown technique " + techniqueId);
                }
                existingEncodable.techniques.put(techniqueId, technique.loadData(buf));
            }
            existingEncodable.dirtyTechniques.clear();
            existingEncodable.toRemoveTechniques.clear();
        }

        @Override
        public void encode(TechniqueHolder encodable, ByteBuf buf, RegistryAccess access) {
            buf.writeInt(encodable.techniques.size());
            for (Identifier techniqueId : encodable.techniques.keySet()) {
                ByteBufHelpers.encodeIdentifier(techniqueId, buf);
                encodable.getTechniqueData(techniqueId).encode(buf);
            }
            encodable.dirtyTechniques.clear();
            encodable.toRemoveTechniques.clear();
        }
    }
    public static class PartialPatchSyncHandler implements SyncHandler<TechniqueHolder> {
        @Override
        public void decode(TechniqueHolder existingEncodable, ByteBuf buf, RegistryAccess access) {
            int size = buf.readInt();
            for (int index = 0; index < size; index++) {
                Identifier techniqueId = ByteBufHelpers.decodeIdentifier(buf);
                Technique technique = existingEncodable.getTechnique(techniqueId, access);
                if (technique == null) {
                    throw new IllegalStateException("unknown technique " + techniqueId);
                }
                existingEncodable.techniques.put(techniqueId, technique.loadData(buf));
            }
            ByteBufHelpers.decodeArray(buf, ByteBufHelpers::decodeIdentifier)
                    .forEach(existingEncodable.techniques::remove);
            existingEncodable.dirtyTechniques.clear();
            existingEncodable.toRemoveTechniques.clear();
        }

        @Override
        public void encode(TechniqueHolder encodable, ByteBuf buf, RegistryAccess access) {
            buf.writeInt(encodable.dirtyTechniques.size());
            for (Identifier techniqueId : encodable.dirtyTechniques) {
                ByteBufHelpers.encodeIdentifier(techniqueId, buf);
                encodable.getTechniqueData(techniqueId).encode(buf);
            }
            ByteBufHelpers.encodeCollection(encodable.toRemoveTechniques, buf, ByteBufHelpers::encodeIdentifier);
            encodable.dirtyTechniques.clear();
            encodable.toRemoveTechniques.clear();
        }
    }
}
