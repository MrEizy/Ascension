package net.zic.ascension.impl.core.path.realms;

public record BreakthroughBehaviour(int delayTicks){
    public static final BreakthroughBehaviour INSTANT = new BreakthroughBehaviour(0);
    public static final BreakthroughBehaviour NONE = new BreakthroughBehaviour(0);

}
