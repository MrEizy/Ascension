package net.thejadeproject.ascension.refactor_packages.bloodlines.generic;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.thejadeproject.ascension.refactor_packages.bloodlines.IBloodlineData;

public class GenericBloodlineData implements IBloodlineData {

    private static final String VERSION = "1";

    @Override
    public String getBloodlineVersion() {
        return VERSION;
    }

    @Override
    public CompoundTag write() {
        CompoundTag tag = new CompoundTag();
        tag.putString("version", VERSION);
        return tag;
    }

    @Override
    public void encode(RegistryFriendlyByteBuf buf) {
        buf.writeUtf(VERSION);
    }
}
