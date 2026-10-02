package net.zic.ascension.network;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.zic.ascension.AscensionCraft;
import net.zic.zenithlib.common.ZenithAttachments;
import net.zic.zenithlib.custom_attributes.SuppressedZenithAttribute;
import net.zic.zenithlib.custom_attributes.ZenithAttributeHolder;
import net.zic.zenithlib.network.ByteBufHelpers;

public record UpdateAttributeSuppressionPacket(
        Identifier attribute,
        double percentage
) implements CustomPacketPayload {
    public static final Type<UpdateAttributeSuppressionPacket> TYPE = new Type<>(
            Identifier.fromNamespaceAndPath(AscensionCraft.MOD_ID, "update_attribute_suppression")
    );

    public static final StreamCodec<FriendlyByteBuf, UpdateAttributeSuppressionPacket> STREAM_CODEC =
            new StreamCodec<>() {
                @Override
                public UpdateAttributeSuppressionPacket decode(FriendlyByteBuf buf) {
                    Identifier attribute = ByteBufHelpers.decodeIdentifier(buf);
                    double percentage = buf.readDouble();

                    return new UpdateAttributeSuppressionPacket(attribute, percentage);
                }

                @Override
                public void encode(FriendlyByteBuf buf, UpdateAttributeSuppressionPacket packet) {
                    ByteBufHelpers.encodeIdentifier(packet.attribute(), buf);
                    buf.writeDouble(packet.percentage());
                }
            };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(UpdateAttributeSuppressionPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {

            if (!(context.player() instanceof ServerPlayer player)) {
                return;
            }

            ZenithAttributeHolder holder = player.getData(ZenithAttachments.ATTRIBUTE_HOLDER);

            Holder<Attribute> attributeHolder = BuiltInRegistries.ATTRIBUTE.get(packet.attribute).get();

            holder.setSuppression(attributeHolder,packet.percentage);

            player.syncData(ZenithAttachments.ATTRIBUTE_HOLDER);

        });
    }
}