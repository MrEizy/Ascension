package net.thejadeproject.ascension.mob_cultivation.events;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.thejadeproject.ascension.AscensionCraft;
import net.thejadeproject.ascension.data_attachments.ModAttachments;
import net.thejadeproject.ascension.mob_cultivation.*;

@EventBusSubscriber(modid = AscensionCraft.MOD_ID)
public final class MobCultivationCombatEvents {

    private static final double REALM_DAMAGE_STEP = 1.25D;
    private static final double MIN_REALM_DAMAGE_MULTIPLIER = 0.08D;
    private static final double MAX_REALM_DAMAGE_MULTIPLIER = 2.5D;

    private MobCultivationCombatEvents() {
    }

    @SubscribeEvent
    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        applyProjectileDamageBonus(event);
        applyRealmDamageScaling(event);
    }

    private static void applyProjectileDamageBonus(LivingIncomingDamageEvent event) {
        Entity directEntity = event.getSource().getDirectEntity();
        Entity sourceEntity = event.getSource().getEntity();

        if (!(directEntity instanceof Projectile)) return;
        if (!(sourceEntity instanceof LivingEntity attacker)) return;

        MobCultivationData data = attacker.getData(ModAttachments.MOB_RANK);
        if (data == null || !data.isInitialized()) return;

        MobCultivationDefinition definition = MobCultivationResolver.resolveDefinition(data);
        MobCultivationStatProfile finalStats = MobCultivationResolver.resolveFinalStats(attacker, definition);

        double bonus = AscensionStatConversions.projectileDamageBonus(finalStats.strength());
        if (bonus <= 0) return;

        event.setAmount((float) (event.getAmount() + bonus));
    }

    private static void applyRealmDamageScaling(LivingIncomingDamageEvent event) {
        LivingEntity target = event.getEntity();

        if (target.level().isClientSide()) return;

        Entity sourceEntity = event.getSource().getEntity();
        if (!(sourceEntity instanceof LivingEntity attacker)) return;

        if (attacker == target) return;

        float originalDamage = event.getAmount();
        if (originalDamage <= 0) return;

        int attackerPower = MobCultivationResolver.resolveCombatPower(attacker);
        int targetPower = MobCultivationResolver.resolveCombatPower(target);

        int realmGap = attackerPower - targetPower;
        if (realmGap == 0) return;

        double multiplier = Math.pow(REALM_DAMAGE_STEP, realmGap);
        multiplier = clamp(multiplier, MIN_REALM_DAMAGE_MULTIPLIER, MAX_REALM_DAMAGE_MULTIPLIER);

        event.setAmount((float) (originalDamage * multiplier));
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}