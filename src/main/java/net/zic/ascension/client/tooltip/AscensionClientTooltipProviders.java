package net.zic.ascension.client.tooltip;


import net.zic.ascension.client.tooltip.providers.AscensionTabletTooltipProvider;
import net.zic.ascension.client.tooltip.providers.AscensionTransferItemTooltipProvider;

/**
 * Register narrow (specific item) providers first, before broadening out.
 */

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