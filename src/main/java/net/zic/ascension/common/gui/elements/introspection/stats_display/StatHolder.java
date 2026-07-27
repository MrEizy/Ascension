package net.zic.ascension.common.gui.elements.introspection.stats_display;

import net.lucent.easygui.gui.RenderableElement;
import net.lucent.easygui.gui.UIFrame;
import net.lucent.easygui.gui.elements.built_in.EasyLabel;
import net.lucent.easygui.gui.layout.positioning.rules.PositioningRules;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.zic.ascension.common.gui.data.ClientAscensionData;
import net.zic.ascension.common.util.AscensionAttributes;
import net.zic.ascension.impl.core.entity.AscensionStats;

import java.text.DecimalFormat;

public class StatHolder extends RenderableElement {
    private static final DecimalFormat FORMAT = new DecimalFormat("0.##");
    private static final double TICKS_PER_SECOND = 20.0D;

    private final EasyLabel healthRegenerationValue;
    private final EasyLabel qiRegenerationValue;
    private final EasyLabel staminaRegenerationValue;

    public StatHolder(UIFrame frame) {
        super(frame);
        setWidth(100);
        setHeight(113);
        getPositioning().setPositioningRule(PositioningRules.CENTER);
        getPositioning().setX(-getWidth() / 2);
        getPositioning().setY(-getHeight() / 2);

        addStat(frame, AscensionStats.VITALITY.get(), 0);
        addStat(frame, AscensionStats.AGILITY.get(), 20);
        addStat(frame, AscensionStats.STRENGTH.get(), 40);
        addStat(frame, AscensionStats.SPIRIT.get(), 60);

        EasyLabel recoveryLabel = new EasyLabel(frame);
        recoveryLabel.setText(Component.translatable("gui.ascension.introspection.recovery"));
        recoveryLabel.setTextColor(0xFFFFFFFF);
        recoveryLabel.setScaleToFit(true);
        recoveryLabel.setWidth(80);
        recoveryLabel.setHeight(7);
        recoveryLabel.getPositioning().setX(10);
        recoveryLabel.getPositioning().setY(80);
        recoveryLabel.setTextPositioningX(EasyLabel.TextPositionRule.CENTER);
        addChild(recoveryLabel);

        healthRegenerationValue = addRecoveryRate(
                frame,
                Component.translatable("gui.ascension.introspection.recovery.health"),
                89
        );
        qiRegenerationValue = addRecoveryRate(
                frame,
                Component.translatable("gui.ascension.introspection.recovery.qi"),
                97
        );
        staminaRegenerationValue = addRecoveryRate(
                frame,
                Component.translatable("gui.ascension.introspection.recovery.stamina"),
                105
        );

        updateRecoveryRates();
    }

    private void addStat(UIFrame frame, net.zic.zenithlib.stats.Stat stat, int y) {
        StatsDisplay display = new StatsDisplay(frame, stat, stat.getName());
        display.getPositioning().setX(10);
        display.getPositioning().setY(y);
        addChild(display);
    }

    private EasyLabel addRecoveryRate(UIFrame frame, Component name, int y) {
        EasyLabel nameLabel = new EasyLabel(frame);
        nameLabel.setText(name);
        nameLabel.setTextColor(0xFFFFFFFF);
        nameLabel.setScaleToFit(true);
        nameLabel.setWidth(45);
        nameLabel.setHeight(7);
        nameLabel.getPositioning().setX(10);
        nameLabel.getPositioning().setY(y);
        addChild(nameLabel);

        EasyLabel valueLabel = new EasyLabel(frame);
        valueLabel.setText(Component.literal("-"));
        valueLabel.setTextColor(0xFFFFFFFF);
        valueLabel.setScaleToFit(true);
        valueLabel.setWidth(35);
        valueLabel.setHeight(7);
        valueLabel.getPositioning().setX(55);
        valueLabel.getPositioning().setY(y);
        valueLabel.setTextPositioningX(EasyLabel.TextPositionRule.CENTER);
        addChild(valueLabel);
        return valueLabel;
    }

    private void updateRecoveryRates() {
        healthRegenerationValue.setText(formatRate(AscensionAttributes.HEALTH_REGEN_RATE, 1.0D));
        qiRegenerationValue.setText(formatRate(AscensionAttributes.QI_REGEN_RATE, 1.0D));
        staminaRegenerationValue.setText(formatRate(AscensionAttributes.STAMINA_REGEN_RATE, TICKS_PER_SECOND));
    }

    private Component formatRate(Holder<Attribute> attribute, double displayMultiplier) {
        String value = ClientAscensionData.getPlayer()
                .filter(player -> player.getAttributes().hasAttribute(attribute))
                .map(player -> FORMAT.format(
                        Math.max(0.0D, player.getAttributeValue(attribute) * displayMultiplier)
                ))
                .orElse("-");

        if (value.equals("-")) {
            return Component.literal(value);
        }

        return Component.translatable(
                "gui.ascension.introspection.recovery.per_second",
                value
        );
    }

    @Override
    public void renderTick(
            GuiGraphicsExtractor graphics,
            int mouseX,
            int mouseY,
            float partialTick
    ) {
        updateRecoveryRates();
    }
}
