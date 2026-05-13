package net.thejadeproject.ascension.refactor_packages.network.client_bound.toast;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.thejadeproject.ascension.AscensionCraft;
import net.thejadeproject.ascension.clients.toast.ClientToastPayloadHandler;

public record ShowAscensionToast(
        String title,
        String message,
        ItemStack icon,
        ResourceLocation background
) implements CustomPacketPayload {

    public static final Type<ShowAscensionToast> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "show_ascension_toast"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ShowAscensionToast> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.STRING_UTF8,
                    ShowAscensionToast::title,

                    ByteBufCodecs.STRING_UTF8,
                    ShowAscensionToast::message,

                    ItemStack.OPTIONAL_STREAM_CODEC,
                    ShowAscensionToast::icon,

                    ResourceLocation.STREAM_CODEC,
                    ShowAscensionToast::background,

                    ShowAscensionToast::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handlePayload(ShowAscensionToast payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (FMLEnvironment.dist == Dist.CLIENT) {
                ClientToastPayloadHandler.handle(payload);
            }
        });
    }
}