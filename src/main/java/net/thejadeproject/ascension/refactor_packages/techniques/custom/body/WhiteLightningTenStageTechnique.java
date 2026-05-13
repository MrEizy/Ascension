package net.thejadeproject.ascension.refactor_packages.techniques.custom.body;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.thejadeproject.ascension.refactor_packages.entity_data.IEntityData;
import net.thejadeproject.ascension.refactor_packages.forms.forms.ModForms;
import net.thejadeproject.ascension.refactor_packages.paths.ModPaths;
import net.thejadeproject.ascension.refactor_packages.paths.PathData;
import net.thejadeproject.ascension.refactor_packages.registries.AscensionRegistries;
import net.thejadeproject.ascension.refactor_packages.skills.custom.ModSkills;
import net.thejadeproject.ascension.refactor_packages.techniques.ITechnique;
import net.thejadeproject.ascension.refactor_packages.techniques.ITechniqueData;
import net.thejadeproject.ascension.refactor_packages.techniques.custom.GenericTechnique;
import net.thejadeproject.ascension.refactor_packages.techniques.custom.stat_change_handlers.BasicStatChangeHandler;

import java.util.Set;

public class WhiteLightningTenStageTechnique extends GenericTechnique {

    private static final int PURGE_UNLOCK_REALM = 2;
    private static final int FIST_UNLOCK_REALM = 3;

    public WhiteLightningTenStageTechnique(BasicStatChangeHandler statChangeHandler) {
        super(
                ModPaths.BODY.getId(),
                Component.translatable("ascension.technique.white_lightning_ten_stage_technique"),
                2.0D,
                Set.of(ModPaths.FIST.getId())
        );

        setStatChangeHandler(statChangeHandler);
    }

    @Override
    public Component getShortDescription() {
        return Component.translatable(
                "ascension.technique.white_lightning_ten_stage_technique.description.short"
        );
    }

    @Override
    public Component getDescription() {
        return Component.translatable(
                "ascension.technique.white_lightning_ten_stage_technique.description"
        );
    }

    @Override
    public void onTechniqueAdded(IEntityData heldEntity) {
        heldEntity.giveSkill(
                ModSkills.WHITE_LIGHTNING_CULTIVATION_SKILL.getId(),
                ModForms.MORTAL_VESSEL.getId()
        );

        heldEntity.getPathBonusHandler().addPathBonus(ModPaths.FIST.getId(), 1.0D);
        ensurePathData(heldEntity, ModPaths.FIST.getId());

        PathData pathData = heldEntity.getPathData(getPath());

        refreshUniversalTechniqueSkills(heldEntity);

        refreshRealmUnlockSkills(
                heldEntity,
                pathData == null ? 0 : pathData.getMajorRealm()
        );
    }

    @Override
    public void onTechniqueRemoved(IEntityData heldEntity, ITechniqueData techniqueData) {
        PathData pathData = heldEntity.getPathData(getPath());

        if (pathData != null) {
            pathData.handleRealmChange(pathData.getMajorRealm(), 0, heldEntity);
        }

        heldEntity.removeSkill(
                ModSkills.WHITE_LIGHTNING_CULTIVATION_SKILL.getId(),
                ModForms.MORTAL_VESSEL.getId()
        );

        heldEntity.getPathBonusHandler().removePathBonus(ModPaths.FIST.getId(), 1.0D);

        refreshRealmUnlockSkills(heldEntity, -1);
        refreshUniversalTechniqueSkills(heldEntity);
    }

    @Override
    public void onRealmChange(
            IEntityData entityData,
            int oldMajorRealm,
            int oldMinorRealm,
            int newMajorRealm,
            int newMinorRealm
    ) {
        super.onRealmChange(entityData, oldMajorRealm, oldMinorRealm, newMajorRealm, newMinorRealm);
        refreshRealmUnlockSkills(entityData, newMajorRealm);
    }

    private void refreshRealmUnlockSkills(IEntityData entityData, int majorRealm) {
        refreshSkill(
                entityData,
                ModSkills.TURBID_ENERGY_PURGE.getId(),
                majorRealm >= PURGE_UNLOCK_REALM
        );

        refreshSkill(
                entityData,
                ModSkills.WHITE_LIGHTNING_FIST.getId(),
                majorRealm >= FIST_UNLOCK_REALM
        );

    }

    private void refreshSkill(IEntityData entityData, ResourceLocation skillId, boolean shouldHave) {
        if (shouldHave) {
            if (!entityData.hasSkill(skillId)) {
                entityData.giveSkill(skillId, ModForms.MORTAL_VESSEL.getId());
            }

            return;
        }

        if (entityData.hasSkill(skillId)) {
            entityData.removeSkill(skillId, ModForms.MORTAL_VESSEL.getId());
        }
    }

    @Override
    public boolean isCompatibleWith(ResourceLocation technique) {
        ITechnique otherTechnique = AscensionRegistries.Techniques.TECHNIQUES_REGISTRY.get(technique);
        return otherTechnique instanceof WhiteLightningTenStageTechnique;
    }

    private void ensurePathData(IEntityData entityData, ResourceLocation path) {
        if (entityData.getPathData(path) != null) return;

        entityData.addPathData(
                path,
                AscensionRegistries.Paths.PATHS_REGISTRY.get(path).freshPathData(entityData)
        );
    }
}