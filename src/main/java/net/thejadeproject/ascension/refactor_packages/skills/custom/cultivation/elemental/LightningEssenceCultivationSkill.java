package net.thejadeproject.ascension.refactor_packages.skills.custom.cultivation.elemental;

import net.lucent.easygui.gui.textures.ITextureData;
import net.lucent.easygui.gui.textures.TextureData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.thejadeproject.ascension.AscensionCraft;
import net.thejadeproject.ascension.refactor_packages.events.skills.ElementalEssenceSkillEvents;
import net.thejadeproject.ascension.refactor_packages.paths.ModPaths;
import net.thejadeproject.ascension.refactor_packages.techniques.custom.essence.ElementalEssenceTechnique;
import net.thejadeproject.ascension.refactor_packages.techniques.custom.essence.LightningEssenceTechnique;

public class LightningEssenceCultivationSkill extends ElementalEssenceCultivationSkill {

    @Override
    protected ResourceLocation getElementPath() {
        return ModPaths.LIGHTNING.getId();
    }

    @Override
    protected double getEnvironmentMultiplier(Entity caster) {
        if (caster instanceof Player player && ElementalEssenceSkillEvents.hasActiveLightningBoost(player)) {
            return 2.50D;
        }

        boolean stormingUnderOpenSky = caster.level().isThundering()
                && caster.level().canSeeSky(caster.blockPosition());

        if (stormingUnderOpenSky) {
            return 1.20D;
        }

        return 1.00D;
    }

    @Override
    protected Class<? extends ElementalEssenceTechnique> getTechniqueClass() {
        return LightningEssenceTechnique.class;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public ITextureData getIcon() {
        return new TextureData(
                ResourceLocation.fromNamespaceAndPath(
                        AscensionCraft.MOD_ID,
                        "textures/spells/icon/lightning_essence_cultivation_skill.png"
                ),
                16,
                16
        );
    }

}