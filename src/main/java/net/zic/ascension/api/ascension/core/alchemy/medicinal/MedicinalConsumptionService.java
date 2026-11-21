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
        int consumerRealm = highestMajorRealm(consumer);
        int disparity = Math.max(0, substance.rankTier() - consumerRealm);
        double absorption = absorption(profile, consumer, refined, disparity);
        MedicinalEffectApplicator.consume(profile, consumer, absorption, refined ? 0.65D : 1.30D, refined);
        MedicinalImpurities.add(consumer, MedicinalImpurities.contaminants(profile, refined));

        double purity = substance.purity();
        double instability = substance.instability();
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

    public static double absorption(MedicinalProfile profile, LivingEntity consumer, boolean refined) {
        int disparity = Math.max(0, profile.composition().substance().rankTier() - highestMajorRealm(consumer));
        return absorption(profile, consumer, refined, disparity);
    }

    private static double absorption(MedicinalProfile profile, LivingEntity consumer,
                                     boolean refined, int disparity) {
        double purity = profile.composition().substance().purity();
        double base = (refined ? 0.90D : 0.28D) * (0.60D + 0.40D * purity)
                / (1.0D + 0.30D * disparity);
        return base * MedicinalImpurities.efficiency(consumer);
    }

    private static int highestMajorRealm(LivingEntity consumer) {
        var source = AscensionOriginSourceHelper.getEntitySource(consumer);
        if (source == null) return 0;
        int highestRealm = 0;
        for (PathInstance<?> path : AscensionOriginSourceHelper.getPathInstances(source)) {
            highestRealm = Math.max(highestRealm, path.getCurrentMajorRealm());
        }
        return highestRealm;
    }
}
