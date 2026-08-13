package net.zic.ascension.impl.core.skill.castable;

import net.zic.ascension.api.ascension.value.ScaledValue;
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
import net.zic.ascension.api.ascension.core.skill.SkillData;
import net.zic.ascension.api.ascension.core.skill.LevelledSkillData;
import net.zic.ascension.api.ascension.core.skill.castable.CastData;
import net.zic.ascension.api.ascension.core.skill.castable.CastableSkill;
import net.zic.ascension.api.ascension.core.skill.castable.PreCastData;
import net.zic.ascension.api.ascension.core.skill.castable.ActiveSkillCostDefinition;
import net.zic.ascension.api.ascension.core.skill.castable.ActiveSkillLevelDefinition;
import net.zic.ascension.api.ascension.core.skill.castable.data.CastResult;
import net.zic.ascension.api.ascension.core.skill.castable.data.CastStatus;
import net.zic.ascension.api.ascension.core.skill.castable.data.CastType;
import net.zic.ascension.api.ascension.core.skill.LevelledSkill;
import net.zic.ascension.api.ascension.core.skill.SkillLevelResolver;
import net.zic.ascension.api.ascension.core.skill.SkillLevelSnapshot;
import net.zic.ascension.api.ascension.core.skill.SkillProgressionData;
import net.zic.ascension.api.ascension.core.skill.SkillDefinitions.Owner;
import net.zic.ascension.api.ascension.core.skill.SkillDefinitions;
import net.zic.ascension.api.ascension.core.source.AscensionOriginSourceHelper;
import net.zic.ascension.api.rpg_engine.source.OriginSource;
import net.zic.ascension.api.ascension.datapack.skill.SkillType;
import net.zic.ascension.impl.datapack.skill.AscensionSkillTypes;
import net.zic.zenithlib.common.ZenithAttachments;
import net.zic.zenithlib.cooldown.EntityCooldownHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class ActiveSkill implements CastableSkill, LevelledSkill, Owner {
    private final Component name;
    private final Component description;
    private final int defaultAccessibleLevel;
    private final List<ActiveSkillLevelDefinition> levels;
    private final List<Double> experienceRequirements;
    private final SkillDefinitions definitions;

    public ActiveSkill(
            Component name,
            Component description,
            int defaultAccessibleLevel,
            SkillDefinitions definitions,
            ActiveSkillLevelDefinition.Template root,
            List<ActiveSkillLevelDefinition.Template> templates,
            List<Double> experienceRequirements
    ) {
        this.name = name;
        this.description = description;
        this.definitions = definitions == null ? SkillDefinitions.EMPTY : definitions;
        this.levels = resolveLevels(root, templates);
        this.defaultAccessibleLevel = Math.clamp(
                defaultAccessibleLevel <= 0 ? this.levels.size() : defaultAccessibleLevel,
                0,
                this.levels.size()
        );
        this.experienceRequirements = experienceRequirements == null
                ? List.of()
                : experienceRequirements.stream().map(value -> Math.max(0.0D, value)).toList();
    }

    private static List<ActiveSkillLevelDefinition> resolveLevels(
            ActiveSkillLevelDefinition.Template root,
            List<ActiveSkillLevelDefinition.Template> templates
    ) {
        ActiveSkillLevelDefinition.Template base = root == null
                ? new ActiveSkillLevelDefinition.Template(java.util.Optional.empty(), java.util.Optional.empty(), java.util.Optional.empty(), java.util.Optional.empty(), java.util.Optional.empty())
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
                level.features().isPresent() ? level.features() : root.features(),
                level.costs().isPresent() ? level.costs() : root.costs(),
                level.cooldown().isPresent() ? level.cooldown() : root.cooldown()
        );
    }

    public int getConfiguredDefaultAccessibleLevel() {
        return defaultAccessibleLevel;
    }

    public List<ActiveSkillLevelDefinition> getLevels() {
        return levels;
    }

    public ActiveSkillLevelDefinition.Template getRootTemplate() {
        return levels.isEmpty()
                ? new ActiveSkillLevelDefinition.Template(java.util.Optional.empty(), java.util.Optional.empty(), java.util.Optional.empty(), java.util.Optional.empty(), java.util.Optional.empty())
                : ActiveSkillLevelDefinition.Template.from(levels.getFirst());
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

    @Override
    public CastType getCastType() {
        return CastType.INSTANT;
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

        ResolvedActiveCast resolved = resolve(level, caster, skillId);
        if (resolved.failureMessage() != null) {
            return CastResult.fail(resolved.failureMessage());
        }
        if (!canPayCosts(caster, skillId, resolved)) {
            return CastResult.fail(Component.literal("Not enough resources"));
        }
        return CastResult.success();
    }

    @Override
    public CastData initialCast(LivingEntity caster, PreCastData preCastData) {
        if (!(caster.level() instanceof ServerLevel level)) {
            return null;
        }
        Identifier skillId = getSkillId(caster);
        if (skillId == null) {
            return null;
        }

        ResolvedActiveCast resolved = resolve(level, caster, skillId);
        if (resolved.failureMessage() != null) {
            sendFailure(caster, resolved.failureMessage());
            return null;
        }
        int cooldown = resolveCooldown(caster, skillId, resolved);
        if (!payCosts(caster, skillId, resolved)) {
            sendFailure(caster, Component.literal("Not enough resources"));
            return null;
        }

        applyFeatures(level, caster, skillId, resolved);
        if (cooldown > 0) {
            caster.getData(ZenithAttachments.COOLDOWN_HANDLER).addCooldown(skillId, cooldown);
        }
        return null;
    }

    @Override
    public void continueCasting(LivingEntity caster, CastStatus castStatus, CastData castData, int ticksElapsed) {
    }

    @Override
    public void finalCast(LivingEntity caster, CastStatus status, CastData castData, int ticksElapsed) {
    }

    private ResolvedActiveCast resolve(ServerLevel level, LivingEntity caster, Identifier skillId) {
        OriginSource source = getOriginSource(caster);
        SkillLevelSnapshot snapshot = SkillLevelResolver.resolve(source, skillId);
        ActiveSkillLevelDefinition definition = getLevelDefinition(snapshot.effectiveLevel());
        if (definition == null) {
            return ResolvedActiveCast.failure(Component.literal("Skill level is not accessible"));
        }
        SkillExecutions.Resolution execution = SkillExecutions.resolve(
                level,
                caster,
                skillId,
                snapshot.effectiveLevel(),
                0.0D,
                Map.of(),
                definition.execution()
        );
        if (!execution.succeeded()) {
            return ResolvedActiveCast.failure(execution.failureMessage());
        }
        return new ResolvedActiveCast(snapshot.effectiveLevel(), definition, execution, null);
    }

    private boolean canPayCosts(LivingEntity caster, Identifier skillId, ResolvedActiveCast resolved) {
        LivingEntity target = SkillExecutions.primaryEntity(resolved.execution());
        ScaledValue.Context context = scaledValueContext(caster, skillId, target, resolved.execution().variables());
        for (ActiveSkillCostDefinition cost : resolved.definition().costs()) {
            if (!cost.canPay(caster, skillId, target, context, resolved.execution().variables())) {
                return false;
            }
        }
        return true;
    }

    private boolean payCosts(LivingEntity caster, Identifier skillId, ResolvedActiveCast resolved) {
        if (!canPayCosts(caster, skillId, resolved)) {
            return false;
        }
        LivingEntity target = SkillExecutions.primaryEntity(resolved.execution());
        ScaledValue.Context context = scaledValueContext(caster, skillId, target, resolved.execution().variables());
        for (ActiveSkillCostDefinition cost : resolved.definition().costs()) {
            if (!cost.pay(caster, skillId, target, context, resolved.execution().variables())) {
                return false;
            }
        }
        return true;
    }

    private void applyFeatures(
            ServerLevel level,
            LivingEntity caster,
            Identifier skillId,
            ResolvedActiveCast resolved
    ) {
        SkillExecutions.apply(
                level,
                caster,
                skillId,
                0.0D,
                resolved.definition().execution(),
                resolved.execution()
        );
    }

    private ScaledValue.Context scaledValueContext(
            LivingEntity caster,
            Identifier skillId,
            LivingEntity target,
            Map<Identifier, Double> variables
    ) {
        return new ScaledValue.Context(
                getOriginSource(caster),
                skillId,
                caster,
                target,
                0.0D,
                variables
        );
    }

    private int resolveCooldown(LivingEntity caster, Identifier skillId, ResolvedActiveCast resolved) {
        LivingEntity target = SkillExecutions.primaryEntity(resolved.execution());
        double value = resolved.definition().cooldown().resolve(
                scaledValueContext(caster, skillId, target, resolved.execution().variables())
        );
        if (!Double.isFinite(value) || value <= 0.0D) {
            return 0;
        }
        return (int) Math.clamp(Math.round(value), 0L, (long) Integer.MAX_VALUE);
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
        return new Data(new SkillProgressionData());
    }

    @Override
    public SkillData loadData(ValueInput input, RegistryAccess access) {
        return new Data(input);
    }

    @Override
    public SkillData loadData(ByteBuf buf) {
        return new Data(buf);
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
        public void write(ValueOutput output,RegistryAccess access) {
            progression.write(output.child("progression"));
        }

        @Override
        public void encode(ByteBuf buf,RegistryAccess access) {
            progression.encode(buf);
        }

        @Override
        public SkillType getType() {
            return AscensionSkillTypes.ACTIVE_SKILL_TYPE.get();
        }
    }

}
