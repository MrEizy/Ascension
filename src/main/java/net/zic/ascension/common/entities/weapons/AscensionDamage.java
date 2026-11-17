package net.zic.ascension.common.entities.weapons;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.zic.ascension.common.util.ModTags;

public final class AscensionDamage {
    private AscensionDamage() {}

    public static DamageSource chakram(Entity chakram, Entity attacker) {
        var reg = chakram.level().registryAccess().lookupOrThrow(Registries.DAMAGE_TYPE);
        var holder = reg.getOrThrow(ModTags.DamageTypes.CHAKRAM);
        return new DamageSource(holder, chakram, attacker);
    }
}