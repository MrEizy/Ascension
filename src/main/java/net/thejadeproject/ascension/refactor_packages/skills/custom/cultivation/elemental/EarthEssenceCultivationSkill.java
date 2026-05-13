package net.thejadeproject.ascension.refactor_packages.skills.custom.cultivation.elemental;

import net.lucent.easygui.gui.textures.ITextureData;
import net.lucent.easygui.gui.textures.TextureData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.thejadeproject.ascension.AscensionCraft;
import net.thejadeproject.ascension.refactor_packages.paths.ModPaths;
import net.thejadeproject.ascension.refactor_packages.techniques.custom.essence.EarthEssenceTechnique;
import net.thejadeproject.ascension.refactor_packages.techniques.custom.essence.ElementalEssenceTechnique;

public class EarthEssenceCultivationSkill extends ElementalEssenceCultivationSkill {

    @Override
    protected ResourceLocation getElementPath() {
        return ModPaths.EARTH.getId();
    }

    @Override
    protected double getEnvironmentMultiplier(Entity caster) {
        BlockState below = caster.level().getBlockState(caster.blockPosition().below());
        boolean onEarth = isEarthResonantBlock(below);
        boolean underground = caster.blockPosition().getY() < caster.level().getSeaLevel();

        if (onEarth && underground) {
            return 1.35D;
        }

        if (onEarth || underground) {
            return 1.0D;
        }

        return 0.80D;
    }

    private boolean isEarthResonantBlock(BlockState state) {
        return state.is(BlockTags.BASE_STONE_OVERWORLD)
                || state.is(BlockTags.SAND)
                || state.is(BlockTags.TERRACOTTA);
    }

    @Override
    protected Class<? extends ElementalEssenceTechnique> getTechniqueClass() {
        return EarthEssenceTechnique.class;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public ITextureData getIcon() {
        return new TextureData(
                ResourceLocation.fromNamespaceAndPath(
                        AscensionCraft.MOD_ID,
                        "textures/spells/icon/earth_essence_cultivation_skill.png"
                ),
                16,
                16
        );
    }


}