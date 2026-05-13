package net.zic.ascension.refactor_packages.breakthroughs;

import net.minecraft.resources.ResourceLocation;
import net.zic.ascension.refactor_packages.entity_data.IEntityData;
import net.zic.ascension.refactor_packages.util.IDataInstance;
//DOES NOT HANDLE STAT CHANGES THAT SHOULD BE DONE BY THE TECHNIQUES
public interface IBreakthroughInstance extends IDataInstance {

    /**
     * does not pass a level or a position, that should be stored and handled by the breakthrough instance
     *
     * @param entity the entity data undergoing the tribulation
     * @param path the path the tribulation is for
     */
    void tick(IEntityData entity, ResourceLocation path);

    void onEntityDeath(IEntityData entity,ResourceLocation path);
}
