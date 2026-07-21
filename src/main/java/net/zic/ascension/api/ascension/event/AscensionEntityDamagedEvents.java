package net.zic.ascension.api.ascension.event;

import net.minecraft.world.entity.Entity;
import net.neoforged.bus.api.Event;
import net.neoforged.neoforge.common.damagesource.DamageContainer;
import net.zic.ascension.handler.AscensionDamageHandler;
import net.zic.zenithlib.value_containers.ValueContainer;
import net.zic.zenithlib.value_containers.ValueContainerModifier;

public abstract class AscensionEntityDamagedEvents{

    public static class Pre extends Event {

        private final AscensionDamageHandler.AscensionDamageSource damageSource;
        private final DamageContainer damageContainer;
        private final ValueContainer modifiers;
        private final Entity target;

        public Pre(AscensionDamageHandler.AscensionDamageSource damageSource, DamageContainer damageContainer,ValueContainer container, Entity target) {
            this.damageSource = damageSource;
            this.damageContainer = damageContainer;
            this.target = target;
            this.modifiers = container;
            modifiers.setBaseValue(damageContainer.getNewDamage());
        }
        public void addModifier(ValueContainerModifier modifier){
            modifiers.addModifier(modifier);
        }
        public DamageContainer getDamageContainer(){return damageContainer;}
        public AscensionDamageHandler.AscensionDamageSource getDamageSource(){return damageSource;}
        public double getBaseDamage(){
            return modifiers.getBaseValue();
        }

        public double getDamage(){
            return modifiers.getValue();
        }
        public Entity getTarget(){return target;}

    }


    public static class Post extends Event {

        private final AscensionDamageHandler.AscensionDamageSource damageSource;
        private final DamageContainer damageContainer;
        private final ValueContainer  modifiers;
        private final Entity target;

        public Post(AscensionDamageHandler.AscensionDamageSource damageSource, DamageContainer damageContainer, ValueContainer modifiers, Entity target) {
            this.damageSource = damageSource;
            this.damageContainer = damageContainer;
            this.modifiers = modifiers;
            this.target = target;
        }
        public AscensionDamageHandler.AscensionDamageSource getDamageSource(){return damageSource;}
        public DamageContainer getDamageContainer(){return damageContainer;}
        public double getBaseDamage(){return modifiers.getBaseValue();}
        public double getDamage(){return modifiers.getValue();}
        public Entity getTarget(){return target;}
    }
}