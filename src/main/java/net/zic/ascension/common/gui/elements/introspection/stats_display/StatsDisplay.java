package net.zic.ascension.common.gui.elements.introspection.stats_display;

import net.lucent.easygui.gui.RenderableElement;
import net.lucent.easygui.gui.UIFrame;
import net.lucent.easygui.gui.elements.built_in.EasyLabel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.zic.ascension.common.gui.data.ClientAscensionData;
import net.zic.ascension.common.gui.elements.general.AscensionTooltip;
import net.zic.ascension.configuration.RealmEffectivenessConfiguration;
import net.zic.zenithlib.common.ZenithAttachments;
import net.zic.zenithlib.stats.Stat;

import java.text.DecimalFormat;

public class StatsDisplay extends RenderableElement {
    private static final DecimalFormat FORMAT = new DecimalFormat("0.##");

    private final Identifier statId;
    private final Stat stat;
    private final EasyLabel valueLabel;
    private final AscensionTooltip tooltip;
    private double rawValue;

    public StatsDisplay(UIFrame frame, Identifier statId, Stat stat, Component displayName) {
        super(frame);
        this.statId = statId;
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

        tooltip = new AscensionTooltip(frame);
        tooltip.setActive(true);
    }

    private void updateValue() {
        if (Minecraft.getInstance().player == null) {
            rawValue = 0.0D;
            valueLabel.setText(Component.literal("-"));
            return;
        }

        rawValue = Minecraft.getInstance().player.getData(ZenithAttachments.STAT_HOLDER).getStat(stat);
        valueLabel.setText(Component.literal(FORMAT.format(rawValue)));
        valueLabel.setTextScale(1.0F);
    }

    private void updateTooltip() {
        ClientAscensionData.getSource().ifPresent(source -> {
            double multiplier = RealmEffectivenessConfiguration.getStatMultiplier(source, statId);
            double effectiveValue = rawValue * multiplier;

            tooltip.setText(Component.empty()
                    .append(Component.translatable(
                            "gui.ascension.introspection.stat_tooltip.raw",
                            FORMAT.format(rawValue)
                    ))
                    .append(Component.translatable(
                            "gui.ascension.introspection.stat_tooltip.realm_effectiveness",
                            FORMAT.format(multiplier)
                    ))
                    .append(Component.translatable(
                            "gui.ascension.introspection.stat_tooltip.effective",
                            FORMAT.format(effectiveValue)
                    ))
            );
            getUiFrame().setTooltip(tooltip);
        });
    }

    @Override
    public void renderTick(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        updateValue();
    }

    @Override
    public void render(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        if (isPointBounded(graphics, mouseX, mouseY)) {
            updateTooltip();
        }
    }
}
