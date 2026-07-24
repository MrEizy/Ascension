package net.thejadeproject.ascension.refactor_packages.skills.custom.cultivation.soul;

import net.lucent.easygui.gui.textures.ITextureData;
import net.lucent.easygui.gui.textures.TextureData;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.thejadeproject.ascension.AscensionCraft;
import net.thejadeproject.ascension.refactor_packages.entity_data.IEntityData;
import net.thejadeproject.ascension.refactor_packages.paths.data.IPathData;
import net.thejadeproject.ascension.refactor_packages.techniques.ITechnique;
import net.thejadeproject.ascension.refactor_packages.techniques.ITechniqueData;
import net.thejadeproject.ascension.refactor_packages.techniques.custom.soul.ScholarlySoulTechnique;

public class ScholarlySoulCultivationSkill extends SimpleSoulCultivationSkill {

    private static final int BOOKSHELF_RANGE = 4;
    private static final int MAX_BOOKSHELF_COUNT = 24;
    private static final double BOOKSHELF_BONUS_PER_BLOCK = 0.025D;
    private static final double MAX_BOOKSHELF_MULTIPLIER = 1.60D;

    public ScholarlySoulCultivationSkill() {
        super();
    }

    @Override
    protected boolean canUseCurrentSoulTechnique(
            Entity caster,
            IPathData pathData,
            ITechnique technique,
            ITechniqueData techniqueData
    ) {
        if (!(technique instanceof ScholarlySoulTechnique scholarlyTechnique)) return false;

        return scholarlyTechnique.canCultivateMajorRealm(
                techniqueData,
                pathData.getMajorRealm()
        );
    }

    @Override
    protected double getEffectiveRate(Entity caster) {
        return super.getEffectiveRate(caster) * getBookshelfCultivationMultiplier(caster);
    }

    private static double getBookshelfCultivationMultiplier(Entity caster) {
        Level level = caster.level();
        BlockPos center = caster.blockPosition();

        int bookshelfCount = countNearbyBookshelves(level, center);

        return Math.min(
                MAX_BOOKSHELF_MULTIPLIER,
                1.0D + bookshelfCount * BOOKSHELF_BONUS_PER_BLOCK
        );
    }

    private static int countNearbyBookshelves(Level level, BlockPos center) {
        int count = 0;

        BlockPos min = center.offset(-BOOKSHELF_RANGE, -BOOKSHELF_RANGE, -BOOKSHELF_RANGE);
        BlockPos max = center.offset(BOOKSHELF_RANGE, BOOKSHELF_RANGE, BOOKSHELF_RANGE);

        for (BlockPos pos : BlockPos.betweenClosed(min, max)) {
            if (isBookshelf(level.getBlockState(pos))) {
                if (++count >= MAX_BOOKSHELF_COUNT) {
                    return MAX_BOOKSHELF_COUNT;
                }
            }
        }

        return count;
    }

    private static boolean isBookshelf(BlockState state) {
        return state.is(Blocks.BOOKSHELF) || state.is(Blocks.CHISELED_BOOKSHELF);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public ITextureData getIcon(IEntityData entityData) {
        return new TextureData(
                ResourceLocation.fromNamespaceAndPath(
                        AscensionCraft.MOD_ID,
                        "textures/spells/icon/scholarly_soul_cultivation_skill.png"
                ),
                16, 16
        );
    }

    @Override
    public Component getTitle(IEntityData entityData) {
        return Component.translatable("ascension.skill.scholarly_soul_cultivation_skill");
    }

    @Override
    public Component getDescription(IEntityData entityData) {
        return Component.translatable("ascension.skill.scholarly_soul_cultivation_skill.description");
    }
}