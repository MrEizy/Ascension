package net.zic.ascension.handler;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.ascension.capabilities.CoreCapabilities;
import net.zic.ascension.api.ascension.capabilities.damage_provider.AscensionDamageSourceProvider;
import net.zic.ascension.api.ascension.core.CoreAttachments;
import net.zic.ascension.api.ascension.core.entity.AscensionEntityPathBonusHolder;
import net.zic.ascension.api.rpg_engine.damage.RPGEngineDamageTypeHolder;
import net.zic.ascension.api.rpg_engine.damage.RPGEngineEntityDamagedEvent;
import net.zic.ascension.api.rpg_engine.damage.RPGEngineGatherDamageTypesEvent;
import net.zic.ascension.util.AscensionDamageUtil;
import net.zic.ascension.util.PathInteractionUtil;
import net.zic.zenithlib.value_containers.ModifierOperation;
import net.zic.zenithlib.value_containers.ValueContainerModifier;

@EventBusSubscriber(modid = AscensionCraft.MOD_ID)
public class AscensionDamageHandler {

    private static final Identifier ID = Identifier.fromNamespaceAndPath(AscensionCraft.MOD_ID,"damage_container");

    public static record AscensionDamageTypeHolder(Identifier path) implements RPGEngineDamageTypeHolder{

    }

    @SubscribeEvent
    public static void gatherDamageType(RPGEngineGatherDamageTypesEvent event){
        if(event.hasTypeHolder(ID)) return;


        if(event.getSource().getEntity()!= null && event.getSource().getEntity() == event.getSource().getDirectEntity()){

            if(event.getSource().getEntity() instanceof LivingEntity livingEntity){
                //the entity used an item and there is no custom damageSource present
                ItemStack item = livingEntity.getActiveItem();

                AscensionDamageSourceProvider provider = item.getCapability(CoreCapabilities.ASCENSION_ITEM_STACK_DAMAGE_SOURCE_PROVIDER);
                if(provider != null) event.addTypeHolder(ID,new AscensionDamageTypeHolder(provider.getPath()));
            }

        }else if(event.getSource().getDirectEntity() != null && event.getSource().getEntity() != null && event.getSource().getEntity() != event.getSource().getDirectEntity()){
            //occurs with things like arrows or charges, and there is no custom damageSource present

            AscensionDamageSourceProvider provider = event.getSource().getDirectEntity().getCapability(CoreCapabilities.ASCENSION_ENTITY_DAMAGE_SOURCE_PROVIDER);
            if(provider != null) event.addTypeHolder(ID,new AscensionDamageTypeHolder(provider.getPath()));

        }else if(event.getSource().getEntity() == null && event.getSource().getDirectEntity() != null){
            //TODO I have no idea what scenario this is triggered
        }
    }

    @SubscribeEvent
    public static void onRPGEngineDamage(RPGEngineEntityDamagedEvent.Pre event){

        if(event.getSource().getEntity() == null) return; //TEMP, when applying affinity only works if attacker had any. but other bonuses still apply(like resistance)

        if(!(event.getSource().getEntity() instanceof LivingEntity attacker)) return;//TEMP, when applying affinity only works if attacker had any. but other bonuses still apply(like resistance)

        AscensionEntityPathBonusHolder attackerBonusHolder = event.getSource().getEntity().getData(CoreAttachments.PATH_BONUS_HOLDER);
        AscensionEntityPathBonusHolder defenderBonusHolder = event.getEntity().getData(CoreAttachments.PATH_BONUS_HOLDER);

        if(!event.getSource().hasDamageTypeHolder(ID)) return;
        if(!(event.getSource().getDamageTypeHolder(ID) instanceof AscensionDamageTypeHolder(Identifier path))) return;

        /*
            TODO:
                add affinity damage
                an attack has an attacker affinity multiplier, and a defender affinity multiplier

         */

    }

}
