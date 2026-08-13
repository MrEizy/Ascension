package net.zic.ascension.configuration.mobs.cultivation;

import net.minecraft.resources.Identifier;

import java.util.List;

public record PotentialPathDefinition(Identifier path, List<PotentialRealmDefinition> potentialRealms,double chance){
}
