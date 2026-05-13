package net.thejadeproject.ascension.refactor_packages.skills.custom.cultivation.elemental;

import net.lucent.easygui.gui.textures.ITextureData;
import net.lucent.easygui.gui.textures.TextureData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.common.Tags;
import net.thejadeproject.ascension.AscensionCraft;
import net.thejadeproject.ascension.refactor_packages.paths.ModPaths;
import net.thejadeproject.ascension.refactor_packages.techniques.custom.essence.ElementalEssenceTechnique;
import net.thejadeproject.ascension.refactor_packages.techniques.custom.essence.MetalEssenceTechnique;

public class MetalEssenceCultivationSkill extends ElementalEssenceCultivationSkill {

    @Override
    protected ResourceLocation getElementPath() {
        return ModPaths.METAL.getId();
    }

    @Override
    protected double getEnvironmentMultiplier(Entity caster) {
        int oreCount = countNearbyBlocks(caster, 3, this::isMetalResonantBlock);

        if (oreCount >= 8) {
            return 1.55D;
        }

        if (oreCount >= 2) {
            return 1.20D;
        }

        return 0.80D;
    }

    private boolean isMetalResonantBlock(BlockState state) {
        return state.is(Tags.Blocks.ORES);
    }

    @Override
    protected Class<? extends ElementalEssenceTechnique> getTechniqueClass() {
        return MetalEssenceTechnique.class;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public ITextureData getIcon() {
        return new TextureData(
                ResourceLocation.fromNamespaceAndPath(
                        AscensionCraft.MOD_ID,
                        "textures/spells/icon/placeholder.png"
                ),
                16,
                16
        );
    }

}