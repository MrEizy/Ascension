package net.thejadeproject.ascension.refactor_packages.skills.custom.cultivation.soul;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.thejadeproject.ascension.data_attachments.ModAttachments;
import net.thejadeproject.ascension.refactor_packages.entity_data.IEntityData;
import net.thejadeproject.ascension.refactor_packages.paths.ModPaths;
import net.thejadeproject.ascension.refactor_packages.paths.data.IPathData;
import net.thejadeproject.ascension.refactor_packages.skill_casting.casting.CastResult;
import net.thejadeproject.ascension.refactor_packages.skills.castable.ICastData;
import net.thejadeproject.ascension.refactor_packages.skills.castable.IPreCastData;
import net.thejadeproject.ascension.refactor_packages.skills.custom.cultivation.GenericCultivationSkill;
import net.thejadeproject.ascension.refactor_packages.techniques.ITechnique;
import net.thejadeproject.ascension.refactor_packages.techniques.ITechniqueData;
import net.thejadeproject.ascension.refactor_packages.util.CultivationUtil;

import java.util.List;

public class SimpleSoulCultivationSkill extends GenericCultivationSkill {

    protected static final ResourceLocation SOUL_PATH = ModPaths.SOUL.getId();
    protected static final double DEFAULT_BASE_RATE = 2.0D;

    public SimpleSoulCultivationSkill() {
        this(DEFAULT_BASE_RATE);
    }

    protected SimpleSoulCultivationSkill(double baseRate) {
        super(baseRate, SOUL_PATH);
    }

    @Override
    public CastResult canCast(Entity caster, IPreCastData preCastData) {
        return new CastResult(CastResult.Type.SUCCESS);
    }

    protected boolean canUseCurrentSoulTechnique(
            Entity caster,
            IPathData pathData,
            ITechnique technique,
            ITechniqueData techniqueData
    ) {
        return true;
    }

    protected double getSoulCultivationGain(Entity caster, IPathData pathData) {
        return getEffectiveRate(caster);
    }

    @Override
    public boolean continueCasting(int ticksElapsed, Entity caster, ICastData castData) {
        if (!caster.hasData(ModAttachments.INPUT_STATES)) return false;

        if (!caster.level().isClientSide()) {
            IEntityData entityData = caster.getData(ModAttachments.ENTITY_DATA);
            IPathData pathData = entityData.getPathData(SOUL_PATH);

            if (pathData == null) return false;
            if (pathData.isBreakingThrough()) return false;
            if (pathData.getCurrentTechniqueId() == null) return false;

            ITechnique technique = pathData.getCurrentTechnique();
            if (technique == null) return false;

            ITechniqueData techniqueData = pathData.getTechniqueData(pathData.getCurrentTechniqueId());
            if (!canUseCurrentSoulTechnique(caster, pathData, technique, techniqueData)) return false;

            CultivationUtil.tryCultivate(
                    caster,
                    SOUL_PATH,
                    getAttributedPaths(caster),
                    getSoulCultivationGain(caster, pathData)
            );

            if (caster instanceof Player player) {
                pathData.sync(player);
            }
        }

        return caster.getData(ModAttachments.INPUT_STATES).isHeld("skill_cast");
    }

    @Override
    public Component getTitle(IEntityData entityData) {
        return Component.translatable("ascension.skill.simple_soul_cultivation_skill");
    }

    @Override
    public Component getDescription(IEntityData entityData) {
        return Component.translatable("ascension.skill.simple_soul_cultivation_skill.description");
    }
}