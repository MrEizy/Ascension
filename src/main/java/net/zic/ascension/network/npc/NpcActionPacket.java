package net.zic.ascension.network.npc;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.common.entities.npc.CultivatorNpcEntity;

import java.util.UUID;

public record NpcActionPacket(int entityId, UUID uuid, Action action) implements CustomPacketPayload {
    public enum Action { TALK, CHALLENGE, MORTAL_CONFRONTATION, WITHDRAW, GUIDANCE, GIFT, INSULT }
    public static final Type<NpcActionPacket> TYPE = new Type<>(AscensionCraft.prefix("npc_action"));
    public static final StreamCodec<FriendlyByteBuf, NpcActionPacket> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public NpcActionPacket decode(FriendlyByteBuf buf) {
            return new NpcActionPacket(buf.readVarInt(), buf.readUUID(), buf.readEnum(Action.class));
        }
        @Override
        public void encode(FriendlyByteBuf buf, NpcActionPacket packet) {
            buf.writeVarInt(packet.entityId());
            buf.writeUUID(packet.uuid());
            buf.writeEnum(packet.action());
        }
    };
    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }
    public static void handle(NpcActionPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)
                    || !(player.level().getEntity(packet.entityId()) instanceof CultivatorNpcEntity npc)
                    || !npc.getUUID().equals(packet.uuid()) || !npc.isAlive()
                    || npc.distanceToSqr(player) > 6 * 6 || player.isSpectator()) return;
            switch (packet.action()) {
                case TALK -> npc.speak(player);
                case CHALLENGE -> npc.challenge(player);
                case MORTAL_CONFRONTATION -> npc.mortalConfrontation(player);
                case WITHDRAW -> npc.withdraw(player);
                case GUIDANCE -> npc.guide(player);
                case GIFT -> npc.gift(player);
                case INSULT -> npc.insult(player);
            }
        });
    }
}
