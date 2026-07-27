package net.zic.ascension.api.ascension.core.path.bonus;

import net.minecraft.resources.Identifier;
import net.zic.zenithlib.stats.Stat;
import net.zic.zenithlib.stats.StatInstance;
import net.zic.zenithlib.value_containers.ValueContainer;

import java.util.Collection;

public interface PathBonusProvider {


    ValueContainer getPathBonusContainer(Identifier category,Identifier path);
    double getPathBonus(Identifier category,Identifier path);

    Collection<PathBonus> getAllPathBonuses();
    Collection<Identifier> getAllPathBonusesInCategory(Identifier category);
}
