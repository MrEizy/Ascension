package net.thejadeproject.ascension.refactor_packages.gui.elements.introspection.stats_display;

import net.lucent.easygui.gui.RenderableElement;
import net.lucent.easygui.gui.UIFrame;
import net.lucent.easygui.gui.textures.ITextureData;
import net.lucent.easygui.gui.textures.TextureDataSubsection;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.thejadeproject.ascension.AscensionCraft;
import net.thejadeproject.ascension.refactor_packages.gui.elements.introspection.BackButton;

public class StatsDisplayContainer extends RenderableElement {
    ResourceLocation textureIdentifier = ResourceLocation.fromNamespaceAndPath(
            AscensionCraft.MOD_ID,
            "textures/gui/main/stats_menu/stats_menu.png"
    );


    ITextureData bg = new TextureDataSubsection(
            textureIdentifier,
            234,236,
            0,39,
            234,140
    );
    public StatsDisplayContainer(UIFrame frame) {
        super(frame);
        setWidth(bg.getWidth());
        setHeight(bg.getHeight());
        getPositioning().setX(-getWidth()/2);
        getPositioning().setY(-getHeight()/2);
        BackButton button = new BackButton(frame);
        button.getPositioning().setX(5);
        button.getPositioning().setY(5);
        addChild(new StatHolder(getUiFrame()));
        addChild(button);
        addLeftAttributes();
        addRightAttributes();
    }
    public void addRightAttributes(){
        int y = 18;
        int x = 165;
        SuppressibleAttributeDisplayContainer attack = new SuppressibleAttributeDisplayContainer(new UIFrame(), Attributes.MOVEMENT_SPEED,
                new TextureDataSubsection(textureIdentifier,234,236,185,226,9,9));
        addChild(attack);
        attack.getPositioning().setX(x);
        attack.getPositioning().setY(y);
        SuppressibleAttributeDisplayContainer jmp = new SuppressibleAttributeDisplayContainer(new UIFrame(), Attributes.JUMP_STRENGTH,
                new TextureDataSubsection(textureIdentifier,234,236,195,226,9,9));
        addChild(jmp);
        jmp.getPositioning().setX(x);
        jmp .getPositioning().setY(y+27);
        SuppressibleAttributeDisplayContainer STEP = new SuppressibleAttributeDisplayContainer(new UIFrame(), Attributes.STEP_HEIGHT,
                new TextureDataSubsection(textureIdentifier,234,236,205,226,9,9));
        addChild(STEP);
        STEP.getPositioning().setX(x);
        STEP .getPositioning().setY(y+54);

        SuppressibleAttributeDisplayContainer Mining = new SuppressibleAttributeDisplayContainer(new UIFrame(), Attributes.MINING_EFFICIENCY,
                new TextureDataSubsection(textureIdentifier,234,236,205,226,9,9));
        addChild(Mining);
        Mining.getPositioning().setX(x);
        Mining .getPositioning().setY(y+81);
    }
    public void addLeftAttributes(){
        int x = 13;
        int y = 18;

        AttributeDisplayContainer health = new AttributeDisplayContainer(new UIFrame(), Attributes.MAX_HEALTH,
                new TextureDataSubsection(textureIdentifier,234,236,20,227,7,7));
        addChild(health);
        health.getPositioning().setX(x);
        health.getPositioning().setY(y);

        SuppressibleAttributeDisplayContainer attack = new SuppressibleAttributeDisplayContainer(new UIFrame(), Attributes.ATTACK_DAMAGE,
                new TextureDataSubsection(textureIdentifier,234,236,28,227,9,9));
        addChild(attack);
        attack.getPositioning().setX(x);
        attack.getPositioning().setY(y+17);

        AttributeDisplayContainer armor = new AttributeDisplayContainer(new UIFrame(), Attributes.ARMOR,
                new TextureDataSubsection(textureIdentifier,234,236,10,227,9,9));
        addChild(armor);
        armor.getPositioning().setX(x);
        armor.getPositioning().setY(y+44);

        AttributeDisplayContainer armorToughness = new AttributeDisplayContainer(new UIFrame(), Attributes.ARMOR_TOUGHNESS,
                new TextureDataSubsection(textureIdentifier,234,236,0,227,9,9));
        addChild(armorToughness);
        armorToughness.getPositioning().setX(x);
        armorToughness.getPositioning().setY(y+61);
        SuppressibleAttributeDisplayContainer attackSpd = new SuppressibleAttributeDisplayContainer(new UIFrame(), Attributes.ATTACK_SPEED,
                new TextureDataSubsection(textureIdentifier,234,236,28,227,9,9));
        addChild(attackSpd);
        attackSpd.getPositioning().setX(x);
        attackSpd.getPositioning().setY(y+78);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        bg.render(guiGraphics);
    }
}
