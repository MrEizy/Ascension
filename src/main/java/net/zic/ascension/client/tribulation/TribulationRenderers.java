package net.zic.ascension.client.tribulation;

import net.zic.ascension.api.ascension.datapack.tribulation.TribulationType;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * client only registry of renderers per TribulationType, kept out of Tribulation so servers never load client classes.
 * register during client setup
 */
public final class TribulationRenderers {
    private static final Map<TribulationType, TribulationRenderer> RENDERERS = new HashMap<>();

    private TribulationRenderers() {
    }

    public static void register(TribulationType type, TribulationRenderer renderer) {
        RENDERERS.put(type, renderer);
    }

    public static @Nullable TribulationRenderer get(TribulationType type) {
        return RENDERERS.get(type);
    }
}
