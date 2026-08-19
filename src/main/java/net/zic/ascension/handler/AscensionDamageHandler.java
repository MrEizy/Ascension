package net.zic.ascension.handler;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.ascension.capabilities.CoreCapabilities;
import net.zic.ascension.api.ascension.capabilities.damage_provider.AscensionDamageSourceProvider;
import net.zic.ascension.api.ascension.core.damage.AscensionDamageProfile;
import net.zic.ascension.api.ascension.core.damage.AscensionDamageTypeHolders;
import net.zic.ascension.api.rpg_engine.damage.RPGEngineEntityDamagedEvent;
import net.zic.ascension.api.rpg_engine.damage.RPGEngineGatherDamageTypesEvent;
import net.zic.ascension.impl.core.damage.AscensionDamageProfileResolver;
import net.zic.ascension.impl.core.damage.DamageTrace;
import net.zic.ascension.impl.core.skill.passive.PassiveCombatService;
import net.zic.ascension.impl.runtime.object.Barriers;
import net.zic.ascension.impl.runtime.object.OwnerBoundConstructs;
import net.zic.ascension.impl.runtime.projectile.NormalProjectileService;

@EventBusSubscriber(modid = AscensionCraft.MOD_ID)
public final class AscensionDamageHandler {
    private AscensionDamageHandler() {
    }

    @SubscribeEvent
    public static void gatherDamageType(RPGEngineGatherDamageTypesEvent event) {
        NormalProjectileService.contributeDamageTypes(event);
        if (event.hasTypeHolder(AscensionDamageTypeHolders.PATH)) {
            return;
        }

        if (event.getSource().getEntity() != null && event.getSource().getEntity() == event.getSource().getDirectEntity()) {
            if (event.getSource().getEntity() instanceof LivingEntity livingEntity) {
                ItemStack item = livingEntity.getActiveItem();
                AscensionDamageSourceProvider provider = item.getCapability(CoreCapabilities.ASCENSION_ITEM_STACK_DAMAGE_SOURCE_PROVIDER);

                if (provider != null) {
                    event.addTypeHolder(AscensionDamageTypeHolders.PATH, new AscensionDamageTypeHolders.Path(provider.getPath()));
                }
            }
            return;
        }

        if (event.getSource().getDirectEntity() != null && event.getSource().getEntity() != null && event.getSource().getEntity() != event.getSource().getDirectEntity()) {
            AscensionDamageSourceProvider provider = event.getSource().getDirectEntity().getCapability(CoreCapabilities.ASCENSION_ENTITY_DAMAGE_SOURCE_PROVIDER);

            if (provider != null) {
                event.addTypeHolder(AscensionDamageTypeHolders.PATH, new AscensionDamageTypeHolders.Path(provider.getPath()));
            }
        }
    }

    @SubscribeEvent
    public static void onRPGEngineDamage(RPGEngineEntityDamagedEvent.Pre event) {
        DamageTrace trace = DamageTrace.begin();

        applyProfile(event, trace);
        if (finishIfResolved(event, trace)) {
            return;
        }

        double beforeProjectile = event.getDamage();
        event.setDamage(NormalProjectileService.resolveDamage(event, beforeProjectile));
        trace.transition("Projectile", beforeProjectile, event.getDamage());
        if (finishIfResolved(event, trace)) {
            return;
        }

        double masteryMultiplier = PassiveCombatService.outgoingDamageMultiplier(event);
        if (Double.isFinite(masteryMultiplier) && masteryMultiplier > 0.0D) {
            event.setDamage(event.getDamage() * masteryMultiplier);
            trace.multiply("Weapon mastery", masteryMultiplier);
        }
        if (finishIfResolved(event, trace)) {
            return;
        }

        double beforeDefense = event.getDamage();
        event.setDamage(PassiveCombatService.incomingDamage(event, beforeDefense));
        trace.transition("Passive defense", beforeDefense, event.getDamage());
        if (finishIfResolved(event, trace)) {
            return;
        }

        double beforeConstructs = event.getDamage();
        OwnerBoundConstructs.applyDamage(event);
        trace.transition("Constructs", beforeConstructs, event.getDamage());
        if (finishIfResolved(event, trace)) {
            return;
        }

        double beforeBarriers = event.getDamage();
        Barriers.applyDamage(event);
        trace.transition("Barriers", beforeBarriers, event.getDamage());
        trace.finish(event);
    }

    private static void applyProfile(RPGEngineEntityDamagedEvent.Pre event, DamageTrace trace) {
        if (!(event.getSource().getDamageTypeHolder(AscensionDamageTypeHolders.PROFILE)
                instanceof AscensionDamageProfile profile)
                || !(event.getSource().getEntity() instanceof LivingEntity attacker)) {
            trace.value("Incoming", event.getDamage());
            return;
        }

        double rawDamage = profile.baseDamage();
        trace.value("Base", profile.baseDamage());

        if (profile.weaponMultiplier() > 0.0D) {
            double contribution = AscensionDamageProfileResolver.weaponDamage(attacker) * profile.weaponMultiplier();
            if (Double.isFinite(contribution) && contribution > 0.0D) {
                rawDamage += contribution;
                trace.add("Weapon", contribution);
            }
        }

        for (var entry : profile.statScaling().entrySet()) {
            double contribution = AscensionDamageProfileResolver.stat(attacker, entry.getKey()) * entry.getValue();
            if (Double.isFinite(contribution) && Math.abs(contribution) > 1.0E-10D) {
                rawDamage += contribution;
                trace.add("Stat " + entry.getKey(), contribution);
            }
        }

        for (var entry : profile.attributeScaling().entrySet()) {
            double contribution = AscensionDamageProfileResolver.attribute(attacker, entry.getKey()) * entry.getValue();
            if (Double.isFinite(contribution) && Math.abs(contribution) > 1.0E-10D) {
                rawDamage += contribution;
                trace.add("Attribute " + entry.getKey(), contribution);
            }
        }

        double composedDamage = Math.max(0.0D, profile.clamp(rawDamage));
        if (Math.abs(composedDamage - rawDamage) > 1.0E-10D) {
            trace.transition("Profile clamp", rawDamage, composedDamage);
        }
        trace.transition("Vanilla reductions", composedDamage, event.getDamage());
    }

    private static boolean finishIfResolved(RPGEngineEntityDamagedEvent.Pre event, DamageTrace trace) {
        if (event.getDamage() > 0.0D) {
            return false;
        }
        trace.finish(event);
        return true;
    }
}
