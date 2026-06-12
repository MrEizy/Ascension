package net.zic.ascension.client.tooltip;


public final class AscensionClientTooltipProviders {

    private static boolean registered;

    private AscensionClientTooltipProviders() {}

    public static void registerAll() {
        if (registered) {
            return;
        }

        registered = true;

        AscensionTransferItemTooltipProvider.register();
        AscensionTabletTooltipProvider.register();

    }
}