package net.zic.ascension.api.ascension.core.path.bonus;

import net.minecraft.resources.Identifier;

public record PathBonus(Identifier category,Identifier path){

    public static PathBonus of(Identifier category,Identifier path){
        return new PathBonus(category,path);
    }
}
