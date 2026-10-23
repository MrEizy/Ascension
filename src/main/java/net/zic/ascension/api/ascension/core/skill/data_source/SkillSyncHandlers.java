package net.zic.ascension.api.ascension.core.skill.data_source;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.RegistryAccess;
import net.zic.ascension.api.rpg_engine.source.data_source.util.SyncHandler;

public class SkillSyncHandlers {
    //syncs every skill
    public static class FullPatchSyncHandler implements SyncHandler<SkillHolder> {
        @Override
        public void decode(SkillHolder existingEncodable, ByteBuf buf, RegistryAccess access) {
            existingEncodable.decodeFull(buf, access);
        }

        @Override
        public void encode(SkillHolder encodable, ByteBuf buf, RegistryAccess access) {
            encodable.encodeFull(buf, access);
        }
    }

    //syncs only the dirty and removed skills
    public static class PartialPatchSyncHandler implements SyncHandler<SkillHolder> {
        @Override
        public void decode(SkillHolder existingEncodable, ByteBuf buf, RegistryAccess access) {
            existingEncodable.decodePartial(buf, access);
        }

        @Override
        public void encode(SkillHolder encodable, ByteBuf buf, RegistryAccess access) {
            encodable.encodePartial(buf, access);
        }
    }
}
