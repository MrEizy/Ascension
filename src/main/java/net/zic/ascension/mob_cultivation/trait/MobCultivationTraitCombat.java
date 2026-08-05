package net.zic.ascension.mob_cultivation.trait;

import net.minecraft.resources.Identifier;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.entity.Mob;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.ascension.core.damage.AscensionDamageTypeHolders;
import net.zic.ascension.api.rpg_engine.damage.RPGEngineEntityDamagedEvent;
import net.zic.ascension.mob_cultivation.MobCultivationData;
import net.zic.ascension.mob_cultivation.MobCultivationManager;
import net.zic.ascension.impl.core.entity.AscensionStats;

@EventBusSubscriber(modid = AscensionCraft.MOD_ID)
public final class MobCultivationTraitCombat {
    private static final Identifier SPIRITUAL_BODY = AscensionCraft.prefix("spiritual_body");
    private static final Identifier ELEMENTAL_BODY = AscensionCraft.prefix("elemental_body");
    private static final Identifier ARMOURED_BODY = AscensionCraft.prefix("armoured_body");

    private MobCultivationTraitCombat() {}

    @SubscribeEvent
    public static void onDamage(RPGEngineEntityDamagedEvent.Pre event) {
        if (!(event.getEntity() instanceof Mob mob) || !MobCultivationManager.isCultivated(mob)) return;
        MobCultivationData data = MobCultivationManager.getCultivationData(mob);
        boolean pathInfused = event.getSource().hasDamageTypeHolder(AscensionDamageTypeHolders.PATH);
        boolean mundanePhysical = !pathInfused && !event.getSource().is(DamageTypeTags.IS_FIRE);

        double multiplier = 1.0D;
        for (Identifier traitId : data.getTraits()) {
            MobCultivationTraitDefinition trait = MobCultivationTraitManager.get(traitId);
            if (trait == null) continue;
            if (mundanePhysical) {
                double reduction = trait.mundanePhysicalReduction();
                double spirit = MobCultivationManager.getEntityData(mob).getSource().getStat(AscensionStats.SPIRIT.get());
                reduction += Math.min(0.45D, spirit * trait.spiritReductionScale());
                if (trait.mundaneNullificationRealmScore() >= 0
                        && MobCultivationManager.getRealmScore(mob) >= trait.mundaneNullificationRealmScore()) {
                    reduction = 1.0D;
                }
                multiplier *= 1.0D - Math.clamp(reduction, 0.0D, 1.0D);
            } else if (pathInfused) {
                multiplier *= 1.0D - trait.pathInfusedReduction();
            }
            if (event.getSource().is(DamageTypeTags.IS_FIRE)) multiplier *= trait.fireDamageMultiplier();
        }
        event.setDamage(event.getDamage() * Math.max(0.0D, multiplier));
    }
}
