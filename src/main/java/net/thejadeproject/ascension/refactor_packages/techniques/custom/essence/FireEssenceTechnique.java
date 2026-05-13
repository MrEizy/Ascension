package net.thejadeproject.ascension.refactor_packages.techniques.custom.essence;

import net.minecraft.network.chat.Component;
import net.thejadeproject.ascension.refactor_packages.paths.ModPaths;
import net.thejadeproject.ascension.refactor_packages.skills.custom.ModSkills;
import net.thejadeproject.ascension.refactor_packages.techniques.custom.stat_change_handlers.BasicStatChangeHandler;

public class FireEssenceTechnique extends ElementalEssenceTechnique {

    public FireEssenceTechnique(BasicStatChangeHandler statChangeHandler) {
        super(
                Component.translatable("ascension.technique.fire_essence_technique"),
                ModPaths.FIRE.getId(),
                ModSkills.FIRE_ESSENCE_CULTIVATION_SKILL.getId(),
                statChangeHandler
        );
    }

    @Override
    public Component getShortDescription() {
        return Component.translatable("ascension.technique.fire_essence_technique.description.short");
    }

    @Override
    public Component getDescription() {
        return Component.translatable(
                "ascension.technique.fire_essence_technique.description"
        );
    }
}