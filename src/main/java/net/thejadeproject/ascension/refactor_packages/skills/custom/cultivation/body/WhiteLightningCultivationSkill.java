package net.thejadeproject.ascension.refactor_packages.skills.custom.cultivation.body;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
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

public class WhiteLightningCultivationSkill extends SimplePassiveSkill {

    private static final float  MIN_DAMAGE        = 10.0f;
    private static final double BASE_MULTIPLIER   = 3.3;
    private static final double UNARMED_BONUS     = 0.25;
    private static final double NO_ARMOR_BONUS    = 0.20;
    private static final double DEBUFF_BONUS      = 0.35;

    public WhiteLightningCultivationSkill() {
        NeoForge.EVENT_BUS.addListener(this::onLivingDamage);
    }

    @SubscribeEvent
    public void onLivingDamage(LivingDamageEvent.Post event) {
        if (event.getEntity().level().isClientSide()) return;
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        float damage = event.getNewDamage();
        if (damage < MIN_DAMAGE) return;

        IEntityData entityData = player.getData(ModAttachments.ENTITY_DATA);
        PathData bodyPath = entityData.getPathData(ModPaths.BODY.getId());
        if (bodyPath == null || bodyPath.isBreakingThrough()) return;

        if (!entityData.hasSkill(ModSkills.WHITE_LIGHTNING_CULTIVATION_SKILL.getId())) return;

        EntityQiContainer qiContainer = entityData.getQiContainer();
        if (qiContainer == null) return;
        if (!qiContainer.hasQi(damage)) return;
        if (!qiContainer.tryConsumeQi(damage)) return;

        double multiplier = BASE_MULTIPLIER * getCultivationMultiplier(player);
        double gain = damage * multiplier;

        ITechnique technique = bodyPath.getLastUsedTechnique() == null ? null :
                AscensionRegistries.Techniques.TECHNIQUES_REGISTRY.get(bodyPath.getLastUsedTechnique());

        if (technique != null && bodyPath.getCurrentRealmProgress() + gain >= technique.getMaxQiForRealm(bodyPath.getMajorRealm(), bodyPath.getMinorRealm())) {
            bodyPath.setCurrentRealmProgress(technique.getMaxQiForRealm(bodyPath.getMajorRealm(), bodyPath.getMinorRealm()));

            if (bodyPath.getMinorRealm() < technique.getMaxMinorRealm(bodyPath.getMajorRealm()) && technique.canBreakthroughMinorRealm(
                    entityData,
                    bodyPath.getMajorRealm(),
                    bodyPath.getMinorRealm(),
                    bodyPath.getCurrentRealmProgress()
            )) {
                bodyPath.handleRealmChange(bodyPath.getMajorRealm(), bodyPath.getMinorRealm() + 1, entityData);
            } else if (bodyPath.getMajorRealm() < technique.getMaxMajorRealm()
                    && technique.canBreakthrough(entityData, bodyPath.getMajorRealm(), bodyPath.getMinorRealm(), bodyPath.getCurrentRealmProgress())) {
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

    private double getCultivationMultiplier(ServerPlayer player) {
        double bonus = 1.0;

        if (isUnarmed(player))   bonus += UNARMED_BONUS;
        if (hasNoArmor(player))  bonus += NO_ARMOR_BONUS;
        if (hasDebuffs(player))  bonus += DEBUFF_BONUS;

        return bonus;
    }

    private boolean isUnarmed(LivingEntity entity) {
        return entity.getMainHandItem().isEmpty() && entity.getOffhandItem().isEmpty();
    }

    private boolean hasNoArmor(LivingEntity entity) {
        return entity.getItemBySlot(EquipmentSlot.HEAD).isEmpty()
                && entity.getItemBySlot(EquipmentSlot.CHEST).isEmpty()
                && entity.getItemBySlot(EquipmentSlot.LEGS).isEmpty()
                && entity.getItemBySlot(EquipmentSlot.FEET).isEmpty();
    }

    private boolean hasDebuffs(LivingEntity entity) {
        for (MobEffectInstance effect : entity.getActiveEffects()) {
            if (!effect.getEffect().value().isBeneficial()) return true;
        }
        return false;
    }

    @Override
    protected String getTitleKey() {
        return "ascension.skill.white_lightning_cultivation_skill";
    }

    @Override
    protected String getDescriptionKey() {
        return "ascension.skill.white_lightning_cultivation_skill.description";
    }

    @Override
    protected String getIconPath() {
        return "textures/spells/icon/placeholder_white.png";
    }
}
