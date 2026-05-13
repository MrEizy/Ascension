package net.thejadeproject.ascension.refactor_packages.network.client_bound.herb_pouch;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.thejadeproject.ascension.AscensionCraft;
import net.thejadeproject.ascension.common.items.data_components.herb_pouch.HerbPouchComponent;
import net.thejadeproject.ascension.menus.custom.herb_pouch.HerbPouchMenu;

public record SyncHerbPouchPayload(HerbPouchComponent component) implements CustomPacketPayload {
    public static final Type<SyncHerbPouchPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "sync_herb_pouch"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncHerbPouchPayload> STREAM_CODEC =
            StreamCodec.of(
                    (buf, payload) -> HerbPouchComponent.encode(buf, payload.component()),
                    buf -> new SyncHerbPouchPayload(HerbPouchComponent.decode(buf))
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handlePayload(SyncHerbPouchPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player().containerMenu instanceof HerbPouchMenu menu)) return;
            menu.setClientPouchData(payload.component());
        });
    }
}