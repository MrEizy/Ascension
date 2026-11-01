package net.zic.ascension.mixins.combat;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.zic.ascension.impl.runtime.object.Barriers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * A hit a barrier with suppress_hurt_effects takes whole (Barriers.hurtEffectsSuppressed, set while the damage is dealt in
 * actuallyHurt) leaves out what hurtServer does after it: the hurt flash and camera shake (the damage event), the velocity
 * sync, the knockback and its direction indicator, and the hurt sounds.
 */
@Mixin(LivingEntity.class)
public abstract class LivingEntityBarrierHurtMixin {
    @Inject(method = "hurtServer", at = @At("HEAD"))
    private void ascension$startHit(ServerLevel level, DamageSource source, float damage, CallbackInfoReturnable<Boolean> cir) {
        Barriers.clearHurtEffectsSuppressed((LivingEntity) (Object) this);
    }

    @Inject(method = "hurtServer", at = @At("RETURN"))
    private void ascension$endHit(ServerLevel level, DamageSource source, float damage, CallbackInfoReturnable<Boolean> cir) {
        Barriers.clearHurtEffectsSuppressed((LivingEntity) (Object) this);
    }

    @WrapWithCondition(method = "hurtServer", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/server/level/ServerLevel;broadcastDamageEvent(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/damagesource/DamageSource;)V"))
    private boolean ascension$damageEvent(ServerLevel level, Entity entity, DamageSource source) {
        return !ascension$suppressed();
    }

    @WrapWithCondition(method = "hurtServer", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/LivingEntity;markHurt()V"))
    private boolean ascension$markHurt(LivingEntity entity) {
        return !ascension$suppressed();
    }

    @WrapWithCondition(method = "hurtServer", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/LivingEntity;knockback(DDD)V"))
    private boolean ascension$knockback(LivingEntity entity, double power, double xd, double zd) {
        return !ascension$suppressed();
    }

    @WrapWithCondition(method = "hurtServer", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/LivingEntity;indicateDamage(DD)V"))
    private boolean ascension$indicateDamage(LivingEntity entity, double xd, double zd) {
        return !ascension$suppressed();
    }

    @WrapWithCondition(method = "hurtServer", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/LivingEntity;playHurtSound(Lnet/minecraft/world/damagesource/DamageSource;)V"))
    private boolean ascension$hurtSound(LivingEntity entity, DamageSource source) {
        return !ascension$suppressed();
    }

    @WrapWithCondition(method = "hurtServer", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/LivingEntity;playSecondaryHurtSound(Lnet/minecraft/world/damagesource/DamageSource;)V"))
    private boolean ascension$secondaryHurtSound(LivingEntity entity, DamageSource source) {
        return !ascension$suppressed();
    }

    private boolean ascension$suppressed() {
        return Barriers.hurtEffectsSuppressed((LivingEntity) (Object) this);
    }
}
