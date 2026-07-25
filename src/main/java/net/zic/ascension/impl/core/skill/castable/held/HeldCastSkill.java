package net.zic.ascension.impl.core.skill.castable.held;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.storage.ValueInput;
import net.zic.ascension.api.core.CoreRegistries;
import net.zic.ascension.api.core.resource.ResourceTransactionResult;
import net.zic.ascension.api.core.skill.SkillData;
import net.zic.ascension.api.core.skill.castable.CastData;
import net.zic.ascension.api.core.skill.castable.CastableSkill;
import net.zic.ascension.api.core.skill.castable.PreCastData;
import net.zic.ascension.api.core.skill.castable.data.CastResult;
import net.zic.ascension.api.core.skill.castable.data.CastStatus;
import net.zic.ascension.api.core.skill.castable.data.CastType;
import net.zic.ascension.api.core.skill.castable.held.HeldCastChargeStage;
import net.zic.ascension.api.core.skill.castable.held.HeldCastData;
import net.zic.ascension.api.core.skill.castable.held.HeldCastSpec;
import net.zic.ascension.api.core.skill.castable.held.HeldCastVisualPhase;
import net.zic.ascension.api.core.skill.castable.held.HeldCastVisualState;
import net.zic.ascension.api.core.skill.castable.held.execution.HeldCastExecution;
import net.zic.ascension.api.core.skill.castable.held.execution.HeldCastExecutionContext;
import net.zic.ascension.api.core.source.OriginSource;
import net.zic.ascension.api.datapack.skill.SkillType;
import net.zic.ascension.api.value.ScaledValueContext;
import net.zic.ascension.impl.core.skill.castable.presentation.CastSoundPlayer;
import net.zic.ascension.impl.datapack.skill.AscensionSkillTypes;
import net.zic.ascension.skill_casting.AscensionSkillListener;
import net.zic.zenithlib.common.ZenithAttachments;
import net.zic.zenithlib.cooldown.EntityCooldownHandler;

import java.util.Map;
import java.util.Optional;

