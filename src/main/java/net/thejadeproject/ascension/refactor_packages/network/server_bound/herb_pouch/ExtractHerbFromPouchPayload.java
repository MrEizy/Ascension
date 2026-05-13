package net.thejadeproject.ascension.refactor_packages.network.server_bound.herb_pouch;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.thejadeproject.ascension.AscensionCraft;
import net.thejadeproject.ascension.common.items.data_components.ModDataComponents;
import net.thejadeproject.ascension.common.items.data_components.herb_pouch.HerbPouchComponent;
import net.thejadeproject.ascension.menus.custom.herb_pouch.HerbPouchMenu;
import net.thejadeproject.ascension.refactor_packages.network.client_bound.herb_pouch.SyncHerbPouchPayload;

import java.util.List;

public record ExtractHerbFromPouchPayload(int summaryIndex, boolean extractAll) implements CustomPacketPayload {
    public static final Type<ExtractHerbFromPouchPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "extract_herb_from_pouch"));

    public static final StreamCodec<FriendlyByteBuf, ExtractHerbFromPouchPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.INT,
                    ExtractHerbFromPouchPayload::summaryIndex,
                    ByteBufCodecs.BOOL,
                    ExtractHerbFromPouchPayload::extractAll,
                    ExtractHerbFromPouchPayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handlePayload(ExtractHerbFromPouchPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;
            if (!(player.containerMenu instanceof HerbPouchMenu menu)) return;

            ItemStack pouchStack = menu.getPouchStack();
            HerbPouchComponent component = pouchStack.get(ModDataComponents.HERB_POUCH_DATA.get());
            if (component == null) return;

            List<ItemStack> summaries = component.getSummaryStacks();
            int index = payload.summaryIndex();

            if (index < 0 || index >= summaries.size()) return;

            ItemStack clickedSummary = summaries.get(index);

            if (payload.extractAll()) {
                HerbPouchComponent.ExtractManyResult result = component.extractAllByItem(clickedSummary);
                if (result.extracted().isEmpty()) return;

                pouchStack.set(ModDataComponents.HERB_POUCH_DATA.get(), result.component());
                menu.setClientPouchData(result.component());
                PacketDistributor.sendToPlayer(player, new SyncHerbPouchPayload(result.component()));

                for (ItemStack extracted : result.extracted()) {
                    boolean added = player.getInventory().add(extracted);
                    if (!added) player.drop(extracted, false);
                }

                menu.broadcastChanges();
                return;
            }

            HerbPouchComponent.ExtractResult result = component.extractOneByItem(clickedSummary);
            if (result.extracted().isEmpty()) return;

            pouchStack.set(ModDataComponents.HERB_POUCH_DATA.get(), result.component());
            menu.setClientPouchData(result.component());
            PacketDistributor.sendToPlayer(player, new SyncHerbPouchPayload(result.component()));

            boolean added = player.getInventory().add(result.extracted());
            if (!added) player.drop(result.extracted(), false);

            menu.broadcastChanges();
        });
    }
}