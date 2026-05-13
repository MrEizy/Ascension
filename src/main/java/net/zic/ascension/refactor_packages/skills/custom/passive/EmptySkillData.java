package net.zic.ascension.refactor_packages.skills.custom.passive;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.zic.ascension.refactor_packages.skills.IPersistentSkillData;

public class EmptySkillData implements IPersistentSkillData {

    @Override
    public CompoundTag write() {
        return new CompoundTag();
    }

    @Override
    public void encode(RegistryFriendlyByteBuf buf) {

    }

    @Override
    public IPersistentSkillData copy() {
        return null;
    }

    @Override
    public IPersistentSkillData merge(IPersistentSkillData other) {
        return null;
    }
}