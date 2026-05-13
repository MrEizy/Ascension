package net.thejadeproject.ascension.refactor_packages.gui.elements.introspection.stats_display;

import net.lucent.easygui.gui.UIFrame;
import net.lucent.easygui.gui.textures.ITextureData;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.thejadeproject.ascension.refactor_packages.attributes.AttributeValueContainer;

public class SuppressibleAttributeDisplayContainer extends AttributeDisplayContainer {

    SuppressedStat suppressedStat;


    public SuppressibleAttributeDisplayContainer(UIFrame frame, Holder<Attribute> attribute, ITextureData textureData) {
        super(frame, attribute, textureData);

        suppressedStat = new SuppressedStat(frame,attribute);
        suppressedStat.getPositioning().setY(getChildren().getLast().getPositioning().getY()+getChildren().getLast().getHeight()+2);

        addChild(suppressedStat);
    }
}
