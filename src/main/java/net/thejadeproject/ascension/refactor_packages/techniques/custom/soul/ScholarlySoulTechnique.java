package net.thejadeproject.ascension.refactor_packages.techniques.custom.soul;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.thejadeproject.ascension.refactor_packages.breakthroughs.IBreakthroughInstance;
import net.thejadeproject.ascension.refactor_packages.entity_data.IEntityData;
import net.thejadeproject.ascension.refactor_packages.forms.forms.ModForms;
import net.thejadeproject.ascension.refactor_packages.paths.ModPaths;
import net.thejadeproject.ascension.refactor_packages.paths.PathData;
import net.thejadeproject.ascension.refactor_packages.registries.AscensionRegistries;
import net.thejadeproject.ascension.refactor_packages.skills.custom.ModSkills;
import net.thejadeproject.ascension.refactor_packages.techniques.ITechniqueData;
import net.thejadeproject.ascension.refactor_packages.techniques.custom.GenericTechnique;
import net.thejadeproject.ascension.refactor_packages.techniques.custom.stat_change_handlers.BasicStatChangeHandler;
import net.thejadeproject.ascension.refactor_packages.techniques.custom.technique_data.ScholarlySoulTechniqueData;
import net.thejadeproject.ascension.refactor_packages.techniques.helpers.TechniqueSkillHelper;

import java.util.Set;

public class ScholarlySoulTechnique extends GenericTechnique {

    private static final double BASE_RATE = 2.0D;
    private static final int SOUL_NEEDLE_UNLOCK_REALM = 2;
    private static final int SOUL_SUPPRESSION_UNLOCK_REALM = 3;

    public ScholarlySoulTechnique(BasicStatChangeHandler statChangeHandler) {
        super(
                ModPaths.SOUL.getId(),
                Component.translatable("ascension.technique.scholarly_soul_technique"),
                BASE_RATE,
                Set.of()
        );

        setStatChangeHandler(statChangeHandler);
    }

    @Override
    public Component getShortDescription() {
        return Component.translatable("ascension.technique.scholarly_soul_technique.description.short");
    }

    @Override
    public Component getDescription() {
        return Component.translatable(
                "ascension.technique.scholarly_soul_technique.description"
        );
    }

    @Override
    public void onTechniqueAdded(IEntityData heldEntity) {
        PathData pathData = heldEntity.getPathData(getPath());
        ResourceLocation techniqueId = AscensionRegistries.Techniques.TECHNIQUES_REGISTRY.getKey(this);

        if (pathData != null && techniqueId != null && pathData.getTechniqueData(techniqueId) == null) {
            pathData.addTechniqueData(techniqueId, freshTechniqueData(heldEntity));
        }

        heldEntity.giveSkill(
                ModSkills.SCHOLARLY_SOUL_CULTIVATION_SKILL.getId(),
                ModForms.MORTAL_VESSEL.getId()
        );

        heldEntity.getPathBonusHandler().addPathBonus(ModPaths.SOUL.getId(), 1.0D);

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
                ModSkills.SCHOLARLY_SOUL_CULTIVATION_SKILL.getId(),
                ModForms.MORTAL_VESSEL.getId()
        );

        heldEntity.getPathBonusHandler().removePathBonus(ModPaths.SOUL.getId(), 1.0D);
        refreshRealmUnlockSkills(heldEntity, -1);
        clearUniversalTechniqueSkills(heldEntity);
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
        TechniqueSkillHelper.refreshSkill(
                entityData,
                ModSkills.SOUL_NEEDLE.getId(),
                majorRealm >= SOUL_NEEDLE_UNLOCK_REALM
        );

        TechniqueSkillHelper.refreshSkill(
                entityData,
                ModSkills.SOUL_SUPPRESSION.getId(),
                majorRealm >= SOUL_SUPPRESSION_UNLOCK_REALM
        );
    }

    public boolean canCultivateMajorRealm(ITechniqueData techniqueData, int majorRealm) {
        if (!(techniqueData instanceof ScholarlySoulTechniqueData scholarlyData)) {
            return majorRealm <= 1;
        }

        return scholarlyData.canCultivateMajorRealm(majorRealm);
    }

    @Override
    public boolean isCompatibleWith(ResourceLocation technique) {
        return AscensionRegistries.Techniques.TECHNIQUES_REGISTRY.get(technique) instanceof ScholarlySoulTechnique;
    }

    @Override
    public ITechniqueData freshTechniqueData(IEntityData heldEntity) {
        return new ScholarlySoulTechniqueData();
    }

    @Override
    public ITechniqueData fromCompound(CompoundTag tag) {
        return new ScholarlySoulTechniqueData(tag);
    }

    @Override
    public ITechniqueData fromNetwork(RegistryFriendlyByteBuf buf) {
        return new ScholarlySoulTechniqueData(buf);
    }
}