package net.zic.ascension.api.core.source;

/**
 * describes a process the server source can perform. a sync will only be triggered
 * if resolveSource is called with the same process as the current process
 * TODO update to be Strings, that way we have an infinite number, then use static strings to save on memory
 */
public enum ProcessType {
    PHYSIQUE,
    MODIFY_PHYSIQUE,
    ADD_BLOODLINE,
    MODIFY_BLOODLINE,
    REMOVE_BLOODLINE,
    ADD_PATH,
    MODIFY_PATH,
    REMOVE_PATH,
    TECHNIQUE,
    ADD_SKILL,
    MODIFY_SKILL,
    REMOVE_SKILL,
    ADD_DATA_SOURCE,
    MODIFY_DATA_SOURCE,
    REMOVE_DATA_SOURCE,
    STAT,
    AFFINITY,
    READ
}
