package net.zic.ascension.client.tooltip;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.core.CoreRegistries;
import net.zic.ascension.api.core.bloodline.Bloodline;
import net.zic.ascension.api.core.path.Path;
import net.zic.ascension.api.core.technique.Technique;
import net.zic.ascension.common.item.components.AscensionComponents;
import net.zic.zenithlib.tooltip.api.context.ZenithTooltipContext;
import net.zic.zenithlib.tooltip.api.value.ZenithTooltipValue;
import net.zic.zenithlib.tooltip.api.value.ZenithTooltipValueSources;

import java.util.Optional;

/**
 * Registers Ascension-owned values that may be referenced by tooltip JSON.
 */
public final class AscensionTooltipValueSources {
    public static final Identifier TECHNIQUE_PATH = AscensionCraft.prefix("technique_path");
    public static final Identifier TECHNIQUE_MAX_REALM = AscensionCraft.prefix("technique_max_realm");
    public static final Identifier BLOODLINE_PURITY = AscensionCraft.prefix("bloodline_purity");

    private static boolean registered;

    private AscensionTooltipValueSources() {}

    public static void register() {
        if (registered) {
            return;
        }

        registered = true;

        ZenithTooltipValueSources.register(TECHNIQUE_PATH, AscensionTooltipValueSources::techniquePath);
        ZenithTooltipValueSources.register(TECHNIQUE_MAX_REALM, AscensionTooltipValueSources::techniqueMaxRealm);
        ZenithTooltipValueSources.register(BLOODLINE_PURITY, AscensionTooltipValueSources::bloodlinePurity);
    }

    private static Optional<ZenithTooltipValue> techniquePath(ZenithTooltipContext context) {
        Optional<Technique> technique = context.subject(Technique.class);
        if (technique.isEmpty() || context.registryAccess().isEmpty()) {
            return Optional.empty();
        }

        Identifier pathId = technique.orElseThrow().getPath();
        if (pathId == null) {
            return Optional.empty();
        }

        Path path = CoreRegistries.safeAccess(
                CoreRegistries.PATH_REGISTRY,
                pathId,
                context.registryAccess().orElseThrow()
        );

        Component displayName = path == null
                ? Component.literal(pathId.toString())
                : path.name();

        return Optional.of(ZenithTooltipValue.text(displayName));
    }

    private static Optional<ZenithTooltipValue> techniqueMaxRealm(ZenithTooltipContext context) {
        Optional<Technique> technique = context.subject(Technique.class);
        if (technique.isEmpty() || context.registryAccess().isEmpty()) {
            return Optional.empty();
        }

        int maxRealm = technique.orElseThrow().getMaxMajorRealm(
                null,
                context.registryAccess().orElseThrow()
        );

        return Optional.of(ZenithTooltipValue.text(
                Component.literal(Integer.toString(maxRealm))
        ));
    }

    private static Optional<ZenithTooltipValue> bloodlinePurity(ZenithTooltipContext context) {
        if (context.subject(Bloodline.class).isEmpty()) {
            return Optional.empty();
        }

        int purity = context.stack().getOrDefault(AscensionComponents.PURITY, 1);
        purity = Math.max(1, Math.min(100, purity));

        return Optional.of(ZenithTooltipValue.progress(
                purity,
                100,
                Component.literal(purity + "%")
        ));
    }
}
