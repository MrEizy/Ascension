package net.thejadeproject.ascension.clients.toast;

import net.minecraft.network.chat.Component;
import net.thejadeproject.ascension.refactor_packages.network.client_bound.toast.ShowAscensionToast;

public final class ClientToastPayloadHandler {

    private ClientToastPayloadHandler() {}

    public static void handle(ShowAscensionToast payload) {
        AscensionToastHelper.show(
                Component.literal(payload.title()),
                Component.literal(payload.message()),
                payload.icon(),
                payload.background()
        );
    }
}