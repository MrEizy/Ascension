package net.zic.ascension.common.item.artifacts.pills;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.Consumables;
import net.minecraft.world.level.Level;
import net.zic.ascension.AscensionCraft;

public final class PillResidueItem extends Item {
    private static final ResourceKey<DamageType> RESIDUE_DAMAGE = ResourceKey.create(
            Registries.DAMAGE_TYPE, AscensionCraft.prefix("pill_residue"));

    public PillResidueItem(Properties properties) {
        super(properties.component(DataComponents.CONSUMABLE,
                Consumables.defaultFood().consumeSeconds(1.0F).build()));
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        ItemStack remaining = super.finishUsingItem(stack, level, entity);
        if (level instanceof ServerLevel server && entity.isAlive()) {
            var damageType = server.registryAccess().lookupOrThrow(Registries.DAMAGE_TYPE)
                    .getOrThrow(RESIDUE_DAMAGE);
            DamageSource source = new DamageSource(damageType);

            entity.getCombatTracker().recordDamage(source, entity.getHealth());
            entity.setHealth(0.0F);
            entity.die(source);
        }
        return remaining;
    }
}
