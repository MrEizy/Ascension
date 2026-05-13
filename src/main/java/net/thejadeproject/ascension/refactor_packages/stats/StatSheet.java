package net.thejadeproject.ascension.refactor_packages.stats;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.PacketDistributor;
import net.thejadeproject.ascension.refactor_packages.entity_data.IEntityData;
import net.thejadeproject.ascension.refactor_packages.network.client_bound.entity_data.stats.SyncStat;
import net.thejadeproject.ascension.refactor_packages.util.IDataInstance;
import net.thejadeproject.ascension.refactor_packages.util.value_modifiers.ValueContainer;
import net.thejadeproject.ascension.refactor_packages.util.value_modifiers.ValueContainerModifier;
import org.checkerframework.checker.units.qual.C;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class StatSheet {

    private final HashMap<Stat,StatInstance> sheetStats = new HashMap<>();


    public StatInstance getStatInstance(Stat stat){
        return sheetStats.get(stat);
    }



    public void addStat(Stat stat, double value){
        if(!sheetStats.containsKey(stat)) sheetStats.put(stat,stat.instance());

        sheetStats.get(stat).setBaseValue(Math.max(0,sheetStats.get(stat).getBaseValue()+value));
    }
    public void removeStat(Stat stat,double value){
        if(!sheetStats.containsKey(stat)) return;

        addStat(stat,-value);
    }
    public void addStatModifier(Stat stat, ValueContainerModifier modifier){
        if(!sheetStats.containsKey(stat)) sheetStats.put(stat,stat.instance());
        sheetStats.get(stat).addModifier(modifier);
    }
    public void removeStatModifier(Stat stat,ResourceLocation identifier){
        if(!sheetStats.containsKey(stat)) return;
        sheetStats.get(stat).removeModifier(identifier);
    }
    public boolean hasStat(Stat stat){return sheetStats.containsKey(stat);}
    public Collection<Stat> getStats(){
        return Set.copyOf(sheetStats.keySet());
    }
    public void setContainer(Stat stat, ValueContainer container){
        sheetStats.put(stat,new StatInstance(container));
    }
    public void sync(ServerPlayer player, ResourceLocation form){
        for (StatInstance stat : sheetStats.values()){
            PacketDistributor.sendToPlayer(player, new SyncStat(form.toString(),stat));
        }
    }
    public void log(){
//        //System.out.println("Stats on " + FMLEnvironment.dist.toString() +" :");
        for(StatInstance instance:sheetStats.values()){
            instance.log();
        }
    }
}
