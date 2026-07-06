package net.zic.ascension.network;

import net.minecraft.core.Holder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.capabilities.AscensionEntityDataHolder;
import net.zic.ascension.api.capabilities.CoreCapabilities;
import net.zic.ascension.api.core.entity.AscensionEntityData;
import net.zic.ascension.common.data_attachements.AscensionAttachments;
import net.zic.ascension.impl.core.entity.SimpleAscensionEntityData;
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

            AscensionEntityDataHolder holder = player.getCapability(
                    CoreCapabilities.ASCENSION_ENTITY_DATA_HOLDER_CAPABILITY
            );

            if (holder == null) {
                return;
            }

            Holder<Attribute> attribute = SimpleAscensionEntityData
                    .getSuppressibleAttribute(packet.attribute())
                    .orElse(null);

            if (attribute == null) {
                return;
            }

            AscensionEntityData data = holder.getData(player);


            data.setAttributeSuppression(attribute, packet.percentage());
            data.applyAttributeSuppression(attribute);

            player.syncData(AscensionAttachments.SIMPLE_ENTITY_DATA);

        });
    }
}