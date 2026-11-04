package net.zic.ascension.common.entities.npc;

public enum NpcDailyActivity {
    NONE,
    CULTIVATION_ENTER,
    MEDITATION,
    QI_CIRCULATION,
    CULTIVATION_EXIT;

    private static final NpcDailyActivity[] ACTIVITIES = values();

    public static NpcDailyActivity fromId(int id) {
        return id >= 0 && id < ACTIVITIES.length ? ACTIVITIES[id] : NONE;
    }
}
