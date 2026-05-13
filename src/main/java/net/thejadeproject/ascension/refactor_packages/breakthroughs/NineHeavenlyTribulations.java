package net.thejadeproject.ascension.refactor_packages.breakthroughs;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.thejadeproject.ascension.refactor_packages.entity_data.IEntityData;
import net.thejadeproject.ascension.util.ModDamageTypes;
import net.thejadeproject.ascension.util.ModTags;

public class NineHeavenlyTribulations implements IBreakthroughInstance{
    private int currentTribulation;
    private int ticksSinceFired;
    private final int MIN_TICKS = 40;
    private final int lightningHeight = 350;
    private final  double baseDamage;
    public NineHeavenlyTribulations(double baseDamage){
        this.baseDamage = baseDamage;
    }

    public boolean tryFire(IEntityData entityData){
        Entity entity = entityData.getAttachedEntity();
        int x= entity.getBlockX();
        int z= entity.getBlockZ();
        for(int y = entity.getBlockY()+1;y<lightningHeight;y++){
            if(!entity.level().getBlockState(new BlockPos(x,y,z)).isAir()) return false;
        }

        //fire
        double damage = baseDamage;
        damage *= (currentTribulation+1);
        damage *= (1+ (double) ticksSinceFired /MIN_TICKS);
        entity.hurt(new DamageSource(entity.level().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(DamageTypes.LIGHTNING_BOLT)), (float) damage);
        //System.out.println("dealing : "+damage);
        if(!entity.level().isClientSide()){
            LightningBolt lightning = EntityType.LIGHTNING_BOLT.create(entity.level());

            if (lightning != null) {
                lightning.moveTo(entity.getX(), entity.getY(), entity.getZ());

                entity.level().addFreshEntity(lightning);
            }
        }
        return true;
    }
    public void endBreakthrough(IEntityData entityData,ResourceLocation path){
        entityData.getPathData(path).handleRealmChange(entityData.getPathData(path).getMajorRealm()+1,0,entityData);
        entityData.getPathData(path).setBreakingThrough(false);
        entityData.getPathData(path).setBreakthroughInstance(null);
    }
    @Override
    public void tick(IEntityData entity, ResourceLocation path) {

        if(currentTribulation >=9)endBreakthrough(entity,path);

        ticksSinceFired ++;
        if(ticksSinceFired >= MIN_TICKS && tryFire(entity)) {
            ticksSinceFired = 0;
            currentTribulation += 1;
        }

    }

    @Override
    public void onEntityDeath(IEntityData entity, ResourceLocation path) {
        entity.getPathData(path).setBreakingThrough(false);
        entity.getPathData(path).setBreakthroughInstance(null);

    }

    @Override
    public CompoundTag write() {
        return null;
    }

    @Override
    public void encode(RegistryFriendlyByteBuf buf) {

    }
}
