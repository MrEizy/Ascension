package net.thejadeproject.ascension.entity.custom;

import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.thejadeproject.ascension.common.items.ModItems;
import net.thejadeproject.ascension.common.items.data_components.ModDataComponents;
import net.thejadeproject.ascension.common.items.tools.data.needle.ModNeedleEffects;
import net.thejadeproject.ascension.entity.ModEntities;

public class NeedleProjectile extends ThrowableItemProjectile {
    public NeedleProjectile(EntityType<? extends NeedleProjectile> entityType, Level level) {
        super(entityType, level);
    }

    public NeedleProjectile(Level level, LivingEntity shooter, ItemStack needleStack) {
        super(ModEntities.NEEDLE_PROJECTILE.get(), shooter, level);
        setItem(needleStack.copy());
    }

    public NeedleProjectile(Level level, double x, double y, double z, ItemStack stack) {
        super(ModEntities.NEEDLE_PROJECTILE.get(), x, y, z, level);
        setItem(stack.copy());
    }


    @Override
    protected Item getDefaultItem() {
        return ModItems.SILVER_NEEDLE.get();
    }

    private ParticleOptions getParticle() {
        ItemStack stack = getItem();
        return !stack.isEmpty() ? new ItemParticleOption(ParticleTypes.ITEM, stack) : ParticleTypes.ITEM_SNOWBALL;
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == 3) {
            ParticleOptions particle = getParticle();
            for (int i = 0; i < 6; i++) {
                level().addParticle(particle, getX(), getY(), getZ(), 0.0, 0.0, 0.0);
            }
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (level().isClientSide()) return;

        if (!(result.getEntity() instanceof LivingEntity target)) return;

        target.hurt(damageSources().thrown(this, getOwner()), 2.0f);

        ItemStack stack = getItem();
        String effectId = stack.get(ModDataComponents.NEEDLE_EFFECT.get());
        if (effectId != null) {
            LivingEntity shooter = getOwner() instanceof LivingEntity le ? le : null;
            ModNeedleEffects.get(effectId).ifPresent(effect -> effect.onHit(target, shooter, this));
        }
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (!level().isClientSide()) {
            level().broadcastEntityEvent(this, (byte) 3);
            discard();
        }
    }
}
