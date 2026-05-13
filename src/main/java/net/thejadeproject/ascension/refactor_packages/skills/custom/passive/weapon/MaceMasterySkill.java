package net.thejadeproject.ascension.refactor_packages.skills.custom.passive.weapon;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.thejadeproject.ascension.refactor_packages.paths.ModPaths;

public class MaceMasterySkill extends GenericWeaponMasterySkill {

    @Override
    protected ResourceLocation getPathId() {
        return ModPaths.MACE.getId();
    }

    @Override
    protected TagKey<Item> getWeaponTag() {
        return ItemTags.MACE_ENCHANTABLE;
    }

    @Override
    protected String getTitleKey() {
        return "ascension.skill.mace_mastery_skill";
    }

    @Override
    protected String getDescriptionKey() {
        return "ascension.skill.mace_mastery.description_skill";
    }

}