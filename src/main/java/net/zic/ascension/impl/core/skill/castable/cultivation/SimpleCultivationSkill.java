package net.zic.ascension.impl.core.skill.castable.cultivation;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.storage.ValueInput;
import net.zic.ascension.api.capabilities.AscensionEntityDataHolder;
import net.zic.ascension.api.capabilities.CoreCapabilities;
import net.zic.ascension.api.core.path.PathData;
import net.zic.ascension.api.core.skill.SkillData;
import net.zic.ascension.api.core.skill.castable.CastData;
import net.zic.ascension.api.core.skill.castable.CastableSkill;
import net.zic.ascension.api.core.skill.castable.PreCastData;
import net.zic.ascension.api.core.skill.castable.data.CastResult;
import net.zic.ascension.api.core.skill.castable.data.CastStatus;
import net.zic.ascension.api.core.skill.castable.data.CastType;
import net.zic.ascension.api.core.skill.castable.particle_field.ParticleFieldDefinition;
import net.zic.ascension.api.core.skill.castable.presentation.CastSoundDefinition;
import net.zic.ascension.api.core.source.OriginSource;
import net.zic.ascension.api.datapack.skill.SkillType;
import net.zic.ascension.impl.core.path.foundation.FoundationPathData;
import net.zic.ascension.util.CultivationUtil;
import net.zic.ascension.impl.core.skill.castable.presentation.CastSoundPlayer;
import net.zic.ascension.impl.datapack.skill.AscensionSkillTypes;
import net.zic.ascension.skill_casting.AscensionSkillListener;
import net.zic.zenithlib.common.ZenithAttachments;

import java.util.List;
import java.util.Optional;

public record SimpleCultivationSkill(
        Component name,
        Component description,
        Identifier primaryPath,
        List<Identifier> secondaryPaths,
        double baseRate,
        Optional<ParticleFieldDefinition> particleField,
        List<CastSoundDefinition> sounds) implements CastableSkill {
    public SimpleCultivationSkill {
        secondaryPaths = secondaryPaths == null ? List.of() : List.copyOf(secondaryPaths);
        baseRate = Double.isFinite(baseRate) ? Math.max(0.0D, baseRate) : 0.0D;
        particleField = particleField == null ? Optional.empty() : particleField;
        sounds = sounds == null ? List.of() : List.copyOf(sounds);
    }

    @Override
    public CastType getCastType() {
        return CastType.LONG;
    }

    @Override
    public void onEquip(LivingEntity entity, PreCastData preCastData) {

    }

    @Override
    public void onUnEquip(LivingEntity entity, PreCastData preCastData) {

    }

    @Override
    public void selected(LivingEntity entity, PreCastData preCastData) {

    }

    @Override
    public void unselected(LivingEntity entity, PreCastData preCastData) {

    }

    @Override
    public CastResult tryCast(LivingEntity caster) {
        return CastResult.success();
    }


    @Override
    public void continueCasting(LivingEntity caster, CastStatus castStatus, CastData castData, int ticksElapsed) {
        if (caster.level().isClientSide()) {
            return;
        }

        if (!caster.getData(ZenithAttachments.ACTION_MANAGER).isActive(AscensionSkillListener.skillCast)) {
            castStatus.finish();
            return;
        }

        AscensionEntityDataHolder holder = caster.getCapability(CoreCapabilities.ASCENSION_ENTITY_DATA_HOLDER_CAPABILITY);
        if(holder == null) return;

        OriginSource source = holder.getData(caster).getSource();


        PathData pathData = source.getPathData(primaryPath());

        if(pathData == null) return;
        CastSoundPlayer.playPeriodic(caster, sounds, ticksElapsed);

        if(holder.getData(caster).isCultivationSuppressed() && pathData instanceof FoundationPathData foundationPathData){
            CultivationUtil.cultivateFoundation(
                    caster,
                    source,
                    foundationPathData,
                    baseRate);
        }else CultivationUtil.cultivate(caster,source,pathData,secondaryPaths(),baseRate);


    }

    @Override
    public CastData initialCast(LivingEntity caster, PreCastData preCastData) {
        return null;
    }

    @Override
    public void finalCast(LivingEntity caster, CastStatus status, CastData castData, int ticksElapsed) {

    }

    @Override
    public CastData loadCastData(ByteBuf buf) {
        return null;
    }

    @Override
    public PreCastData newPreCastData() {
        return null;
    }

    @Override
    public PreCastData loadPreCastData(ValueInput input) {
        return null;
    }

    @Override
    public PreCastData loadPreCastData(ByteBuf buf) {
        return null;
    }

    @Override
    public SkillType getType() {
        return AscensionSkillTypes.SIMPLE_CULTIVATION_SKILL_TYPE.get();
    }

    @Override
    public Component getName() {
        return name;
    }

    @Override
    public Component getDescription() {
        return description;
    }

    @Override
    public void onAdded(OriginSource source, SkillData data) {

    }

    @Override
    public void onRemoved(OriginSource source, SkillData data) {

    }

    @Override
    public void applyToEntity(LivingEntity entity, SkillData data) {

    }

    @Override
    public void removeFromEntity(LivingEntity entity, SkillData data) {

    }

    @Override
    public SkillData newData(RegistryAccess access) {
        return new SimpleCultivationSkillData();
    }

    @Override
    public SkillData loadData(ValueInput input,RegistryAccess access) {
        return new SimpleCultivationSkillData();
    }

    @Override
    public SkillData loadData(ByteBuf buf) {
        return new SimpleCultivationSkillData();
    }
}
