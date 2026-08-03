package net.zic.ascension.api.ascension.datapack;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public final class CodecHelpers {
    public static final Codec<Vec3> VEC3 = Codec.DOUBLE.listOf().comapFlatMap(
            values -> values.size() == 3
                    ? DataResult.success(new Vec3(values.get(0), values.get(1), values.get(2)))
                    : DataResult.error(() -> "Expected three vector values"),
            value -> List.of(value.x, value.y, value.z)
    );

    private CodecHelpers() {
    }
}
