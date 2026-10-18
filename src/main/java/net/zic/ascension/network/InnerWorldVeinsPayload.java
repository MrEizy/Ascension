package net.zic.ascension.network;

import net.minecraft.core.BlockPos;
import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.client.innerworld.InnerWorldVeinClientState;

import java.util.ArrayList;
import java.util.List;

/** Server to client: positions of treasure veins inside a Divine Sense radius. The wave timing itself comes from the existing Divine Sense packet. */
public record InnerWorldVeinsPayload(List<BlockPos> positions) implements CustomPacketPayload {

    public static final Type<InnerWorldVeinsPayload> TYPE = new Type<>(AscensionCraft.prefix("inner_world_veins"));

    public static final StreamCodec<RegistryFriendlyByteBuf, InnerWorldVeinsPayload> CODEC = StreamCodec.of(
            (buf, payload) -> {
                buf.writeVarInt(payload.positions.size());
                for (BlockPos pos : payload.positions) {
                    buf.writeBlockPos(pos);
                }
            },
            buf -> {
                int size = Math.min(buf.readVarInt(), 256);
                List<BlockPos> positions = new ArrayList<>(size);
                for (int i = 0; i < size; i++) {
                    positions.add(buf.readBlockPos());
                }
                return new InnerWorldVeinsPayload(positions);
            }
    );

    @Override
    public Type<InnerWorldVeinsPayload> type() {
        return TYPE;
    }

    public static void handle(InnerWorldVeinsPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (Minecraft.getInstance().level != null) {
                InnerWorldVeinClientState.set(payload.positions());
            }
        });
    }
}