package net.zic.ascension.network.npc;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.client.gui.NpcDialogueScreen;

import java.util.UUID;

public record NpcDialoguePacket(int entityId, UUID uuid, String name, String message, boolean challenged,
                                boolean guidance, boolean enlightened, String personality, String standing, int respect, String affiliation, int reputation, String giftHint, int talks, boolean giftUnlocked, boolean insultUnlocked,
                                boolean challengeUnlocked, boolean guidanceUnlocked, boolean mortalAvailable,
                                boolean mortalActive, int wins, int losses, String realm) implements CustomPacketPayload {
    public static final Type<NpcDialoguePacket> TYPE = new Type<>(AscensionCraft.prefix("npc_dialogue"));
    public static final StreamCodec<FriendlyByteBuf, NpcDialoguePacket> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public NpcDialoguePacket decode(FriendlyByteBuf buf) {
            return new NpcDialoguePacket(buf.readVarInt(), buf.readUUID(), buf.readUtf(128),
                    buf.readUtf(512), buf.readBoolean(), buf.readBoolean(), buf.readBoolean(), buf.readUtf(48), buf.readUtf(24),
                    buf.readVarInt(), buf.readUtf(24), buf.readVarInt(), buf.readUtf(256),
                    buf.readVarInt(), buf.readBoolean(), buf.readBoolean(), buf.readBoolean(), buf.readBoolean(),
                    buf.readBoolean(), buf.readBoolean(), buf.readVarInt(), buf.readVarInt(), buf.readUtf(128));
        }
        @Override
        public void encode(FriendlyByteBuf buf, NpcDialoguePacket packet) {
            buf.writeVarInt(packet.entityId());
            buf.writeUUID(packet.uuid());
            buf.writeUtf(packet.name(), 128);
            buf.writeUtf(packet.message(), 512);
            buf.writeBoolean(packet.challenged());
            buf.writeBoolean(packet.guidance());
            buf.writeBoolean(packet.enlightened());
            buf.writeUtf(packet.personality(), 48);
            buf.writeUtf(packet.standing(), 24);
            buf.writeVarInt(packet.respect());
            buf.writeUtf(packet.affiliation(), 24);
            buf.writeVarInt(packet.reputation());
            buf.writeUtf(packet.giftHint(), 256);
            buf.writeVarInt(packet.talks());
            buf.writeBoolean(packet.giftUnlocked());
            buf.writeBoolean(packet.insultUnlocked());
            buf.writeBoolean(packet.challengeUnlocked());
            buf.writeBoolean(packet.guidanceUnlocked());
            buf.writeBoolean(packet.mortalAvailable());
            buf.writeBoolean(packet.mortalActive());
            buf.writeVarInt(packet.wins());
            buf.writeVarInt(packet.losses());
            buf.writeUtf(packet.realm(), 128);
        }
    };
    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }
    public static void handle(NpcDialoguePacket packet, IPayloadContext context) {
        context.enqueueWork(() -> NpcDialogueScreen.open(packet));
    }
}
