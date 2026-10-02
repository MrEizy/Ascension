package net.zic.ascension.impl.core.technique.realm_change.condition;

import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.zic.ascension.api.ascension.core.path.PathInstance;
import net.zic.ascension.api.ascension.core.path.realm.realm_change.RealmChangeActionCondition;
import net.zic.ascension.api.ascension.core.progression.ProgressDirection;
import net.zic.ascension.api.ascension.datapack.progresison.ProgressActionConditionType;
import net.zic.ascension.api.rpg_engine.source.OriginSource;
import net.zic.ascension.impl.datapack.progression.AscensionProgressActionConditionTypes;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;

public final class RealmChangeConditions {
    private RealmChangeConditions() {}

    public static final class EveryRealm implements RealmChangeActionCondition {
        @Override
        public boolean test(OriginSource source, Identifier path, PathInstance PathInstance, int majorRealm, int minorRealm, ProgressDirection direction) {
            return true;
        }

        @Override
        public Component getDescription(RegistryAccess access) {
            return Component.translatable(
                    "ascension.tooltip.progression.condition.each_realm"
            );
        }

        @Override
        public ProgressActionConditionType getType() {
            return AscensionProgressActionConditionTypes.EVERY_REALM.get();
        }
    }

    public static final class EveryMinorRealm implements RealmChangeActionCondition {
        @Override
        public boolean test(OriginSource source, Identifier path, PathInstance PathInstance, int majorRealm, int minorRealm, ProgressDirection direction) {
            return majorRealm != 0;
        }

        @Override
        public Component getDescription(RegistryAccess access) {
            return Component.translatable(
                    "ascension.tooltip.progression.condition.each_minor_realm"
            );
        }

        @Override
        public ProgressActionConditionType getType() {
            return AscensionProgressActionConditionTypes.EVERY_MINOR_REALM.get();
        }
    }

    public static final class EveryMajorRealm implements RealmChangeActionCondition {
        @Override
        public boolean test(OriginSource source, Identifier path, PathInstance PathInstance, int majorRealm, int minorRealm, ProgressDirection direction) {
            return minorRealm == 0 && majorRealm > 0;
        }

        @Override
        public Component getDescription(RegistryAccess access) {
            return Component.translatable(
                    "ascension.tooltip.progression.condition.each_major_realm"
            );
        }

        @Override
        public ProgressActionConditionType getType() {
            return AscensionProgressActionConditionTypes.EVERY_MAJOR_REALM.get();
        }
    }

    public record MajorRealmsIn(List<Integer> majorRealms) implements RealmChangeActionCondition {
        @Override
        public boolean test(OriginSource source, Identifier path, PathInstance PathInstance, int majorRealm, int minorRealm, ProgressDirection direction) {
            return minorRealm == 0 && majorRealms.contains(majorRealm);
        }

        @Override
        public OptionalInt getEarliestMajorRealm() {
            return majorRealms.stream()
                    .mapToInt(Integer::intValue)
                    .min();
        }

        @Override
        public Component getDescription(RegistryAccess access) {
            if (majorRealms.size() == 1 && majorRealms.contains(0)) {
                return Component.translatable(
                        "ascension.tooltip.progression.condition.on_learn"
                );
            }

            if (majorRealms.size() == 1) {
                return Component.translatable(
                        "ascension.tooltip.progression.condition.major_realm",
                        majorRealms.get(0)
                );
            }

            return Component.translatable(
                    "ascension.tooltip.progression.condition.major_realms",
                    joinInts(majorRealms)
            );
        }

        @Override
        public ProgressActionConditionType getType() {
            return AscensionProgressActionConditionTypes.EVERY_MAJOR_REALM_IN.get();
        }
    }

    public record MinorRealmsIn(List<Integer> minorRealms) implements RealmChangeActionCondition {
        @Override
        public boolean test(OriginSource source, Identifier path, PathInstance PathInstance, int majorRealm, int minorRealm, ProgressDirection direction) {
            return minorRealm != 0 && minorRealms.contains(minorRealm);
        }

        @Override
        public Component getDescription(RegistryAccess access) {
            if (minorRealms.size() == 1) {
                return Component.translatable(
                        "ascension.tooltip.progression.condition.minor_realm",
                        minorRealms.get(0)
                );
            }

            return Component.translatable(
                    "ascension.tooltip.progression.condition.minor_realms",
                    joinInts(minorRealms)
            );
        }

        @Override
        public ProgressActionConditionType getType() {
            return AscensionProgressActionConditionTypes.EVERY_MINOR_REALM_IN.get();
        }
    }

    public record RealmsIn(Map<Integer, List<Integer>> realms) implements RealmChangeActionCondition {
        @Override
        public boolean test(OriginSource source, Identifier path, PathInstance PathInstance, int majorRealm, int minorRealm, ProgressDirection direction) {
            return realms.containsKey(majorRealm) && realms.get(majorRealm).contains(minorRealm);
        }

        @Override
        public OptionalInt getEarliestMajorRealm() {
            return realms.keySet().stream()
                    .mapToInt(Integer::intValue)
                    .min();
        }

        @Override
        public Component getDescription(RegistryAccess access) {
            return Component.translatable(
                    "ascension.tooltip.progression.condition.selected_realms"
            );
        }

        @Override
        public ProgressActionConditionType getType() {
            return AscensionProgressActionConditionTypes.EVERY_REALM_IN.get();
        }
    }

    private static String joinInts(Collection<Integer> values) {
        return values.stream()
                .sorted()
                .map(String::valueOf)
                .reduce((left, right) -> left + ", " + right)
                .orElse("");
    }
}
