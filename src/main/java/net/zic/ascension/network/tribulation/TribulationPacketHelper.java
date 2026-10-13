package net.zic.ascension.network.tribulation;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
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

    /**
     * writes encoded tribulation data with its length, so the client can skip it if it fails to decode.
     * does not move the source buffer's position, so the same packet can be sent to multiple players
     */
    static void writeData(FriendlyByteBuf buf, ByteBuf data) {
        buf.writeVarInt(data.readableBytes());
        buf.writeBytes(data, data.readerIndex(), data.readableBytes());
    }

    //copies the data into its own buffer, it is decoded later on the main thread
    static ByteBuf readData(FriendlyByteBuf buf) {
        int length = buf.readVarInt();
        ByteBuf data = Unpooled.buffer(length);
        buf.readBytes(data, length);
        return data;
    }
}
