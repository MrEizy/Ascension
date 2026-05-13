package net.thejadeproject.ascension.refactor_packages.techniques.custom.essence;

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

public abstract class ElementalEssenceTechnique extends GenericTechnique {

    private final ResourceLocation elementPath;
    private final ResourceLocation cultivationSkill;

    protected ElementalEssenceTechnique(
            Component title,
            ResourceLocation elementPath,
            ResourceLocation cultivationSkill,
            BasicStatChangeHandler statChangeHandler
    ) {
        super(
                ModPaths.ESSENCE.getId(),
                title,
                2.0D,
                Set.of(elementPath)
        );

        this.elementPath = elementPath;
        this.cultivationSkill = cultivationSkill;

        setStatChangeHandler(statChangeHandler);
    }

    public ResourceLocation getElementPath() {
        return elementPath;
    }

    @Override
    public void onTechniqueAdded(IEntityData heldEntity) {
        heldEntity.giveSkill(
                cultivationSkill,
                ModForms.MORTAL_VESSEL.getId()
        );

        heldEntity.getPathBonusHandler().addPathBonus(elementPath, 1.0D);

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
                cultivationSkill,
                ModForms.MORTAL_VESSEL.getId()
        );

        heldEntity.getPathBonusHandler().removePathBonus(elementPath, 1.0D);
        refreshUniversalTechniqueSkills(heldEntity);
        refreshRealmUnlockSkills(heldEntity, -1);
    }

    @Override
    public void onRealmChange(
            IEntityData entityData,
            int oldMajorRealm,
            int oldMinorRealm,
            int newMajorRealm,
            int newMinorRealm
    ) {
        super.onRealmChange(
                entityData,
                oldMajorRealm,
                oldMinorRealm,
                newMajorRealm,
                newMinorRealm
        );

        refreshRealmUnlockSkills(entityData, newMajorRealm);
    }

    protected void refreshRealmUnlockSkills(IEntityData entityData, int majorRealm) {
        if (getElementPath().equals(ModPaths.FIRE.getId())) {
            refreshSkill(
                    entityData,
                    ModSkills.FIRE_SPRAY.getId(),
                    majorRealm >= 2
            );
            refreshSkill(
                    entityData,
                    ModSkills.FLAME_TEMPERED_BODY.getId(),
                    majorRealm >= 3
            );
        } else if (getElementPath().equals(ModPaths.EARTH.getId())) {
            refreshSkill(
                    entityData,
                    ModSkills.STONE_ROOT.getId(),
                    majorRealm >= 2
            );
        } else if (getElementPath().equals(ModPaths.WATER.getId())) {
            refreshSkill(
                    entityData,
                    ModSkills.AQUATIC_CIRCULATION.getId(),
                    majorRealm >= 4
            );
        } else if (getElementPath().equals(ModPaths.WOOD.getId())) {
            refreshSkill(
                    entityData,
                    ModSkills.VERDANT_RECOVERY.getId(),
                    majorRealm >= 2
            );
            refreshSkill(
                    entityData,
                    ModSkills.THORN_BIND.getId(),
                    majorRealm >= 3
            );
        } else if (getElementPath().equals(ModPaths.LIGHTNING.getId())) {
            refreshSkill(
                    entityData,
                    ModSkills.THUNDER_PALM.getId(),
                    majorRealm >= 2
            );
        } else if (getElementPath().equals(ModPaths.WIND.getId())) {
            refreshSkill(
                    entityData,
                    ModSkills.GALE_STEP.getId(),
                    majorRealm >= 2
            );
        }
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
        ITechnique other = AscensionRegistries.Techniques.TECHNIQUES_REGISTRY.get(technique);
        return other instanceof ElementalEssenceTechnique;
    }
}