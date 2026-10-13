package net.zic.ascension.api.ascension.core.tribulation;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;

//what an entity tracking tribulation does when its target changes dimension
public enum DimensionChangeBehaviour implements StringRepresentable {
    //tribulation stays where it is, and will act as if the target is not loaded
    STAY("stay"),
    //tribulation moves with the target
    FOLLOW("follow"),
    //tribulation fails
    FAIL("fail");

    public static final Codec<DimensionChangeBehaviour> CODEC = StringRepresentable.fromEnum(DimensionChangeBehaviour::values);

    private final String name;

    DimensionChangeBehaviour(String name) {
        this.name = name;
    }

    @Override
    public String getSerializedName() {
        return name;
    }
}
