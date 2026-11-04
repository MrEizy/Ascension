package net.zic.ascension.api.ascension.core.alchemy.medicinal;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.zic.ascension.api.ascension.core.path.PathInstance;
import net.zic.ascension.api.ascension.core.source.AscensionOriginSourceHelper;
import net.zic.ascension.api.rpg_engine.source.OriginSource;
import net.zic.ascension.api.ascension.capabilities.CoreCapabilities;
import net.zic.ascension.api.ascension.capabilities.EntityQiProvider;
import net.zic.ascension.impl.resource.AscensionResourceSources;
import net.zic.ascension.impl.resource.stamina.StaminaService;
import net.zic.ascension.impl.core.effect.SkillEffectService;

/**
 * Applies stored medicinal effects through a small set of validated gameplay operations.
 * The server resolves actions from datapacks; unknown effects do nothing rather than guessing.
 */
public final class MedicinalEffectApplicator {
    private MedicinalEffectApplicator() {
    }

    public static void consume(MedicinalProfile profile, LivingEntity consumer) {
        consume(profile, consumer, 1.0D, 1.0D);
    }

    public static void consume(MedicinalProfile profile, LivingEntity consumer, double absorption) {
        consume(profile, consumer, absorption, 1.0D);
    }

    public static void consume(MedicinalProfile profile, LivingEntity consumer,
                               double absorption, double adverseExposure) {
        if (profile == null || !(consumer.level() instanceof ServerLevel server) || profile.composition().isEmpty()) return;
        if (!Double.isFinite(absorption) || !Double.isFinite(adverseExposure)) return;
        double absorbed = Mth.clamp(absorption, 0.0D, 1.0D);
        double exposed = Mth.clamp(adverseExposure, 0.0D, 3.0D);
        Registry<MedicinalEffectDefinition> definitions = MedicinalRegistries.EFFECTS.get(server.registryAccess());
        for (MedicinalProfile.Effect effect : profile.effects()) {
            MedicinalEffectDefinition definition = definitions.getValue(effect.id());
            if (definition == null) continue;
            MedicinalEffectAction action = definition.application();
            double power = Mth.clamp(effect.potency(), 0.0D, 64.0D) * action.scale()
                    * (effect.adverse() ? exposed : absorbed);
            if (power <= 0.0D) continue;
            switch (action.action()) {
                case "health" -> {
                    int tier = Mth.clamp(profile.composition().substance().rankTier(), 0, 5);
                    double restored = power * Math.pow(1.3D, tier);
                    consumer.heal((float) Math.min(consumer.getMaxHealth() * 0.50D, restored));
                }
                case "qi" -> {
                    EntityQiProvider qi = consumer.getCapability(CoreCapabilities.ASCENSION_ENTITY_QI_PROVIDER);
                    if (qi != null && qi.getMaxQi() > 0.0D) {
                        int tier = Mth.clamp(profile.composition().substance().rankTier(), 0, 5);
                        double restored = power * 2.0D * Math.pow(2.25D, tier);
                        double limit = qi.getMaxQi() * (0.03D + 0.025D * tier);
                        qi.regenQi(Math.min(restored, limit));
                    }
                }
                case "stamina" -> {
                    double maximum = StaminaService.getMaximumStamina(consumer);
                    int tier = Mth.clamp(profile.composition().substance().rankTier(), 0, 5);
                    double fraction = Math.min(0.40D, power * 0.04D * Math.pow(1.20D, tier));
                    if (maximum > 0.0D) StaminaService.restore(consumer, AscensionResourceSources.DIRECT,
                            maximum * fraction);
                }
                case "food" -> {
                    if (consumer instanceof Player player) {
                        FoodData food = player.getFoodData();
                        food.setFoodLevel(Math.min(20, food.getFoodLevel() + Math.max(1, (int) Math.ceil(power * 1.5D))));
                        food.setSaturation(Math.min((float) food.getFoodLevel(), food.getSaturationLevel() + (float) (power * 0.8D)));
                    }
                }
                case "cultivation" -> action.path().ifPresent(pathId -> {
                    if (!(consumer instanceof Player player)) return;
                    OriginSource source = AscensionOriginSourceHelper.getEntitySource(player);
                    if (source == null) return;
                    PathInstance<?> path = AscensionOriginSourceHelper.getPathInstance(source, pathId);
                    if (path == null || !path.canProgress()) return;
                    int tier = Mth.clamp(profile.composition().substance().rankTier(), 0, 5);
                    double fraction = Math.min(0.03D + 0.008D * tier,
                            power * 0.002D * Math.pow(1.35D, tier));
                    double maximum = Math.max(0.0D, path.getMaxProgress());
                    double remaining = Math.max(0.0D, maximum - path.getProgress());
                    double progress = Math.min(remaining, maximum * fraction);
                    if (progress <= 0.0D || !Double.isFinite(progress)) return;
                    path.progressPath(progress, source, player);
                    AscensionOriginSourceHelper.markPathDirty(source, pathId);
                });
                case "damage" -> consumer.hurtServer(server, server.damageSources().magic(), (float) Math.min(20.0D, power));
                case "cleanse" -> {
                    if (power >= 0.35D) {
                        consumer.removeEffect(MobEffects.POISON);
                        consumer.removeEffect(MobEffects.WEAKNESS);
                    }
                    if (power >= 2.0D) consumer.removeEffect(MobEffects.WITHER);
                }
                case "status" -> action.status().ifPresent(identifier -> {
                    server.registryAccess().lookupOrThrow(Registries.MOB_EFFECT)
                            .get(ResourceKey.create(Registries.MOB_EFFECT, identifier))
                            .ifPresent(holder -> consumer.addEffect(new MobEffectInstance(holder,
                                    Math.max(20, effect.durationTicks()), Mth.clamp((int) (power / 8.0D), 0, 2))));
                });
                case "skill_effect" -> action.effect().ifPresent(identifier ->
                        SkillEffectService.apply(consumer, identifier, consumer.getUUID(), null,
                                Math.max(20, effect.durationTicks()), power));
                default -> { }
            }
        }
    }
}
