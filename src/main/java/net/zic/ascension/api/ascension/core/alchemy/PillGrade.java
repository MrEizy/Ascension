package net.zic.ascension.api.ascension.core.alchemy;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;

public enum PillGrade implements StringRepresentable {
    LOW("low"),
    MID("mid"),
    HIGH("high"),
    PEAK("peak"),
    SUPREME("supreme");

    public static final Codec<PillGrade> CODEC = StringRepresentable.fromEnum(PillGrade::values);

    private final String serializedName;

    PillGrade(String serializedName) {
        this.serializedName = serializedName;
    }

    @Override
    public String getSerializedName() {
        return serializedName;
    }

    public String translationKey() {
        return "ascension.pill.grade." + serializedName;
    }

    public int step() {
        return ordinal();
    }

    public static PillGrade fromQuality(double quality) {
        double resolved = Double.isFinite(quality) ? Math.max(0.0D, Math.min(1.0D, quality)) : 0.0D;
        if (resolved >= 0.95D) return SUPREME;
        if (resolved >= 0.80D) return PEAK;
        if (resolved >= 0.65D) return HIGH;
        if (resolved >= 0.50D) return MID;
        return LOW;
    }
}
