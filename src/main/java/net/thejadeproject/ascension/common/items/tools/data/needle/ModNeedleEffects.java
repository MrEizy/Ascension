package net.thejadeproject.ascension.common.items.tools.data.needle;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.thejadeproject.ascension.AscensionCraft;
import net.thejadeproject.ascension.refactor_packages.forms.forms.ModForms;
import net.thejadeproject.ascension.refactor_packages.skills.custom.passive.debuff.skill_data.DebuffSkillHelper;
import net.thejadeproject.ascension.refactor_packages.skills.custom.ModSkills;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ModNeedleEffects {

    private static final Map<ResourceLocation, INeedleEffect> REGISTRY = new HashMap<>();


    public static final INeedleEffect POISON = register(new INeedleEffect() {
        private final ResourceLocation id = ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "needle_poison");

        @Override
        public ResourceLocation getId() {return id;}

        @Override
        public void onHit(LivingEntity target, LivingEntity shooter, Projectile projectile) {
            target.addEffect(new MobEffectInstance(MobEffects.POISON, 100, 0));
        }
    });
    public static final INeedleEffect BLINDNESS = register(new INeedleEffect() {
        private final ResourceLocation id = ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "needle_blindness");

        @Override
        public ResourceLocation getId() {return id;}

        @Override
        public void onHit(LivingEntity target, LivingEntity shooter, Projectile projectile) {
            target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60, 0));
        }
    });
    public static final INeedleEffect SLOWNESS = register(new INeedleEffect() {
        private final ResourceLocation id = ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "needle_slowness");

        @Override
        public ResourceLocation getId() {return id;}

        @Override
        public void onHit(LivingEntity target, LivingEntity shooter, Projectile projectile) {
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 80, 0));
        }
    });
    public static final INeedleEffect WEAKNESS = register(new INeedleEffect() {
        private final ResourceLocation id = ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "needle_weakness");

        @Override
        public ResourceLocation getId() {return id;}

        @Override
        public void onHit(LivingEntity target, LivingEntity shooter, Projectile projectile) {
            target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 0));
        }
    });

    // Only Checks ServerPlayer because I don't think mobs can have skills? maybe
    public static final INeedleEffect CRACKED_MERIDIANS = temporaryPassive(
            "needle_cracked_meridians",
            ModSkills.CRACKED_MERIDIANS.getId(), // the player takes 50% more damage
            20 * 8 // lasts 8 seconds
    );

    public static final INeedleEffect BLINDED_SENSES = temporaryPassive(
            "needle_blinded_senses",
            ModSkills.BLINDED_SENSES.getId(), // blindness like effect
            20 * 12 // lasts 12 seconds
    );

    public static final INeedleEffect PARALYZED_BODY = temporaryPassive(
            "needle_paralyzed_body",
            ModSkills.PARALYZED_BODY.getId(), // potentially suppresses player movement? idk might need to change this
            20 * 6 // lasts 6 seconds
    );

    public static final INeedleEffect VENOMOUS_MERIDIANS = temporaryPassive(
            "needle_venomous_meridians",
            ModSkills.VENOMOUS_MERIDIANS.getId(), // DoT based on max health
            20 * 10 // lasts 10 seconds
    );
    public static final INeedleEffect SCORCHING_YANG_POISON = temporaryPassive(
            "needle_scorching_yang_poison",
            ModSkills.SCORCHING_YANG_POISON.getId(),
            20 * 10 // lasts 10 seconds
    );

    public static final INeedleEffect QI_DEVOURING_POISON = temporaryPassive(
            "needle_qi_devouring_poison",
            ModSkills.QI_DEVOURING_POISON.getId(),
            20 * 10
    );
    public static final INeedleEffect CORROSIVE_POISON = temporaryPassive(
            "needle_corrosive_poison",
            ModSkills.CORROSIVE_POISON_DEBUFF.getId(),
            20 * 10
    );
    public static final INeedleEffect FROST_SILKWORM_POISON_TEMP = temporaryPassive(
            "needle_frost_silkworm_poison_temp",
            ModSkills.FROST_SILKWORM_POISON_TEMP.getId(),
            20 * 45 // 45 seconds
    );
    public static final INeedleEffect FROST_SILKWORM_POISON = temporaryPassive(
            "needle_frost_silkworm_poison",
            ModSkills.FROST_SILKWORM_POISON.getId(),
            20 * 10
    );

    private static INeedleEffect temporaryPassive(String path, ResourceLocation skillId, int durationTicks) {
        return register(new INeedleEffect() {
            private final ResourceLocation id = ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, path);

            @Override
            public ResourceLocation getId() {
                return id;
            }

            @Override
            public void onHit(LivingEntity target, LivingEntity shooter, Projectile projectile) {
                if (!(target instanceof ServerPlayer serverTarget)) return;
                DebuffSkillHelper.giveTempDebuffSource(
                        serverTarget,
                        id,
                        skillId,
                        durationTicks,
                        ModForms.MORTAL_VESSEL.getId()
                );
            }
        });
    }

    private static INeedleEffect permanentPassive(String path, ResourceLocation skillId) {
        return register(new INeedleEffect() {
            private final ResourceLocation id = ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, path);

            @Override
            public ResourceLocation getId() {
                return id;
            }

            @Override
            public void onHit(LivingEntity target, LivingEntity shooter, Projectile projectile) {
                if (!(target instanceof ServerPlayer serverTarget)) return;

                DebuffSkillHelper.givePermanentDebuff(
                        serverTarget,
                        skillId
                );
            }
        });
    }

    private static INeedleEffect register(INeedleEffect effect) {
        REGISTRY.put(effect.getId(), effect);
        return effect;
    }

    public static Optional<INeedleEffect> get(ResourceLocation id) {
        return Optional.ofNullable(REGISTRY.get(id));
    }

    public static Optional<INeedleEffect> get(String id) {
        return get(ResourceLocation.parse(id));
    }
}
