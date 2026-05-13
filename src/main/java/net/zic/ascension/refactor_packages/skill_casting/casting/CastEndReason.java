package net.zic.ascension.refactor_packages.skill_casting.casting;

/*
    skills will have a finalCast() method, which is called when
    continueCasting returns false or when manually triggered.
    if called after continueCasting is false will use ENDED,
    other methods can choose to use CANCELLED;
 */
public enum CastEndReason {
    CANCELLED,
    ENDED
}
