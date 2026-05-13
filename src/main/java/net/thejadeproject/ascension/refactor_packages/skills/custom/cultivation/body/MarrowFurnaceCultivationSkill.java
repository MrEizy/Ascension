package net.thejadeproject.ascension.refactor_packages.skills.custom.cultivation.body;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.thejadeproject.ascension.data_attachments.ModAttachments;
import net.thejadeproject.ascension.refactor_packages.breakthroughs.IBreakthroughInstance;
import net.thejadeproject.ascension.refactor_packages.entity_data.IEntityData;
import net.thejadeproject.ascension.refactor_packages.paths.ModPaths;
import net.thejadeproject.ascension.refactor_packages.paths.PathData;
import net.thejadeproject.ascension.refactor_packages.qi.EntityQiContainer;
import net.thejadeproject.ascension.refactor_packages.registries.AscensionRegistries;
import net.thejadeproject.ascension.refactor_packages.skills.custom.ModSkills;
import net.thejadeproject.ascension.refactor_packages.skills.custom.passive.SimplePassiveSkill;
import net.thejadeproject.ascension.refactor_packages.techniques.ITechnique;

public class MarrowFurnaceCultivationSkill extends SimplePassiveSkill {

    private static final float MIN_DAMAGE = 4.0F;
    private static final double BASE_MULTIPLIER = 2.0D;
    private static final double QI_COST_MULTIPLIER = 0.35D;
    private static final double LOW_HEALTH_BONUS = 0.35D;

    public MarrowFurnaceCultivationSkill() {
        NeoForge.EVENT_BUS.addListener(this::onLivingDamage);
    }

    @SubscribeEvent
    public void onLivingDamage(LivingDamageEvent.Post event) {
        if (event.getEntity().level().isClientSide()) return;
        if (!(event.getSource().getEntity() instanceof ServerPlayer player)) return;

        float damage = event.getNewDamage();
        if (damage < MIN_DAMAGE) return;

        IEntityData entityData = player.getData(ModAttachments.ENTITY_DATA);
        if (entityData == null) return;
        if (!entityData.hasSkill(ModSkills.MARROW_FURNACE.getId())) return;

        PathData bodyPath = entityData.getPathData(ModPaths.BODY.getId());
        if (bodyPath == null || bodyPath.isBreakingThrough()) return;

        EntityQiContainer qiContainer = entityData.getQiContainer();
        if (qiContainer == null) return;

        double qiCost = damage * QI_COST_MULTIPLIER;
        if (!qiContainer.tryConsumeQi(qiCost)) return;

        double multiplier = BASE_MULTIPLIER;

        if (player.getHealth() <= player.getMaxHealth() * 0.5F) {
            multiplier += LOW_HEALTH_BONUS;
        }

        double bodyBonus = Math.max(
                1.0D,
                entityData.getPathBonusHandler().getPathBonus(ModPaths.BODY.getId())
        );

        double effectiveDamage = Math.sqrt(damage);
        double gain = effectiveDamage * multiplier * bodyBonus;

        ITechnique technique = bodyPath.getLastUsedTechnique() == null ? null :
                AscensionRegistries.Techniques.TECHNIQUES_REGISTRY.get(bodyPath.getLastUsedTechnique());

        if (technique == null) return;

        double maxProgress = technique.getMaxQiForRealm(
                bodyPath.getMajorRealm(),
                bodyPath.getMinorRealm()
        );

        if (bodyPath.getCurrentRealmProgress() + gain >= maxProgress) {
            bodyPath.setCurrentRealmProgress(maxProgress);

            if (bodyPath.getMinorRealm() < technique.getMaxMinorRealm(bodyPath.getMajorRealm())
                    && technique.canBreakthroughMinorRealm(
                    entityData,
                    bodyPath.getMajorRealm(),
                    bodyPath.getMinorRealm(),
                    bodyPath.getCurrentRealmProgress()
            )) {
                bodyPath.handleRealmChange(
                        bodyPath.getMajorRealm(),
                        bodyPath.getMinorRealm() + 1,
                        entityData
                );
            } else if (bodyPath.getMajorRealm() < technique.getMaxMajorRealm()
                    && technique.canBreakthrough(
                    entityData,
                    bodyPath.getMajorRealm(),
                    bodyPath.getMinorRealm(),
                    bodyPath.getCurrentRealmProgress()
            )) {
                IBreakthroughInstance instance = technique.freshBreakthroughData(entityData);

                if (instance != null) {
                    bodyPath.setBreakthroughInstance(instance);
                    bodyPath.setBreakingThrough(true);
                }
            }
        } else {
            bodyPath.setCurrentRealmProgress(bodyPath.getCurrentRealmProgress() + gain);
        }

        bodyPath.sync(player);
    }

    @Override
    protected String getTitleKey() {
        return "ascension.skill.marrow_furnace";
    }

    @Override
    protected String getDescriptionKey() {
        return "ascension.skill.marrow_furnace.description";
    }

    @Override
    protected String getIconPath() {
        return "textures/spells/icon/placeholder.png";
    }
}