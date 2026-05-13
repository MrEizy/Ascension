package net.thejadeproject.ascension.refactor_packages.skills.custom.passive.weapon;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.item.Item;
import net.thejadeproject.ascension.refactor_packages.paths.ModPaths;

public class TridentMasterySkill extends GenericWeaponMasterySkill {

    @Override
    protected ResourceLocation getPathId() {
        return ModPaths.TRIDENT.getId();
    }

    @Override
    protected TagKey<Item> getWeaponTag() {
        return ItemTags.TRIDENT_ENCHANTABLE;
    }

    @Override
    public boolean matchesDamage(ServerPlayer player, DamageSource source) {
        return super.matchesDamage(player, source)
                || source.getDirectEntity() instanceof ThrownTrident;
    }

    @Override
    protected String getTitleKey() {
        return "ascension.skill.trident_mastery_skill";
    }

    @Override
    protected String getDescriptionKey() {
        return "ascension.skill.trident_mastery.description_skill";
    }

}