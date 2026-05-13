package net.thejadeproject.ascension.refactor_packages.network.server_bound.herb_pouch;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.thejadeproject.ascension.AscensionCraft;
import net.thejadeproject.ascension.common.items.data_components.ModDataComponents;
import net.thejadeproject.ascension.common.items.data_components.herb_pouch.HerbPouchComponent;
import net.thejadeproject.ascension.menus.custom.herb_pouch.HerbPouchMenu;
import net.thejadeproject.ascension.refactor_packages.network.client_bound.herb_pouch.SyncHerbPouchPayload;

public record InsertCarriedHerbIntoPouchPayload() implements CustomPacketPayload {
    public static final Type<InsertCarriedHerbIntoPouchPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "insert_carried_herb_into_pouch"));

    public static final StreamCodec<FriendlyByteBuf, InsertCarriedHerbIntoPouchPayload> STREAM_CODEC =
            StreamCodec.of(
                    (buf, payload) -> {},
                    buf -> new InsertCarriedHerbIntoPouchPayload()
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handlePayload(InsertCarriedHerbIntoPouchPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;
            if (!(player.containerMenu instanceof HerbPouchMenu menu)) return;

            boolean inserted = menu.insertCarriedStack();
            if (!inserted) return;

            HerbPouchComponent component = menu.getPouchStack().get(ModDataComponents.HERB_POUCH_DATA.get());
            if (component == null) return;

            PacketDistributor.sendToPlayer(player, new SyncHerbPouchPayload(component));
        });
    }
}