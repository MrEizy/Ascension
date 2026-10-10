package net.zic.ascension.api.ascension.core.skill.data_source;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.Identifier;
import net.zic.ascension.api.ascension.core.CoreRegistries;
import net.zic.ascension.api.ascension.core.skill.Skill;
import net.zic.ascension.api.ascension.core.skill.SkillData;
import net.zic.ascension.api.rpg_engine.source.data_source.util.SyncHandler;
import net.zic.zenithlib.network.ByteBufHelpers;

public class SkillSyncHandlers {
    public static class FullPatchSyncHandler implements SyncHandler<SkillHolder> {
        @Override
        public void decode(SkillHolder existingEncodable, ByteBuf buf, RegistryAccess access) {
            existingEncodable.skills.clear();
            int size = buf.readInt();
            for(int i = 0;i<size;i++){
                Identifier skillId = ByteBufHelpers.decodeIdentifier(buf);
                Skill skill = CoreRegistries.safeAccess(CoreRegistries.SKILL_REGISTRY,skillId,access);
                SkillData data = skill.loadData(buf);
                existingEncodable.skills.put(skillId,data);
            }
            existingEncodable.dirtySkills.clear();
            existingEncodable.toRemoveSkills.clear();
        }

        @Override
        public void encode(SkillHolder encodable, ByteBuf buf, RegistryAccess access) {
            buf.writeInt(encodable.skills.size());
            for(Identifier skill : encodable.skills.keySet()){
                ByteBufHelpers.encodeIdentifier(skill,buf);
                encodable.getSkillData(skill).encode(buf,access);
            }
            encodable.dirtySkills.clear();
            encodable.toRemoveSkills.clear();
        }
    }
    public static class PartialPatchSyncHandler implements SyncHandler<SkillHolder> {
        @Override
        public void decode(SkillHolder existingEncodable, ByteBuf buf, RegistryAccess access) {
            int size = buf.readInt();
            for(int i = 0;i<size;i++){
                Identifier skillId = ByteBufHelpers.decodeIdentifier(buf);
                Skill skill = CoreRegistries.safeAccess(CoreRegistries.SKILL_REGISTRY,skillId,access);
                SkillData data = skill.loadData(buf);
                existingEncodable.skills.put(skillId,data);
            }
            ByteBufHelpers.decodeArray(buf,ByteBufHelpers::decodeIdentifier).forEach(existingEncodable.skills::remove);
            existingEncodable.dirtySkills.clear();
            existingEncodable.toRemoveSkills.clear();
        }

        @Override
        public void encode(SkillHolder encodable, ByteBuf buf, RegistryAccess access) {
            buf.writeInt(encodable.dirtySkills.size());
            for(Identifier dirtySkill : encodable.dirtySkills){
                ByteBufHelpers.encodeIdentifier(dirtySkill,buf);
                encodable.getSkillData(dirtySkill).encode(buf,access);
            }
            ByteBufHelpers.encodeCollection(encodable.toRemoveSkills,buf,ByteBufHelpers::encodeIdentifier);
            encodable.dirtySkills.clear();
            encodable.toRemoveSkills.clear();
        }
    }
}
