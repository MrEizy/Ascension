package net.zic.ascension.refactor_packages.gui.elements.introspection.stats_display;

import net.lucent.easygui.gui.RenderableElement;
import net.lucent.easygui.gui.UIFrame;
import net.lucent.easygui.gui.elements.built_in.EasyLabel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.zic.ascension.data_attachments.ModAttachments;
import net.zic.ascension.refactor_packages.stats.Stat;
import net.zic.ascension.refactor_packages.stats.StatInstance;

import java.text.DecimalFormat;

public class StatsDisplay extends RenderableElement {

    public DecimalFormat format = new DecimalFormat("0.##");
    public final Stat stat;

    public EasyLabel valueLabel;
    private EasyLabel nameLabel;
    public StatsDisplay(UIFrame frame, Stat stat) {
        super(frame);
        this.stat =stat;

        nameLabel = new EasyLabel(frame);
        nameLabel.setText(Component.empty());

        nameLabel.setTextColor(-1);
        nameLabel.setScaleToFit(true);
        nameLabel.setWidth(40);
        nameLabel.getPositioning().setX(-nameLabel.getWidth()/2);
        nameLabel.setHeight(6);
        nameLabel.setTextPositioningX(EasyLabel.TextPositionRule.CENTER);

        valueLabel = new EasyLabel(frame);
        valueLabel.setText(Component.empty());

        valueLabel.setWidth(40);
        valueLabel.setHeight(6);
        valueLabel.getPositioning().setX(-valueLabel.getWidth()/2);
        valueLabel.setScaleToFit(true);
        valueLabel.setTextColor(-1);
        valueLabel.getPositioning().setY(8);
        valueLabel.setTextPositioningX(EasyLabel.TextPositionRule.CENTER);

        StatInstance statInstance =  Minecraft.getInstance().player.getData(ModAttachments.ENTITY_DATA).getActiveFormData().getStatSheet().getStatInstance(stat);
        if(statInstance == null) return;
        nameLabel.setText(statInstance.getDisplayName());



        addChild(nameLabel);
        addChild(valueLabel);
    }
    public void updateValue(){
        StatInstance statInstance =  Minecraft.getInstance().player.getData(ModAttachments.ENTITY_DATA).getActiveFormData().getStatSheet().getStatInstance(stat);
        if(statInstance == null) return;
        valueLabel.setText(Component.literal(format.format(statInstance.getValue())));
    }

    @Override
    protected void run(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        updateValue();
        super.run(guiGraphics, mouseX, mouseY, partialTick);
    }


}
