package net.thejadeproject.ascension.network.serverBound;

import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.thejadeproject.ascension.AscensionCraft;

import java.util.Map;

public record UnlockChapterPayload(String entryId) implements CustomPacketPayload {

    public static final Type<UnlockChapterPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "unlock_chapter")
    );

    public static final StreamCodec<FriendlyByteBuf, UnlockChapterPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8,
            UnlockChapterPayload::entryId,
            UnlockChapterPayload::new
    );

    private static final Map<String, ResourceLocation> ENTRY_ADVANCEMENTS = Map.of(
            "ch1_1", ResourceLocation.fromNamespaceAndPath("ascension", "journal/read_ch1_1"),
            "ch1_2", ResourceLocation.fromNamespaceAndPath("ascension", "journal/read_ch1_2"),
            "ch1_3", ResourceLocation.fromNamespaceAndPath("ascension", "journal/read_ch1_3")
    );

    @Override
    public @javax.annotation.Nonnull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handlePayload(UnlockChapterPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;

            ResourceLocation advancementId = ENTRY_ADVANCEMENTS.get(payload.entryId());
            if (advancementId == null) return;

            if (player.getServer() == null) return;
            AdvancementHolder holder = player.getServer().getAdvancements().get(advancementId);
            if (holder == null) return;

            player.getAdvancements().award(holder, "unlocked");
        });
    }
}
