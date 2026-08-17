package net.zic.ascension.impl.core.progression;

import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.zic.ascension.api.ascension.core.CoreRegistries;
import net.zic.ascension.api.ascension.core.path.Path;
import net.zic.ascension.api.ascension.core.skill.Skill;
import net.zic.ascension.api.ascension.core.source.AscensionOriginSourceHelper;
import net.zic.zenithlib.common.ZenithRegistries;
import net.zic.zenithlib.stats.Stat;

import java.math.BigDecimal;
import java.util.StringJoiner;

final class ProgressionDescriptionUtil {
    private ProgressionDescriptionUtil() {}

    static Component statName(Identifier id) {
        Stat stat = ZenithRegistries.STAT_REGISTRY.getValue(id);
        return stat == null ? Component.literal(humanName(id.getPath())) : stat.getName();
    }

    static Component pathName(Identifier id, RegistryAccess access) {
        Path path = CoreRegistries.safeAccess(CoreRegistries.PATH_REGISTRY, id, access);
        return path == null ? Component.literal(humanName(id.getPath())) : path.name();
    }

    static Component skillName(Identifier id, RegistryAccess access) {
        Skill skill = CoreRegistries.safeAccess(CoreRegistries.SKILL_REGISTRY, id, access);
        return skill == null ? Component.literal(humanName(id.getPath())) : skill.getName();
    }

    static Component pathBonusName(Identifier category, Identifier path, RegistryAccess access
    ) {
        Component pathName = pathName(path, access);

        if (AscensionOriginSourceHelper.AFFINITY_CATEGORY.equals(category)) {
            return Component.translatable(
                    "ascension.tooltip.progression.affinity",
                    pathName
            );
        }

        return Component.translatable(
                "ascension.tooltip.progression.path_bonus",
                pathName,
                Component.literal(humanName(category.getPath()))
        );
    }

    static String signedNumber(double value) {
        String number = BigDecimal.valueOf(Math.abs(value)).stripTrailingZeros().toPlainString();

        if (value > 0.0) {
            return "+" + number;
        }
        if (value < 0.0) {
            return "-" + number;
        }
        return number;
    }

    private static String humanName(String value) {
        String path = value;
        int slash = path.lastIndexOf('/');
        if (slash >= 0 && slash + 1 < path.length()) {
            path = path.substring(slash + 1);
        }

        StringJoiner result = new StringJoiner(" ");
        for (String word : path.replace('-', '_').split("_")) {
            if (word.isBlank()) {
                continue;
            }
            result.add(Character.toUpperCase(word.charAt(0)) + word.substring(1));
        }
        return result.toString();
    }
}
