package net.zic.ascension.api.ascension.core.progression;

public enum ProgressDirection {
    UP(1),
    DOWN(-1);
    public final int value;

    ProgressDirection(int value) {
        this.value = value;
    }
}
