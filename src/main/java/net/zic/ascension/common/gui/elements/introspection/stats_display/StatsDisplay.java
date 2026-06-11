package net.zic.ascension.common.gui.elements.introspection.stats_display;

import net.lucent.easygui.gui.RenderableElement;
import net.lucent.easygui.gui.UIFrame;
import net.lucent.easygui.gui.elements.built_in.EasyLabel;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.zic.ascension.common.gui.data.ClientAscensionData;
import net.zic.zenithlib.stats.Stat;

import java.text.DecimalFormat;

public class StatsDisplay extends RenderableElement {
    private static final DecimalFormat FORMAT = new DecimalFormat("0.##");

    private final Stat stat;
    private final EasyLabel valueLabel;

    public StatsDisplay(UIFrame frame, Stat stat, Component displayName) {
        super(frame);
        this.stat = stat;
        setWidth(80);
        setHeight(18);

        EasyLabel nameLabel = new EasyLabel(frame);
        nameLabel.setText(displayName);
        nameLabel.setTextColor(0xFFFFFFFF);
        nameLabel.setScaleToFit(true);
        nameLabel.setWidth(80);
        nameLabel.setHeight(7);
        nameLabel.setTextPositioningX(EasyLabel.TextPositionRule.CENTER);
        addChild(nameLabel);

        valueLabel = new EasyLabel(frame);
        valueLabel.setText(Component.literal("0"));
        valueLabel.setTextColor(0xFFFFFFFF);
        valueLabel.setScaleToFit(true);
        valueLabel.setWidth(80);
        valueLabel.setHeight(7);
        valueLabel.getPositioning().setY(9);
        valueLabel.setTextPositioningX(EasyLabel.TextPositionRule.CENTER);
        addChild(valueLabel);
    }

    private void updateValue() {
        double value = ClientAscensionData.getSource()
                .map(source -> source.getValue(stat))
                .orElse(0.0D);
        valueLabel.setText(Component.literal(FORMAT.format(value)));
    }

    @Override
    public void renderTick(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        updateValue();
    }
}
