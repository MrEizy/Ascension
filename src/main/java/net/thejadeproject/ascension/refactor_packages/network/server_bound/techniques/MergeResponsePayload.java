package net.thejadeproject.ascension.refactor_packages.network.server_bound.techniques;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.thejadeproject.ascension.AscensionCraft;
import net.thejadeproject.ascension.refactor_packages.techniques.merge.TechniqueMergeHandler;
import net.thejadeproject.ascension.refactor_packages.util.ByteBufUtil;

import java.util.HashSet;
import java.util.Set;

public record MergeResponsePayload(Set<ResourceLocation> selectedTechniques) implements CustomPacketPayload {

    public static final Type<MergeResponsePayload> TYPE =
        new Type<>(ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "merge_response"));
    public static final StreamCodec<RegistryFriendlyByteBuf, MergeResponsePayload> STREAM_CODEC =
        StreamCodec.of(MergeResponsePayload::encode, MergeResponsePayload::decode);

    public static void encode(RegistryFriendlyByteBuf buf, MergeResponsePayload packet) {
        buf.writeInt(packet.selectedTechniques.size());
        for (ResourceLocation id : packet.selectedTechniques) {
            ByteBufUtil.encodeString(buf, id.toString());
        }
    }

    public static MergeResponsePayload decode(RegistryFriendlyByteBuf buf) {
        int size = buf.readInt();
        Set<ResourceLocation> set = new HashSet<>();
        for (int i = 0; i < size; i++) {
            set.add(ByteBufUtil.readResourceLocation(buf));
        }
        return new MergeResponsePayload(set);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handlePayload(MergeResponsePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer serverPlayer)) return;
            ResourceLocation result = TechniqueMergeHandler.getMergeResult(payload.selectedTechniques);
            if (result != null) TechniqueMergeHandler.applyMerge(serverPlayer, result);
        });
    }
}
