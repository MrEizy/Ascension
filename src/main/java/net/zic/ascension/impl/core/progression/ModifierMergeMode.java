package net.zic.ascension.impl.core.progression;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;

public enum ModifierMergeMode {
    AUTO("auto"),
    KEEP("keep"),
    REPLACE("replace"),
    ACCUMULATE("accumulate");

    public static final Codec<ModifierMergeMode> CODEC = Codec.STRING.comapFlatMap(
            value -> switch (value) {
                case "auto" -> DataResult.success(AUTO);
                case "keep" -> DataResult.success(KEEP);
                case "replace" -> DataResult.success(REPLACE);
                case "accumulate" -> DataResult.success(ACCUMULATE);
                default -> DataResult.error(() -> "Unknown modifier merge mode: " + value);
            },
            ModifierMergeMode::serializedName
    );

    private final String serializedName;

    ModifierMergeMode(String serializedName) {
        this.serializedName = serializedName;
    }

    public String serializedName() {
        return serializedName;
    }

    public ModifierMergeMode resolve(boolean perPurity) {
        return this == AUTO ? (perPurity ? REPLACE : KEEP) : this;
    }
}