public record HeldCastSkill(
        Component name,
        Component description,
        HeldCastSpec cast,
        HeldCastExecution execution
) implements CastableSkill {
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
        Identifier skillId = getSkillId(caster);
        if (skillId == null) {
            return CastResult.fail(Component.literal("Invalid held skill"));
        }

        EntityCooldownHandler cooldownHandler = caster.getData(ZenithAttachments.COOLDOWN_HANDLER);
        if (cooldownHandler.isOnCooldown(skillId)) {
            return CastResult.fail(Component.literal(
                    "On Cooldown (" + cooldownHandler.getCooldown(skillId) / 20.0D + "s)"
            ));
        }

        if (cast.cost().isPresent()) {
            ScaledValueContext context = scaledValueContext(caster, skillId, cast.charge(1));
            if (!cast.cost().get().canStart(caster, skillId, context, cast.maximumCharge())) {
                return CastResult.fail(Component.literal("Not enough resources"));
            }
        }
        return CastResult.success();
    }

    @Override
    public CastData initialCast(LivingEntity caster, PreCastData preCastData) {
        HeldCastData data = new HeldCastData();
        data.setStageIndex(cast.stage(0.0D));
        return data;
    }

    @Override
    public void continueCasting(
            LivingEntity caster,
            CastStatus castStatus,
            CastData castData,
            int ticksElapsed
    ) {
        if (!(castData instanceof HeldCastData data)) {
            castStatus.invalidate();
            return;
        }

        boolean inputActive = caster.getData(ZenithAttachments.ACTION_MANAGER)
                .isActive(AscensionSkillListener.skillCast);
        if (!inputActive) {
            if (cast.minimumReached(data.getChargeTicks())) {
                castStatus.release();
            } else {
                castStatus.cancel();
            }
            return;
        }

        if (data.getChargeTicks() >= cast.maximumCharge()) {
            castStatus.complete();
            return;
        }

        int nextTicks = Math.min(cast.maximumCharge(), data.getChargeTicks() + 1);
        data.setChargeTicks(nextTicks);
        double charge = cast.charge(nextTicks);
        data.setStageIndex(cast.stage(charge));

        Identifier skillId = getSkillId(caster);
        if (skillId == null) {
            castStatus.invalidate();
            return;
        }

        ScaledValueContext scaledContext = scaledValueContext(caster, skillId, charge);
        cast.movement().apply(caster, scaledContext);

        HeldCastChargeStage stage = cast.stages().get(data.getStageIndex());
        CastSoundPlayer.playPeriodic(caster, stage.sounds(), ticksElapsed);

        if (!caster.level().isClientSide() && cast.cost().isPresent()) {
            ResourceTransactionResult result = cast.cost().get().payIncrement(
                    caster,
                    skillId,
                    data,
                    scaledContext,
                    cast.maximumCharge()
            );
            if (result != null && (!result.succeeded()
                    || result.appliedAmount() + 1.0E-8D < result.resolvedAmount())) {
                if (cast.cost().get().releaseOnFailure() && cast.minimumReached(data.getChargeTicks())) {
                    castStatus.outOfResource();
                } else {
                    castStatus.cancel();
                }
                return;
            }
        }

        if (data.getChargeTicks() >= cast.maximumCharge()) {
            castStatus.complete();
        }
    }

    @Override
    public void finalCast(
            LivingEntity caster,
            CastStatus status,
            CastData castData,
            int ticksElapsed
    ) {
        if (!(caster instanceof ServerPlayer player)
                || !(player.level() instanceof ServerLevel level)
                || !(castData instanceof HeldCastData data)) {
            return;
        }

        boolean validRelease = status.isReleased()
                || status.isCompleted()
                || status.isOutOfResource()
                || status.isInterrupted() && cast.interruption()
                .map(interruption -> interruption.releaseAfterMinimum())
                .orElse(false);
        if (!validRelease || !cast.minimumReached(data.getChargeTicks())) {
            return;
        }

        Identifier skillId = getSkillId(caster);
        if (skillId == null) {
            return;
        }

        execution.execute(new HeldCastExecutionContext(
                level,
                player,
                skillId,
                data,
                data.getChargeTicks(),
                cast.maximumCharge(),
                cast.charge(data.getChargeTicks())
        ));
        if (cast.cooldown() > 0) {
            caster.getData(ZenithAttachments.COOLDOWN_HANDLER).addCooldown(skillId, cast.cooldown());
        }
    }

    public boolean shouldInterrupt(
            LivingEntity caster,
            HeldCastData data,
            int ticksElapsed,
            double damage
    ) {
        Identifier skillId = getSkillId(caster);
        return skillId != null && cast.interruption()
                .map(interruption -> interruption.shouldInterrupt(
                        data,
                        ticksElapsed,
                        damage,
                        scaledValueContext(caster, skillId, cast.charge(data.getChargeTicks()))
                ))
                .orElse(false);
    }

    public HeldCastVisualState visualState(LivingEntity caster, HeldCastData data) {
        Identifier skillId = getSkillId(caster);
        return skillId == null ? null : new HeldCastVisualState(
                skillId,
                HeldCastVisualPhase.CHARGING,
                data.getStageIndex(),
                cast.charge(data.getChargeTicks())
        );
    }

    public Optional<net.zic.ascension.api.core.skill.castable.particle_field.ParticleFieldDefinition> particleField(int stage) {
        if (stage < 0 || stage >= cast.stages().size()) {
            return Optional.empty();
        }
        return cast.stages().get(stage).particleField();
    }

    private ScaledValueContext scaledValueContext(LivingEntity caster, Identifier skillId, double charge) {
        OriginSource source = null;
        var holder = caster.getCapability(
                net.zic.ascension.api.capabilities.CoreCapabilities.ASCENSION_ENTITY_DATA_HOLDER_CAPABILITY
        );
        if (holder != null) {
            source = holder.getData(caster).getSource();
        }
        return new ScaledValueContext(source, skillId, caster, null, charge, Map.of());
    }

    private Identifier getSkillId(LivingEntity caster) {
        return CoreRegistries.SKILL_REGISTRY.get(caster.registryAccess()).getKey(this);
    }

    @Override
    public CastData loadCastData(ByteBuf buf) {
        return new HeldCastData(buf);
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
        return AscensionSkillTypes.HELD_CAST_SKILL_TYPE.get();
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
        return new HeldCastSkillData();
    }

    @Override
    public SkillData loadData(ValueInput input, RegistryAccess access) {
        return new HeldCastSkillData();
    }

    @Override
    public SkillData loadData(ByteBuf buf) {
        return new HeldCastSkillData();
    }
}
