package net.zic.ascension.entity;

import java.util.Arrays;
import java.util.Comparator;

public enum RatVariant {
    JADE(0),
    CYAN(1),
    GOLDEN(2),
    CRIMSON(3);

    private static final RatVariant[] BY_ID = Arrays.stream(values()).sorted(
            Comparator.comparingInt(RatVariant::getId)).toArray(RatVariant[]::new);
    private final int id;

    RatVariant(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public static RatVariant byId(int id) {
        return BY_ID[id % BY_ID.length];
    }
}
