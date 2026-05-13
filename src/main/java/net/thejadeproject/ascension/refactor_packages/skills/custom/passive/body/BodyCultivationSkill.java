package net.thejadeproject.ascension.refactor_packages.skills.custom.passive.body;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.thejadeproject.ascension.AscensionCraft;
import net.thejadeproject.ascension.data_attachments.ModAttachments;
import net.thejadeproject.ascension.refactor_packages.entity_data.IEntityData;
import net.thejadeproject.ascension.refactor_packages.paths.ModPaths;
import net.thejadeproject.ascension.refactor_packages.paths.PathData;
import net.thejadeproject.ascension.refactor_packages.qi.EntityQiContainer;
import net.thejadeproject.ascension.refactor_packages.breakthroughs.IBreakthroughInstance;
import net.thejadeproject.ascension.refactor_packages.registries.AscensionRegistries;
import net.thejadeproject.ascension.refactor_packages.skills.custom.passive.SimplePassiveSkill;
import net.thejadeproject.ascension.refactor_packages.techniques.ITechnique;

public class BodyCultivationSkill extends SimplePassiveSkill {

    private static final float   MIN_DAMAGE       = 10.0f;
    private static final double  BASE_MULTIPLIER  = 3.3;

    private final String titleKey;
    private final String descriptionKey;
    private final ResourceLocation skillId;

    public BodyCultivationSkill(String titleKey, String descriptionKey, ResourceLocation skillId) {
        this.titleKey = titleKey;
        this.descriptionKey = descriptionKey;
        this.skillId = skillId;
        NeoForge.EVENT_BUS.addListener(this::onLivingDamage);
    }

    @SubscribeEvent
    public  void onLivingDamage(LivingDamageEvent.Post event) {
        if (event.getEntity().level().isClientSide()) return;
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        float damage = event.getNewDamage();
        if (damage < MIN_DAMAGE) return;

        IEntityData entityData = player.getData(ModAttachments.ENTITY_DATA);
        if (!entityData.hasSkill(skillId)) return;

        PathData bodyPath = entityData.getPathData(ModPaths.BODY.getId());
        if (bodyPath == null || bodyPath.isBreakingThrough()) return;

        EntityQiContainer qiContainer = entityData.getQiContainer();
        if (qiContainer == null) return;
        if (!qiContainer.hasQi(damage)) return;
        if (!qiContainer.tryConsumeQi(damage)) return;

        double bodyBonus = Math.max(1.0D, entityData.getPathBonusHandler().getPathBonus(ModPaths.BODY.getId()));
        double gain = damage * BASE_MULTIPLIER * bodyBonus;
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

    @Override
    protected String getTitleKey() { return titleKey; }

    @Override
    protected String getDescriptionKey() { return descriptionKey; }

    @Override
    protected String getIconPath() { return "textures/spells/icon/placeholder.png"; }
}
