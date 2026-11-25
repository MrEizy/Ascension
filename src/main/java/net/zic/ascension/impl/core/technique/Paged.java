package net.zic.ascension.impl.core.technique;

/**
 * Implemented by techniques that are found as technique pages (lost_technique). The pages are collected in a technique binder,
 * and a complete binder turns into the technique's manual.
 */
public interface Paged {
    PageConfig pages();
}