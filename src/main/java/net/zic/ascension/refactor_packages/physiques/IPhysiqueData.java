package net.zic.ascension.refactor_packages.physiques;

import net.minecraft.resources.ResourceLocation;
import net.zic.ascension.refactor_packages.util.IDataInstance;

public interface IPhysiqueData extends IDataInstance {

    ResourceLocation getPhysiqueKey(); //useful when "removing" the physique when changing forms
    IPhysique getPhysique();
}
