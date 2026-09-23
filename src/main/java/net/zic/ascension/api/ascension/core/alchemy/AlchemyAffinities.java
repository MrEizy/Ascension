package net.zic.ascension.api.ascension.core.alchemy;

import net.minecraft.resources.Identifier;
import net.zic.ascension.AscensionCraft;
// This class isn't actually needed tbh, you can remove it if you want :)
public final class AlchemyAffinities {
    public static final Identifier FIRE = id("fire");
    public static final Identifier ICE = id("ice");
    public static final Identifier LIGHTNING = id("lightning");
    public static final Identifier WATER = id("water");
    public static final Identifier WOOD = id("wood");

    private AlchemyAffinities() {
    }

    private static Identifier id(String path) {
        return AscensionCraft.prefix("elemental/" + path);
    }
}
