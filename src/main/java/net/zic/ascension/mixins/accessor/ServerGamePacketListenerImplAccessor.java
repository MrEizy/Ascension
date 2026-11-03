package net.zic.ascension.mixins.accessor;

import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Lets Qi Steps reset the server's count of ticks a player has floated, so running on qi plates isn't kicked as flying.
 */
@Mixin(ServerGamePacketListenerImpl.class)
public interface ServerGamePacketListenerImplAccessor {
    @Accessor("aboveGroundTickCount")
    void ascension$setAboveGroundTickCount(int ticks);
}
