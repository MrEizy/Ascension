package net.zic.ascension.impl.core.skill.castable;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.zic.ascension.api.ascension.core.resource.ResourceTransactionService;
import net.zic.ascension.api.ascension.core.skill.ProgressingSkill;
import net.zic.ascension.api.ascension.core.skill.ProgressingSkillData;
import net.zic.ascension.api.ascension.core.skill.SkillInstance;
import net.zic.ascension.api.rpg_engine.source.data_source.util.SerializerHandler;
import net.zic.ascension.api.rpg_engine.source.data_source.util.SyncHandler;
import net.zic.ascension.api.ascension.core.skill.SkillDefinitions;
import net.zic.ascension.api.ascension.core.skill.SkillMasteryRank;
import net.zic.ascension.api.ascension.core.skill.SkillProgressionData;
import net.zic.ascension.api.ascension.core.skill.SkillProgressionResolver;
import net.zic.ascension.api.ascension.core.skill.SkillProgressionService;
import net.zic.ascension.api.ascension.core.skill.SkillProgressionSnapshot;
import net.zic.ascension.api.ascension.core.skill.passive.PassiveTrigger;
import net.zic.ascension.api.ascension.core.skill.castable.ActiveCastData;
import net.zic.ascension.api.ascension.core.skill.castable.ActiveCastDefinition;
import net.zic.ascension.api.ascension.core.skill.castable.ActiveCastVisualState;
import net.zic.ascension.api.ascension.core.skill.castable.ActiveSkillCostDefinition;
import net.zic.ascension.api.ascension.core.skill.castable.CastingSkillInstance;
import net.zic.ascension.api.ascension.core.skill.castable.EquipSkillData;
import net.zic.ascension.api.ascension.core.skill.castable.CastableSkill;
import net.zic.ascension.api.ascension.core.skill.castable.action.SkillAction;
import net.zic.ascension.api.ascension.core.skill.castable.data.CastResult;
import net.zic.ascension.api.ascension.core.skill.castable.data.CastStatus;
import org.jspecify.annotations.Nullable;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.ascension.core.skill.particle_field.ParticleFieldDefinition;
import net.zic.ascension.api.ascension.core.source.AscensionOriginSourceHelper;
import net.zic.ascension.api.ascension.core.targeting.TargetingDefinition;
import net.zic.ascension.api.ascension.datapack.skill.SkillType;
import net.zic.ascension.api.ascension.value.ScaledValue;
import net.zic.ascension.api.rpg_engine.source.OriginSource;
import net.zic.ascension.impl.datapack.skill.AscensionSkillTypes;
import net.zic.ascension.impl.core.skill.passive.PassiveTriggerService;
import net.zic.ascension.impl.core.targeting.TargetingDefinitions;
import net.zic.zenithlib.common.ZenithAttachments;
import net.zic.zenithlib.cooldown.EntityCooldownHandler;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class ActiveSkill implements CastableSkill<ActiveSkill.Data, ActiveCastData, EquipSkillData.Empty>, ProgressingSkill<ActiveSkill.Data>, SkillDefinitions.Owner {
    //charge skills build up while held and release when let go, channel skills execute every tick while held
    public static final Identifier CHARGING = AscensionCraft.prefix("charging");
    public static final Identifier CHANNELING = AscensionCraft.prefix("channeling");

    private static final SerializerHandler<Data> SERIALIZER_HANDLER = new DataSerializerHandler();
    private static final SyncHandler<Data> SYNC_HANDLER = new DataSyncHandler();

    private static final int CHANNEL_MASTERY_INTERVAL = 20;
    private static final double MASTERY_EXPERIENCE_PER_USE = 1.0D;

    private final Component name;
    private final Component description;
    private final SkillDefinitions definitions;
    private final ActiveCastDefinition cast;
    private final TargetingDefinition target;
    private final boolean requireTargets;
    private final List<SkillAction> actions;
    private final List<ActiveSkillCostDefinition> costs;
    private final ScaledValue cooldown;
    private final SkillMasteryRank defaultMasteryCap;
    private final Map<SkillMasteryRank, Double> masteryRequirements;

    public ActiveSkill(
            Component name,
            Component description,
            SkillDefinitions definitions,
            ActiveCastDefinition cast,
            TargetingDefinition target,
            boolean requireTargets,
            List<SkillAction> actions,
            List<ActiveSkillCostDefinition> costs,
            ScaledValue cooldown,
            SkillMasteryRank defaultMasteryCap,
            Map<SkillMasteryRank, Double> masteryRequirements
    ) {
        this.name = name;
        this.description = description;
        this.definitions = definitions == null ? SkillDefinitions.EMPTY : definitions;
        this.cast = cast == null ? ActiveCastDefinition.instant() : cast;
        this.target = target == null ? new TargetingDefinitions.Self() : target;
        this.requireTargets = requireTargets;
        this.actions = actions == null ? List.of() : List.copyOf(actions);
        this.costs = costs == null ? List.of() : List.copyOf(costs);
        this.cooldown = cooldown == null ? ScaledValue.constant(0.0D) : cooldown;
        this.defaultMasteryCap = defaultMasteryCap == null ? SkillMasteryRank.INITIATE : defaultMasteryCap;
        EnumMap<SkillMasteryRank, Double> requirements = new EnumMap<>(SkillMasteryRank.class);
        if (masteryRequirements != null) {
            masteryRequirements.forEach((rank, value) -> {
                if (rank != null
                        && rank != SkillMasteryRank.INITIATE
                        && value != null
                        && Double.isFinite(value)
                        && value > 0.0D) {
                    requirements.put(rank, value);
                }
            });
        }
        this.masteryRequirements = Map.copyOf(requirements);
    }

    public ActiveCastDefinition cast() {
        return cast;
    }

    public TargetingDefinition target() {
        return target;
    }

    public boolean requireTargets() {
        return requireTargets;
    }

    public List<SkillAction> actions() {
        return actions;
    }

    public List<ActiveSkillCostDefinition> costs() {
        return costs;
    }

    public ScaledValue cooldown() {
        return cooldown;
    }

    public SkillMasteryRank defaultMasteryCap() {
        return defaultMasteryCap;
    }

    public Map<SkillMasteryRank, Double> masteryRequirements() {
        return masteryRequirements;
    }

    @Override
    public SkillDefinitions definitions() {
        return definitions;
    }

    public Optional<ParticleFieldDefinition> particleField(int stage) {
        return cast.particleField(stage);
    }

    public Optional<Identifier> cultivationPath() {
        return cultivationPath(actions);
    }

    private static Optional<Identifier> cultivationPath(List<SkillAction> actions) {
        for (SkillAction action : actions) {
            if (action instanceof SkillActions.Cultivate cultivate) {
                return Optional.of(cultivate.path());
            }
            if (action instanceof SkillActions.MasteryGate gate) {
                Optional<Identifier> nested = cultivationPath(gate.actions());
                if (nested.isPresent()) {
                    return nested;
                }
            }
        }
        return Optional.empty();
    }

    //──Equip────────────────────────────────────────────────────────

    @Override
    public EquipSkillData.Empty newEquipData(RegistryAccess access) {
        return EquipSkillData.Empty.INSTANCE;
    }

    @Override
    public SerializerHandler<EquipSkillData.Empty> equipSerializerHandler() {
        return EquipSkillData.Empty.SERIALIZER_HANDLER;
    }

    @Override
    public SyncHandler<EquipSkillData.Empty> equipSyncHandler() {
        return EquipSkillData.Empty.SYNC_HANDLER;
    }

    //──Cast────────────────────────────────────────────────────────

    @Override
    public ActiveCastData newCastData(RegistryAccess access) {
        return new ActiveCastData();
    }

    @Override
    public SyncHandler<ActiveCastData> castSyncHandler() {
        return ActiveCastData.SYNC_HANDLER;
    }

    @Override
    public @Nullable ActiveCastVisualState visualState(CastingSkillInstance<Data, ActiveCastData, EquipSkillData.Empty> instance) {
        if (cast.mode() == ActiveCastDefinition.Mode.INSTANT) {
            return null;
        }
        return new ActiveCastVisualState(instance.getSkillId(), instance.getCastData().getStageIndex(), cast.progress(instance.getPhaseTicks()));
    }

    @Override
    public void onDamaged(CastingSkillInstance<Data, ActiveCastData, EquipSkillData.Empty> instance, double damage) {
        LivingEntity caster = instance.getCaster();
        int ticks = instance.getPhaseTicks();
        boolean interrupt = cast.interruption()
                .map(interruption -> interruption.shouldInterrupt(
                        instance.getCastData(),
                        instance.getTotalTicks(),
                        damage,
                        scaledValueContext(caster, instance.getSkillId(), null, cast.progress(ticks), castVariables(ticks))
                ))
                .orElse(false);
        if (interrupt) {
            instance.end(CastStatus.Reason.INTERRUPTED);
        }
    }

    @Override
    public CastResult tryCast(CastingSkillInstance<Data, ActiveCastData, EquipSkillData.Empty> instance) {
        LivingEntity caster = instance.getCaster();
        Identifier skillId = instance.getSkillId();

        EntityCooldownHandler cooldownHandler = caster.getData(ZenithAttachments.COOLDOWN_HANDLER);
        if (cooldownHandler.isOnCooldown(skillId)) {
            return CastResult.fail(Component.literal(
                    "On Cooldown (" + cooldownHandler.getCooldown(skillId) / 20.0D + "s)"
            ));
        }

        if (caster.level().isClientSide()) {
            return CastResult.success();
        }
        if (!(caster.level() instanceof ServerLevel level)) {
            return CastResult.fail();
        }

        ResolvedMastery mastery = resolveMastery(caster, skillId);
        if (mastery.failureMessage() != null) {
            return CastResult.fail(mastery.failureMessage());
        }

        return switch (cast.mode()) {
            case INSTANT, CHANNEL -> {
                ResolvedActiveCast resolved = resolve(level, caster, skillId, mastery, 0.0D, Map.of());
                if (resolved.failureMessage() != null) {
                    yield CastResult.fail(resolved.failureMessage());
                }
                yield canPayCosts(caster, skillId, resolved)
                        ? CastResult.success()
                        : CastResult.fail(Component.literal("Not enough resources"));
            }
            case CHARGE -> {
                if (cast.cost().isEmpty()) {
                    yield CastResult.success();
                }
                double progress = cast.progress(1);
                ScaledValue.Context context = scaledValueContext(caster, skillId, null, progress, Map.of(
                        SkillExecutions.CAST_TICKS, 1.0D,
                        SkillExecutions.MAXIMUM_CAST_TICKS, (double) cast.maximumTicks()
                ));
                yield cast.cost().get().canStart(caster, skillId, context, cast.maximumTicks())
                        ? CastResult.success()
                        : CastResult.fail(Component.literal("Not enough resources"));
            }
        };
    }

    @Override
    public void startCast(CastingSkillInstance<Data, ActiveCastData, EquipSkillData.Empty> instance) {
        LivingEntity caster = instance.getCaster();
        if (cast.mode() == ActiveCastDefinition.Mode.INSTANT) {
            if (caster.level() instanceof ServerLevel level) {
                executeInstant(level, caster, instance.getSkillId());
            }
            instance.end(CastStatus.Reason.COMPLETED);
            return;
        }

        instance.getCastData().setStageIndex(cast.stage(cast.progress(0)));
        instance.setPhase(cast.mode() == ActiveCastDefinition.Mode.CHARGE ? CHARGING : CHANNELING);
    }

    @Override
    public void tickCast(CastingSkillInstance<Data, ActiveCastData, EquipSkillData.Empty> instance) {
        boolean charging = instance.isPhase(CHARGING);
        if (!charging && !instance.isPhase(CHANNELING)) {
            instance.end(CastStatus.Reason.INVALIDATED);
            return;
        }

        //ticks completed so far, the maximum is checked first so the tick reaching it still runs
        int ticks = instance.getPhaseTicks();
        if (cast.maximumReached(ticks)) {
            instance.end(CastStatus.Reason.COMPLETED);
            return;
        }

        if (!instance.isInputHeld()) {
            if (charging) {
                instance.end(cast.minimumReached(ticks) ? CastStatus.Reason.RELEASED : CastStatus.Reason.CANCELLED);
            } else {
                instance.end(CastStatus.Reason.COMPLETED);
            }
            return;
        }

        LivingEntity caster = instance.getCaster();
        Identifier skillId = instance.getSkillId();
        ActiveCastData data = instance.getCastData();
        int nextTicks = ticks + 1;
        double progress = cast.progress(nextTicks);
        if (data.setStageIndex(cast.stage(progress))) {
            instance.markCastDataDirty();
        }
        Map<Identifier, Double> variables = castVariables(nextTicks);
        cast.movement().apply(caster, scaledValueContext(caster, skillId, null, progress, variables));
        ActiveCastDefinition.Stage stage = cast.stages().get(data.getStageIndex());
        CastSoundPlayer.playPeriodic(caster, stage.sounds(), ticks);

        if (charging) {
            if (!caster.level().isClientSide() && cast.cost().isPresent()) {
                ResourceTransactionService.Result result = cast.cost().get().payIncrement(
                        caster,
                        skillId,
                        data,
                        scaledValueContext(caster, skillId, null, progress, variables),
                        nextTicks,
                        cast.maximumTicks()
                );
                if (result != null && (!result.succeeded()
                        || result.appliedAmount() + 1.0E-8D < result.resolvedAmount())) {
                    //the failed tick does not count, the release uses the charge paid so far
                    if (cast.cost().get().releaseOnFailure() && cast.minimumReached(ticks)) {
                        instance.end(CastStatus.Reason.OUT_OF_RESOURCE);
                    } else {
                        instance.end(CastStatus.Reason.CANCELLED);
                    }
                }
            }
            return;
        }

        if (caster.level() instanceof ServerLevel level) {
            executeChannelTick(level, instance, nextTicks);
        }
    }

    @Override
    public void endCast(CastingSkillInstance<Data, ActiveCastData, EquipSkillData.Empty> instance) {
        LivingEntity caster = instance.getCaster();
        if (!(caster.level() instanceof ServerLevel level) || cast.mode() == ActiveCastDefinition.Mode.INSTANT) {
            return;
        }

        Identifier skillId = instance.getSkillId();
        CastStatus status = instance.getStatus();
        int ticks = instance.getPhaseTicks();

        if (cast.mode() == ActiveCastDefinition.Mode.CHARGE) {
            boolean validRelease = status.isReleased()
                    || status.isCompleted()
                    || status.isOutOfResource()
                    || status.isInterrupted() && cast.interruption()
                    .map(ActiveCastDefinition.Interruption::releaseAfterMinimum)
                    .orElse(false);
            if (!validRelease || !cast.minimumReached(ticks)) {
                return;
            }

            double progress = cast.progress(ticks);
            ResolvedMastery mastery = resolveMastery(caster, skillId);
            if (mastery.failureMessage() != null) {
                return;
            }
            ResolvedActiveCast resolved = resolve(
                    level,
                    caster,
                    skillId,
                    mastery,
                    progress,
                    castVariables(ticks)
            );
            if (resolved.failureMessage() != null) {
                sendFailure(caster, resolved.failureMessage());
                return;
            }
            if (!payCosts(caster, skillId, resolved)) {
                sendFailure(caster, Component.literal("Not enough resources"));
                return;
            }
            SkillExecutions.apply(level, caster, skillId, progress, actions, resolved.execution());
            PassiveTriggerService.trigger(caster, PassiveTrigger.Event.SKILL_CAST, SkillExecutions.primaryEntity(resolved.execution()), resolved.execution().variables());
            applyCooldown(caster, skillId, resolved);
            awardMasteryExperience(caster, skillId);
            return;
        }

        //a channel that ran at all always goes on cooldown, however it ended, so it can not be skipped by cancelling, logging out or dying
        if (ticks > 0) {
            ResolvedMastery mastery = resolveMastery(caster, skillId);
            if (mastery.failureMessage() == null) {
                ResolvedActiveCast resolved = resolve(
                        level,
                        caster,
                        skillId,
                        mastery,
                        cast.progress(ticks),
                        castVariables(ticks)
                );
                if (resolved.failureMessage() == null) {
                    applyCooldown(caster, skillId, resolved);
                }
            }
        }
    }

    private void executeInstant(ServerLevel level, LivingEntity caster, Identifier skillId) {
        ResolvedMastery mastery = resolveMastery(caster, skillId);
        if (mastery.failureMessage() != null) {
            sendFailure(caster, mastery.failureMessage());
            return;
        }
        ResolvedActiveCast resolved = resolve(level, caster, skillId, mastery, 0.0D, Map.of());
        if (resolved.failureMessage() != null) {
            sendFailure(caster, resolved.failureMessage());
            return;
        }
        if (!payCosts(caster, skillId, resolved)) {
            sendFailure(caster, Component.literal("Not enough resources"));
            return;
        }
        SkillExecutions.apply(level, caster, skillId, 0.0D, actions, resolved.execution());
        PassiveTriggerService.trigger(caster, PassiveTrigger.Event.SKILL_CAST, SkillExecutions.primaryEntity(resolved.execution()), resolved.execution().variables());
        applyCooldown(caster, skillId, resolved);
        awardMasteryExperience(caster, skillId);
    }

    private void executeChannelTick(ServerLevel level, CastingSkillInstance<Data, ActiveCastData, EquipSkillData.Empty> instance, int ticks) {
        LivingEntity caster = instance.getCaster();
        Identifier skillId = instance.getSkillId();
        double progress = cast.progress(ticks);
        ResolvedMastery mastery = resolveMastery(caster, skillId);
        if (mastery.failureMessage() != null) {
            instance.end(CastStatus.Reason.INVALIDATED);
            return;
        }
        ResolvedActiveCast resolved = resolve(
                level,
                caster,
                skillId,
                mastery,
                progress,
                castVariables(ticks)
        );
        if (resolved.failureMessage() != null) {
            return;
        }
        if (!canPayCosts(caster, skillId, resolved) || !payCosts(caster, skillId, resolved)) {
            instance.end(CastStatus.Reason.OUT_OF_RESOURCE);
            return;
        }
        SkillExecutions.apply(level, caster, skillId, progress, actions, resolved.execution());
        if (ticks == 1) {
            PassiveTriggerService.trigger(caster, PassiveTrigger.Event.SKILL_CAST, SkillExecutions.primaryEntity(resolved.execution()), resolved.execution().variables());
        }
        if (ticks % CHANNEL_MASTERY_INTERVAL == 0) {
            awardMasteryExperience(caster, skillId);
        }
    }

    private ResolvedMastery resolveMastery(LivingEntity caster, Identifier skillId) {
        OriginSource source = getOriginSource(caster);
        SkillProgressionSnapshot snapshot = SkillProgressionResolver.resolve(source, skillId);
        if (snapshot.effectiveProgression() < SkillMasteryRank.INITIATE.progression()) {
            return ResolvedMastery.failure(Component.literal("Skill mastery is not accessible"));
        }
        return new ResolvedMastery(snapshot.effectiveProgression(), null);
    }

    private ResolvedActiveCast resolve(
            ServerLevel level,
            LivingEntity caster,
            Identifier skillId,
            ResolvedMastery mastery,
            double progress,
            Map<Identifier, Double> variables
    ) {
        SkillExecutions.Resolution resolvedExecution = SkillExecutions.resolve(
                level,
                caster,
                skillId,
                mastery.effectiveMastery(),
                progress,
                variables,
                target,
                requireTargets
        );
        if (!resolvedExecution.succeeded()) {
            return ResolvedActiveCast.failure(resolvedExecution.failureMessage());
        }
        return new ResolvedActiveCast(mastery.effectiveMastery(), resolvedExecution, null);
    }

    private boolean canPayCosts(LivingEntity caster, Identifier skillId, ResolvedActiveCast resolved) {
        LivingEntity target = SkillExecutions.primaryEntity(resolved.execution());
        ScaledValue.Context context = scaledValueContext(
                caster,
                skillId,
                target,
                resolved.execution().variables().getOrDefault(SkillExecutions.CAST_PROGRESS, 0.0D),
                resolved.execution().variables()
        );
        for (ActiveSkillCostDefinition cost : costs) {
            if (!cost.canPay(caster, skillId, target, context, resolved.execution().variables())) {
                return false;
            }
        }
        return true;
    }

    private boolean payCosts(LivingEntity caster, Identifier skillId, ResolvedActiveCast resolved) {
        LivingEntity target = SkillExecutions.primaryEntity(resolved.execution());
        double progress = resolved.execution().variables().getOrDefault(SkillExecutions.CAST_PROGRESS, 0.0D);
        ScaledValue.Context context = scaledValueContext(caster, skillId, target, progress, resolved.execution().variables());
        for (ActiveSkillCostDefinition cost : costs) {
            if (!cost.pay(caster, skillId, target, context, resolved.execution().variables())) {
                return false;
            }
        }
        return true;
    }

    private void applyCooldown(LivingEntity caster, Identifier skillId, ResolvedActiveCast resolved) {
        int resolvedCooldown = resolveCooldown(caster, skillId, resolved);
        if (resolvedCooldown > 0) {
            caster.getData(ZenithAttachments.COOLDOWN_HANDLER).addCooldown(skillId, resolvedCooldown);
        }
    }

    private void awardMasteryExperience(LivingEntity caster, Identifier skillId) {
        OriginSource source = getOriginSource(caster);
        if (source != null) {
            SkillProgressionService.addExperience(source, skillId, MASTERY_EXPERIENCE_PER_USE);
        }
    }

    private ScaledValue.Context scaledValueContext(
            LivingEntity caster,
            Identifier skillId,
            LivingEntity target,
            double progress,
            Map<Identifier, Double> variables
    ) {
        return new ScaledValue.Context(
                getOriginSource(caster),
                skillId,
                caster,
                target,
                progress,
                variables
        );
    }

    private int resolveCooldown(LivingEntity caster, Identifier skillId, ResolvedActiveCast resolved) {
        LivingEntity target = SkillExecutions.primaryEntity(resolved.execution());
        double progress = resolved.execution().variables().getOrDefault(SkillExecutions.CAST_PROGRESS, 0.0D);
        double value = cooldown.resolve(
                scaledValueContext(caster, skillId, target, progress, resolved.execution().variables())
        );
        if (!Double.isFinite(value) || value <= 0.0D) {
            return 0;
        }
        return (int) Math.clamp(Math.round(value), 0L, (long) Integer.MAX_VALUE);
    }

    private Map<Identifier, Double> castVariables(int ticks) {
        return Map.of(
                SkillExecutions.CAST_TICKS, (double) ticks,
                SkillExecutions.MAXIMUM_CAST_TICKS, (double) cast.maximumTicks(),
                SkillExecutions.CAST_PROGRESS, cast.progress(ticks)
        );
    }

    private static void sendFailure(LivingEntity caster, Component message) {
        if (caster instanceof ServerPlayer player && message != null) {
            player.sendOverlayMessage(message);
        }
    }

    private OriginSource getOriginSource(LivingEntity caster) {
        return AscensionOriginSourceHelper.getEntitySource(caster);
    }

    @Override
    public int getMaximumProgression() {
        return SkillMasteryRank.TRANSCENDENCE.progression();
    }

    @Override
    public int getInitialProgression() {
        return SkillMasteryRank.INITIATE.progression();
    }

    @Override
    public int getDefaultProgressionCap() {
        return defaultMasteryCap.progression();
    }

    @Override
    public double getExperienceRequiredForNextProgression(int currentProgression) {
        if (currentProgression >= SkillMasteryRank.TRANSCENDENCE.progression()) {
            return Double.POSITIVE_INFINITY;
        }
        SkillMasteryRank target = SkillMasteryRank.fromProgression(currentProgression + 1);
        return Math.max(0.0D, masteryRequirements.getOrDefault(target, target.defaultExperienceToReach()));
    }

    @Override
    public SkillType getType() {
        return AscensionSkillTypes.ACTIVE_SKILL_TYPE.get();
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
    public void onAdded(OriginSource source, SkillInstance<Data> instance) {
    }

    @Override
    public void onRemoved(OriginSource source, SkillInstance<Data> instance) {
    }

    @Override
    public void applyToEntity(LivingEntity entity, SkillInstance<Data> instance) {
    }

    @Override
    public void removeFromEntity(LivingEntity entity, SkillInstance<Data> instance) {
    }

    @Override
    public Data newData(RegistryAccess access) {
        return new Data(new SkillProgressionData(getInitialProgression(), 0.0D, Map.of()));
    }

    @Override
    public SerializerHandler<Data> serializerHandler() {
        return SERIALIZER_HANDLER;
    }

    @Override
    public SyncHandler<Data> syncHandler() {
        return SYNC_HANDLER;
    }

    private record ResolvedMastery(int effectiveMastery, Component failureMessage) {
        private static ResolvedMastery failure(Component message) {
            return new ResolvedMastery(0, message);
        }
    }

    private record ResolvedActiveCast(
            int effectiveMastery,
            SkillExecutions.Resolution execution,
            Component failureMessage
    ) {
        private static ResolvedActiveCast failure(Component message) {
            return new ResolvedActiveCast(0, null, message);
        }
    }

    public static final class Data implements ProgressingSkillData {
        private final SkillProgressionData progression;

        public Data(SkillProgressionData progression) {
            this.progression = progression == null ? new SkillProgressionData() : progression;
        }

        @Override
        public SkillProgressionData getSkillProgression() {
            return progression;
        }
    }

    private static final class DataSerializerHandler implements SerializerHandler<Data> {
        @Override
        public Data read(ValueInput input, RegistryAccess access) {
            return new Data(new SkillProgressionData(input.childOrEmpty("progression")));
        }

        @Override
        public void write(Data data, ValueOutput output, RegistryAccess access) {
            data.progression.write(output.child("progression"));
        }
    }

    private static final class DataSyncHandler implements SyncHandler<Data> {
        @Override
        public void decode(Data data, ByteBuf buf, RegistryAccess access) {
            data.progression.decode(buf);
        }

        @Override
        public void encode(Data data, ByteBuf buf, RegistryAccess access) {
            data.progression.encode(buf);
        }
    }
}
