package net.thejadeproject.ascension.refactor_packages.skills.custom.passive.weapon;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.thejadeproject.ascension.refactor_packages.paths.ModPaths;
import net.thejadeproject.ascension.util.ModTags;

public class BladeMasterySkill extends GenericWeaponMasterySkill {

    @Override
    protected ResourceLocation getPathId() {
        return ModPaths.BLADE.getId();
    }

    @Override
    protected TagKey<Item> getWeaponTag() {
        return ModTags.Items.BLADE;
    }

    @Override
    protected String getTitleKey() {
        return "ascension.skill.blade_mastery_skill";
    }

    @Override
    protected String getDescriptionKey() {
        return "ascension.skill.blade_mastery.description_skill";
    }

}