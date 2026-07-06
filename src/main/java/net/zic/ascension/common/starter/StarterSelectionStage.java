package net.zic.ascension.common.starter;

public enum StarterSelectionStage {
    BLOODLINE,
    PHYSIQUE,
    COMPLETE;

    public boolean isSelectable() {
        return this == BLOODLINE || this == PHYSIQUE;
    }
}
