package net.thejadeproject.ascension.refactor_packages.techniques.helpers;

import net.minecraft.resources.ResourceLocation;
import net.thejadeproject.ascension.refactor_packages.entity_data.IEntityData;
import net.thejadeproject.ascension.refactor_packages.forms.forms.ModForms;
import net.thejadeproject.ascension.refactor_packages.skills.custom.ModSkills;

public final class TechniqueSkillHelper {

    private static final int QI_RELEASE_UNLOCK_REALM = 1;
    private static final int REGENERATION_UNLOCK_REALM = 2;
    private static final int QI_FLIGHT_UNLOCK_REALM = 2;
    private static final int QI_SUSTAINED_UNLOCK_REALM = 3;
    private static final int TRUE_FLIGHT_UNLOCK_REALM = 4;

    private TechniqueSkillHelper() {
    }

    public static void refreshUniversal(IEntityData entityData, int majorRealm) {
        refreshSkill(
                entityData,
                ModSkills.QI_RELEASE.getId(),
                majorRealm >= QI_RELEASE_UNLOCK_REALM
        );

        refreshSkill(
                entityData,
                ModSkills.REGENERATION_BOOST.getId(),
                majorRealm >= REGENERATION_UNLOCK_REALM
        );

        refreshSkill(
                entityData,
                ModSkills.QI_SUSTAINED_BODY.getId(),
                majorRealm >= QI_SUSTAINED_UNLOCK_REALM
        );

        refreshSkill(
                entityData,
                ModSkills.TRUE_FLIGHT.getId(),
                majorRealm >= TRUE_FLIGHT_UNLOCK_REALM
        );

        refreshSkill(
                entityData,
                ModSkills.AIR_STEP.getId(),
                majorRealm >= QI_FLIGHT_UNLOCK_REALM
        );
    }


    public static void refreshSkill(IEntityData entityData, ResourceLocation skillId, boolean shouldHave) {
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
}