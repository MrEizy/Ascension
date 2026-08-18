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
import net.zic.ascension.api.ascension.core.skill.LevelledSkill;
import net.zic.ascension.api.ascension.core.skill.LevelledSkillData;
import net.zic.ascension.api.ascension.core.skill.SkillData;
import net.zic.ascension.api.ascension.core.skill.SkillDefinitions;
import net.zic.ascension.api.ascension.core.skill.SkillLevelResolver;
import net.zic.ascension.api.ascension.core.skill.SkillLevelSnapshot;
import net.zic.ascension.api.ascension.core.skill.SkillProgressionData;
import net.zic.ascension.api.ascension.core.skill.castable.ActiveCastData;
import net.zic.ascension.api.ascension.core.skill.castable.ActiveCastDefinition;
import net.zic.ascension.api.ascension.core.skill.castable.ActiveCastVisualState;
import net.zic.ascension.api.ascension.core.skill.castable.ActiveSkillCostDefinition;
import net.zic.ascension.api.ascension.core.skill.castable.ActiveSkillLevelDefinition;
import net.zic.ascension.api.ascension.core.skill.castable.CastData;
import net.zic.ascension.api.ascension.core.skill.castable.CastableSkill;
import net.zic.ascension.api.ascension.core.skill.castable.PreCastData;
import net.zic.ascension.api.ascension.core.skill.castable.data.CastResult;
import net.zic.ascension.api.ascension.core.skill.castable.data.CastStatus;
import net.zic.ascension.api.ascension.core.skill.castable.data.CastType;
import net.zic.ascension.api.ascension.core.skill.particle_field.ParticleFieldDefinition;
import net.zic.ascension.api.ascension.core.source.AscensionOriginSourceHelper;
import net.zic.ascension.api.ascension.datapack.skill.SkillType;
import net.zic.ascension.api.ascension.value.ScaledValue;
import net.zic.ascension.api.rpg_engine.source.OriginSource;
import net.zic.ascension.impl.datapack.skill.AscensionSkillTypes;
import net.zic.zenithlib.common.ZenithAttachments;
import net.zic.zenithlib.cooldown.EntityCooldownHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class ActiveSkill implements CastableSkill, LevelledSkill, SkillDefinitions.Owner {
    private final Component name;
    private final Component description;
    private final int defaultAccessibleLevel;
    private final int initialLevel;
    private final ActiveCastDefinition cast;
    private final List<ActiveSkillLevelDefinition> levels;
    private final List<Double> experienceRequirements;
    private final SkillDefinitions definitions;

    public ActiveSkill(
            Component name,
            Component description,
            int defaultAccessibleLevel,
            int initialLevel,
            SkillDefinitions definitions,
            ActiveCastDefinition cast,
            ActiveSkillLevelDefinition.Template root,
            List<ActiveSkillLevelDefinition.Template> templates,
            List<Double> experienceRequirements
    ) {
        this.name = name;
        this.description = description;
        this.definitions = definitions == null ? SkillDefinitions.EMPTY : definitions;
        this.cast = cast == null ? ActiveCastDefinition.instant() : cast;
        this.levels = resolveLevels(root, templates);
        this.defaultAccessibleLevel = Math.clamp(
                defaultAccessibleLevel <= 0 ? this.levels.size() : defaultAccessibleLevel,
                0,
                this.levels.size()
        );
        this.initialLevel = Math.clamp(initialLevel, 0, this.levels.size());
        this.experienceRequirements = experienceRequirements == null
                ? List.of()
                : experienceRequirements.stream().map(value -> Math.max(0.0D, value)).toList();
    }

    private static List<ActiveSkillLevelDefinition> resolveLevels(
            ActiveSkillLevelDefinition.Template root,
            List<ActiveSkillLevelDefinition.Template> templates
    ) {
        ActiveSkillLevelDefinition.Template base = root == null
                ? emptyTemplate()
                : root;
        List<ActiveSkillLevelDefinition> values = new ArrayList<>();
        ActiveSkillLevelDefinition previous = null;
        if (templates == null || templates.isEmpty()) {
            values.add(base.resolve(null));
            return List.copyOf(values);
        }
        for (ActiveSkillLevelDefinition.Template template : templates) {
            ActiveSkillLevelDefinition.Template current = merge(base, template);
            previous = current.resolve(previous);
            values.add(previous);
        }
        return List.copyOf(values);
    }

    private static ActiveSkillLevelDefinition.Template emptyTemplate() {
        return new ActiveSkillLevelDefinition.Template(
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty()
        );
    }

    private static ActiveSkillLevelDefinition.Template merge(
            ActiveSkillLevelDefinition.Template root,
            ActiveSkillLevelDefinition.Template level
    ) {
        if (level == null) {
            return root;
        }
        return new ActiveSkillLevelDefinition.Template(
                level.targeting().isPresent() ? level.targeting() : root.targeting(),
                level.requireTargets().isPresent() ? level.requireTargets() : root.requireTargets(),
                level.actions().isPresent() ? level.actions() : root.actions(),
                level.costs().isPresent() ? level.costs() : root.costs(),
                level.cooldown().isPresent() ? level.cooldown() : root.cooldown()
        );
    }

    public int getConfiguredDefaultAccessibleLevel() {
        return defaultAccessibleLevel;
    }

    public int getInitialLevel() {
        return initialLevel;
    }

    public ActiveCastDefinition cast() {
        return cast;
    }

    public List<ActiveSkillLevelDefinition> getLevels() {
        return levels;
    }

    public ActiveSkillLevelDefinition.Template getRootTemplate() {
        return levels.isEmpty() ? emptyTemplate() : ActiveSkillLevelDefinition.Template.from(levels.getFirst());
    }

    public List<ActiveSkillLevelDefinition.Template> getLevelTemplates() {
        return levels.stream().map(ActiveSkillLevelDefinition.Template::from).toList();
    }

    public List<Double> getExperienceRequirements() {
        return experienceRequirements;
    }

    @Override
    public SkillDefinitions definitions() {
        return definitions;
    }

    public ActiveSkillLevelDefinition getLevelDefinition(int level) {
        if (level <= 0 || level > levels.size()) {
            return null;
        }
        return levels.get(level - 1);
    }

    public Optional<ParticleFieldDefinition> particleField(int stage) {
        return cast.particleField(stage);
    }

    public Optional<Identifier> cultivationPath() {
        for (ActiveSkillLevelDefinition level : levels) {
            for (var action : level.execution().actions()) {
                if (action instanceof SkillActions.Cultivate cultivate) {
                    return Optional.of(cultivate.path());
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

        ResolvedLevel resolvedLevel = resolveLevel(caster, skillId);
        if (resolvedLevel.failureMessage() != null) {
            return CastResult.fail(resolvedLevel.failureMessage());
        }

        return switch (cast.mode()) {
            case INSTANT -> {
                ResolvedActiveCast resolved = resolve(level, caster, skillId, resolvedLevel, 0.0D, Map.of());
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
                ResolvedActiveCast resolved = resolve(level, caster, skillId, resolvedLevel, 0.0D, Map.of());
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
            ResolvedLevel resolvedLevel = resolveLevel(caster, skillId);
            if (resolvedLevel.failureMessage() != null) {
                return;
            }
            ResolvedActiveCast resolved = resolve(
                    level,
                    caster,
                    skillId,
                    resolvedLevel,
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
            SkillExecutions.apply(level, caster, skillId, progress, resolved.definition().execution(), resolved.execution());
            applyCooldown(caster, skillId, resolved);
            return;
        }

        if (data.getTicks() > 0 && !status.isCancelled() && !status.isInvalidated()) {
            ResolvedLevel resolvedLevel = resolveLevel(caster, skillId);
            if (resolvedLevel.failureMessage() == null) {
                ResolvedActiveCast resolved = resolve(
                        level,
                        caster,
                        skillId,
                        resolvedLevel,
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
        ResolvedLevel resolvedLevel = resolveLevel(caster, skillId);
        if (resolvedLevel.failureMessage() != null) {
            sendFailure(caster, resolvedLevel.failureMessage());
            return;
        }
        ResolvedActiveCast resolved = resolve(level, caster, skillId, resolvedLevel, 0.0D, Map.of());
        if (resolved.failureMessage() != null) {
            sendFailure(caster, resolved.failureMessage());
            return;
        }
        if (!payCosts(caster, skillId, resolved)) {
            sendFailure(caster, Component.literal("Not enough resources"));
            return;
        }
        SkillExecutions.apply(level, caster, skillId, 0.0D, resolved.definition().execution(), resolved.execution());
        applyCooldown(caster, skillId, resolved);
    }

    private void executeChannelTick(
            ServerLevel level,
            LivingEntity caster,
            Identifier skillId,
            ActiveCastData data,
            CastStatus status
    ) {
        double progress = cast.progress(data.getTicks());
        ResolvedLevel resolvedLevel = resolveLevel(caster, skillId);
        if (resolvedLevel.failureMessage() != null) {
            status.invalidate();
            return;
        }
        ResolvedActiveCast resolved = resolve(
                level,
                caster,
                skillId,
                resolvedLevel,
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
        SkillExecutions.apply(level, caster, skillId, progress, resolved.definition().execution(), resolved.execution());
    }

    private ResolvedLevel resolveLevel(LivingEntity caster, Identifier skillId) {
        OriginSource source = getOriginSource(caster);
        SkillLevelSnapshot snapshot = SkillLevelResolver.resolve(source, skillId);
        ActiveSkillLevelDefinition definition = getLevelDefinition(snapshot.effectiveLevel());
        if (definition == null) {
            return ResolvedLevel.failure(Component.literal("Skill level is not accessible"));
        }
        return new ResolvedLevel(snapshot.effectiveLevel(), definition, null);
    }

    private ResolvedActiveCast resolve(
            ServerLevel level,
            LivingEntity caster,
            Identifier skillId,
            ResolvedLevel resolvedLevel,
            double progress,
            Map<Identifier, Double> variables
    ) {
        SkillExecutions.Resolution execution = SkillExecutions.resolve(
                level,
                caster,
                skillId,
                resolvedLevel.effectiveLevel(),
                progress,
                variables,
                resolvedLevel.definition().execution()
        );
        if (!execution.succeeded()) {
            return ResolvedActiveCast.failure(execution.failureMessage());
        }
        return new ResolvedActiveCast(
                resolvedLevel.effectiveLevel(),
                resolvedLevel.definition(),
                execution,
                null
        );
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
        for (ActiveSkillCostDefinition cost : resolved.definition().costs()) {
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
        for (ActiveSkillCostDefinition cost : resolved.definition().costs()) {
            if (!cost.pay(caster, skillId, target, context, resolved.execution().variables())) {
                return false;
            }
        }
        return true;
    }

    private void applyCooldown(LivingEntity caster, Identifier skillId, ResolvedActiveCast resolved) {
        int cooldown = resolveCooldown(caster, skillId, resolved);
        if (cooldown > 0) {
            caster.getData(ZenithAttachments.COOLDOWN_HANDLER).addCooldown(skillId, cooldown);
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
        double value = resolved.definition().cooldown().resolve(
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
    public int getMaximumLevel() {
        return levels.size();
    }

    @Override
    public int getDefaultAccessibleLevel() {
        return defaultAccessibleLevel;
    }

    @Override
    public double getExperienceRequiredForNextLevel(int currentLevel) {
        if (currentLevel < 0 || currentLevel >= experienceRequirements.size()) {
            return Double.POSITIVE_INFINITY;
        }
        return experienceRequirements.get(currentLevel);
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
        return new Data(new SkillProgressionData(initialLevel, 0.0D, Map.of(), Map.of()));
    }

    @Override
    public SkillData loadData(ValueInput input, RegistryAccess access) {
        return new Data(input);
    }

    @Override
    public SkillData loadData(ByteBuf buf) {
        return new Data(buf);
    }

    private record ResolvedLevel(
            int effectiveLevel,
            ActiveSkillLevelDefinition definition,
            Component failureMessage
    ) {
        private static ResolvedLevel failure(Component message) {
            return new ResolvedLevel(0, null, message);
        }
    }

    private record ResolvedActiveCast(
            int effectiveLevel,
            ActiveSkillLevelDefinition definition,
            SkillExecutions.Resolution execution,
            Component failureMessage
    ) {
        private static ResolvedActiveCast failure(Component message) {
            return new ResolvedActiveCast(0, null, null, message);
        }
    }

    public static final class Data implements LevelledSkillData {
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
