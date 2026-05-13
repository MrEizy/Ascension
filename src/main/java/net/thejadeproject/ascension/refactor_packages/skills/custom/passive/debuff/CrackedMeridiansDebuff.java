package net.thejadeproject.ascension.refactor_packages.skills.custom.passive.debuff;

import net.thejadeproject.ascension.refactor_packages.skills.custom.passive.SimpleDebuffSkill;

public class CrackedMeridiansDebuff extends SimpleDebuffSkill {

    @Override
    protected String getTitleKey() {
        return "ascension.skill.cracked_meridians_debuff";
    }

    @Override
    protected String getDescriptionKey() {
        return "ascension.skill.cracked_meridians_debuff.description";
    }
}