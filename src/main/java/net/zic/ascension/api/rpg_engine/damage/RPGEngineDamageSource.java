package net.zic.ascension.api.rpg_engine.damage;

import net.minecraft.resources.Identifier;
import net.minecraft.world.damagesource.DamageSource;

import java.util.HashMap;

public class RPGEngineDamageSource extends DamageSource {
    private final HashMap<Identifier,RPGEngineDamageTypeHolder> damageTypeHolders = new HashMap<>();
    public RPGEngineDamageSource(DamageSource source) {
        super(source.typeHolder(), source.getDirectEntity(), source.getEntity(), source.getSourcePosition());
    }



    public void addDamageTypeHolder(Identifier id,RPGEngineDamageTypeHolder holder){
        damageTypeHolders.put(id,holder);
    }
    public RPGEngineDamageTypeHolder getDamageTypeHolder(Identifier id){
        return damageTypeHolders.get(id);
    }

    public boolean hasDamageTypeHolder(Identifier id){
        return damageTypeHolders.containsKey(id);
    }
}
