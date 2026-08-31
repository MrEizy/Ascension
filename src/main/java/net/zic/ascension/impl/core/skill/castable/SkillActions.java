package net.zic.ascension.impl.core.skill.castable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.ascension.core.control.StaggerDefinition;
import net.zic.ascension.api.ascension.core.damage.SkillDamageDefinition;
import net.zic.ascension.api.ascension.core.effect.SkillEffectDefinition;
import net.zic.ascension.api.ascension.core.path.PathInstance;
import net.zic.ascension.api.ascension.core.projectile.VirtualProjectileDefinition;
import net.zic.ascension.api.ascension.core.resource.ResourceOperation;
import net.zic.ascension.api.ascension.core.resource.ResourceTransactionRequest;
import net.zic.ascension.api.ascension.core.resource.ResourceTransactionService;
import net.zic.ascension.api.ascension.core.runtime.AnchorNetworkDefinition;
import net.zic.ascension.api.ascension.core.runtime.AreaFieldDefinition;
import net.zic.ascension.api.ascension.core.runtime.BarrierDefinition;
import net.zic.ascension.api.ascension.core.runtime.BeamDefinition;
import net.zic.ascension.api.ascension.core.runtime.OwnerBoundConstructDefinition;
import net.zic.ascension.api.ascension.core.runtime.RuntimeVisualDefinition;
import net.zic.ascension.api.ascension.core.runtime.RuntimeVisualState;
import net.zic.ascension.api.ascension.core.skill.castable.SkillCondition;
import net.zic.ascension.api.ascension.core.skill.castable.action.ActionSubject;
import net.zic.ascension.api.ascension.core.skill.castable.action.SkillActionContext;
import net.zic.ascension.api.ascension.core.skill.castable.action.SkillAction;
import net.zic.ascension.api.ascension.core.skill.DefinitionRef;
import net.zic.ascension.api.ascension.core.skill.SkillDefinitions.Resolved;
import net.zic.ascension.api.ascension.core.skill.SkillMasteryRank;
import net.zic.ascension.api.ascension.core.skill.SkillDefinitions;
import net.zic.ascension.api.ascension.core.source.AscensionOriginSourceHelper;
import net.zic.ascension.api.ascension.datapack.CodecHelpers;
import net.zic.ascension.api.ascension.datapack.CodecType;
import net.zic.ascension.api.ascension.value.HexColorCodec;
import net.zic.ascension.api.ascension.value.ScaledValue;
import net.zic.ascension.impl.core.control.StaggerService;
import net.zic.ascension.impl.core.damage.AscensionDamageService;
import net.zic.ascension.impl.core.effect.AscensionBuildupChannels;
import net.zic.ascension.impl.core.effect.SkillEffectService;
import net.zic.ascension.impl.core.movement.MovementService;
import net.zic.ascension.impl.datapack.skill.AscensionSkillActionTypes;
import net.zic.ascension.impl.runtime.object.Barriers;
import net.zic.ascension.impl.runtime.object.AnchorNetworks;
import net.zic.ascension.impl.runtime.object.AreaFields;
import net.zic.ascension.impl.runtime.object.OwnerBoundConstructs;
import net.zic.ascension.impl.runtime.projectile.VirtualProjectiles;
import net.zic.ascension.impl.runtime.object.RuntimeVisualSync;
import net.zic.ascension.impl.runtime.object.Beams;
import net.zic.ascension.impl.runtime.object.PersistentRuntimeVisuals;
import net.zic.ascension.impl.runtime.weapon.WeaponSwingSpec;
import net.zic.ascension.impl.runtime.weapon.WeaponTechniqueResolver;
import net.zic.ascension.impl.runtime.weapon.WeaponVfxUtils;
import net.zic.ascension.network.ClientboundDivineSensePacket;
import net.zic.ascension.util.CultivationUtil;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import net.zic.ascension.api.ascension.core.resource.ResourceSourceIdentity;

public final class SkillActions {
    private static final Identifier DEFAULT_DAMAGE_TYPE = Identifier.fromNamespaceAndPath("minecraft", "player_attack");
    private static final Identifier DEFAULT_RESOURCE_SOURCE = AscensionCraft.prefix("skill_casting");
    private static final Identifier FROZEN = AscensionCraft.prefix("frozen");

    private SkillActions() {
    }

    private static Vec3 subjectPosition(SkillActionContext context, ActionSubject subject) {
        LivingEntity entity = context.entity(subject);
        return entity == null ? context.position() : entity.getBoundingBox().getCenter();
    }

