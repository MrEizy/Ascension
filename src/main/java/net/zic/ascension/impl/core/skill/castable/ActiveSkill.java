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
import net.zic.ascension.api.ascension.core.CoreRegistries;
import net.zic.ascension.api.ascension.core.resource.ResourceTransactionService;
import net.zic.ascension.api.ascension.core.skill.ProgressingSkill;
import net.zic.ascension.api.ascension.core.skill.ProgressingSkillData;
import net.zic.ascension.api.ascension.core.skill.SkillData;
import net.zic.ascension.api.ascension.core.skill.SkillDefinitions;
import net.zic.ascension.api.ascension.core.skill.SkillMasteryRank;
import net.zic.ascension.api.ascension.core.skill.SkillProgressionData;
import net.zic.ascension.api.ascension.core.skill.SkillProgressionResolver;
import net.zic.ascension.api.ascension.core.skill.SkillProgressionService;
import net.zic.ascension.api.ascension.core.skill.SkillProgressionSnapshot;
import net.zic.ascension.api.ascension.core.skill.castable.ActiveCastData;
import net.zic.ascension.api.ascension.core.skill.castable.ActiveCastDefinition;
import net.zic.ascension.api.ascension.core.skill.castable.ActiveCastVisualState;
import net.zic.ascension.api.ascension.core.skill.castable.ActiveSkillCostDefinition;
import net.zic.ascension.api.ascension.core.skill.castable.CastData;
import net.zic.ascension.api.ascension.core.skill.castable.CastableSkill;
import net.zic.ascension.api.ascension.core.skill.castable.PreCastData;
import net.zic.ascension.api.ascension.core.skill.castable.action.SkillAction;
import net.zic.ascension.api.ascension.core.skill.castable.data.CastResult;
import net.zic.ascension.api.ascension.core.skill.castable.data.CastStatus;
import net.zic.ascension.api.ascension.core.skill.castable.data.CastType;
import net.zic.ascension.api.ascension.core.skill.particle_field.ParticleFieldDefinition;
import net.zic.ascension.api.ascension.core.source.AscensionOriginSourceHelper;
import net.zic.ascension.api.ascension.core.targeting.TargetingDefinition;
import net.zic.ascension.api.ascension.datapack.skill.SkillType;
import net.zic.ascension.api.ascension.value.ScaledValue;
import net.zic.ascension.api.rpg_engine.source.OriginSource;
import net.zic.ascension.impl.datapack.skill.AscensionSkillTypes;
import net.zic.ascension.impl.core.targeting.TargetingDefinitions;
import net.zic.zenithlib.common.ZenithAttachments;
import net.zic.zenithlib.cooldown.EntityCooldownHandler;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class ActiveSkill implements CastableSkill, ProgressingSkill, SkillDefinitions.Owner {
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

    public ActiveCastVisualState visualState(LivingEntity caster, CastData castData) {
        if (!(castData instanceof ActiveCastData data) || cast.mode() == ActiveCastDefinition.Mode.INSTANT) {
            return null;
        }
        Identifier skillId = getSkillId(caster);
        if (skillId == null) {
            return null;
        }
        return new ActiveCastVisualState(skillId, data.getStageIndex(), cast.progress(data.getTicks()));
    }

    public boolean shouldInterrupt(
            LivingEntity caster,
            CastData castData,
            int ticksElapsed,
            double damage
    ) {
        if (!(castData instanceof ActiveCastData data)) {
            return false;
        }
        Identifier skillId = getSkillId(caster);
        return skillId != null && cast.interruption()
                .map(interruption -> interruption.shouldInterrupt(
                        data,
                        ticksElapsed,
                        damage,
                        scaledValueContext(caster, skillId, null, cast.progress(data.getTicks()), castVariables(data))
                ))
                .orElse(false);
    }

    @Override
    public CastType getCastType() {
        return cast.mode().castType();
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
            return CastResult.fail(Component.literal("Invalid active skill"));
        }

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
            case INSTANT -> {
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
            case CHANNEL -> {
                ResolvedActiveCast resolved = resolve(level, caster, skillId, mastery, 0.0D, Map.of());
                if (resolved.failureMessage() != null) {
                    yield CastResult.fail(resolved.failureMessage());
                }
                yield canPayCosts(caster, skillId, resolved)
                        ? CastResult.success()
                        : CastResult.fail(Component.literal("Not enough resources"));
            }
        };
    }

    @Override
    public CastData initialCast(LivingEntity caster, PreCastData preCastData) {
        Identifier skillId = getSkillId(caster);
        if (skillId == null) {
            return null;
        }

        if (cast.mode() == ActiveCastDefinition.Mode.INSTANT) {
            if (caster.level() instanceof ServerLevel level) {
                executeInstant(level, caster, skillId);
            }
            return null;
        }

        ActiveCastData data = new ActiveCastData();
        data.setStageIndex(cast.stage(cast.progress(0)));
        return data;
    }

    @Override
    public void continueCasting(
            LivingEntity caster,
            CastStatus castStatus,
            CastData castData,
            int ticksElapsed
    ) {
        if (!(castData instanceof ActiveCastData data) || cast.mode() == ActiveCastDefinition.Mode.INSTANT) {
            castStatus.invalidate();
            return;
        }

        boolean inputActive = caster.getData(ZenithAttachments.ACTION_MANAGER)
                .isActive(net.zic.ascension.skill_casting.AscensionSkillListener.skillCast);
        if (!inputActive) {
            if (cast.mode() == ActiveCastDefinition.Mode.CHARGE) {
                if (cast.minimumReached(data.getTicks())) {
                    castStatus.release();
                } else {
                    castStatus.cancel();
                }
            } else {
                castStatus.complete();
            }
            return;
        }

        int nextTicks = cast.maximumTicks() > 0
                ? Math.min(cast.maximumTicks(), data.getTicks() + 1)
                : data.getTicks() + 1;
        data.setTicks(nextTicks);

        Identifier skillId = getSkillId(caster);
        if (skillId == null) {
            castStatus.invalidate();
            return;
        }

        double progress = cast.progress(nextTicks);
        data.setStageIndex(cast.stage(progress));
        Map<Identifier, Double> variables = castVariables(data);
        cast.movement().apply(caster, scaledValueContext(caster, skillId, null, progress, variables));
        ActiveCastDefinition.Stage stage = cast.stages().get(data.getStageIndex());
        CastSoundPlayer.playPeriodic(caster, stage.sounds(), ticksElapsed);

        if (cast.mode() == ActiveCastDefinition.Mode.CHARGE) {
            if (!caster.level().isClientSide() && cast.cost().isPresent()) {
                ResourceTransactionService.Result result = cast.cost().get().payIncrement(
                        caster,
                        skillId,
                        data,
                        scaledValueContext(caster, skillId, null, progress, variables),
                        cast.maximumTicks()
                );
                if (result != null && (!result.succeeded()
                        || result.appliedAmount() + 1.0E-8D < result.resolvedAmount())) {
                    if (cast.cost().get().releaseOnFailure() && cast.minimumReached(data.getTicks())) {
                        castStatus.outOfResource();
                    } else {
                        castStatus.cancel();
                    }
                    return;
                }
            }

            if (cast.maximumReached(data.getTicks())) {
                castStatus.complete();
            }
            return;
        }

        if (caster.level() instanceof ServerLevel level) {
            executeChannelTick(level, caster, skillId, data, castStatus);
        }
        if (cast.maximumReached(data.getTicks()) && castStatus.isCasting()) {
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
        if (!(caster.level() instanceof ServerLevel level)
                || !(castData instanceof ActiveCastData data)
                || cast.mode() == ActiveCastDefinition.Mode.INSTANT) {
            return;
        }

        Identifier skillId = getSkillId(caster);
        if (skillId == null) {
            return;
        }

        if (cast.mode() == ActiveCastDefinition.Mode.CHARGE) {
            boolean validRelease = status.isReleased()
                    || status.isCompleted()
                    || status.isOutOfResource()
                    || status.isInterrupted() && cast.interruption()
                    .map(ActiveCastDefinition.Interruption::releaseAfterMinimum)
                    .orElse(false);
            if (!validRelease || !cast.minimumReached(data.getTicks())) {
                return;
            }

            double progress = cast.progress(data.getTicks());
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
                    castVariables(data)
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
            applyCooldown(caster, skillId, resolved);
            awardMasteryExperience(caster, skillId);
            return;
        }

        if (data.getTicks() > 0 && !status.isCancelled() && !status.isInvalidated()) {
            ResolvedMastery mastery = resolveMastery(caster, skillId);
            if (mastery.failureMessage() == null) {
                ResolvedActiveCast resolved = resolve(
                        level,
                        caster,
                        skillId,
                        mastery,
                        cast.progress(data.getTicks()),
                        castVariables(data)
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
        applyCooldown(caster, skillId, resolved);
        awardMasteryExperience(caster, skillId);
    }

    private void executeChannelTick(
            ServerLevel level,
            LivingEntity caster,
            Identifier skillId,
            ActiveCastData data,
            CastStatus status
    ) {
        double progress = cast.progress(data.getTicks());
        ResolvedMastery mastery = resolveMastery(caster, skillId);
        if (mastery.failureMessage() != null) {
            status.invalidate();
            return;
        }
        ResolvedActiveCast resolved = resolve(
                level,
                caster,
                skillId,
                mastery,
                progress,
                castVariables(data)
        );
        if (resolved.failureMessage() != null) {
            return;
        }
        if (!canPayCosts(caster, skillId, resolved) || !payCosts(caster, skillId, resolved)) {
            status.outOfResource();
            return;
        }
        SkillExecutions.apply(level, caster, skillId, progress, actions, resolved.execution());
        if (data.getTicks() % CHANNEL_MASTERY_INTERVAL == 0) {
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

    private Map<Identifier, Double> castVariables(ActiveCastData data) {
        return Map.of(
                SkillExecutions.CAST_TICKS, (double) data.getTicks(),
                SkillExecutions.MAXIMUM_CAST_TICKS, (double) cast.maximumTicks(),
                SkillExecutions.CAST_PROGRESS, cast.progress(data.getTicks())
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

    private Identifier getSkillId(LivingEntity caster) {
        return CoreRegistries.SKILL_REGISTRY.get(caster.registryAccess()).getKey(this);
    }

    @Override
    public CastData loadCastData(ByteBuf buf) {
        return cast.mode() == ActiveCastDefinition.Mode.INSTANT ? null : new ActiveCastData(buf);
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
        return new Data(new SkillProgressionData(getInitialProgression(), 0.0D, Map.of()));
    }

    @Override
    public SkillData loadData(ValueInput input, RegistryAccess access) {
        return new Data(input);
    }

    @Override
    public SkillData loadData(ByteBuf buf) {
        return new Data(buf);
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

        public Data(ValueInput input) {
            this(new SkillProgressionData(input.childOrEmpty("progression")));
        }

        public Data(ByteBuf buf) {
            this(new SkillProgressionData(buf));
        }

        @Override
        public SkillProgressionData getSkillProgression() {
            return progression;
        }

        @Override
        public void write(ValueOutput output, RegistryAccess access) {
            progression.write(output.child("progression"));
        }

        @Override
        public void encode(ByteBuf buf, RegistryAccess access) {
            progression.encode(buf);
        }

        @Override
        public SkillType getType() {
            return AscensionSkillTypes.ACTIVE_SKILL_TYPE.get();
        }
    }
}
