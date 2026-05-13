package net.thejadeproject.ascension.refactor_packages.paths;

import net.lucent.easygui.gui.RenderableElement;
import net.lucent.easygui.gui.UIFrame;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.thejadeproject.ascension.refactor_packages.entity_data.IEntityData;
import net.thejadeproject.ascension.refactor_packages.physiques.IPhysiqueData;
import org.apache.commons.lang3.tuple.Pair;

public interface IPath {


    Component getDisplayTitle();
    Component getDescription();


    Component getDisplayPathInteractions();

    Double getInteractionValue(ResourceLocation path);
    PathInteraction getInteractionType(ResourceLocation path);

    Component getMajorRealmName(int majorRealm);
    Component getMinorRealmName(int majorRealm,int minorRealm);

    RenderableElement getInformationContainer(UIFrame frame,PathData pathData);

    int getMaxMajorRealm();
    int getMaxMinorRealm(int majorRealm);
    double getMaxQiForRealm(int majorRealm,int minorRealm);

    double getQiConversionRatio(); // return value is what the path qi needs to be multiplied by
    double tryRegenQi(double amount,IEntityData heldEntity);// pass the amount of qi we WANT to regen and return HOW MUCH to regen
    void qiConsumed(double amount,IEntityData heldEntity);
    //the default form that holds this
    ResourceLocation defaultForm();

    PathData freshPathData(IEntityData heldEntity);
    //expected to simulate progression
    PathData fromCompound(CompoundTag tag, IEntityData heldEntity);
    PathData fromNetwork(RegistryFriendlyByteBuf buf);
}
