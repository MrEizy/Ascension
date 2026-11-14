package net.zic.ascension.common.entities.rat;

public enum RatVariant {
    JADE(0),
    CYAN(1),
    GOLDEN(2),
    CRIMSON(3);

    private static final RatVariant[] VALUES = values();
    private final int id;

    RatVariant(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public static RatVariant byId(int id) {
        return VALUES[Math.floorMod(id, VALUES.length)];
    }
}
