package net.zic.ascension.api.rpg_engine.damage;

import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

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
