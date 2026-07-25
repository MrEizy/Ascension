package net.zic.ascension.api.rpg_engine.damage;

import net.minecraft.resources.Identifier;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.Event;
import net.neoforged.neoforge.common.damagesource.DamageContainer;

import java.util.Collection;
import java.util.HashMap;

public class RPGEngineGatherDamageTypesEvent extends Event {

    private final RPGEngineDamageSource source;
    private final LivingEntity entity;
    public RPGEngineGatherDamageTypesEvent(LivingEntity entity,RPGEngineDamageSource source) {
        this.source = source;
        this.entity = entity;
    }

    public DamageSource getSource() {
        return source;
    }
    public LivingEntity getEntity(){
        return entity;
    }

    public void addTypeHolder(Identifier id,RPGEngineDamageTypeHolder holder){
        source.addDamageTypeHolder(id,holder);
    }

    public boolean hasTypeHolder(Identifier id){
        return source.hasDamageTypeHolder(id);
    }

    public RPGEngineDamageTypeHolder getTypeHolder(Identifier id){
        return source.getDamageTypeHolder(id);
    }


}
