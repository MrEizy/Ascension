package net.zic.ascension.api.ascension.core.alchemy.medicinal;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.zic.ascension.api.ascension.core.path.PathInstance;
import net.zic.ascension.api.ascension.core.source.AscensionOriginSourceHelper;

public final class MedicinalConsumptionService {
    private MedicinalConsumptionService() {
    }

    public static void consume(MedicinalProfile profile, LivingEntity consumer, boolean refined) {
        if (profile == null || profile.composition().isEmpty()
                || !(consumer.level() instanceof ServerLevel server)) return;

        var substance = profile.composition().substance();
        int consumerRealm = 0;
        for (PathInstance<?> path : AscensionOriginSourceHelper.getPathInstances(
                AscensionOriginSourceHelper.getEntitySource(consumer))) {
            consumerRealm = Math.max(consumerRealm, path.getCurrentMajorRealm());
        }
        int disparity = Math.max(0, substance.rankTier() - consumerRealm);
        double purity = substance.purity();
        double instability = substance.instability();
        double absorption = (refined ? 0.90D : 0.28D) * (0.60D + 0.40D * purity)
                / (1.0D + 0.30D * disparity);
        absorption *= MedicinalImpurities.efficiency(consumer);
        MedicinalEffectApplicator.consume(profile, consumer, absorption, refined ? 0.65D : 1.30D);
        MedicinalImpurities.add(consumer, MedicinalImpurities.contaminants(profile, refined));

        double impurityBurden = (1.0D - purity) * (refined ? 0.6D : 2.2D);
        double unstableBurden = instability * (refined ? 0.35D : 0.90D);
        double toxicityBurden = profile.composition().toxicity() * (refined ? 0.85D : 1.25D);
        double overloadBurden = disparity * (refined ? 0.65D : 1.65D)
                * Mth.clamp(substance.amplifier(), 0.25D, 4.0D);
        double totalBurden = impurityBurden + unstableBurden + toxicityBurden + overloadBurden;

        if (totalBurden >= 2.0D) {
            consumer.addEffect(new MobEffectInstance(MobEffects.WEAKNESS,
                    Mth.clamp((int) (80 + totalBurden * 60), 80, 1200), 0));
        }
        if (totalBurden >= 4.0D) {
            consumer.addEffect(new MobEffectInstance(MobEffects.NAUSEA,
                    Mth.clamp((int) (60 + totalBurden * 30), 60, 600), 0));
            consumer.hurtServer(server, server.damageSources().magic(),
                    (float) Mth.clamp((totalBurden - 3.0D) * 1.5D, 1.0D, 20.0D));
        }
    }
}
