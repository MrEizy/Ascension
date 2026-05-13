package net.thejadeproject.ascension.refactor_packages.network.client_bound.entity_data.techniques;

import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.thejadeproject.ascension.AscensionCraft;
import net.thejadeproject.ascension.refactor_packages.gui.screens.TechniqueMergeScreen;
import net.thejadeproject.ascension.refactor_packages.util.ByteBufUtil;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public record ShowMergePromptPayload(List<Set<ResourceLocation>> eligibleMerges) implements CustomPacketPayload {

    public static final Type<ShowMergePromptPayload> TYPE =
        new Type<>(ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "show_merge_prompt"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ShowMergePromptPayload> STREAM_CODEC =
        StreamCodec.of(ShowMergePromptPayload::encode, ShowMergePromptPayload::decode);

    public static void encode(RegistryFriendlyByteBuf buf, ShowMergePromptPayload packet) {
        buf.writeInt(packet.eligibleMerges.size());
        for (Set<ResourceLocation> merge : packet.eligibleMerges) {
            buf.writeInt(merge.size());
            for (ResourceLocation id : merge) {
                ByteBufUtil.encodeString(buf, id.toString());
            }
        }
    }

    public static ShowMergePromptPayload decode(RegistryFriendlyByteBuf buf) {
        int mergeCount = buf.readInt();
        List<Set<ResourceLocation>> merges = new ArrayList<>();
        for (int i = 0; i < mergeCount; i++) {
            int setSize = buf.readInt();
            Set<ResourceLocation> set = new HashSet<>();
            for (int j = 0; j < setSize; j++) {
                set.add(ByteBufUtil.readResourceLocation(buf));
            }
            merges.add(set);
        }
        return new ShowMergePromptPayload(merges);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handlePayload(ShowMergePromptPayload payload, IPayloadContext context) {
        context.enqueueWork(()->
                open(payload.eligibleMerges));
    }

    @OnlyIn(Dist.CLIENT)
    public static void open(List<Set<ResourceLocation>> eligibleMerges){
        if(FMLEnvironment.dist == Dist.CLIENT){
            Minecraft.getInstance().setScreen(new TechniqueMergeScreen(eligibleMerges));
        }
    }
}
