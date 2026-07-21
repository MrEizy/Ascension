package net.zic.ascension.api.ascension.core.data_source;
/**
 * Determines the order that data sources are loaded in
 * INITIAL -> before everything else, used for sources of truth that provide the existence of things like skills or paths
 * FINAL -> after everything else, used for things that rely on previous data to either exist or initialize
 */
public enum LoadOrder {
    INITIAL,
    FINAL
}
