package net.zic.ascension.refactor_packages.util.value_modifiers;

/**
 * for reductions (e.g reduce by 20%) use -0.2
 */
public enum ModifierOperation {
    MULTIPLY_BASE, // total += base*(1+val)
    ADD_BASE,
    MULTIPLY_FINAL,
    ADD_FINAL, // total += val;
}