    public record MasteryGate(
            SkillMasteryRank minimum,
            List<SkillAction> actions
    ) implements SkillAction {
        public static final MapCodec<MasteryGate> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                SkillMasteryRank.CODEC.fieldOf("minimum").forGetter(MasteryGate::minimum),
                SkillAction.CODEC.listOf().fieldOf("actions").forGetter(MasteryGate::actions)
        ).apply(instance, MasteryGate::new));

        public MasteryGate {
            minimum = minimum == null ? SkillMasteryRank.INITIATE : minimum;
            actions = actions == null ? List.of() : List.copyOf(actions);
        }

        @Override
        public CodecType<SkillAction> getType() {
            return AscensionSkillActionTypes.MASTERY_GATE.get();
        }

        @Override
        public ActionSubject subject() {
            return ActionSubject.CASTER;
        }

        @Override
        public void apply(SkillActionContext context) {
        }
    }

    public record Conditional(
            ActionSubject subject,
            SkillCondition condition,
            List<SkillAction> ifTrue,
            List<SkillAction> ifFalse
    ) implements SkillAction {
        public static final MapCodec<Conditional> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                ActionSubject.CODEC.optionalFieldOf("subject", ActionSubject.CASTER).forGetter(Conditional::subject),
                SkillCondition.CODEC.fieldOf("condition").forGetter(Conditional::condition),
                SkillAction.CODEC.listOf().fieldOf("if_true").forGetter(Conditional::ifTrue),
                SkillAction.CODEC.listOf().optionalFieldOf("if_false", List.of()).forGetter(Conditional::ifFalse)
        ).apply(instance, Conditional::new));

        public Conditional {
            subject = subject == null ? ActionSubject.CASTER : subject;
            ifTrue = ifTrue == null ? List.of() : List.copyOf(ifTrue);
            ifFalse = ifFalse == null ? List.of() : List.copyOf(ifFalse);
        }

        @Override
        public CodecType<SkillAction> getType() {
            return AscensionSkillActionTypes.CONDITIONAL.get();
        }

        @Override
        public void apply(SkillActionContext context) {
            if (context == null || condition == null) {
                return;
            }
            List<SkillAction> actions = condition.test(context) ? ifTrue : ifFalse;
            for (SkillAction action : actions) {
                if (action != null) {
                    action.apply(context);
                }
            }
        }
    }

    public record Delay(ActionSubject subject, int ticks, List<SkillAction> actions) implements SkillAction {
        public static final MapCodec<Delay> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                ActionSubject.CODEC.optionalFieldOf("subject", ActionSubject.CASTER).forGetter(Delay::subject),
                Codec.intRange(1, 72000).fieldOf("ticks").forGetter(Delay::ticks),
                SkillAction.CODEC.listOf().fieldOf("actions").forGetter(Delay::actions)
        ).apply(instance, Delay::new));

        public Delay {
            actions = actions == null ? List.of() : List.copyOf(actions);
        }

        @Override
        public CodecType<SkillAction> getType() {
            return AscensionSkillActionTypes.DELAY.get();
        }

        @Override
        public void apply(SkillActionContext context) {
            SkillActionScheduler.schedule(context, ticks, actions);
        }
    }

    public record Repeat(ActionSubject subject, int times, int interval, List<SkillAction> actions) implements SkillAction {
        public static final MapCodec<Repeat> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                ActionSubject.CODEC.optionalFieldOf("subject", ActionSubject.CASTER).forGetter(Repeat::subject),
                Codec.intRange(1, 1024).fieldOf("times").forGetter(Repeat::times),
                Codec.intRange(1, 72000).optionalFieldOf("interval", 1).forGetter(Repeat::interval),
                SkillAction.CODEC.listOf().fieldOf("actions").forGetter(Repeat::actions)
        ).apply(instance, Repeat::new));

        public Repeat {
            actions = actions == null ? List.of() : List.copyOf(actions);
        }

        @Override
        public CodecType<SkillAction> getType() {
            return AscensionSkillActionTypes.REPEAT.get();
        }

        @Override
        public void apply(SkillActionContext context) {
            for (int i = 1; i <= times; i++) {
                SkillActionScheduler.schedule(context, (long) interval * i, actions);
            }
        }
    }

    public record Variable(Identifier variable, VariableOperation operation, ScaledValue value) implements SkillAction {
        public static final MapCodec<Variable> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Identifier.CODEC.fieldOf("variable").forGetter(Variable::variable),
                VariableOperation.CODEC.optionalFieldOf("operation", VariableOperation.SET).forGetter(Variable::operation),
                ScaledValue.COMPACT_CODEC.fieldOf("value").forGetter(Variable::value)
        ).apply(instance, Variable::new));

        @Override
        public CodecType<SkillAction> getType() {
            return AscensionSkillActionTypes.VARIABLE.get();
        }

        @Override
        public ActionSubject subject() {
            return ActionSubject.CASTER;
        }

        @Override
        public void apply(SkillActionContext context) {
        }
    }

    public enum VariableOperation implements StringRepresentable {
        SET("set"), ADD("add"), MULTIPLY("multiply");

        public static final Codec<VariableOperation> CODEC = StringRepresentable.fromEnum(VariableOperation::values);
        private final String name;

        VariableOperation(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }

    public record Message(ActionSubject subject, Component message, boolean overlay) implements SkillAction {
        public static final MapCodec<Message> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                ActionSubject.CODEC.optionalFieldOf("subject", ActionSubject.CASTER).forGetter(Message::subject),
                ComponentSerialization.CODEC.fieldOf("message").forGetter(Message::message),
                Codec.BOOL.optionalFieldOf("overlay", true).forGetter(Message::overlay)
        ).apply(instance, Message::new));

        @Override
        public CodecType<SkillAction> getType() {
            return AscensionSkillActionTypes.MESSAGE.get();
        }

        @Override
        public void apply(SkillActionContext context) {
            if (!(context.caster() instanceof ServerPlayer player)) {
                return;
            }
            if (overlay) {
                player.sendOverlayMessage(message);
            } else {
                player.sendSystemMessage(message);
            }
        }
    }

    public record Sound(
            ActionSubject subject,
            Identifier sound,
            ScaledValue volume,
            ScaledValue pitch
    ) implements SkillAction {
        public static final MapCodec<Sound> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                ActionSubject.CODEC.optionalFieldOf("subject", ActionSubject.ORIGIN).forGetter(Sound::subject),
                Identifier.CODEC.fieldOf("sound").forGetter(Sound::sound),
                ScaledValue.COMPACT_CODEC.optionalFieldOf("volume", ScaledValue.constant(1.0D)).forGetter(Sound::volume),
                ScaledValue.COMPACT_CODEC.optionalFieldOf("pitch", ScaledValue.constant(1.0D)).forGetter(Sound::pitch)
        ).apply(instance, Sound::new));

        @Override
        public CodecType<SkillAction> getType() {
            return AscensionSkillActionTypes.SOUND.get();
        }

        @Override
        public void apply(SkillActionContext context) {
            SoundEvent event = BuiltInRegistries.SOUND_EVENT.getValue(sound);
            if (event == null) {
                return;
            }

            Vec3 position = subjectPosition(context, subject);

            context.level().playSeededSound(
                    null,
                    position.x,
                    position.y,
                    position.z,
                    event,
                    SoundSource.PLAYERS,
                    (float) Math.clamp(volume.resolve(context.scaledValueContext()), 0.0D, 4.0D),
                    (float) Math.clamp(pitch.resolve(context.scaledValueContext()), 0.01D, 4.0D),
                    context.level().getRandom().nextLong()
            );
        }
    }

    public record Particles(
            ActionSubject subject,
            Identifier particle,
            ScaledValue count,
            ScaledValue spread,
            ScaledValue speed
    ) implements SkillAction {
        public static final MapCodec<Particles> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                ActionSubject.CODEC.optionalFieldOf("subject", ActionSubject.POSITION).forGetter(Particles::subject),
                Identifier.CODEC.fieldOf("particle").forGetter(Particles::particle),
                ScaledValue.COMPACT_CODEC.optionalFieldOf("count", ScaledValue.constant(12.0D)).forGetter(Particles::count),
                ScaledValue.COMPACT_CODEC.optionalFieldOf("spread", ScaledValue.constant(0.5D)).forGetter(Particles::spread),
                ScaledValue.COMPACT_CODEC.optionalFieldOf("speed", ScaledValue.constant(0.05D)).forGetter(Particles::speed)
        ).apply(instance, Particles::new));

        @Override
        public CodecType<SkillAction> getType() {
            return AscensionSkillActionTypes.PARTICLES.get();
        }

        @Override
        public void apply(SkillActionContext context) {
            ParticleType<?> type = BuiltInRegistries.PARTICLE_TYPE.getValue(particle);
            if (!(type instanceof SimpleParticleType simple)) {
                return;
            }

            Vec3 position = subjectPosition(context, subject);
            double resolvedSpread = Math.clamp(spread.resolve(context.scaledValueContext()), 0.0D, 16.0D);

            context.level().sendParticles(
                    simple,
                    position.x,
                    position.y,
                    position.z,
                    Math.clamp((int) Math.round(count.resolve(context.scaledValueContext())), 0, 512),
                    resolvedSpread,
                    resolvedSpread,
                    resolvedSpread,
                    Math.clamp(speed.resolve(context.scaledValueContext()), 0.0D, 4.0D)
            );
        }
    }

    public record DivineSense(
            ScaledValue radius,
            ScaledValue speed,
            ScaledValue durationTicks,
            int color,
            boolean includeItems,
            boolean includeSelf
    ) implements SkillAction {
        public static final MapCodec<DivineSense> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                ScaledValue.COMPACT_CODEC.optionalFieldOf("radius", ScaledValue.constant(16.0D)).forGetter(DivineSense::radius),
                ScaledValue.COMPACT_CODEC.optionalFieldOf("speed", ScaledValue.constant(9.0D)).forGetter(DivineSense::speed),
                ScaledValue.COMPACT_CODEC.optionalFieldOf("duration", ScaledValue.constant(100.0D)).forGetter(DivineSense::durationTicks),
                HexColorCodec.CODEC.optionalFieldOf("color", 0xFFFFFF).forGetter(DivineSense::color),
                Codec.BOOL.optionalFieldOf("include_items", true).forGetter(DivineSense::includeItems),
                Codec.BOOL.optionalFieldOf("include_self", false).forGetter(DivineSense::includeSelf)
        ).apply(instance, DivineSense::new));

        public DivineSense {
            radius = radius == null ? ScaledValue.constant(16.0D) : radius;
            speed = speed == null ? ScaledValue.constant(9.0D) : speed;
            durationTicks = durationTicks == null ? ScaledValue.constant(100.0D) : durationTicks;
            color &= 0xFFFFFF;
        }

        @Override
        public CodecType<SkillAction> getType() {
            return AscensionSkillActionTypes.DIVINE_SENSE.get();
        }

        @Override
        public ActionSubject subject() {
            return ActionSubject.CASTER;
        }

        @Override
        public void apply(SkillActionContext context) {
            if (!(context.caster() instanceof ServerPlayer player)) {
                return;
            }

            double resolvedRadius = Math.max(0.0D, radius.resolve(context.scaledValueContext()));
            if (!Double.isFinite(resolvedRadius) || resolvedRadius <= 0.0D) {
                return;
            }
            double resolvedSpeed = speed.resolve(context.scaledValueContext());
            if (!Double.isFinite(resolvedSpeed) || resolvedSpeed <= 0.0D) {
                return;
            }
            resolvedSpeed = Math.clamp(resolvedSpeed, 0.1D, 512.0D);
            int resolvedDuration = Math.clamp((int) Math.round(durationTicks.resolve(context.scaledValueContext())), 1, 12000);
            Vec3 center = player.position();
            AABB area = AABB.ofSize(center, resolvedRadius * 2.0D, resolvedRadius * 2.0D, resolvedRadius * 2.0D);
            List<Integer> highlighted = new ArrayList<>();
            for (Entity entity : player.level().getEntities(includeSelf ? null : player, area, this::isValidTarget)) {
                if (entity.position().distanceToSqr(center) <= resolvedRadius * resolvedRadius) {
                    highlighted.add(entity.getId());
                }
            }
            ClientboundDivineSensePacket.sendToPlayer(player, new ClientboundDivineSensePacket(center, (float) resolvedRadius, (float) resolvedSpeed, resolvedDuration, color, highlighted));
        }

        private boolean isValidTarget(Entity entity) {
            if (entity instanceof LivingEntity living) {
                return living.isAlive();
            }
            return includeItems && entity instanceof ItemEntity item && item.isAlive();
        }
    }

    public record Resource(
            ActionSubject subject,
            Identifier resource,
            ResourceOperation operation,
            Identifier source,
            ScaledValue amount
    ) implements SkillAction {
        public static final MapCodec<Resource> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                ActionSubject.CODEC.optionalFieldOf("subject", ActionSubject.TARGET).forGetter(Resource::subject),
                Identifier.CODEC.fieldOf("resource").forGetter(Resource::resource),
                ResourceOperation.CODEC.fieldOf("operation").forGetter(Resource::operation),
                Identifier.CODEC.optionalFieldOf("source", DEFAULT_RESOURCE_SOURCE).forGetter(Resource::source),
                ScaledValue.COMPACT_CODEC.fieldOf("amount").forGetter(Resource::amount)
        ).apply(instance, Resource::new));

        @Override
        public CodecType<SkillAction> getType() {
            return AscensionSkillActionTypes.RESOURCE.get();
        }

        @Override
        public void apply(SkillActionContext context) {
            LivingEntity entity = context.entity(subject);
            if (entity == null) {
                return;
            }
            double resolved = amount.resolve(context.scaledValueContext());
            if (!Double.isFinite(resolved) || resolved <= 0.0D) {
                return;
            }
            ResourceTransactionService.transact(new ResourceTransactionRequest(
                    entity,
                    resource,
                    operation,
                    resolved,
                    ResourceSourceIdentity.of(source, ResourceSourceIdentity.Tags.SKILL),
                    context.skill(),
                    context.target(),
                    context.variables(),
                    Set.of()
            ));
        }
    }


    public record Cultivate(
            Identifier path,
            Optional<Identifier> secondaryPath,
            ScaledValue rate
    ) implements SkillAction {
        public static final MapCodec<Cultivate> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Identifier.CODEC.fieldOf("path").forGetter(Cultivate::path),
                Identifier.CODEC.optionalFieldOf("secondary_path").forGetter(Cultivate::secondaryPath),
                ScaledValue.COMPACT_CODEC.fieldOf("rate").forGetter(Cultivate::rate)
        ).apply(instance, Cultivate::new));

        public Cultivate {
            secondaryPath = secondaryPath == null ? Optional.empty() : secondaryPath;
            rate = rate == null ? ScaledValue.constant(0.0D) : rate;
        }

        @Override
        public CodecType<SkillAction> getType() {
            return AscensionSkillActionTypes.CULTIVATE.get();
        }

        @Override
        public ActionSubject subject() {
            return ActionSubject.CASTER;
        }

        @Override
        public void apply(SkillActionContext context) {
            PathInstance pathInstance = AscensionOriginSourceHelper.getPathInstance(context.originSource(), path);
            if (pathInstance == null) {
                return;
            }
            double resolvedRate = rate.resolve(context.scaledValueContext());
            if (!Double.isFinite(resolvedRate) || resolvedRate <= 0.0D) {
                return;
            }
            CultivationUtil.cultivate(
                    context.caster(),
                    context.originSource(),
                    path,
                    pathInstance,
                    secondaryPath.orElse(path),
                    resolvedRate
            );
        }
    }

    public record Damage(
            ActionSubject subject,
            SkillDamageDefinition damage,
            Identifier damageType,
            List<Identifier> classifications,
            Optional<Identifier> path,
            Optional<Identifier> technique
    ) implements SkillAction {
        public static final MapCodec<Damage> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                ActionSubject.CODEC.optionalFieldOf("subject", ActionSubject.TARGET).forGetter(Damage::subject),
                SkillDamageDefinition.CODEC.forGetter(Damage::damage),
                Identifier.CODEC.optionalFieldOf("damage_type", DEFAULT_DAMAGE_TYPE).forGetter(Damage::damageType),
                Identifier.CODEC.listOf().optionalFieldOf("classifications", List.of()).forGetter(Damage::classifications),
                Identifier.CODEC.optionalFieldOf("path").forGetter(Damage::path),
                Identifier.CODEC.optionalFieldOf("technique").forGetter(Damage::technique)
        ).apply(instance, Damage::new));

        public Damage {
            damage = damage == null ? SkillDamageDefinition.base(0.0D) : damage;
            classifications = classifications == null ? List.of() : List.copyOf(classifications);
            path = path == null ? Optional.empty() : path;
            technique = technique == null ? Optional.empty() : technique;
        }

        @Override
        public CodecType<SkillAction> getType() {
            return AscensionSkillActionTypes.DAMAGE.get();
        }

        @Override
        public void apply(SkillActionContext context) {
            LivingEntity entity = context.entity(subject);
            if (entity != null) {
                SkillActionContext targetContext = context.retarget(entity, entity.getBoundingBox().getCenter());
                AscensionDamageService.apply(
                        targetContext,
                        damage.resolve(targetContext.scaledValueContext()),
                        damageType,
                        new LinkedHashSet<>(classifications),
                        path,
                        technique
                );
            }
        }
    }

    public record Effect(
            ActionSubject subject,
            DefinitionRef<SkillEffectDefinition> definition,
            ScaledValue duration,
            ScaledValue potency
    ) implements SkillAction {
        public static final MapCodec<Effect> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                ActionSubject.CODEC.optionalFieldOf("subject", ActionSubject.TARGET).forGetter(Effect::subject),
                DefinitionRef.codec(SkillEffectDefinition.CODEC).fieldOf("definition").forGetter(Effect::definition),
                ScaledValue.COMPACT_CODEC.fieldOf("duration").forGetter(Effect::duration),
                ScaledValue.COMPACT_CODEC.optionalFieldOf("potency", ScaledValue.constant(1.0D)).forGetter(Effect::potency)
        ).apply(instance, Effect::new));

        @Override
        public CodecType<SkillAction> getType() {
            return AscensionSkillActionTypes.EFFECT.get();
        }

        @Override
        public void apply(SkillActionContext context) {
            LivingEntity entity = context.entity(subject);
            if (entity == null) {
                return;
            }
            Resolved<SkillEffectDefinition> resolved = SkillDefinitions.effect(context, definition);
            if (resolved == null) {
                return;
            }
            SkillEffectService.apply(
                    entity,
                    resolved.id(),
                    context.caster().getUUID(),
                    context.skill(),
                    Math.max(1, (int) Math.round(duration.resolve(context.scaledValueContext()))),
                    Math.max(0.0D, potency.resolve(context.scaledValueContext()))
            );
        }
    }

    public record Buildup(
            ActionSubject subject,
            Identifier channel,
            ScaledValue amount,
            int decayDelay
    ) implements SkillAction {
        public static final MapCodec<Buildup> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                ActionSubject.CODEC.optionalFieldOf("subject", ActionSubject.TARGET).forGetter(Buildup::subject),
                Identifier.CODEC.optionalFieldOf("channel", FROZEN).forGetter(Buildup::channel),
                ScaledValue.COMPACT_CODEC.fieldOf("amount").forGetter(Buildup::amount),
                Codec.INT.optionalFieldOf("decay_delay", 40).forGetter(Buildup::decayDelay)
        ).apply(instance, Buildup::new));

        @Override
        public CodecType<SkillAction> getType() {
            return AscensionSkillActionTypes.BUILDUP.get();
        }

        @Override
        public void apply(SkillActionContext context) {
            LivingEntity entity = context.entity(subject);
            var buildup = AscensionBuildupChannels.get(channel);
            if (entity != null && buildup != null) {
                buildup.apply(entity, amount.resolve(context.scaledValueContext()), decayDelay);
            }
        }
    }

    public record Stagger(
            ActionSubject subject,
            StaggerAction action,
            Optional<DefinitionRef<StaggerDefinition>> definition,
            ScaledValue amount,
            ScaledValue duration
    ) implements SkillAction {
        public static final MapCodec<Stagger> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                ActionSubject.CODEC.optionalFieldOf("subject", ActionSubject.TARGET).forGetter(Stagger::subject),
                StaggerAction.CODEC.optionalFieldOf("action", StaggerAction.APPLY).forGetter(Stagger::action),
                DefinitionRef.codec(StaggerDefinition.CODEC).optionalFieldOf("definition").forGetter(Stagger::definition),
                ScaledValue.COMPACT_CODEC.optionalFieldOf("amount", ScaledValue.constant(0.0D)).forGetter(Stagger::amount),
                ScaledValue.COMPACT_CODEC.optionalFieldOf("duration", ScaledValue.constant(0.0D)).forGetter(Stagger::duration)
        ).apply(instance, Stagger::new));

        public Stagger {
            definition = definition == null ? Optional.empty() : definition;
        }

        @Override
        public CodecType<SkillAction> getType() {
            return AscensionSkillActionTypes.STAGGER.get();
        }

        @Override
        public void apply(SkillActionContext context) {
            LivingEntity entity = context.entity(subject);
            if (entity == null) {
                return;
            }
            Resolved<StaggerDefinition> resolved = definition.map(value -> SkillDefinitions.stagger(context, value)).orElse(null);
            switch (action) {
                case APPLY -> {
                    if (resolved != null) {
                        StaggerService.apply(context, entity, resolved.id(), amount.resolve(context.scaledValueContext()));
                    }
                }
                case REDUCE -> StaggerService.reduce(entity, amount.resolve(context.scaledValueContext()));
                case CLEAR -> StaggerService.clear(entity);
                case IMMUNITY -> StaggerService.grantImmunity(entity, Math.max(0, (int) Math.round(duration.resolve(context.scaledValueContext()))));
                case BREAK -> {
                    if (resolved != null) {
                        StaggerService.forceGuardBreak(context, entity, resolved.id());
                    }
                }
            }
        }
    }

    public record Barrier(
            ActionSubject subject,
            RuntimeAction action,
            DefinitionRef<BarrierDefinition> definition,
            ScaledValue amount
    ) implements SkillAction {
        public static final MapCodec<Barrier> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                ActionSubject.CODEC.optionalFieldOf("subject", ActionSubject.TARGET).forGetter(Barrier::subject),
                RuntimeAction.CODEC.optionalFieldOf("action", RuntimeAction.APPLY).forGetter(Barrier::action),
                DefinitionRef.codec(BarrierDefinition.CODEC).fieldOf("definition").forGetter(Barrier::definition),
                ScaledValue.COMPACT_CODEC.optionalFieldOf("amount", ScaledValue.constant(0.0D)).forGetter(Barrier::amount)
        ).apply(instance, Barrier::new));

        @Override
        public CodecType<SkillAction> getType() {
            return AscensionSkillActionTypes.BARRIER.get();
        }

        @Override
        public void apply(SkillActionContext context) {
            LivingEntity entity = context.entity(subject);
            if (entity == null) {
                return;
            }
            Resolved<BarrierDefinition> resolved = SkillDefinitions.barrier(context, definition);
            if (resolved == null) {
                return;
            }
            switch (action) {
                case APPLY -> Barriers.apply(context.retarget(entity, entity.getBoundingBox().getCenter()), entity, resolved.id());
                case REPAIR -> Barriers.repair(
                        context.level(),
                        context.caster().getUUID(),
                        entity.getUUID(),
                        resolved.id(),
                        amount.resolve(context.scaledValueContext())
                );
                case REMOVE -> Barriers.removeMatching(
                        context.level(),
                        context.caster().getUUID(),
                        entity.getUUID(),
                        resolved.id(),
                        Barriers.Removal.EXPLICIT
                );
                default -> {
                }
            }
        }
    }

    public record Projectile(
            ActionSubject subject,
            DefinitionRef<VirtualProjectileDefinition> definition,
            VirtualProjectileDefinition.Direction direction
    ) implements SkillAction {
        public static final MapCodec<Projectile> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                ActionSubject.CODEC.optionalFieldOf("subject", ActionSubject.CASTER).forGetter(Projectile::subject),
                DefinitionRef.codec(VirtualProjectileDefinition.CODEC).fieldOf("definition").forGetter(Projectile::definition),
                VirtualProjectileDefinition.Direction.CODEC.optionalFieldOf("direction", VirtualProjectileDefinition.Direction.LOOK).forGetter(Projectile::direction)
        ).apply(instance, Projectile::new));

        @Override
        public CodecType<SkillAction> getType() {
            return AscensionSkillActionTypes.PROJECTILE.get();
        }

        @Override
        public void apply(SkillActionContext context) {
            Resolved<VirtualProjectileDefinition> resolved = SkillDefinitions.projectile(context, definition);
            if (resolved != null) {
                VirtualProjectiles.spawn(context, resolved.id(), direction);
            }
        }
    }

    public record Field(
            ActionSubject subject,
            RuntimeAction action,
            DefinitionRef<AreaFieldDefinition> definition
    ) implements SkillAction {
        public static final MapCodec<Field> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                ActionSubject.CODEC.optionalFieldOf("subject", ActionSubject.POSITION).forGetter(Field::subject),
                RuntimeAction.CODEC.optionalFieldOf("action", RuntimeAction.APPLY).forGetter(Field::action),
                DefinitionRef.codec(AreaFieldDefinition.CODEC).fieldOf("definition").forGetter(Field::definition)
        ).apply(instance, Field::new));

        @Override
        public CodecType<SkillAction> getType() {
            return AscensionSkillActionTypes.FIELD.get();
        }

        @Override
        public void apply(SkillActionContext context) {
            Resolved<AreaFieldDefinition> resolved = SkillDefinitions.field(context, definition);
            if (resolved == null) {
                return;
            }
            if (action == RuntimeAction.REMOVE) {
                AreaFields.removeOwned(context.level(), context.caster().getUUID(), resolved.id());
            } else {
                AreaFields.spawn(context, resolved.id(), context.position());
            }
        }
    }

    public record Network(
            ActionSubject subject,
            RuntimeAction action,
            DefinitionRef<AnchorNetworkDefinition> definition
    ) implements SkillAction {
        public static final MapCodec<Network> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                ActionSubject.CODEC.optionalFieldOf("subject", ActionSubject.POSITION).forGetter(Network::subject),
                RuntimeAction.CODEC.optionalFieldOf("action", RuntimeAction.APPLY).forGetter(Network::action),
                DefinitionRef.codec(AnchorNetworkDefinition.CODEC).fieldOf("definition").forGetter(Network::definition)
        ).apply(instance, Network::new));

        @Override
        public CodecType<SkillAction> getType() {
            return AscensionSkillActionTypes.NETWORK.get();
        }

        @Override
        public void apply(SkillActionContext context) {
            Resolved<AnchorNetworkDefinition> resolved = SkillDefinitions.network(context, definition);
            if (resolved == null) {
                return;
            }
            if (action == RuntimeAction.REMOVE) {
                AnchorNetworks.removeOwned(context.level(), context.caster().getUUID(), resolved.id());
            } else {
                AnchorNetworks.spawn(context, resolved.id(), context.position());
            }
        }
    }

    public record Construct(
            ActionSubject subject,
            RuntimeAction action,
            DefinitionRef<OwnerBoundConstructDefinition> definition,
            ScaledValue amount
    ) implements SkillAction {
        public static final MapCodec<Construct> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                ActionSubject.CODEC.optionalFieldOf("subject", ActionSubject.CASTER).forGetter(Construct::subject),
                RuntimeAction.CODEC.optionalFieldOf("action", RuntimeAction.APPLY).forGetter(Construct::action),
                DefinitionRef.codec(OwnerBoundConstructDefinition.CODEC).fieldOf("definition").forGetter(Construct::definition),
                ScaledValue.COMPACT_CODEC.optionalFieldOf("amount", ScaledValue.constant(0.0D)).forGetter(Construct::amount)
        ).apply(instance, Construct::new));

        @Override
        public CodecType<SkillAction> getType() {
            return AscensionSkillActionTypes.CONSTRUCT.get();
        }

        @Override
        public void apply(SkillActionContext context) {
            Resolved<OwnerBoundConstructDefinition> resolved = SkillDefinitions.construct(context, definition);
            if (resolved == null) {
                return;
            }
            switch (action) {
                case APPLY -> OwnerBoundConstructs.spawn(context, resolved.id());
                case REMOVE -> OwnerBoundConstructs.removeOwned(context.level(), context.caster().getUUID(), resolved.id());
                case REPAIR -> {
                    double value = amount.resolve(context.scaledValueContext());
                    for (OwnerBoundConstructDefinition.View construct : OwnerBoundConstructs.findOwned(context.caster().getUUID(), resolved.id())) {
                        OwnerBoundConstructs.modifyStability(construct.runtimeId(), value);
                    }
                }
            }
        }
    }

    public record Move(
            ActionSubject subject,
            MoveMode mode,
            ScaledValue distance,
            MoveDirection direction,
            boolean includeVertical,
            ScaledValue maximumDistance,
            ScaledValue stoppingDistance,
            ScaledValue verticalOffset,
            Optional<Identifier> anchor,
            boolean consumeAnchor,
            boolean restoreRotation,
            MovementService.CollisionPolicy collision,
            boolean preserveVelocity
    ) implements SkillAction {
        public static final MapCodec<Move> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                ActionSubject.CODEC.optionalFieldOf("subject", ActionSubject.CASTER).forGetter(Move::subject),
                MoveMode.CODEC.fieldOf("mode").forGetter(Move::mode),
                ScaledValue.COMPACT_CODEC.optionalFieldOf("distance", ScaledValue.constant(0.0D)).forGetter(Move::distance),
                MoveDirection.CODEC.optionalFieldOf("direction", MoveDirection.LOOK).forGetter(Move::direction),
                Codec.BOOL.optionalFieldOf("include_vertical", true).forGetter(Move::includeVertical),
                ScaledValue.COMPACT_CODEC.optionalFieldOf("maximum_distance", ScaledValue.constant(Double.MAX_VALUE)).forGetter(Move::maximumDistance),
                ScaledValue.COMPACT_CODEC.optionalFieldOf("stopping_distance", ScaledValue.constant(0.0D)).forGetter(Move::stoppingDistance),
                ScaledValue.COMPACT_CODEC.optionalFieldOf("vertical_offset", ScaledValue.constant(0.0D)).forGetter(Move::verticalOffset),
                Identifier.CODEC.optionalFieldOf("anchor").forGetter(Move::anchor),
                Codec.BOOL.optionalFieldOf("consume_anchor", true).forGetter(Move::consumeAnchor),
                Codec.BOOL.optionalFieldOf("restore_rotation", true).forGetter(Move::restoreRotation),
                MovementService.CollisionPolicy.CODEC.optionalFieldOf("collision", MovementService.CollisionPolicy.STOP_BEFORE_COLLISION).forGetter(Move::collision),
                Codec.BOOL.optionalFieldOf("preserve_velocity", false).forGetter(Move::preserveVelocity)
        ).apply(instance, Move::new));

        public Move {
            anchor = anchor == null ? Optional.empty() : anchor;
        }

        @Override
        public CodecType<SkillAction> getType() {
            return AscensionSkillActionTypes.MOVE.get();
        }

        @Override
        public void apply(SkillActionContext context) {
            LivingEntity mover = context.entity(subject);
            if (mover == null) {
                return;
            }
            switch (mode) {
                case DIRECTIONAL -> directional(context, mover);
                case TARGET_POSITION -> targetPosition(context, mover);
                case ANCHOR -> anchor(context, mover);
            }
        }

        private void directional(SkillActionContext context, LivingEntity mover) {
            LivingEntity target = context.target() == context.caster() ? null : context.target();
            Vec3 vector = switch (direction) {
                case LOOK -> mover.getLookAngle();
                case TOWARD_TARGET -> target == null ? null : target.position().subtract(mover.position());
                case AWAY_FROM_TARGET -> target == null ? null : mover.position().subtract(target.position());
            };
            double resolved = distance.resolve(context.scaledValueContext());
            if (vector == null || !Double.isFinite(resolved) || resolved <= 0.0D) {
                return;
            }
            if (!includeVertical) {
                vector = new Vec3(vector.x, 0.0D, vector.z);
            }
            if (vector.lengthSqr() > 1.0E-8D) {
                MovementService.move(context.level(), mover, mover.position().add(vector.normalize().scale(resolved)), collision, preserveVelocity);
            }
        }

        private void targetPosition(SkillActionContext context, LivingEntity mover) {
            Vec3 delta = context.position().subtract(mover.position());
            double length = delta.length();
            double stopping = Math.max(0.0D, stoppingDistance.resolve(context.scaledValueContext()));

            if (length <= stopping || length <= 1.0E-8D) {
                return;
            }

            double maximum = maximumDistance.resolve(context.scaledValueContext());
            if (!Double.isFinite(maximum) || maximum <= 0.0D) {
                return;
            }

            double travelled = Math.min(length - stopping, maximum);
            double offset = verticalOffset.resolve(context.scaledValueContext());
            if (!Double.isFinite(offset)) {
                offset = 0.0D;
            }

            Vec3 destination = mover.position().add(delta.normalize().scale(travelled)).add(0.0D, offset, 0.0D);
            MovementService.Result result = MovementService.move(context.level(), mover, destination, collision, preserveVelocity);

            if (!result.succeeded()) {
                AscensionCraft.LOGGER.debug("Movement action for {} failed: {} from {} toward {}", context.skill(), result.failureReason(), result.origin(), destination);
            }
        }

        private void anchor(SkillActionContext context, LivingEntity mover) {
            if (anchor.isEmpty()) {
                return;
            }
            MovementService.Anchors.Anchor saved = MovementService.getAnchor(mover, anchor.get());
            if (saved == null || !mover.level().dimension().identifier().equals(saved.dimension())) {
                return;
            }
            MovementService.Result result = MovementService.move(context.level(), mover, saved.position(), collision, preserveVelocity);
            if (!result.succeeded()) {
                return;
            }
            if (restoreRotation) {
                mover.setYRot(saved.yaw());
                mover.setXRot(saved.pitch());
            }
            if (consumeAnchor) {
                MovementService.removeAnchor(mover, anchor.get());
            }
        }
    }

    public record Anchor(
            ActionSubject subject,
            AnchorAction action,
            Identifier anchor,
            ScaledValue duration
    ) implements SkillAction {
        public static final MapCodec<Anchor> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                ActionSubject.CODEC.optionalFieldOf("subject", ActionSubject.CASTER).forGetter(Anchor::subject),
                AnchorAction.CODEC.fieldOf("action").forGetter(Anchor::action),
                Identifier.CODEC.fieldOf("anchor").forGetter(Anchor::anchor),
                ScaledValue.COMPACT_CODEC.optionalFieldOf("duration", ScaledValue.constant(0.0D)).forGetter(Anchor::duration)
        ).apply(instance, Anchor::new));

        @Override
        public CodecType<SkillAction> getType() {
            return AscensionSkillActionTypes.ANCHOR.get();
        }

        @Override
        public void apply(SkillActionContext context) {
            if (action == AnchorAction.CLEAR) {
                MovementService.removeAnchor(context.caster(), anchor);
            } else {
                double value = duration.resolve(context.scaledValueContext());
                MovementService.setAnchor(context.caster(), anchor, !Double.isFinite(value) || value <= 0.0D ? 0L : Math.round(value));
            }
        }
    }

    public record WeaponSwing(
            ActionSubject subject,
            String vfxType,
            String color,
            java.util.Map<Identifier, String> techniqueColors,
            Vec3 radius,
            SkillDamageDefinition damage,
            ScaledValue knockback,
            ScaledValue duration,
            float rotationZ,
            Vec3 movement,
            Optional<Identifier> path,
            Optional<Identifier> weaponTag,
            List<Identifier> classifications,
            Extras extras
    ) implements SkillAction {
        public static final MapCodec<WeaponSwing> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                ActionSubject.CODEC.optionalFieldOf("subject", ActionSubject.CASTER).forGetter(WeaponSwing::subject),
                Codec.STRING.optionalFieldOf("vfx_type", "sword_swing").forGetter(WeaponSwing::vfxType),
                Codec.STRING.optionalFieldOf("color", "blue").forGetter(WeaponSwing::color),
                Codec.unboundedMap(Identifier.CODEC, Codec.STRING).optionalFieldOf("technique_colors", java.util.Map.of()).forGetter(WeaponSwing::techniqueColors),
                CodecHelpers.VEC3.optionalFieldOf("radius", new Vec3(2.0D, 2.0D, 2.0D)).forGetter(WeaponSwing::radius),
                SkillDamageDefinition.CODEC.codec().optionalFieldOf("damage", SkillDamageDefinition.base(4.0D)).forGetter(WeaponSwing::damage),
                ScaledValue.COMPACT_CODEC.optionalFieldOf("knockback", ScaledValue.constant(1.0D)).forGetter(WeaponSwing::knockback),
                ScaledValue.COMPACT_CODEC.optionalFieldOf("duration", ScaledValue.constant(10.0D)).forGetter(WeaponSwing::duration),
                Codec.FLOAT.optionalFieldOf("rotation_z", 0.0F).forGetter(WeaponSwing::rotationZ),
                CodecHelpers.VEC3.optionalFieldOf("movement", Vec3.ZERO).forGetter(WeaponSwing::movement),
                Identifier.CODEC.optionalFieldOf("path").forGetter(WeaponSwing::path),
                Identifier.CODEC.optionalFieldOf("weapon_tag").forGetter(WeaponSwing::weaponTag),
                Identifier.CODEC.listOf().optionalFieldOf("classifications", List.of()).forGetter(WeaponSwing::classifications),
                Extras.CODEC.forGetter(WeaponSwing::extras)
        ).apply(instance, WeaponSwing::new));

        public WeaponSwing {
            vfxType = vfxType == null || vfxType.isBlank() ? "sword_swing" : vfxType;
            color = color == null || color.isBlank() ? "blue" : color;
            techniqueColors = techniqueColors == null ? java.util.Map.of() : java.util.Map.copyOf(techniqueColors);
            radius = radius == null ? new Vec3(2.0D, 2.0D, 2.0D) : radius;
            damage = damage == null ? SkillDamageDefinition.base(4.0D) : damage;
            knockback = knockback == null ? ScaledValue.constant(1.0D) : knockback;
            duration = duration == null ? ScaledValue.constant(10.0D) : duration;
            movement = movement == null ? Vec3.ZERO : movement;
            path = path == null ? Optional.empty() : path;
            weaponTag = weaponTag == null ? Optional.empty() : weaponTag;
            classifications = classifications == null ? List.of() : List.copyOf(classifications);
            extras = extras == null ? Extras.DEFAULT : extras;
        }

        @Override
        public CodecType<SkillAction> getType() {
            return AscensionSkillActionTypes.WEAPON_SWING.get();
        }

        @Override
        public void apply(SkillActionContext context) {
            LivingEntity owner = context.entity(subject);
            if (owner == null) {
                owner = context.caster();
            }
            if (!WeaponVfxUtils.matchesWeapon(owner, weaponTag, extras.allowEmptyHand())) {
                return;
            }

            Identifier pathId = path.orElse(null);
            var source = AscensionOriginSourceHelper.getEntitySource(owner);
            WeaponTechniqueResolver.Resolution style = WeaponTechniqueResolver.resolve(
                    source,
                    pathId,
                    vfxType,
                    color,
                    techniqueColors
            );
            Optional<WeaponSwingSpec.HitEffect> hitEffect = extras.hitEffect().flatMap(effect -> {
                Resolved<SkillEffectDefinition> resolved = SkillDefinitions.effect(context, effect.definition());
                if (resolved == null) {
                    return Optional.empty();
                }
                return Optional.of(new WeaponSwingSpec.HitEffect(
                        resolved.id(),
                        Math.max(1, (int) Math.round(effect.duration().resolve(context.scaledValueContext()))),
                        Math.max(0.0D, effect.potency().resolve(context.scaledValueContext()))
                ));
            });

            WeaponVfxUtils.spawnSwingVfxAhead(
                    context.level(),
                    owner,
                    rotationZ,
                    radius,
                    damage.resolve(context.scaledValueContext()),
                    Math.max(0.0D, knockback.resolve(context.scaledValueContext())),
                    Math.clamp((int) Math.round(duration.resolve(context.scaledValueContext())), 1, 1200),
                    vfxType,
                    context.skill(),
                    pathId,
                    style.technique(),
                    style.colorFolder(),
                    movement,
                    extras.hitShape(),
                    extras.blockImpact(),
                    hitEffect,
                    classifications
            );
        }

        public record Extras(
                boolean allowEmptyHand,
                WeaponSwingSpec.HitShape hitShape,
                WeaponSwingSpec.BlockImpact blockImpact,
                Optional<ActiveHitEffect> hitEffect
        ) {
            public static final Extras DEFAULT = new Extras(
                    false,
                    WeaponSwingSpec.HitShape.AUTO,
                    WeaponSwingSpec.BlockImpact.NONE,
                    Optional.empty()
            );
            public static final MapCodec<Extras> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                    Codec.BOOL.optionalFieldOf("allow_empty_hand", false).forGetter(Extras::allowEmptyHand),
                    WeaponSwingSpec.HitShape.CODEC.optionalFieldOf("hit_shape", WeaponSwingSpec.HitShape.AUTO)
                            .forGetter(Extras::hitShape),
                    WeaponSwingSpec.BlockImpact.CODEC.optionalFieldOf("block_impact", WeaponSwingSpec.BlockImpact.NONE)
                            .forGetter(Extras::blockImpact),
                    ActiveHitEffect.CODEC.optionalFieldOf("hit_effect").forGetter(Extras::hitEffect)
            ).apply(instance, Extras::new));

            public Extras {
                hitShape = hitShape == null ? WeaponSwingSpec.HitShape.AUTO : hitShape;
                blockImpact = blockImpact == null ? WeaponSwingSpec.BlockImpact.NONE : blockImpact;
                hitEffect = hitEffect == null ? Optional.empty() : hitEffect;
            }
        }

        public record ActiveHitEffect(
                DefinitionRef<SkillEffectDefinition> definition,
                ScaledValue duration,
                ScaledValue potency
        ) {
            public static final Codec<ActiveHitEffect> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    DefinitionRef.codec(SkillEffectDefinition.CODEC).fieldOf("definition")
                            .forGetter(ActiveHitEffect::definition),
                    ScaledValue.COMPACT_CODEC.optionalFieldOf("duration", ScaledValue.constant(20.0D))
                            .forGetter(ActiveHitEffect::duration),
                    ScaledValue.COMPACT_CODEC.optionalFieldOf("potency", ScaledValue.constant(1.0D))
                            .forGetter(ActiveHitEffect::potency)
            ).apply(instance, ActiveHitEffect::new));

            public ActiveHitEffect {
                duration = duration == null ? ScaledValue.constant(20.0D) : duration;
                potency = potency == null ? ScaledValue.constant(1.0D) : potency;
            }
        }
    }

    public record Beam(DefinitionRef<BeamDefinition> definition) implements SkillAction {
        public static final MapCodec<Beam> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                DefinitionRef.codec(BeamDefinition.CODEC).fieldOf("definition").forGetter(Beam::definition)
        ).apply(instance, Beam::new));

        @Override
        public CodecType<SkillAction> getType() {
            return AscensionSkillActionTypes.BEAM.get();
        }

        @Override
        public ActionSubject subject() {
            return ActionSubject.CASTER;
        }

        @Override
        public void apply(SkillActionContext context) {
            Resolved<BeamDefinition> resolved = SkillDefinitions.beam(context, definition);
            if (resolved != null) {
                Beams.spawn(context, resolved);
            }
        }
    }

    public record PersistentVisual(
            PersistentVisualAction action,
            ActionSubject subject,
            Identifier key,
            Optional<Identifier> visual,
            ScaledValue scale,
            ScaledValue spin,
            RuntimeVisualDefinition.VisualColor tint,
            Optional<RuntimeVisualDefinition.VisualColor> secondaryTint,
            boolean rotateWithSubject,
            Vec3 offset
    ) implements SkillAction {
        public static final MapCodec<PersistentVisual> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                PersistentVisualAction.CODEC.optionalFieldOf("action", PersistentVisualAction.APPLY).forGetter(PersistentVisual::action),
                ActionSubject.CODEC.optionalFieldOf("subject", ActionSubject.CASTER).forGetter(PersistentVisual::subject),
                Identifier.CODEC.fieldOf("key").forGetter(PersistentVisual::key),
                Identifier.CODEC.optionalFieldOf("visual").forGetter(PersistentVisual::visual),
                ScaledValue.COMPACT_CODEC.optionalFieldOf("scale", ScaledValue.constant(1.0D)).forGetter(PersistentVisual::scale),
                ScaledValue.COMPACT_CODEC.optionalFieldOf("spin", ScaledValue.constant(0.0D)).forGetter(PersistentVisual::spin),
                RuntimeVisualDefinition.VisualColor.CODEC.optionalFieldOf("tint", RuntimeVisualDefinition.VisualColor.WHITE).forGetter(PersistentVisual::tint),
                RuntimeVisualDefinition.VisualColor.CODEC.optionalFieldOf("secondary_tint").forGetter(PersistentVisual::secondaryTint),
                Codec.BOOL.optionalFieldOf("rotate_with_subject", false).forGetter(PersistentVisual::rotateWithSubject),
                CodecHelpers.VEC3.optionalFieldOf("offset", Vec3.ZERO).forGetter(PersistentVisual::offset)
        ).apply(instance, PersistentVisual::new));

        public PersistentVisual {
            action = action == null ? PersistentVisualAction.APPLY : action;
            subject = subject == null ? ActionSubject.CASTER : subject;
            visual = visual == null ? Optional.empty() : visual;
            scale = scale == null ? ScaledValue.constant(1.0D) : scale;
            spin = spin == null ? ScaledValue.constant(0.0D) : spin;
            tint = tint == null ? RuntimeVisualDefinition.VisualColor.WHITE : tint;
            secondaryTint = secondaryTint == null ? Optional.empty() : secondaryTint;
            offset = offset == null ? Vec3.ZERO : offset;
        }

        @Override
        public CodecType<SkillAction> getType() {
            return AscensionSkillActionTypes.PERSISTENT_VISUAL.get();
        }

        @Override
        public void apply(SkillActionContext context) {
            if (action == PersistentVisualAction.REMOVE) {
                PersistentRuntimeVisuals.remove(context, key);
                return;
            }
            LivingEntity attached = context.entity(subject);
            if (attached == null || visual.isEmpty()) {
                return;
            }
            RuntimeVisualDefinition.VisualColor endTint = secondaryTint.orElse(tint);
            PersistentRuntimeVisuals.apply(
                    context, key, visual.get(), attached, offset, rotateWithSubject,
                    (float) Math.max(0.0001D, scale.resolve(context.scaledValueContext())),
                    spin.resolve(context.scaledValueContext()), tint.argb(), endTint.argb()
            );
        }
    }

    public record Visual(
            ActionSubject subject,
            Identifier visual,
            ScaledValue duration,
            ScaledValue scale,
            ScaledValue spin,
            RuntimeVisualDefinition.VisualColor tint,
            Optional<RuntimeVisualDefinition.VisualColor> secondaryTint,
            boolean follow,
            boolean rotateWithSubject,
            Vec3 offset,
            PointMode points,
            ScaledValue progress,
            ScaledValue primary,
            ScaledValue secondary,
            int stage
    ) implements SkillAction {
        public static final MapCodec<Visual> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                ActionSubject.CODEC.optionalFieldOf("subject", ActionSubject.ORIGIN).forGetter(Visual::subject),
                Identifier.CODEC.fieldOf("visual").forGetter(Visual::visual),
                ScaledValue.COMPACT_CODEC.optionalFieldOf("duration", ScaledValue.constant(20.0D)).forGetter(Visual::duration),
                ScaledValue.COMPACT_CODEC.optionalFieldOf("scale", ScaledValue.constant(1.0D)).forGetter(Visual::scale),
                ScaledValue.COMPACT_CODEC.optionalFieldOf("spin", ScaledValue.constant(0.0D)).forGetter(Visual::spin),
                RuntimeVisualDefinition.VisualColor.CODEC.optionalFieldOf("tint", RuntimeVisualDefinition.VisualColor.WHITE).forGetter(Visual::tint),
                RuntimeVisualDefinition.VisualColor.CODEC.optionalFieldOf("secondary_tint").forGetter(Visual::secondaryTint),
                Codec.BOOL.optionalFieldOf("follow", false).forGetter(Visual::follow),
                Codec.BOOL.optionalFieldOf("rotate_with_subject", false).forGetter(Visual::rotateWithSubject),
                CodecHelpers.VEC3.optionalFieldOf("offset", Vec3.ZERO).forGetter(Visual::offset),
                PointMode.CODEC.optionalFieldOf("points", PointMode.NONE).forGetter(Visual::points),
                ScaledValue.COMPACT_CODEC.optionalFieldOf("progress", ScaledValue.constant(0.0D)).forGetter(Visual::progress),
                ScaledValue.COMPACT_CODEC.optionalFieldOf("primary", ScaledValue.constant(0.0D)).forGetter(Visual::primary),
                ScaledValue.COMPACT_CODEC.optionalFieldOf("secondary", ScaledValue.constant(0.0D)).forGetter(Visual::secondary),
                Codec.intRange(0, 1024).optionalFieldOf("stage", 0).forGetter(Visual::stage)
        ).apply(instance, Visual::new));

        public Visual {
            scale = scale == null ? ScaledValue.constant(1.0D) : scale;
            spin = spin == null ? ScaledValue.constant(0.0D) : spin;
            tint = tint == null ? RuntimeVisualDefinition.VisualColor.WHITE : tint;
            secondaryTint = secondaryTint == null ? Optional.empty() : secondaryTint;
            offset = offset == null ? Vec3.ZERO : offset;
            points = points == null ? PointMode.NONE : points;
        }

        @Override
        public CodecType<SkillAction> getType() {
            return AscensionSkillActionTypes.VISUAL.get();
        }

        @Override
        public void apply(SkillActionContext context) {
            Resolved<RuntimeVisualDefinition> resolved = SkillDefinitions.visual(context, visual);
            if (resolved == null) {
                return;
            }
            LivingEntity attached = follow ? context.entity(subject) : null;
            Vec3 base = attached == null
                    ? context.position()
                    : attached.position().add(0.0D, attached.getBbHeight() * 0.5D, 0.0D);
            UUID runtimeId = UUID.randomUUID();
            List<Vec3> resolvedPoints = points.resolve(context);
            RuntimeVisualDefinition.VisualColor endTint = secondaryTint.orElse(tint);
            RuntimeVisualSync.spawn(context.level(), new RuntimeVisualState(
                    runtimeId,
                    resolved.id(),
                    attached == null ? context.caster().getUUID() : attached.getUUID(),
                    base.add(attached == null ? offset : Vec3.ZERO),
                    attached == null ? Vec3.ZERO : offset,
                    resolvedPoints,
                    resolvedPoints.size() >= 2 ? List.of(new RuntimeVisualState.Link(0, 1)) : List.of(),
                    context.level().getGameTime() + Math.max(1L, Math.round(duration.resolve(context.scaledValueContext()))),
                    Math.max(0, stage),
                    attached == null ? 0 : RuntimeVisualState.OWNER_RELATIVE | (rotateWithSubject ? RuntimeVisualState.ROTATE_WITH_OWNER : 0),
                    (float) Math.clamp(progress.resolve(context.scaledValueContext()), 0.0D, 1.0D),
                    runtimeId.getMostSignificantBits(),
                    primary.resolve(context.scaledValueContext()),
                    secondary.resolve(context.scaledValueContext()),
                    (float) Math.max(0.0001D, scale.resolve(context.scaledValueContext())),
                    spin.resolve(context.scaledValueContext()),
                    tint.argb(),
                    endTint.argb(),
                    null
            ));
        }
    }

    public enum PersistentVisualAction implements StringRepresentable {
        APPLY("apply"), REMOVE("remove");

        public static final Codec<PersistentVisualAction> CODEC = StringRepresentable.fromEnum(PersistentVisualAction::values);
        private final String name;

        PersistentVisualAction(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }

    public enum RuntimeAction implements StringRepresentable {
        APPLY("apply"),
        REPAIR("repair"),
        REMOVE("remove");

        public static final Codec<RuntimeAction> CODEC = StringRepresentable.fromEnum(RuntimeAction::values);
        private final String name;

        RuntimeAction(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }

    public enum StaggerAction implements StringRepresentable {
        APPLY("apply"),
        REDUCE("reduce"),
        CLEAR("clear"),
        IMMUNITY("immunity"),
        BREAK("break");

        public static final Codec<StaggerAction> CODEC = StringRepresentable.fromEnum(StaggerAction::values);
        private final String name;

        StaggerAction(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }

    public enum MoveMode implements StringRepresentable {
        DIRECTIONAL("directional"),
        TARGET_POSITION("target_position"),
        ANCHOR("anchor");

        public static final Codec<MoveMode> CODEC = StringRepresentable.fromEnum(MoveMode::values);
        private final String name;

        MoveMode(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }

    public enum MoveDirection implements StringRepresentable {
        LOOK("look"),
        TOWARD_TARGET("toward_target"),
        AWAY_FROM_TARGET("away_from_target");

        public static final Codec<MoveDirection> CODEC = StringRepresentable.fromEnum(MoveDirection::values);
        private final String name;

        MoveDirection(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }

    public enum AnchorAction implements StringRepresentable {
        SET("set"),
        CLEAR("clear");

        public static final Codec<AnchorAction> CODEC = StringRepresentable.fromEnum(AnchorAction::values);
        private final String name;

        AnchorAction(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }

    public enum PointMode implements StringRepresentable {
        NONE("none"),
        CASTER_TO_EXECUTION("caster_to_execution"),
        CASTER_TO_TARGET("caster_to_target");

        public static final Codec<PointMode> CODEC = StringRepresentable.fromEnum(PointMode::values);
        private final String name;

        PointMode(String name) {
            this.name = name;
        }

        public List<Vec3> resolve(SkillActionContext context) {
            Vec3 caster = context.caster().position().add(0.0D, context.caster().getBbHeight() * 0.5D, 0.0D);
            return switch (this) {
                case NONE -> List.of();
                case CASTER_TO_EXECUTION -> List.of(caster, context.position());
                case CASTER_TO_TARGET -> context.target() == null
                        ? List.of(caster, context.position())
                        : List.of(caster, context.target().getBoundingBox().getCenter());
            };
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }
}
