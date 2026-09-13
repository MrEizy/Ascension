package net.zic.ascension.api.ascension.core.alchemy;

import net.minecraft.resources.Identifier;
import net.zic.ascension.AscensionCraft;

public final class AlchemyAffinities {
    public static final Identifier BLOOD = id("blood");
    public static final Identifier FIRE = id("fire");
    public static final Identifier ICE = id("ice");
    public static final Identifier LIFE = id("life");
    public static final Identifier LIGHTNING = id("lightning");
    public static final Identifier METAL = id("metal");
    public static final Identifier MOON = id("moon");
    public static final Identifier WATER = id("water");
    public static final Identifier WOOD = id("wood");
    public static final Identifier YANG = id("yang");
    public static final Identifier YIN = id("yin");

    private AlchemyAffinities() {
    }

    private static Identifier id(String path) {
        return AscensionCraft.prefix("elemental/" + path);
    }
}
