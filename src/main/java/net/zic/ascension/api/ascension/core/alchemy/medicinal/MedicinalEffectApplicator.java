package net.zic.ascension.api.ascension.core.alchemy.medicinal;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.zic.ascension.api.ascension.capabilities.CoreCapabilities;
import net.zic.ascension.api.ascension.capabilities.EntityQiProvider;
import net.zic.ascension.impl.resource.AscensionResourceSources;
import net.zic.ascension.impl.resource.stamina.StaminaService;

/**
 * Applies stored medicinal effects through a small set of validated gameplay operations.
 * The server resolves actions from datapacks; unknown effects do nothing rather than guessing.
 */
public final class MedicinalEffectApplicator {
    private MedicinalEffectApplicator() {
    }

    public static void consume(MedicinalProfile profile, LivingEntity consumer) {
        if (profile == null || !(consumer.level() instanceof ServerLevel server) || profile.composition().isEmpty()) return;
        Registry<MedicinalEffectAction> actions = MedicinalRegistries.ACTIONS.get(server.registryAccess());
        for (MedicinalProfile.Effect effect : profile.effects()) {
            MedicinalEffectAction action = actions.getValue(effect.id());
            if (action == null) continue;
            double power = Mth.clamp(effect.potency(), 0.0D, 64.0D) * action.scale();
            if (power <= 0.0D) continue;
            switch (action.action()) {
                case "health" -> consumer.heal((float) Math.min(consumer.getMaxHealth(), consumer.getMaxHealth() * power));
                case "qi" -> {
                    EntityQiProvider qi = consumer.getCapability(CoreCapabilities.ASCENSION_ENTITY_QI_PROVIDER);
                    if (qi != null && qi.getMaxQi() > 0.0D) qi.regenQi(qi.getMaxQi() * Math.min(1.0D, power));
                }
                case "stamina" -> {
                    double maximum = StaminaService.getMaximumStamina(consumer);
                    if (maximum > 0.0D) StaminaService.restore(consumer, AscensionResourceSources.DIRECT,
                            maximum * Math.min(1.0D, power));
                }
                case "damage" -> consumer.hurtServer(server, server.damageSources().magic(), (float) Math.min(20.0D, power));
                case "cleanse" -> {
                    consumer.removeEffect(MobEffects.POISON);
                    consumer.removeEffect(MobEffects.WITHER);
                    consumer.removeEffect(MobEffects.WEAKNESS);
                }
                case "status" -> action.status().ifPresent(identifier -> {
                    server.registryAccess().lookupOrThrow(Registries.MOB_EFFECT)
                            .get(ResourceKey.create(Registries.MOB_EFFECT, identifier))
                            .ifPresent(holder -> consumer.addEffect(new MobEffectInstance(holder,
                                    Math.max(20, effect.durationTicks()), Mth.clamp((int) (power / 8.0D), 0, 2))));
                });
                default -> { }
            }
        }
    }
}
