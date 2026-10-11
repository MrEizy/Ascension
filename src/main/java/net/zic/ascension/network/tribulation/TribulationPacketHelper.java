package net.zic.ascension.network.tribulation;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;

final class TribulationPacketHelper {
    private TribulationPacketHelper() {
    }

    static Vec3 readVec3(FriendlyByteBuf buf) {
        return new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble());
    }

    static void writeVec3(FriendlyByteBuf buf, Vec3 value) {
        buf.writeDouble(value.x);
        buf.writeDouble(value.y);
        buf.writeDouble(value.z);
    }
}
