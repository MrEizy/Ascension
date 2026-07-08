package net.zic.ascension.common.gui.elements.introspection.stats_display;

import net.lucent.easygui.gui.RenderableElement;
import net.lucent.easygui.gui.UIFrame;
import net.lucent.easygui.gui.layout.positioning.rules.PositioningRules;
import net.minecraft.network.chat.Component;
import net.zic.ascension.impl.core.entity.AscensionStats;

public class StatHolder extends RenderableElement {
    public StatHolder(UIFrame frame) {
        super(frame);
        setWidth(100);
        setHeight(113);
        getPositioning().setPositioningRule(PositioningRules.CENTER);
        getPositioning().setX(-getWidth() / 2);
        getPositioning().setY(-getHeight() / 2);

        addStat(frame, AscensionStats.VITALITY.get(),  0);
        addStat(frame, AscensionStats.AGILITY.get(),  20);
        addStat(frame, AscensionStats.STRENGTH.get(),  40);
        addStat(frame, AscensionStats.SPIRIT.get(), 60);
    }

    private void addStat(UIFrame frame, net.zic.zenithlib.stats.Stat stat,  int y) {
        StatsDisplay display = new StatsDisplay(frame, stat, stat.getName());
        display.getPositioning().setX(10);
        display.getPositioning().setY(y);
        addChild(display);
    }
}
