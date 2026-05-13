package net.thejadeproject.ascension.refactor_packages.techniques.custom.essence;

import net.minecraft.network.chat.Component;
import net.thejadeproject.ascension.refactor_packages.paths.ModPaths;
import net.thejadeproject.ascension.refactor_packages.skills.custom.ModSkills;
import net.thejadeproject.ascension.refactor_packages.techniques.custom.stat_change_handlers.BasicStatChangeHandler;

public class WaterEssenceTechnique extends ElementalEssenceTechnique {

    public WaterEssenceTechnique(BasicStatChangeHandler statChangeHandler) {
        super(
                Component.translatable("ascension.technique.water_essence_technique"),
                ModPaths.WATER.getId(),
                ModSkills.WATER_ESSENCE_CULTIVATION_SKILL.getId(),
                statChangeHandler
        );
    }

    @Override
    public Component getShortDescription() {
        return Component.translatable("ascension.technique.water_essence_technique.description.short");
    }

    @Override
    public Component getDescription() {
        return Component.translatable(
                "ascension.technique.water_essence_technique.description"
        );
    }
}