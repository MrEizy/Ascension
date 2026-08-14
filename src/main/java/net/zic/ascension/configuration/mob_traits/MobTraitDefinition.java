package net.zic.ascension.configuration.mob_traits;

import net.minecraft.world.entity.Mob;
import net.zic.ascension.api.rpg_engine.source.OriginSource;

public interface MobTraitDefinition {
    MobTraitDefinitionType getType();



    boolean test(Mob mob);

    void applyToSource(OriginSource source);
    void applyToMob(Mob mob);


    void removeFromSource(OriginSource source);
    void removeFromBom(Mob mob);


    //called when the trait is first created on a mob
    void initializeTrait(Mob mob);
}
