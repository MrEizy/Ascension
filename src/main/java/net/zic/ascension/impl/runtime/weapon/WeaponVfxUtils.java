package net.zic.ascension.impl.runtime.weapon;

import net.minecraft.core.registries.Registries;
import net.zic.ascension.api.ascension.core.damage.AscensionDamageProfile;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Optional;

public final class WeaponVfxUtils {
    private WeaponVfxUtils() {
    }

    public static void spawnSwingVfx(
            ServerLevel level,
            LivingEntity owner,
            Vec3 position,
            float yRotOffset,
            float xRotOffset,
            float rotationZ,
            Vec3 radius,
            AscensionDamageProfile damage,
            double knockback,
            int duration,
            String vfxType,
            Vec3 movement,
            Identifier skillId,
            Identifier pathId,
            Identifier techniqueId,
            String colorFolder,
            WeaponSwingSpec.HitShape hitShape,
            WeaponSwingSpec.BlockImpact blockImpact,
            Optional<WeaponSwingSpec.HitEffect> hitEffect,
            List<Identifier> classifications
    ) {
        Vec3 velocity = localMovement(owner, movement);
        WeaponSwings.spawn(
                level,
                owner,
                position,
                owner.getXRot() + xRotOffset,
                owner.getYRot() + yRotOffset,
                new WeaponSwingSpec(
                        skillId,
                        Optional.ofNullable(pathId),
                        Optional.ofNullable(techniqueId),
                        vfxType,
                        colorFolder,
                        radius,
                        damage,
                        knockback,
                        duration,
                        rotationZ,
                        velocity,
                        hitShape,
                        blockImpact,
                        hitEffect,
                        classifications
                )
        );
    }

    public static void spawnSwingVfxAhead(
            ServerLevel level,
            LivingEntity owner,
            float rotationZ,
            Vec3 radius,
            AscensionDamageProfile damage,
            double knockback,
            int duration,
            String vfxType,
            Identifier skillId,
            Identifier pathId,
            Identifier techniqueId,
            String colorFolder,
            Vec3 movement,
            WeaponSwingSpec.HitShape hitShape,
            WeaponSwingSpec.BlockImpact blockImpact,
            Optional<WeaponSwingSpec.HitEffect> hitEffect,
            List<Identifier> classifications
    ) {
        Vec3 forward = owner.getLookAngle().normalize();
        Vec3 up = owner.getUpVector(1.0F);
        Vec3 position = owner.getEyePosition()
                .add(forward.scale(1.2D))
                .add(up.scale(-0.3D));

        spawnSwingVfx(
                level,
                owner,
                position,
                0.0F,
                0.0F,
                rotationZ,
                radius,
                damage,
                knockback,
                duration,
                vfxType,
                movement,
                skillId,
                pathId,
                techniqueId,
                colorFolder,
                hitShape,
                blockImpact,
                hitEffect,
                classifications
        );
    }

    public static boolean matchesWeapon(
            LivingEntity owner,
            Optional<Identifier> weaponTag,
            boolean allowEmptyHand
    ) {
        if (owner == null) {
            return false;
        }
        ItemStack stack = owner.getMainHandItem();
        if (stack.isEmpty()) {
            return allowEmptyHand;
        }
        if (weaponTag == null || weaponTag.isEmpty()) {
            return false;
        }
        TagKey<Item> tag = TagKey.create(Registries.ITEM, weaponTag.get());
        return stack.is(tag);
    }

    private static Vec3 localMovement(LivingEntity owner, Vec3 movement) {
        if (movement == null || movement.lengthSqr() <= 1.0E-10D) {
            return Vec3.ZERO;
        }
        Vec3 forward = owner.getLookAngle().normalize();
        Vec3 right = forward.cross(new Vec3(0.0D, 1.0D, 0.0D));
        if (right.lengthSqr() > 1.0E-10D) {
            right = right.normalize();
        }
        Vec3 up = new Vec3(0.0D, 1.0D, 0.0D);
        return forward.scale(movement.z)
                .add(up.scale(movement.y))
                .add(right.scale(movement.x))
                .scale(0.8D);
    }
}
