package net.zic.ascension.configuration.mob_traits;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Mob;
import net.zic.ascension.api.rpg_engine.source.OriginSource;



public interface MobTraitDefinition {
    MobTraitDefinitionType getType();

    Component name();

    void applyToSource(OriginSource source);
    void applyToMob(Mob mob);


    void removeFromSource(OriginSource source);
    void removeFromMob(Mob mob);


}
