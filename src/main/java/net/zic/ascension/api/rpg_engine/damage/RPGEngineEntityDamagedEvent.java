package net.zic.ascension.api.rpg_engine.damage;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.common.damagesource.DamageContainer;
import net.neoforged.neoforge.common.damagesource.IReductionFunction;
import net.neoforged.neoforge.event.entity.living.LivingEvent;
import net.zic.ascension.AscensionCraft;
import net.zic.zenithlib.value_containers.typed.*;


public abstract class RPGEngineEntityDamagedEvent extends LivingEvent {
    protected final DamageContainer container;
    protected final RPGEngineDamageSource source;
    protected ValueContainer<Double> damageContainer;
    protected static final Identifier CONTAINER_ID = Identifier.fromNamespaceAndPath(AscensionCraft.MOD_ID,"rpg_engine_damage_instance");

    public RPGEngineEntityDamagedEvent(LivingEntity entity, DamageContainer container, RPGEngineDamageSource source) {
        super(entity);
        this.container = container;
        damageContainer = ValueContainerHelpers.doubleValueContainer(CONTAINER_ID,container.getNewDamage());
        this.source = source;
    }
    public static class Pre extends RPGEngineEntityDamagedEvent  {
        public Pre(LivingEntity entity, DamageContainer container, RPGEngineDamageSource source) {
            super(entity, container,source);

        }
        public void addFlatModifier(Modifier<Double> modifier) {damageContainer.addFlatModifier(modifier);}
        public void addMultiplierModifier(Modifier<Double> modifier){damageContainer.addMultiplierModifier(modifier);}

        public void removeModifier(Identifier identifier){
            damageContainer.removeModifier(identifier);
        }

        public void addModifier(DamageContainer.Reduction type, IReductionFunction reductionFunction) {
            this.container.addModifier(type,reductionFunction);
        }
        public void setPostAttackInvulnerabilityTicks(int ticks) {
            container.setPostAttackInvulnerabilityTicks(ticks);
        }

    }
    public static class Post extends RPGEngineEntityDamagedEvent {
        public Post(Pre event){
            this(event.getEntity(),event.container,event.damageContainer,event.source);
        }
        public Post(LivingEntity entity, DamageContainer container,ValueContainer<Double> valueContainer, RPGEngineDamageSource source) {
            super(entity, container,source);
            this.damageContainer = valueContainer;
        }
    }


    public double getDamage(){
        return damageContainer.getValue();
    }
    public double getBaseDamage(){
        return damageContainer.getBaseValue();
    }

    public RPGEngineDamageSource getSource() {
        return source;
    }


    public int getPostAttackInvulnerabilityTicks() {
        return container.getPostAttackInvulnerabilityTicks();
    }

    public float getReduction(DamageContainer.Reduction type) {
        return container.getReduction(type);
    }

}
