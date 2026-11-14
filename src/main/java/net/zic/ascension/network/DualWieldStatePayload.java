//package net.zic.ascension.network;
//
//import net.minecraft.network.RegistryFriendlyByteBuf;
//import net.minecraft.network.codec.StreamCodec;
//import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
//import net.neoforged.neoforge.network.handling.IPayloadContext;
//import net.zic.ascension.AscensionCraft;
//import net.zic.ascension.client.combat.DualWieldClientState;
//
//public record DualWieldStatePayload(boolean active) implements CustomPacketPayload {
//    public static final Type<DualWieldStatePayload> TYPE = new Type<>(AscensionCraft.prefix("dual_wield_state"));
//    public static final StreamCodec<RegistryFriendlyByteBuf, DualWieldStatePayload> CODEC = StreamCodec.of(
//            (buf, payload) -> buf.writeBoolean(payload.active),
//            buf -> new DualWieldStatePayload(buf.readBoolean())
//    );
//
//    @Override
//    public Type<DualWieldStatePayload> type() {
//        return TYPE;
//    }
//
//    public static void handle(DualWieldStatePayload payload, IPayloadContext context) {
//        context.enqueueWork(() -> DualWieldClientState.set(payload.active()));
//    }
//}