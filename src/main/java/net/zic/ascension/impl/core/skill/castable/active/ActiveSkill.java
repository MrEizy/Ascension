package net.zic.ascension.impl.core.skill.castable.active;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.storage.ValueInput;
import net.zic.ascension.api.ascension.capabilities.AscensionEntityDataProvider;
import net.zic.ascension.api.ascension.capabilities.CoreCapabilities;
import net.zic.ascension.api.ascension.core.CoreRegistries;
import net.zic.ascension.api.ascension.core.skill.SkillData;
import net.zic.ascension.api.ascension.core.skill.castable.CastData;
import net.zic.ascension.api.ascension.core.skill.castable.CastableSkill;
import net.zic.ascension.api.ascension.core.skill.castable.PreCastData;
import net.zic.ascension.api.core.skill.castable.active.ActiveSkillCostDefinition;
import net.zic.ascension.api.core.skill.castable.active.ActiveSkillLevelDefinition;
import net.zic.ascension.api.ascension.core.skill.castable.data.CastResult;
import net.zic.ascension.api.ascension.core.skill.castable.data.CastStatus;
import net.zic.ascension.api.ascension.core.skill.castable.data.CastType;
import net.zic.ascension.api.core.skill.levelled.LevelledSkill;
import net.zic.ascension.api.core.skill.levelled.SkillLevelResolver;
import net.zic.ascension.api.core.skill.levelled.SkillLevelSnapshot;
import net.zic.ascension.api.core.skill.levelled.SkillProgressionData;
import net.zic.ascension.api.rpg_engine.source.OriginSource;
import net.zic.ascension.api.ascension.datapack.skill.SkillType;
import net.zic.ascension.api.value.ScaledValueContext;
import net.zic.ascension.common.skill.castable.SkillExecutions;
import net.zic.ascension.impl.datapack.skill.AscensionSkillTypes;
import net.zic.zenithlib.common.ZenithAttachments;
import net.zic.zenithlib.cooldown.EntityCooldownHandler;

import java.util.List;
import java.util.Map;

public final class ActiveSkill implements CastableSkill, LevelledSkill {
    private final Component name;
    private final Component description;
    private final int defaultAccessibleLevel;
    private final List<ActiveSkillLevelDefinition> levels;
    private final List<Double> experienceRequirements;

    public ActiveSkill(
            Component name,
            Component description,
            int defaultAccessibleLevel,
            List<ActiveSkillLevelDefinition> levels,
            List<Double> experienceRequirements
    ) {
        this.name = name;
        this.description = description;
        this.levels = levels == null ? List.of() : List.copyOf(levels);
        this.defaultAccessibleLevel = Math.clamp(defaultAccessibleLevel, 0, this.levels.size());
        this.experienceRequirements = experienceRequirements == null
                ? List.of()
                : experienceRequirements.stream().map(value -> Math.max(0.0D, value)).toList();
    }

    public int getConfiguredDefaultAccessibleLevel() {
        return defaultAccessibleLevel;
    }

    public List<ActiveSkillLevelDefinition> getLevels() {
        return levels;
    }

    public List<Double> getExperienceRequirements() {
        return experienceRequirements;
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
        if (!(caster instanceof ServerPlayer player) || !(player.level() instanceof ServerLevel level)) {
            return CastResult.fail();
        }

        ResolvedActiveCast resolved = resolve(level, player, skillId);
        if (resolved.failureMessage() != null) {
            return CastResult.fail(resolved.failureMessage());
        }
        if (!canPayCosts(player, skillId, resolved)) {
            return CastResult.fail(Component.literal("Not enough resources"));
        }
        return CastResult.success();
    }

    @Override
    public CastData initialCast(LivingEntity caster, PreCastData preCastData) {
        if (!(caster instanceof ServerPlayer player) || !(player.level() instanceof ServerLevel level)) {
            return null;
        }
        Identifier skillId = getSkillId(caster);
        if (skillId == null) {
            return null;
        }

        ResolvedActiveCast resolved = resolve(level, player, skillId);
        if (resolved.failureMessage() != null) {
            player.sendOverlayMessage(resolved.failureMessage());
            return null;
        }
        int cooldown = resolveCooldown(player, skillId, resolved);
        if (!payCosts(player, skillId, resolved)) {
            player.sendOverlayMessage(Component.literal("Not enough resources"));
            return null;
        }

        applyFeatures(level, player, skillId, resolved);
        if (cooldown > 0) {
            player.getData(ZenithAttachments.COOLDOWN_HANDLER).addCooldown(skillId, cooldown);
        }
        return null;
    }

    @Override
    public void continueCasting(LivingEntity caster, CastStatus castStatus, CastData castData, int ticksElapsed) {
    }

    @Override
    public void finalCast(LivingEntity caster, CastStatus status, CastData castData, int ticksElapsed) {
    }

    private ResolvedActiveCast resolve(ServerLevel level, ServerPlayer caster, Identifier skillId) {
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

    private boolean canPayCosts(ServerPlayer caster, Identifier skillId, ResolvedActiveCast resolved) {
        LivingEntity target = SkillExecutions.primaryEntity(resolved.execution());
        ScaledValueContext context = scaledValueContext(caster, skillId, target, resolved.execution().variables());
        for (ActiveSkillCostDefinition cost : resolved.definition().costs()) {
            if (!cost.canPay(caster, skillId, target, context, resolved.execution().variables())) {
                return false;
            }
        }
        return true;
    }

    private boolean payCosts(ServerPlayer caster, Identifier skillId, ResolvedActiveCast resolved) {
        if (!canPayCosts(caster, skillId, resolved)) {
            return false;
        }
        LivingEntity target = SkillExecutions.primaryEntity(resolved.execution());
        ScaledValueContext context = scaledValueContext(caster, skillId, target, resolved.execution().variables());
        for (ActiveSkillCostDefinition cost : resolved.definition().costs()) {
            if (!cost.pay(caster, skillId, target, context, resolved.execution().variables())) {
                return false;
            }
        }
        return true;
    }

    private void applyFeatures(
            ServerLevel level,
            ServerPlayer caster,
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

    private ScaledValueContext scaledValueContext(
            ServerPlayer caster,
            Identifier skillId,
            LivingEntity target,
            Map<Identifier, Double> variables
    ) {
        return new ScaledValueContext(
                getOriginSource(caster),
                skillId,
                caster,
                target,
                0.0D,
                variables
        );
    }

    private int resolveCooldown(ServerPlayer caster, Identifier skillId, ResolvedActiveCast resolved) {
        LivingEntity target = SkillExecutions.primaryEntity(resolved.execution());
        double value = resolved.definition().cooldown().resolve(
                scaledValueContext(caster, skillId, target, resolved.execution().variables())
        );
        if (!Double.isFinite(value) || value <= 0.0D) {
            return 0;
        }
        return (int) Math.clamp(Math.round(value), 0L, (long) Integer.MAX_VALUE);
    }



    private OriginSource getOriginSource(LivingEntity caster) {
        AscensionEntityDataProvider provider = caster.getCapability(
                CoreCapabilities.ASCENSION_ENTITY_DATA_PROVIDER_CAPABILITY
        );
        return provider == null ? null : provider.getData(caster).getSource();
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
        return new ActiveSkillData(new SkillProgressionData());
    }

    @Override
    public SkillData loadData(ValueInput input, RegistryAccess access) {
        return new ActiveSkillData(input);
    }

    @Override
    public SkillData loadData(ByteBuf buf) {
        return new ActiveSkillData(buf);
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
}
