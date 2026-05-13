package net.thejadeproject.ascension.common.items.tools.data.needle;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;

public interface INeedleEffect {
    ResourceLocation getId();

    void onHit(LivingEntity target, LivingEntity shooter, Projectile projectile);
}
