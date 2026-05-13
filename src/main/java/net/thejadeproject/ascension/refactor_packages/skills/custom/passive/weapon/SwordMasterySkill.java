package net.thejadeproject.ascension.refactor_packages.skills.custom.passive.weapon;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.thejadeproject.ascension.refactor_packages.paths.ModPaths;

public class SwordMasterySkill extends GenericWeaponMasterySkill {

    @Override
    protected ResourceLocation getPathId() {
        return ModPaths.SWORD.getId();
    }

    @Override
    protected TagKey<Item> getWeaponTag() {
        return ItemTags.SWORDS;
    }

    @Override
    protected String getTitleKey() {
        return "ascension.skill.sword_mastery_skill";
    }

    @Override
    protected String getDescriptionKey() {
        return "ascension.skill.sword_mastery_skill.description";
    }

}