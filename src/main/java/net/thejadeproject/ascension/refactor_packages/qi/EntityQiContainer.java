package net.thejadeproject.ascension.refactor_packages.qi;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.thejadeproject.ascension.refactor_packages.entity_data.IEntityData;
import net.thejadeproject.ascension.refactor_packages.network.client_bound.entity_data.qi.SyncQi;
import net.thejadeproject.ascension.util.ModAttributes;

import java.util.HashMap;

/**
 * Holds the qi of an entity, the max amount is based of the MAX QI attribute
 *
 * however internally it is handled a bit different,the players qi is separated into "sections"
 * any leftover is assumed to be attributed qi.
 *
 * when trying to regen qi we use the tryRegen method in paths, and after consuming qi we call qiConsumed
 * this allows for more custom behaviour. path also has a conversion rate e.g 1 fire qi = 2 qi. so if a player has
 * max 100 qi, after conversion they have 50 fire qi
 *
 * when trying to use qi you can either specify the qi to use. OR you can not specify in which case it consumes it randomly
 *
 *
 */
public class EntityQiContainer {


    private double currentQi;
    private final IEntityData attachedEntity;

    public EntityQiContainer(IEntityData attachedEntity){
        this.attachedEntity = attachedEntity;

    }
    public void fullFillQi(){
        currentQi = getMaxQi();
    }


    public double getMaxQi(){
        return attachedEntity.getAscensionAttributeHolder().getAttribute(ModAttributes.MAX_QI).getValue();
    }

    public double getCurrentQi(){
        return currentQi;
    }

    public boolean hasQi(double amount) {
        return currentQi >= amount;
    }

    public void setCurrentQi(double amount) {
        currentQi = Math.clamp(amount, 0.0D, getMaxQi());
        sync();

    }

    public void tryRegenQi(){
        double regenRate = attachedEntity.getAscensionAttributeHolder().getAttribute(ModAttributes.QI_REGEN_RATE).getValue();

        setCurrentQi(currentQi + regenRate);

    }

    public boolean tryConsumeQi(double amount){
        if (!hasQi(amount)) return false;

        setCurrentQi(currentQi - amount);

        return true;
    }


    public void sync(){
        if(attachedEntity.getAttachedEntity() instanceof ServerPlayer player && player.connection != null){
            PacketDistributor.sendToPlayer(player,new SyncQi(currentQi));
        }
    }





}
