package net.zic.ascension.handler;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.capabilities.AscensionEntityDataHolder;
import net.zic.ascension.api.capabilities.CoreCapabilities;
import net.zic.ascension.api.capabilities.damage_provider.AscensionDamageSourceProvider;
import net.zic.ascension.api.core.entity.AscensionEntityData;
import net.zic.ascension.api.core.path.PathEffectValueUtil;
import net.zic.ascension.api.event.AscensionEntityDamagedEvents;
import net.zic.ascension.util.AscensionDamageUtil;
import net.zic.zenithlib.value_containers.ValueContainer;

import javax.annotation.Nullable;

@EventBusSubscriber(modid = AscensionCraft.MOD_ID)
public class AscensionDamageHandler {
    public static final ResourceKey<DamageType> PATH_TYPE =
            ResourceKey.create(
                    Registries.DAMAGE_TYPE,
                    Identifier.fromNamespaceAndPath(AscensionCraft.MOD_ID,"path")
            );
    public static class AscensionDamageSource extends DamageSource{
        public final Identifier path;
        public AscensionDamageSource(Identifier path,Holder<DamageType> type, @Nullable Entity directEntity, @Nullable Entity causingEntity) {
            super(type,directEntity,causingEntity);
            this.path = path;
        }
        public AscensionDamageSource(Identifier path,DamageSource source){
            this(path,source.typeHolder(),source.getDirectEntity(),source.getEntity());
        }
    }
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onDamage(LivingDamageEvent.Pre event){
        AscensionDamageSource finalDamageSource = null;
        if(event.getSource() instanceof AscensionDamageSource ascensionDamageSource){
            finalDamageSource = ascensionDamageSource;
        }else{

            finalDamageSource = new AscensionDamageSource(null,event.getSource());
            if(event.getSource().getEntity()!= null && event.getSource().getEntity() == event.getSource().getDirectEntity()){
                System.out.println("weapon damage");
                if(event.getSource().getEntity() instanceof LivingEntity livingEntity){
                    //the entity used an item and there is no custom damageSource present
                    ItemStack item = livingEntity.getActiveItem();
                    System.out.println(item);
                    AscensionDamageSourceProvider provider = item.getCapability(CoreCapabilities.ASCENSION_ITEM_STACK_DAMAGE_SOURCE_PROVIDER);
                    System.out.println(provider);
                    if(provider != null) finalDamageSource = new AscensionDamageSource(provider.getPath(),event.getSource());
                }

            }else if(event.getSource().getDirectEntity() != null && event.getSource().getEntity() != null && event.getSource().getEntity() != event.getSource().getDirectEntity()){
                //occurs with things like arrows or charges, and there is no custom damageSource present

                AscensionDamageSourceProvider provider = event.getSource().getDirectEntity().getCapability(CoreCapabilities.ASCENSION_ENTITY_DAMAGE_SOURCE_PROVIDER);
                if(provider != null) finalDamageSource = new AscensionDamageSource(provider.getPath(),event.getSource());

            }else if(event.getSource().getEntity() == null && event.getSource().getDirectEntity() != null){
                //TODO I have no idea what scenario this is triggered
            }

        }

        if(finalDamageSource.path == null) return;

        double attackerAffinity = 0;

        AscensionEntityDataHolder holder = event.getSource().getEntity().getCapability(CoreCapabilities.ASCENSION_ENTITY_DATA_HOLDER_CAPABILITY);
        if(holder != null && event.getSource().getEntity() instanceof LivingEntity livingEntity){

            AscensionEntityData data = holder.getData(livingEntity);
            attackerAffinity = AscensionDamageUtil.getEffectiveAttackerAffinity(
                    data.getAffinity(finalDamageSource.path)+ data.getAffinity(finalDamageSource.path,AscensionDamageUtil.DAMAGE_CATEGORY),
                    livingEntity,
                    finalDamageSource.path
            );
        }
        System.out.println("attacker affinity :" +attackerAffinity);
        double defenderAffinity = 0;
        AscensionEntityDataHolder defenderHolder = event.getEntity().getCapability(CoreCapabilities.ASCENSION_ENTITY_DATA_HOLDER_CAPABILITY);

        if(defenderHolder != null && event.getEntity() instanceof LivingEntity livingEntity){
            attackerAffinity = AscensionDamageUtil.getFinalAttackerAffinity(attackerAffinity,livingEntity,finalDamageSource.path);
            AscensionEntityData data = defenderHolder.getData(livingEntity);
            defenderAffinity = AscensionDamageUtil.getEffectiveDefenderAffinity(
                    data.getAffinity(finalDamageSource.path)+data.getAffinity(finalDamageSource.path,AscensionDamageUtil.RESISTANCE_CATEGORY),
                    livingEntity,
                    finalDamageSource.path
            );
        }
        System.out.println("final attacker affinity :"+attackerAffinity);
        System.out.println("defender affinity : "+defenderAffinity);
        System.out.println("base damage :"+event.getNewDamage());
        ValueContainer container = new ValueContainer(
                Identifier.fromNamespaceAndPath(AscensionCraft.MOD_ID,"damage_modifiers"),
                0
        );
        event.setNewDamage((float) AscensionDamageUtil.getDamage(event.getNewDamage(),attackerAffinity,defenderAffinity));

        AscensionEntityDamagedEvents.Pre pre = new AscensionEntityDamagedEvents.Pre(
                finalDamageSource,
                event.getContainer(),
                container,
                event.getEntity()
        );
        NeoForge.EVENT_BUS.post(pre);
        event.setNewDamage((float) pre.getDamage());
        AscensionEntityDamagedEvents.Post post = new AscensionEntityDamagedEvents.Post(finalDamageSource,event.getContainer(),container,event.getEntity());
        NeoForge.EVENT_BUS.post(post);

        if(event.getSource().getEntity() instanceof Player player){
            System.out.println("dealt : "+event.getNewDamage());
        }
    }
}
