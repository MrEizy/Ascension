package net.zic.ascension.api.rpg_engine.source.data_source;

public enum LoadPriority {
    HIGHEST,
    HIGH,
    NORMAL,
    LOW,
    LOWEST,
    NO_LOAD; //special state, just means onAdded is not called
}
