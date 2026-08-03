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
import net.zic.ascension.api.ascension.core.damage.AscensionDamageTypeHolders;
import net.zic.ascension.api.ascension.core.entity.AscensionEntityPathBonusHolder;
import net.zic.ascension.api.ascension.core.path.PathEffectValueUtil;
import net.zic.ascension.api.rpg_engine.damage.RPGEngineEntityDamagedEvent;
import net.zic.ascension.api.rpg_engine.damage.RPGEngineGatherDamageTypesEvent;
import net.zic.ascension.util.AscensionDamageUtil;
import net.zic.zenithlib.value_containers.ModifierOperation;
import net.zic.zenithlib.value_containers.ValueContainerModifier;

@EventBusSubscriber(modid = AscensionCraft.MOD_ID)
public class AscensionDamageHandler {


    @SubscribeEvent
    public static void gatherDamageType(RPGEngineGatherDamageTypesEvent event){
        if(event.hasTypeHolder(AscensionDamageTypeHolders.PATH)) return;


        if(event.getSource().getEntity()!= null && event.getSource().getEntity() == event.getSource().getDirectEntity()){

            if(event.getSource().getEntity() instanceof LivingEntity livingEntity){
                //the entity used an item and there is no custom damageSource present
                ItemStack item = livingEntity.getActiveItem();

                AscensionDamageSourceProvider provider = item.getCapability(CoreCapabilities.ASCENSION_ITEM_STACK_DAMAGE_SOURCE_PROVIDER);
                if(provider != null) event.addTypeHolder(AscensionDamageTypeHolders.PATH, new AscensionDamageTypeHolders.Path(provider.getPath()));
            }

        }else if(event.getSource().getDirectEntity() != null && event.getSource().getEntity() != null && event.getSource().getEntity() != event.getSource().getDirectEntity()){
            //occurs with things like arrows or charges, and there is no custom damageSource present

            AscensionDamageSourceProvider provider = event.getSource().getDirectEntity().getCapability(CoreCapabilities.ASCENSION_ENTITY_DAMAGE_SOURCE_PROVIDER);
            if(provider != null) event.addTypeHolder(AscensionDamageTypeHolders.PATH, new AscensionDamageTypeHolders.Path(provider.getPath()));

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

        if(!event.getSource().hasDamageTypeHolder(AscensionDamageTypeHolders.PATH)) return;
        if(!(event.getSource().getDamageTypeHolder(AscensionDamageTypeHolders.PATH) instanceof AscensionDamageTypeHolders.Path(Identifier path))) return;


        double attackerAffinity = AscensionDamageUtil.getEffectiveAttackerAffinity(
                attackerBonusHolder.getPathBonus(PathEffectValueUtil.AFFINITY_CATEGORY, path),
                attacker,
                path
        );

        //apply the targets affinities to attacker affinity, but ignore related affinity
        attackerAffinity = AscensionDamageUtil.getFinalAttackerAffinity(attackerAffinity,event.getEntity(),path);

        double defenderAffinity = AscensionDamageUtil.getEffectiveDefenderAffinity(
                defenderBonusHolder.getPathBonus(PathEffectValueUtil.AFFINITY_CATEGORY,path),
                event.getEntity(),
                path
        );

        double multiplier = AscensionDamageUtil.getFinalAffinity(attackerAffinity,defenderAffinity);

        event.addDamageModifier(new ValueContainerModifier(
                multiplier,
                ModifierOperation.MULTIPLY_FINAL,
                Identifier.fromNamespaceAndPath(AscensionCraft.MOD_ID,"affinity"),
                Identifier.fromNamespaceAndPath(AscensionCraft.MOD_ID,"affinity")
        ));
    }

}
