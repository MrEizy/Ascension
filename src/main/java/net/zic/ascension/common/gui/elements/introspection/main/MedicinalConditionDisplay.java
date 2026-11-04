package net.zic.ascension.common.gui.elements.introspection.main;

import net.lucent.easygui.gui.RenderableElement;
import net.lucent.easygui.gui.UIFrame;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.zic.ascension.api.ascension.core.alchemy.medicinal.MedicinalImpurities;
import net.zic.ascension.common.gui.data.ClientAscensionData;
import net.zic.ascension.common.gui.elements.general.AscensionTooltip;

import java.util.Locale;


public final class MedicinalConditionDisplay extends RenderableElement {
    private final AscensionTooltip tooltip;

    public MedicinalConditionDisplay(UIFrame frame) {
        super(frame);
        setWidth(126);
        setHeight(11);
        tooltip = new AscensionTooltip(frame);
        tooltip.setActive(true);
    }

    @Override
    public void render(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        double amount = ClientAscensionData.getPlayer().map(MedicinalImpurities::get).orElse(0.0D);
        double efficiency = Math.max(15.0D, 100.0D - amount);
        String displayed = String.format(Locale.ROOT, "%.1f", amount);
        Component line = Component.translatable("gui.ascension.introspection.medicinal_impurities", displayed);
        graphics.text(Minecraft.getInstance().font, line, 0, 0, 0xFFE7B876, false);
        if (isPointBounded(mouseX, mouseY)) {
            tooltip.setText(Component.translatable("gui.ascension.introspection.medicinal_impurities_tooltip",
                    String.format(Locale.ROOT, "%.1f", efficiency),
                    String.format(Locale.ROOT, "%.1f", 100.0D - efficiency)));
            getUiFrame().setTooltip(tooltip);
        }
    }
}
