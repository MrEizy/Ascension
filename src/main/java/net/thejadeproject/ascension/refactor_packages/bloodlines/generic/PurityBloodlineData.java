package net.thejadeproject.ascension.refactor_packages.bloodlines.generic;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.thejadeproject.ascension.refactor_packages.bloodlines.IBloodlineData;

public class PurityBloodlineData implements IBloodlineData {

    private static final String VERSION = "1";
    public static final double MAX_PURITY = 100.0;

    private double purity;

    public PurityBloodlineData(double purity) {
        this.purity = Math.max(0, Math.min(MAX_PURITY, purity));
    }

    public double getPurity() {
        return purity;
    }

    public boolean isMaxPurity() {
        return purity >= MAX_PURITY;
    }

    public boolean addPurity(double amount) {
        if (purity >= MAX_PURITY) return false;
        purity = Math.min(MAX_PURITY, purity + amount);
        return purity >= MAX_PURITY;
    }

    public double getPurityPercent() {
        return (purity / MAX_PURITY) * 100.0;
    }

    @Override
    public String getBloodlineVersion() {
        return VERSION;
    }

    @Override
    public CompoundTag write() {
        CompoundTag tag = new CompoundTag();
        tag.putString("version", VERSION);
        tag.putDouble("purity", purity);
        return tag;
    }

    @Override
    public void encode(RegistryFriendlyByteBuf buf) {
        buf.writeUtf(VERSION);
        buf.writeDouble(purity);
    }

    public static PurityBloodlineData read(CompoundTag tag) {
        return new PurityBloodlineData(tag.getDouble("purity"));
    }

    public static PurityBloodlineData decode(RegistryFriendlyByteBuf buf) {
        buf.readUtf();
        return new PurityBloodlineData(buf.readDouble());
    }
}
