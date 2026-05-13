package net.thejadeproject.ascension.refactor_packages.handlers;

import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingEvent;
import net.neoforged.neoforge.event.entity.living.LivingHealEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.thejadeproject.ascension.AscensionCraft;
import net.thejadeproject.ascension.data_attachments.ModAttachments;
import net.thejadeproject.ascension.refactor_packages.entity_data.IEntityData;
import net.thejadeproject.ascension.refactor_packages.events.AscensionDamageEvent;
import net.thejadeproject.ascension.refactor_packages.util.value_modifiers.ValueContainer;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@EventBusSubscriber(modid = AscensionCraft.MOD_ID)
public class AscensionDamageHandler {
    public static class AscensionDamageSource extends DamageSource{
        private final HashSet<ResourceLocation> pathAttributes;
        public AscensionDamageSource(HashSet<ResourceLocation> pathAttributes,DamageSource damageSource) {
            super(damageSource.typeHolder(), damageSource.getDirectEntity(), damageSource.getEntity(), damageSource.getSourcePosition());
            this.pathAttributes = pathAttributes;
        }

        public void addPathAttribute(ResourceLocation attribute){
            pathAttributes.add(attribute);
        }
        public boolean hasAttribute(ResourceLocation attribute){
            return pathAttributes.contains(attribute);
        }
        public Set<ResourceLocation> getPathAttributes(){
            return Set.copyOf(pathAttributes);
        }

    }
    public static void onAttack(AttackEntityEvent event){

    }


    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onIncomingDamage(LivingIncomingDamageEvent event){
        if(event.getSource().getEntity() == null) return;
        if(event.getSource().getDirectEntity() != event.getSource().getEntity()) return;

        if(!event.getSource().getEntity().hasData(ModAttachments.ENTITY_DATA)) return;
        //System.out.println("dealing increased damage");
        //event.setAmount((float)event.getSource().getEntity().getData(ModAttachments.ENTITY_DATA).getAscensionAttributeHolder().getAttribute(Attributes.ATTACK_DAMAGE).getValue());
    }


    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onDamage(LivingDamageEvent.Pre event){
        AscensionDamageSource finalDamageSource = null;
        if(event.getSource() instanceof AscensionDamageSource ascensionDamageSource){
            finalDamageSource = ascensionDamageSource;
        }else{
            //todo create a new damage source
            finalDamageSource = new AscensionDamageSource(new HashSet<>(),event.getSource());
            if(event.getSource().getDirectEntity()!= null && event.getSource().getDirectEntity() == event.getSource().getEntity()){
                //the entity used an item

                //call the AscensionDamageSourceCapability
                //TODO

            }else if(event.getSource().getEntity() != null && event.getSource().getEntity() != event.getSource().getDirectEntity()){
                //first pull damage source using  AscensionDamageSourceCapability
                //TODO


            }else if(event.getSource().getEntity() == null && event.getSource().getDirectEntity() != null){
                //check if it has AscensionDamageSourceCapability otherwise it does pathless damage
                //TODO
            }else {
                //no paths to add
                //TODO
            }

            //temp

        }
        ValueContainer container = new ValueContainer(
                ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID,"damage_modifiers"),
                Component.empty(),
                0
        );
        AscensionDamageEvent.Pre preEvent = new AscensionDamageEvent.Pre(finalDamageSource,event.getContainer(),container,event.getEntity());
        NeoForge.EVENT_BUS.post(preEvent);
        //System.out.println("dealt : "+preEvent.getDamage());
        AscensionDamageEvent.Post postEvent = new AscensionDamageEvent.Post(finalDamageSource,event.getContainer(),container,event.getEntity());
        NeoForge.EVENT_BUS.post(postEvent);
        event.setNewDamage((float) postEvent.getDamage());
        if(event.getSource().getEntity() != null && event.getSource().getEntity().hasData(ModAttachments.ENTITY_DATA)){
            IEntityData entityData = event.getSource().getEntity().getData(ModAttachments.ENTITY_DATA);
            for(ResourceLocation path : finalDamageSource.getPathAttributes()){
                event.setNewDamage((float) (event.getNewDamage()*entityData.getPathBonusHandler().getPathBonus(path)));
            }
        }
        //System.out.println("final damage "+event.getNewDamage());


    }

}





