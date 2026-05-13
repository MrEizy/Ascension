package net.thejadeproject.ascension.refactor_packages.skills.custom.passive.debuff;

import net.thejadeproject.ascension.refactor_packages.skills.custom.passive.SimpleDebuffSkill;

public class BlindedSensesDebuff extends SimpleDebuffSkill {

    @Override
    protected String getTitleKey() {
        return "ascension.skill.blinded_senses_debuff";
    }

    @Override
    protected String getDescriptionKey() {
        return "ascension.skill.blinded_senses_debuff.description";
    }
}