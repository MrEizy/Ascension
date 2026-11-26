package net.zic.ascension.mixins.combat;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.zic.ascension.api.ascension.core.attribute.AttributeScalingMath;
import net.zic.ascension.impl.core.attribute.ScaledAttributes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LivingEntity.class)
public abstract class LivingEntityExtendedArmorMixin {
    @WrapOperation(method = "getDamageAfterArmorAbsorb", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/damagesource/CombatRules;getDamageAfterAbsorb(Lnet/minecraft/world/entity/LivingEntity;FLnet/minecraft/world/damagesource/DamageSource;FF)F"))
    private float ascension$extendedArmor(LivingEntity victim, float damage, DamageSource source,
                                          float armor, float toughness, Operation<Float> original) {
        if (!ScaledAttributes.applies(victim) || source.is(DamageTypeTags.BYPASSES_ARMOR)
                || !victim.getAttributes().hasAttribute(Attributes.ARMOR)) {
            return original.call(victim, damage, source, armor, toughness);
        }
        var profile = ScaledAttributes.profile(victim);
        double total = victim.getAttributeValue(Attributes.ARMOR);
        if (profile == null || !Double.isFinite(total) || total <= 20.0D) {
            return original.call(victim, damage, source, armor, toughness);
        }
        float vanilla = original.call(victim, damage, source, 20.0F, toughness);
        double effectiveness = AttributeScalingMath.armorEffectiveness(damage, toughness, vanilla);
        return (float) (vanilla * AttributeScalingMath.excessArmorMultiplier(total, profile.excessArmorResistance(), effectiveness));
    }
}
