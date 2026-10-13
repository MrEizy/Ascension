package net.zic.ascension.api.ascension.core.tribulation;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;

/**
 * encodes and decodes TribulationData for syncing to clients.
 * buffers are unpooled heap buffers, so they do not need to be released
 */
public final class TribulationSyncHelper {
    private TribulationSyncHelper() {
    }

    public static <T extends TribulationData> ByteBuf encode(Tribulation<T> tribulation, T data, RegistryAccess access) {
        RegistryFriendlyByteBuf buf = new RegistryFriendlyByteBuf(Unpooled.buffer(), access);
        tribulation.getType().dataStreamCodec().encode(buf, data);
        return buf;
    }

    //reads from a duplicate, so the source buffer can be decoded again
    public static <T extends TribulationData> T decode(Tribulation<T> tribulation, ByteBuf data, RegistryAccess access) {
        return tribulation.getType().dataStreamCodec().decode(new RegistryFriendlyByteBuf(data.duplicate(), access));
    }
}
