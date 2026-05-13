package net.zic.ascension.refactor_packages.skills.custom.passive;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.zic.ascension.refactor_packages.entity_data.IEntityData;
import net.zic.ascension.refactor_packages.skills.IPersistentSkillData;
import net.zic.ascension.refactor_packages.skills.custom.passive.debuff.skill_data.DebuffSkillData;

public abstract class SimpleDebuffSkill extends SimplePassiveSkill {

    @Override
    public IPersistentSkillData freshPersistentData(IEntityData entityData) {
        return DebuffSkillData.permanent();
    }

    @Override
    public IPersistentSkillData fromCompound(CompoundTag tag, IEntityData entityData) {
        return DebuffSkillData.read(tag);
    }

    @Override
    public IPersistentSkillData fromNetwork(RegistryFriendlyByteBuf buf) {
        return DebuffSkillData.decode(buf);
    }
}