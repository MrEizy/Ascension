package net.zic.ascension.refactor_packages.paths.custom;

import net.minecraft.network.chat.Component;
import net.zic.ascension.refactor_packages.entity_data.IEntityData;
import net.zic.ascension.refactor_packages.paths.ModPaths;
import net.zic.ascension.refactor_packages.paths.PathData;
import net.zic.ascension.refactor_packages.techniques.ModTechniques;

public class ComprehensionPath extends GenericPath{
    public ComprehensionPath(Component title) {
        super(title);
    }

    @Override
    public int getMaxMajorRealm() {
        return 4;
    }

    @Override
    public PathData freshPathData(IEntityData heldEntity) {
        if(this == ModPaths.SWORD.get()){
            PathData pathData = super.freshPathData(heldEntity);
            pathData.setLastUsedTechnique(ModTechniques.SWORD_COMPREHENSION_TECHNIQUE.getId());
            return pathData;
        }
        return super.freshPathData(heldEntity);
    }
}
