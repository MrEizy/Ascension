package net.zic.ascension.api.ascension.core.path.realm;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;

/**
 * how a realm's breakthrough tribulation is started
 */
public enum TribulationMode implements StringRepresentable {
    //started the moment the realm's progress is full
    AUTO("auto"),
    //the path waits at full progress until an external source (e.g. a skill) triggers the tribulation
    MANUAL("manual");

    public static final Codec<TribulationMode> CODEC = StringRepresentable.fromEnum(TribulationMode::values);

    private final String serializedName;

    TribulationMode(String serializedName) {
        this.serializedName = serializedName;
    }

    @Override
    public String getSerializedName() {
        return serializedName;
    }
}
