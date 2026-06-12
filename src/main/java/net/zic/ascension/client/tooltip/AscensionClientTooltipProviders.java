package net.zic.ascension.client.tooltip;

import net.zic.ascension.client.tooltip.providers.AscensionTabletTooltipProvider;
import net.zic.ascension.client.tooltip.providers.AscensionTransferItemTooltipProvider;

/** Please register narrow/item-specific providers before broader ones. */
public final class AscensionClientTooltipProviders {
    private static boolean registered;

    private AscensionClientTooltipProviders() {}

    public static void registerAll() {
        if (registered) {
            return;
        }

        registered = true;

        AscensionTooltipValueSources.register();
        AscensionTransferItemTooltipProvider.register();
        AscensionTabletTooltipProvider.register();
    }
}
