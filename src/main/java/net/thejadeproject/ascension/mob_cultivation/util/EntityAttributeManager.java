package net.thejadeproject.ascension.mob_cultivation.util;

import net.minecraft.world.entity.ai.attributes.RangedAttribute;

public final class EntityAttributeManager {
    private EntityAttributeManager() {
    }

    public static void changeAttributeRange(double min, double max, RangedAttribute attribute) {
        attribute.minValue = min;
        attribute.maxValue = max;

        if (attribute.defaultValue < min) {
            attribute.defaultValue = min;
        } else if (attribute.defaultValue > max) {
            attribute.defaultValue = max;
        }
    }

}