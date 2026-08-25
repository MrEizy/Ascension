package net.zic.ascension.impl.core.skill.body;

import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.ascension.core.CoreRegistries;
import net.zic.ascension.api.ascension.core.path.PathInstance;
import net.zic.ascension.api.ascension.core.resource.ResourceOperation;
import net.zic.ascension.api.ascension.core.resource.ResourceSourceIdentity;
import net.zic.ascension.api.ascension.core.resource.ResourceTransactionRequest;
import net.zic.ascension.api.ascension.core.resource.ResourceTransactionService;
import net.zic.ascension.api.ascension.core.skill.Skill;
import net.zic.ascension.api.ascension.core.skill.SkillData;
import net.zic.ascension.api.ascension.core.source.AscensionOriginSourceHelper;
import net.zic.ascension.api.ascension.value.ScaledValue;
import net.zic.ascension.api.rpg_engine.source.OriginSource;
import net.zic.ascension.impl.resource.AscensionResourceSources;
import net.zic.ascension.util.CultivationUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class BodyCultivationSkillService {
    public static final Identifier STIMULUS_AMOUNT = AscensionCraft.prefix("body_cultivation/stimulus_amount");
    public static final Identifier TEMPERING = AscensionCraft.prefix("body_cultivation/tempering");
    public static final Identifier CONVERSION_AMOUNT = AscensionCraft.prefix("body_cultivation/conversion_amount");
    private static final int SYNC_INTERVAL = 10;
    private static final double EPSILON = 1.0E-9D;

    private BodyCultivationSkillService() {
    }

    public static void stimulate(ServerPlayer player, ResourceSourceIdentity stimulusSource, double amount) {
        if (!validPlayer(player) || stimulusSource == null || !Double.isFinite(amount) || amount <= 0.0D) {
            return;
        }
        OriginSource source = AscensionOriginSourceHelper.getEntitySource(player);
        if (source == null) {
            return;
        }

        Entry entry = activeEntry(player, source);
        if (entry == null) {
            return;
        }
        BodyCultivationSkill bodySkill = entry.skill();
        BodyCultivationSkill.Data data = entry.data();
        Identifier skillId = entry.skillId();

        PathInstance path = AscensionOriginSourceHelper.getPathInstance(source, bodySkill.path());
        if (path == null || !path.canProgress()) {
            return;
        }

        ScaledValue.Context context = context(source, skillId, player, data).withVariable(STIMULUS_AMOUNT, amount);
        double gain = 0.0D;
        for (BodyCultivationSkill.Stimulus stimulus : bodySkill.stimuli()) {
            if (!stimulus.source().matches(stimulusSource)) {
                continue;
            }
            double resolved = stimulus.gain().resolve(context);
            if (Double.isFinite(resolved) && resolved > 0.0D) {
                gain += amount * resolved;
            }
        }
        if (gain <= EPSILON) {
            return;
        }

        double multiplier = 1.0D;
        for (BodyCultivationSkill.Multiplier definition : bodySkill.multipliers()) {
            if (!definition.source().matches(stimulusSource) || !definition.condition().matches(player)) {
                continue;
            }
            double resolved = definition.multiplier().resolve(context);
            if (!Double.isFinite(resolved)) {
                continue;
            }
            multiplier *= Math.max(0.0D, resolved);
        }
        gain *= multiplier;
        if (gain <= EPSILON) {
            return;
        }

        double maximum = Math.max(0.0D, bodySkill.maximumTempering().resolve(context));
        data.setTempering(Math.min(maximum, data.tempering() + gain));
    }

    public static void tick(ServerPlayer player) {
        if (!validPlayer(player)) {
            return;
        }
        OriginSource source = AscensionOriginSourceHelper.getEntitySource(player);
        if (source == null) {
            return;
        }

        Entry entry = activeEntry(player, source);
        if (entry == null) {
            return;
        }

        long gameTime = player.level().getGameTime();
        if (gameTime % entry.skill().conversion().interval() == 0L) {
            convert(player, source, entry.skillId(), entry.skill(), entry.data());
        }
        if (gameTime % SYNC_INTERVAL == 0L && entry.data().consumeDirty()) {
            AscensionOriginSourceHelper.markSkillDirty(source, entry.skillId());
        }
    }

    private static void convert(
            ServerPlayer player,
            OriginSource source,
            Identifier skillId,
            BodyCultivationSkill skill,
            BodyCultivationSkill.Data data
    ) {
        if (data.tempering() <= EPSILON) {
            return;
        }
        PathInstance path = AscensionOriginSourceHelper.getPathInstance(source, skill.path());
        if (path == null || !path.canProgress()) {
            return;
        }

        ScaledValue.Context baseContext = context(source, skillId, player, data);
        double rate = skill.conversion().rate().resolve(baseContext);
        if (!Double.isFinite(rate) || rate <= EPSILON) {
            return;
        }
        double amount = Math.min(data.tempering(), rate);
        ScaledValue.Context conversionContext = baseContext.withVariable(CONVERSION_AMOUNT, amount);
        double progressPerTempering = skill.conversion().progressPerTempering().resolve(conversionContext);
        if (!Double.isFinite(progressPerTempering) || progressPerTempering <= EPSILON) {
            return;
        }

        List<ResourceTransactionRequest> requests = skill.conversion().costs().stream()
                .map(cost -> request(player, skillId, cost, amount, conversionContext))
                .filter(java.util.Objects::nonNull)
                .toList();

        for (ResourceTransactionRequest request : requests) {
            ResourceTransactionRequest simulation = request.withFlags(Set.of(ResourceTransactionRequest.Flag.SIMULATE));
            if (!ResourceTransactionService.transact(simulation).succeeded()) {
                return;
            }
        }
        for (ResourceTransactionRequest request : requests) {
            if (!ResourceTransactionService.transact(request).succeeded()) {
                return;
            }
        }

        CultivationUtil.cultivate(
                player,
                source,
                skill.path(),
                path,
                skill.path(),
                amount * progressPerTempering
        );
        data.setTempering(Math.max(0.0D, data.tempering() - amount));
    }

    private static ResourceTransactionRequest request(
            ServerPlayer player,
            Identifier skillId,
            BodyCultivationSkill.ResourceCost cost,
            double temperingAmount,
            ScaledValue.Context context
    ) {
        double perTempering = cost.amount().resolve(context);
        if (!Double.isFinite(perTempering) || perTempering <= EPSILON) {
            return null;
        }
        double amount = perTempering * temperingAmount;
        if (!Double.isFinite(amount) || amount <= EPSILON) {
            return null;
        }

        Map<Identifier, Double> values = new HashMap<>(context.variables());
        return ResourceTransactionRequest.of(
                        player,
                        cost.resource(),
                        ResourceOperation.CONSUME,
                        amount,
                        AscensionResourceSources.BODY_CULTIVATION
                )
                .withSkill(skillId)
                .withValues(values);
    }

    private static ScaledValue.Context context(
            OriginSource source,
            Identifier skillId,
            ServerPlayer player,
            BodyCultivationSkill.Data data
    ) {
        return new ScaledValue.Context(
                source,
                skillId,
                player,
                player,
                0.0D,
                Map.of(TEMPERING, data.tempering())
        );
    }

    private static Entry activeEntry(ServerPlayer player, OriginSource source) {
        Entry selected = null;
        for (Identifier skillId : List.copyOf(AscensionOriginSourceHelper.getSkills(source))) {
            Skill skill = CoreRegistries.safeAccess(CoreRegistries.SKILL_REGISTRY, skillId, player.registryAccess());
            SkillData skillData = AscensionOriginSourceHelper.getSkillData(source, skillId);
            if (!(skill instanceof BodyCultivationSkill bodySkill) || !(skillData instanceof BodyCultivationSkill.Data data)) {
                continue;
            }
            Entry candidate = new Entry(skillId, bodySkill, data);
            if (selected == null
                    || candidate.skill().priority() > selected.skill().priority()
                    || (candidate.skill().priority() == selected.skill().priority()
                    && candidate.skillId().toString().compareTo(selected.skillId().toString()) < 0)) {
                selected = candidate;
            }
        }
        return selected;
    }

    private record Entry(Identifier skillId, BodyCultivationSkill skill, BodyCultivationSkill.Data data) {
    }

    private static boolean validPlayer(ServerPlayer player) {
        return player != null
                && !player.level().isClientSide()
                && player.isAlive()
                && !player.isRemoved()
                && !player.isSpectator()
                && !player.getAbilities().instabuild;
    }
}
