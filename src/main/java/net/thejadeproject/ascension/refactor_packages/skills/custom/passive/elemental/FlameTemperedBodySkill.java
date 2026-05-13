package net.thejadeproject.ascension.refactor_packages.skills.custom.passive.elemental;

import net.thejadeproject.ascension.refactor_packages.skills.custom.passive.SimplePassiveSkill;

public class FlameTemperedBodySkill extends SimplePassiveSkill {

    // marker class

    @Override
    protected String getTitleKey() {
        return "ascension.skill.flame_tempered_body";
    }

    @Override
    protected String getDescriptionKey() {
        return "ascension.skill.flame_tempered_body.description";
    }

    @Override
    protected String getIconPath() {
        return "textures/spells/icon/placeholder.png";
    }
}