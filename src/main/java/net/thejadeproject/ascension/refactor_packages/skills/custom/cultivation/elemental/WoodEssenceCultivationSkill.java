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
import net.thejadeproject.ascension.refactor_packages.techniques.custom.essence.ElementalEssenceTechnique;
import net.thejadeproject.ascension.refactor_packages.techniques.custom.essence.WoodEssenceTechnique;

public class WoodEssenceCultivationSkill extends ElementalEssenceCultivationSkill {

    @Override
    protected ResourceLocation getElementPath() {
        return ModPaths.WOOD.getId();
    }

    @Override
    protected double getEnvironmentMultiplier(Entity caster) {
        int plants = countNearbyBlocks(caster, 3, this::isWoodResonantBlock);

        if (plants >= 24) {
            return 1.35D;
        }

        if (plants >= 8) {
            return 1.15D;
        }

        return 0.85D;
    }

    private boolean isWoodResonantBlock(BlockState state) {
        return state.is(BlockTags.SAPLINGS)
                || state.is(BlockTags.LEAVES)
                || state.is(BlockTags.LOGS)
                || state.is(BlockTags.FLOWERS)
                || state.is(BlockTags.CROPS);
    }

    @Override
    protected Class<? extends ElementalEssenceTechnique> getTechniqueClass() {
        return WoodEssenceTechnique.class;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public ITextureData getIcon() {
        return new TextureData(
                ResourceLocation.fromNamespaceAndPath(
                        AscensionCraft.MOD_ID,
                        "textures/spells/icon/wood_essence_cultivation_skill.png"
                ),
                16,
                16
        );
    }

}