package net.zic.ascension.mob_cultivation.profile;

import com.google.gson.JsonObject;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.zic.ascension.mob_cultivation.MobCultivationCategory;

import java.util.Locale;

public record MobCultivationProfileTarget(Type type, String value) {
    public enum Type {
        GLOBAL(0),
        CATEGORY(1),
        NAMESPACE(2),
        ENTITY_TAG(3),
        ENTITY(4);

        private final int specificity;

        Type(int specificity) {
            this.specificity = specificity;
        }

        public int specificity() {
            return specificity;
        }
    }

    public static MobCultivationProfileTarget global() {
        return new MobCultivationProfileTarget(Type.GLOBAL, "");
    }

    public static MobCultivationProfileTarget parse(JsonObject root) {
        if (!root.has("target") || !root.get("target").isJsonObject()) {
            return global();
        }

        JsonObject target = root.getAsJsonObject("target");
        if (target.has("entity")) {
            return new MobCultivationProfileTarget(Type.ENTITY, target.get("entity").getAsString());
        }
        if (target.has("entity_tag")) {
            return new MobCultivationProfileTarget(Type.ENTITY_TAG, target.get("entity_tag").getAsString());
        }
        if (target.has("namespace")) {
            return new MobCultivationProfileTarget(Type.NAMESPACE, target.get("namespace").getAsString());
        }
        if (target.has("category")) {
            return new MobCultivationProfileTarget(
                    Type.CATEGORY,
                    normalizeCategory(target.get("category").getAsString())
            );
        }
        return global();
    }

    private static String normalizeCategory(String value) {
        String normalized = value.toUpperCase(Locale.ROOT);
        return normalized.equals("BOSSES") ? "BOSS" : normalized;
    }

    public boolean matches(Mob mob, Identifier entityId, MobCultivationCategory category) {
        return switch (type) {
            case GLOBAL -> true;
            case CATEGORY -> category.name().equals(value);
            case NAMESPACE -> entityId.getNamespace().equals(value);
            case ENTITY -> entityId.toString().equals(value);
            case ENTITY_TAG -> matchesTag(mob);
        };
    }

    private boolean matchesTag(Mob mob) {
        try {
            Identifier tagId = Identifier.parse(value.startsWith("#") ? value.substring(1) : value);
            TagKey<EntityType<?>> tag = TagKey.create(Registries.ENTITY_TYPE, tagId);
            return mob.getType().builtInRegistryHolder().is(tag);
        } catch (Exception ignored) {
            return false;
        }
    }
}
