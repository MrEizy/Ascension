package net.zic.ascension.impl.core.path.realms;

public record BreakthroughBehaviour(int delayTicks){
    public static final BreakthroughBehaviour INSTANT = new BreakthroughBehaviour(0);
    public static final BreakthroughBehaviour NOTHING = new BreakthroughBehaviour(0);

}
