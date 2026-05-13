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
import net.thejadeproject.ascension.refactor_packages.techniques.helpers.TechniqueSkillHelper;

import java.util.Set;

public class ZenithSunTechnique extends GenericTechnique {

    public static final double BASE_RATE = 1.5D;
    private static final int SOUL_NEEDLE_UNLOCK_REALM = 2;
    private static final int SOUL_SUPPRESSION_UNLOCK_REALM = 3;

    public ZenithSunTechnique(BasicStatChangeHandler statChangeHandler) {
        super(
                ModPaths.SOUL.getId(),
                Component.translatable("ascension.technique.zenith_sun_scripture"),
                BASE_RATE,
                Set.of()
        );
        setStatChangeHandler(statChangeHandler);
    }

    @Override
    public Component getShortDescription() {
        return Component.translatable("ascension.technique.zenith_sun_scripture.description.short");
    }

    @Override
    public Component getDescription() {
        return Component.translatable(
                "ascension.technique.zenith_sun_scripture.description"
        );
    }

    @Override
    public void onTechniqueAdded(IEntityData heldEntity) {
        heldEntity.giveSkill(ModSkills.ZENITH_SUN_CULTIVATION_SKILL.getId(), ModForms.MORTAL_VESSEL.getId());

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
        heldEntity.removeSkill(ModSkills.ZENITH_SUN_CULTIVATION_SKILL.getId(), ModForms.MORTAL_VESSEL.getId());
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
        TechniqueSkillHelper.refreshSkill(
                entityData,
                ModSkills.SOUL_LANTERN.getId(),
                majorRealm >= SOUL_SUPPRESSION_UNLOCK_REALM
        );
    }

    @Override
    public boolean isCompatibleWith(ResourceLocation technique) {
        var tech = AscensionRegistries.Techniques.TECHNIQUES_REGISTRY.get(technique);
        return !(tech instanceof ZenithSunTechnique)
                && !(tech instanceof DawningSunTechnique)
                && !(tech instanceof PaleMoonTechnique)
                && !(tech instanceof GibbousMoonTechnique)
                && !(tech instanceof ScholarlySoulTechnique);
    }

    @Override
    public ITechniqueData freshTechniqueData(IEntityData heldEntity) { return null; }

    @Override
    public ITechniqueData fromCompound(CompoundTag tag) { return null; }

    @Override
    public ITechniqueData fromNetwork(RegistryFriendlyByteBuf buf) { return null; }
}
