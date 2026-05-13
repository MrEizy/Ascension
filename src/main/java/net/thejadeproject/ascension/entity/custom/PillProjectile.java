package net.thejadeproject.ascension.entity.custom;

import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.thejadeproject.ascension.common.items.ModItems;
import net.thejadeproject.ascension.refactor_packages.alchemy.IPillEffect;
import net.thejadeproject.ascension.refactor_packages.util.PillEffectUtil;

import java.util.List;

public class PillProjectile extends ThrowableItemProjectile {

    private Item defaultItem;

    public PillProjectile(EntityType<? extends PillProjectile> entityType, Level level, ItemStack pillItem) {
        super(entityType, level);
        this.defaultItem = pillItem.getItem();
        setItem(pillItem.copy());
    }
    public PillProjectile(EntityType<? extends PillProjectile> entityType, Level level, LivingEntity shooter, ItemStack pillItem) {
        super(entityType, shooter, level);
        this.defaultItem = pillItem.getItem();
        setItem(pillItem.copy());
    }
    public PillProjectile(EntityType<? extends PillProjectile> entityType, Level level, double x, double y, double z, ItemStack pillItem) {
        super(entityType, x, y, z, level);
        this.defaultItem = pillItem.getItem();
        setItem(pillItem.copy());
    }
    @Override
    protected Item getDefaultItem() {
        return defaultItem != null ? defaultItem : ModItems.QI_DEVOURING_PARASITE_PILL.get();
    }

    private ParticleOptions getParticle() {
        ItemStack itemstack = this.getItem(); // Use getItemRaw to avoid entity data issues
        return !itemstack.isEmpty() ? new ItemParticleOption(ParticleTypes.ITEM, itemstack) : ParticleTypes.ITEM_SNOWBALL;
    }


    @Override
    public void handleEntityEvent(byte id) {
        if (id == 3) {
            ParticleOptions particleoptions = this.getParticle();

            for(int i = 0; i < 8; ++i) {
                this.level().addParticle(particleoptions, this.getX(), this.getY(), this.getZ(), 0.0, 0.0, 0.0);
            }
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        Entity entity = result.getEntity();

        if (!this.level().isClientSide && entity instanceof LivingEntity livingEntity) {
            // Apply the effect to the hit entity
            List<IPillEffect> effects = PillEffectUtil.getPillEffects(getItem());
            for(IPillEffect effect : effects){
                effect.tryConsume(livingEntity,getItem(),PillEffectUtil.getPurityScale(getItem()),PillEffectUtil.getRealmMultiplier(getItem()));
            }
        }
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (!this.level().isClientSide) {
            this.level().broadcastEntityEvent(this, (byte)3);
            this.discard();
        }
    }
}
