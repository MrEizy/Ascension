package net.zic.ascension.api.ascension.core.alchemy;

import net.minecraft.resources.Identifier;
import net.zic.ascension.AscensionCraft;

public final class AlchemyProperties {
    public static final Identifier NOURISHMENT = id("nourishment");
    public static final Identifier RESTORATION = id("restoration");
    public static final Identifier CLEANSING = id("cleansing");
    public static final Identifier REINFORCEMENT = id("reinforcement");
    public static final Identifier CIRCULATION = id("circulation");

    private AlchemyProperties() {
    }

    private static Identifier id(String path) {
        return AscensionCraft.prefix("alchemy/" + path);
    }
}
