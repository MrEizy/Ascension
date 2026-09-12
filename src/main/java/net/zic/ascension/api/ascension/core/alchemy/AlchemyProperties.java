package net.zic.ascension.api.ascension.core.alchemy;

import net.minecraft.resources.Identifier;
import net.zic.ascension.AscensionCraft;

public final class AlchemyProperties {
    public static final Identifier ANTIDOTE = id("antidote");
    public static final Identifier BLOOD_NOURISHMENT = id("blood_nourishment");
    public static final Identifier CALMING = id("calming");
    public static final Identifier CIRCULATION = id("circulation");
    public static final Identifier CLEANSING = id("cleansing");
    public static final Identifier COOLING = id("cooling");
    public static final Identifier ESSENCE_GATHERING = id("essence_gathering");
    public static final Identifier HEATING = id("heating");
    public static final Identifier MARROW_CLEANSING = id("marrow_cleansing");
    public static final Identifier PURIFICATION = id("purification");
    public static final Identifier REINFORCEMENT = id("reinforcement");
    public static final Identifier RESTORATION = id("restoration");
    public static final Identifier SOUL_NOURISHMENT = id("soul_nourishment");
    public static final Identifier SPIRIT_NOURISHMENT = id("spirit_nourishment");
    public static final Identifier STABILIZATION = id("stabilization");
    public static final Identifier VITALITY = id("vitality");

    private AlchemyProperties() {
    }

    private static Identifier id(String path) {
        return AscensionCraft.prefix("alchemy/" + path);
    }
}
