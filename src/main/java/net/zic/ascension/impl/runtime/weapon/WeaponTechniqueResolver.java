package net.zic.ascension.impl.runtime.weapon;

import net.minecraft.resources.Identifier;
import net.zic.ascension.api.ascension.core.path.PathInstance;
import net.zic.ascension.api.ascension.core.source.AscensionOriginSourceHelper;
import net.zic.ascension.api.rpg_engine.source.OriginSource;

import java.util.Map;

public final class WeaponTechniqueResolver {
    private WeaponTechniqueResolver() {
    }

    public static Resolution resolve(
            OriginSource source,
            Identifier preferredPath,
            String vfxType,
            String fallbackColor,
            Map<Identifier, String> techniqueColors
    ) {
        Map<Identifier, String> colors = techniqueColors == null ? Map.of() : techniqueColors;
        Identifier preferred = currentTechnique(source, preferredPath);
        if (preferred != null && colors.containsKey(preferred)) {
            return new Resolution(preferred, colors.get(preferred));
        }

        if (source != null) {
            for (Identifier path : AscensionOriginSourceHelper.getPaths(source)) {
                Identifier candidate = currentTechnique(source, path);
                if (candidate != null && colors.containsKey(candidate)) {
                    return new Resolution(candidate, colors.get(candidate));
                }
            }
        }

        Identifier technique = preferred;
        if (technique == null && source != null) {
            for (Identifier path : AscensionOriginSourceHelper.getPaths(source)) {
                technique = currentTechnique(source, path);
                if (technique != null) {
                    break;
                }
            }
        }
        return new Resolution(
                technique,
                VfxColorRegistry.resolve(vfxType, technique, fallbackColor)
        );
    }

    private static Identifier currentTechnique(OriginSource source, Identifier path) {
        if (source == null || path == null || !AscensionOriginSourceHelper.hasPath(source, path)) {
            return null;
        }
        PathInstance data = AscensionOriginSourceHelper.getPathInstance(source, path);
        return Identifier.parse("none");
    }

    public record Resolution(Identifier technique, String colorFolder) {
    }
}
