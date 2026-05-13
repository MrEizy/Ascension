package net.thejadeproject.ascension.refactor_packages.gui.elements.introspection.stats_display;

import net.lucent.easygui.gui.RenderableElement;
import net.lucent.easygui.gui.UIFrame;
import net.lucent.easygui.gui.elements.built_in.EasyLabel;
import net.lucent.easygui.gui.events.EasyEvents;
import net.lucent.easygui.gui.events.type.EasyEvent;
import net.lucent.easygui.gui.events.type.EasyMouseEvent;
import net.lucent.easygui.gui.textures.TextureDataSubsection;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.neoforged.neoforge.network.PacketDistributor;
import net.thejadeproject.ascension.AscensionCraft;
import net.thejadeproject.ascension.data_attachments.ModAttachments;
import net.thejadeproject.ascension.refactor_packages.attributes.AttributeValueContainer;
import net.thejadeproject.ascension.refactor_packages.gui.elements.general.BetterButton;
import net.thejadeproject.ascension.refactor_packages.network.server_bound.supressors.UpdateSuppressionValue;


import java.text.DecimalFormat;

public class SuppressedStat extends RenderableElement {
    public DecimalFormat format = new DecimalFormat("0.##");
    private BetterButton minusBtn;
    private BetterButton plusBtn;
    private EasyLabel label;
    private Holder<Attribute> attributeHolder;
    ResourceLocation textureIdentifier = ResourceLocation.fromNamespaceAndPath(
            AscensionCraft.MOD_ID,
            "textures/gui/main/stats_menu/stats_menu.png"
    );

    public SuppressedStat(UIFrame frame, Holder<Attribute> attributeHolder) {
        super(frame);
        setHeight(10);

        this.attributeHolder = attributeHolder;
        minusBtn = new ModifierButton(frame,6,0,new TextureDataSubsection(
            textureIdentifier,234,236,
                29,195,8,8
        ));
        minusBtn.setWidth(8);
        minusBtn.setHeight(8);

        addChild(minusBtn);

        plusBtn = new ModifierButton(frame,45,0,new TextureDataSubsection(
                textureIdentifier,234,236,
                38,195,8,8
        ));
        plusBtn.setWidth(8);
        plusBtn.setHeight(8);

        addChild(plusBtn);

        label = new EasyLabel(frame);
        label.setText(Component.empty());
        label.setTextColor(-1);
        label.setScaleToFit(true);
        label.getPositioning().setX(15);
        label.setHeight(6);
        label.setTextPositioningX(EasyLabel.TextPositionRule.CENTER);
        label.setWidth(30);


        addChild(label);
        addEventListener(EasyEvents.MOUSE_UP_EVENT,this::onMouseClick);
    }

    public void onMouseClick(EasyEvent event){
        if(!(event instanceof EasyMouseEvent easyMouseEvent)) return;
        double amt = 0;
        //System.out.println("heard button click");
        //System.out.println(event.getTarget());
        if(event.getTarget() == minusBtn){
            //System.out.println();
            amt = Screen.hasShiftDown() ? -0.01 : -0.1;
            amt = Screen.hasControlDown() ? -0.001 : amt;
        }
        if(event.getTarget() == plusBtn){
            amt = Screen.hasShiftDown() ? 0.01 : 0.1;
            amt = Screen.hasControlDown() ? 0.001 : amt;
        }
        AttributeValueContainer holder = Minecraft.getInstance().player.getData(ModAttachments.ENTITY_DATA).getAttribute(attributeHolder);
        if(holder == null) return;
        //System.out.println(holder.getValue());
        //System.out.println(holder.getValue());
        //System.out.println(holder.getValue()/holder.getUnsuppressedValue());

        double percentage = Math.clamp(amt+ holder.getValue()/holder.getUnsuppressedValue(),0.001,1);
        //System.out.println("trying to apply supression val : "+percentage);
        double val = holder.getUnsuppressedValue()*percentage;
        //System.out.println("val :"+val);
        PacketDistributor.sendToServer(new UpdateSuppressionValue(BuiltInRegistries.ATTRIBUTE.getKey(holder.getAttributeHolder().value()).toString(),val));
    }


    public void updatePercentage(){
        AttributeValueContainer holder = Minecraft.getInstance().player.getData(ModAttachments.ENTITY_DATA).getAttribute(attributeHolder);
        if(holder == null) return;
        if(!holder.isSuppressed()) label.setText(Component.literal("100%"));
        else{
            double percentage = holder.getValue()/holder.getUnsuppressedValue();
            label.setText(Component.literal(format.format(percentage*100)).append("%"));
        }
    }

    @Override
    protected void run(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        updatePercentage();
        super.run(guiGraphics, mouseX, mouseY, partialTick);
    }
}
