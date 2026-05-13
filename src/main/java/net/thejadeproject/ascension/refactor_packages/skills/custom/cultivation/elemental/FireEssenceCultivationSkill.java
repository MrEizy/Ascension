package net.thejadeproject.ascension.refactor_packages.skills.custom.cultivation.elemental;

import net.lucent.easygui.gui.textures.ITextureData;
import net.lucent.easygui.gui.textures.TextureData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.thejadeproject.ascension.AscensionCraft;
import net.thejadeproject.ascension.refactor_packages.paths.ModPaths;
import net.thejadeproject.ascension.refactor_packages.techniques.custom.essence.ElementalEssenceTechnique;
import net.thejadeproject.ascension.refactor_packages.techniques.custom.essence.FireEssenceTechnique;

public class FireEssenceCultivationSkill extends ElementalEssenceCultivationSkill {

    @Override
    protected ResourceLocation getElementPath() {
        return ModPaths.FIRE.getId();
    }

    @Override
    protected double getEnvironmentMultiplier(Entity caster) {
        if (caster.isInLava()) {
            return 2.25D;
        }

        if (caster.isOnFire()) {
            return 1.50D;
        }

        return 0.75D;
    }

    @Override
    protected Class<? extends ElementalEssenceTechnique> getTechniqueClass() {
        return FireEssenceTechnique.class;
    }
    @OnlyIn(Dist.CLIENT)
    @Override
    public ITextureData getIcon() {
        return new TextureData(
                ResourceLocation.fromNamespaceAndPath(
                        AscensionCraft.MOD_ID,
                        "textures/spells/icon/fire_essence_cultivation_skill.png"
                ),
                16,
                16
        );
    }

}