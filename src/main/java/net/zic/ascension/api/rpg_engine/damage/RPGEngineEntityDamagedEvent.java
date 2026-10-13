package net.zic.ascension.api.rpg_engine.damage;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.common.damagesource.DamageContainer;
import net.neoforged.neoforge.common.damagesource.IReductionFunction;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingEvent;
import net.zic.ascension.AscensionCraft;
import net.zic.zenithlib.value_containers.typed.Modifier;
import net.zic.zenithlib.value_containers.typed.ValueContainer;
import net.zic.zenithlib.value_containers.typed.ValueContainerHelpers;

public abstract class RPGEngineEntityDamagedEvent extends LivingEvent {
    protected final DamageContainer container;
    protected final RPGEngineDamageSource source;
    protected final ValueContainer<Double> damageContainer;
    protected static final Identifier CONTAINER_ID = Identifier.fromNamespaceAndPath(AscensionCraft.MOD_ID, "rpg_engine_damage_instance");

    protected RPGEngineEntityDamagedEvent(LivingEntity entity, DamageContainer container, RPGEngineDamageSource source, double initialDamage) {
        super(entity);
        this.container = container;
        this.source = source;
        this.damageContainer = ValueContainerHelpers.doubleValueContainer(CONTAINER_ID, initialDamage);
    }

    public static class Pre extends RPGEngineEntityDamagedEvent {
        public Pre(LivingEntity entity, DamageContainer container, RPGEngineDamageSource source) {
            super(entity, container, source, container.getNewDamage());
        }

        public void addFlatModifier(Modifier<Double> modifier) {
            damageContainer.addFlatModifier(modifier);
        }

        public void addMultiplierModifier(Modifier<Double> modifier) {
            damageContainer.addMultiplierModifier(modifier);
        }

        public void removeModifier(Identifier identifier) {
            damageContainer.removeModifier(identifier);
        }

        public void resolveDamage(Identifier identifier, int operationGroup, double resolvedDamage) {
            double currentDamage = getDamage();
            if (!Double.isFinite(currentDamage) || !Double.isFinite(resolvedDamage) || currentDamage <= 0.0D) {
                return;
            }
            double targetDamage = Math.max(0.0D, resolvedDamage);
            double difference = targetDamage - currentDamage;
            if (Double.isFinite(difference) && Math.abs(difference) > 1.0E-10D) {
                addFlatModifier(Modifier.flat(identifier, operationGroup, difference));
            }
        }

        public void multiplyDamage(Identifier identifier, int operationGroup, double multiplier) {
            if (getDamage() <= 0.0D || !Double.isFinite(multiplier) || multiplier < 0.0D || multiplier == 1.0D) {
                return;
            }
            addMultiplierModifier(Modifier.multiplier(identifier, operationGroup, multiplier - 1.0D));
        }

        public void addModifier(DamageContainer.Reduction type, IReductionFunction reductionFunction) {
            container.addModifier(type, reductionFunction);
        }

        public void setPostAttackInvulnerabilityTicks(int ticks) {
            container.setPostAttackInvulnerabilityTicks(ticks);
        }
    }

    public static class Post extends RPGEngineEntityDamagedEvent {
        private final LivingDamageEvent.Post original;

        public Post(LivingDamageEvent.Post event, RPGEngineDamageSource source) {
            super(event.getEntity(), null, source, event.getHealthDamage());
            original = event;
        }

        @Override
        public double getDamage() {
            return original.getHealthDamage();
        }

        @Override
        public int getPostAttackInvulnerabilityTicks() {
            return original.getPostAttackInvulnerabilityTicks();
        }

        @Override
        public float getReduction(DamageContainer.Reduction type) {
            return original.getReduction(type);
        }
    }

    public double getDamage() {
        return damageContainer.getValue();
    }

    public double getBaseDamage() {
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
