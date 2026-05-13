package net.zic.ascension.entity;

import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public class PoisonPillProjectile extends ThrowableItemProjectile {
    private final MobEffectInstance effect;
    private Item overrideItem; // Store the item separately to avoid entity data issues

    public PoisonPillProjectile(EntityType<? extends PoisonPillProjectile> entityType, Level level) {
        super(entityType, level);
        this.effect = null;
        this.overrideItem = null;
    }

    public PoisonPillProjectile(EntityType<? extends PoisonPillProjectile> entityType, Level level, LivingEntity shooter, MobEffectInstance effect, Item item) {
        super(entityType, shooter, level);
        this.effect = effect;
        this.overrideItem = item;
        // Set the item immediately in the entity data
        this.setItem(new ItemStack(item));
    }

    public PoisonPillProjectile(EntityType<? extends PoisonPillProjectile> entityType, Level level, double x, double y, double z, MobEffectInstance effect, Item item) {
        super(entityType, x, y, z, level);
        this.effect = effect;
        this.overrideItem = item;
        this.setItem(new ItemStack(item));
    }

    @Override
    protected Item getDefaultItem() {
        // Use the override item if available, otherwise fallback
        return overrideItem != null ? overrideItem : net.minecraft.world.item.Items.SNOWBALL;
    }

    private ParticleOptions getParticle() {
        ItemStack itemstack = this.getItemRaw(); // Use getItemRaw to avoid entity data issues
        return !itemstack.isEmpty() ? new ItemParticleOption(ParticleTypes.ITEM, itemstack) : ParticleTypes.ITEM_SNOWBALL;
    }

    // Helper method to safely get the item without triggering entity data issues
    private ItemStack getItemRaw() {
        try {
            return this.getItem();
        } catch (NullPointerException e) {
            return overrideItem != null ? new ItemStack(overrideItem) : ItemStack.EMPTY;
        }
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

        if (!this.level().isClientSide && entity instanceof LivingEntity livingEntity && effect != null) {
            // Apply the effect to the hit entity
            livingEntity.addEffect(new MobEffectInstance(effect));
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