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
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.ascension.core.control.StaggerDefinition;
import net.zic.ascension.api.ascension.core.effect.SkillEffectDefinition;
import net.zic.ascension.api.ascension.core.projectile.VirtualProjectileDefinition;
import net.zic.ascension.api.ascension.core.resource.ResourceOperation;
import net.zic.ascension.api.ascension.core.resource.ResourceTransactionRequest;
import net.zic.ascension.api.ascension.core.resource.ResourceTransactionService;
import net.zic.ascension.api.ascension.core.runtime.AnchorNetworkDefinition;
import net.zic.ascension.api.ascension.core.runtime.AreaFieldDefinition;
import net.zic.ascension.api.ascension.core.runtime.BarrierDefinition;
import net.zic.ascension.api.ascension.core.runtime.OwnerBoundConstructDefinition;
import net.zic.ascension.api.ascension.core.runtime.RuntimeVisualDefinition;
import net.zic.ascension.api.ascension.core.runtime.RuntimeVisualState;
import net.zic.ascension.api.ascension.core.skill.castable.feature.ExecutionSubject;
import net.zic.ascension.api.ascension.core.skill.castable.feature.SkillExecutionContext;
import net.zic.ascension.api.ascension.core.skill.castable.feature.SkillExecutionFeature;
import net.zic.ascension.api.ascension.core.skill.DefinitionRef;
import net.zic.ascension.api.ascension.core.skill.SkillDefinitions.Resolved;
import net.zic.ascension.api.ascension.core.skill.SkillDefinitions;
import net.zic.ascension.api.ascension.datapack.CodecHelpers;
import net.zic.ascension.api.ascension.datapack.CodecType;
import net.zic.ascension.api.ascension.value.ScaledValue;
import net.zic.ascension.impl.core.control.StaggerService;
import net.zic.ascension.impl.core.damage.AscensionDamageService;
import net.zic.ascension.impl.core.effect.AscensionBuildupChannels;
import net.zic.ascension.impl.core.effect.SkillEffectService;
import net.zic.ascension.impl.core.movement.MovementService;
import net.zic.ascension.impl.datapack.skill.AscensionSkillExecutionFeatureTypes;
import net.zic.ascension.impl.runtime.object.Barriers;
import net.zic.ascension.impl.runtime.object.AnchorNetworks;
import net.zic.ascension.impl.runtime.object.AreaFields;
import net.zic.ascension.impl.runtime.object.OwnerBoundConstructs;
import net.zic.ascension.impl.runtime.projectile.VirtualProjectiles;
import net.zic.ascension.impl.runtime.object.RuntimeVisualSync;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import net.zic.ascension.api.ascension.core.resource.ResourceSourceIdentity;

public final class SkillFeatures {
    private static final Identifier DEFAULT_DAMAGE_TYPE = Identifier.fromNamespaceAndPath("minecraft", "player_attack");
    private static final Identifier DEFAULT_RESOURCE_SOURCE = AscensionCraft.prefix("skill_casting");
    private static final Identifier FROZEN = AscensionCraft.prefix("frozen");

    private SkillFeatures() {
    }

    private static Vec3 subjectPosition(SkillExecutionContext context, ExecutionSubject subject) {
        LivingEntity entity = context.entity(subject);
        return entity == null ? context.position() : entity.getBoundingBox().getCenter();
    }


    public record Message(ExecutionSubject subject, Component message, boolean overlay) implements SkillExecutionFeature {
        public static final MapCodec<Message> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                ExecutionSubject.CODEC.optionalFieldOf("subject", ExecutionSubject.CASTER).forGetter(Message::subject),
                ComponentSerialization.CODEC.fieldOf("message").forGetter(Message::message),
                Codec.BOOL.optionalFieldOf("overlay", true).forGetter(Message::overlay)
        ).apply(instance, Message::new));

        @Override
        public CodecType<SkillExecutionFeature> getType() {
            return AscensionSkillExecutionFeatureTypes.MESSAGE.get();
        }

        @Override
        public void apply(SkillExecutionContext context) {
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
            ExecutionSubject subject,
            Identifier sound,
            ScaledValue volume,
            ScaledValue pitch
    ) implements SkillExecutionFeature {
        public static final MapCodec<Sound> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                ExecutionSubject.CODEC.optionalFieldOf("subject", ExecutionSubject.ORIGIN).forGetter(Sound::subject),
                Identifier.CODEC.fieldOf("sound").forGetter(Sound::sound),
                ScaledValue.COMPACT_CODEC.optionalFieldOf("volume", ScaledValue.constant(1.0D)).forGetter(Sound::volume),
                ScaledValue.COMPACT_CODEC.optionalFieldOf("pitch", ScaledValue.constant(1.0D)).forGetter(Sound::pitch)
        ).apply(instance, Sound::new));

        @Override
        public CodecType<SkillExecutionFeature> getType() {
            return AscensionSkillExecutionFeatureTypes.SOUND.get();
        }

        @Override
        public void apply(SkillExecutionContext context) {
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
            ExecutionSubject subject,
            Identifier particle,
            ScaledValue count,
            ScaledValue spread,
            ScaledValue speed
    ) implements SkillExecutionFeature {
        public static final MapCodec<Particles> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                ExecutionSubject.CODEC.optionalFieldOf("subject", ExecutionSubject.POSITION).forGetter(Particles::subject),
                Identifier.CODEC.fieldOf("particle").forGetter(Particles::particle),
                ScaledValue.COMPACT_CODEC.optionalFieldOf("count", ScaledValue.constant(12.0D)).forGetter(Particles::count),
                ScaledValue.COMPACT_CODEC.optionalFieldOf("spread", ScaledValue.constant(0.5D)).forGetter(Particles::spread),
                ScaledValue.COMPACT_CODEC.optionalFieldOf("speed", ScaledValue.constant(0.05D)).forGetter(Particles::speed)
        ).apply(instance, Particles::new));

        @Override
        public CodecType<SkillExecutionFeature> getType() {
            return AscensionSkillExecutionFeatureTypes.PARTICLES.get();
        }

        @Override
        public void apply(SkillExecutionContext context) {
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

    public record Resource(
            ExecutionSubject subject,
            Identifier resource,
            ResourceOperation operation,
            Identifier source,
            ScaledValue amount
    ) implements SkillExecutionFeature {
        public static final MapCodec<Resource> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                ExecutionSubject.CODEC.optionalFieldOf("subject", ExecutionSubject.TARGET).forGetter(Resource::subject),
                Identifier.CODEC.fieldOf("resource").forGetter(Resource::resource),
                ResourceOperation.CODEC.fieldOf("operation").forGetter(Resource::operation),
                Identifier.CODEC.optionalFieldOf("source", DEFAULT_RESOURCE_SOURCE).forGetter(Resource::source),
                ScaledValue.COMPACT_CODEC.fieldOf("amount").forGetter(Resource::amount)
        ).apply(instance, Resource::new));

        @Override
        public CodecType<SkillExecutionFeature> getType() {
            return AscensionSkillExecutionFeatureTypes.RESOURCE.get();
        }

        @Override
        public void apply(SkillExecutionContext context) {
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

    public record Damage(
            ExecutionSubject subject,
            ScaledValue amount,
            Identifier damageType,
            List<Identifier> classifications,
            Optional<Identifier> path,
            Optional<Identifier> technique
    ) implements SkillExecutionFeature {
        public static final MapCodec<Damage> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                ExecutionSubject.CODEC.optionalFieldOf("subject", ExecutionSubject.TARGET).forGetter(Damage::subject),
                ScaledValue.COMPACT_CODEC.fieldOf("amount").forGetter(Damage::amount),
                Identifier.CODEC.optionalFieldOf("damage_type", DEFAULT_DAMAGE_TYPE).forGetter(Damage::damageType),
                Identifier.CODEC.listOf().optionalFieldOf("classifications", List.of()).forGetter(Damage::classifications),
                Identifier.CODEC.optionalFieldOf("path").forGetter(Damage::path),
                Identifier.CODEC.optionalFieldOf("technique").forGetter(Damage::technique)
        ).apply(instance, Damage::new));

        public Damage {
            classifications = classifications == null ? List.of() : List.copyOf(classifications);
            path = path == null ? Optional.empty() : path;
            technique = technique == null ? Optional.empty() : technique;
        }

        @Override
        public CodecType<SkillExecutionFeature> getType() {
            return AscensionSkillExecutionFeatureTypes.DAMAGE.get();
        }

        @Override
        public void apply(SkillExecutionContext context) {
            LivingEntity entity = context.entity(subject);
            if (entity != null) {
                AscensionDamageService.apply(
                        context.retarget(entity, entity.getBoundingBox().getCenter()),
                        amount.resolve(context.scaledValueContext()),
                        damageType,
                        new LinkedHashSet<>(classifications),
                        path,
                        technique
                );
            }
        }
    }

    public record Effect(
            ExecutionSubject subject,
            DefinitionRef<SkillEffectDefinition> definition,
            ScaledValue duration,
            ScaledValue potency
    ) implements SkillExecutionFeature {
        public static final MapCodec<Effect> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                ExecutionSubject.CODEC.optionalFieldOf("subject", ExecutionSubject.TARGET).forGetter(Effect::subject),
                DefinitionRef.codec(SkillEffectDefinition.CODEC).fieldOf("definition").forGetter(Effect::definition),
                ScaledValue.COMPACT_CODEC.fieldOf("duration").forGetter(Effect::duration),
                ScaledValue.COMPACT_CODEC.optionalFieldOf("potency", ScaledValue.constant(1.0D)).forGetter(Effect::potency)
        ).apply(instance, Effect::new));

        @Override
        public CodecType<SkillExecutionFeature> getType() {
            return AscensionSkillExecutionFeatureTypes.EFFECT.get();
        }

        @Override
        public void apply(SkillExecutionContext context) {
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
            ExecutionSubject subject,
            Identifier channel,
            ScaledValue amount,
            int decayDelay
    ) implements SkillExecutionFeature {
        public static final MapCodec<Buildup> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                ExecutionSubject.CODEC.optionalFieldOf("subject", ExecutionSubject.TARGET).forGetter(Buildup::subject),
                Identifier.CODEC.optionalFieldOf("channel", FROZEN).forGetter(Buildup::channel),
                ScaledValue.COMPACT_CODEC.fieldOf("amount").forGetter(Buildup::amount),
                Codec.INT.optionalFieldOf("decay_delay", 40).forGetter(Buildup::decayDelay)
        ).apply(instance, Buildup::new));

        @Override
        public CodecType<SkillExecutionFeature> getType() {
            return AscensionSkillExecutionFeatureTypes.BUILDUP.get();
        }

        @Override
        public void apply(SkillExecutionContext context) {
            LivingEntity entity = context.entity(subject);
            var buildup = AscensionBuildupChannels.get(channel);
            if (entity != null && buildup != null) {
                buildup.apply(entity, amount.resolve(context.scaledValueContext()), decayDelay);
            }
        }
    }

    public record Stagger(
            ExecutionSubject subject,
            StaggerAction action,
            Optional<DefinitionRef<StaggerDefinition>> definition,
            ScaledValue amount,
            ScaledValue duration
    ) implements SkillExecutionFeature {
        public static final MapCodec<Stagger> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                ExecutionSubject.CODEC.optionalFieldOf("subject", ExecutionSubject.TARGET).forGetter(Stagger::subject),
                StaggerAction.CODEC.optionalFieldOf("action", StaggerAction.APPLY).forGetter(Stagger::action),
                DefinitionRef.codec(StaggerDefinition.CODEC).optionalFieldOf("definition").forGetter(Stagger::definition),
                ScaledValue.COMPACT_CODEC.optionalFieldOf("amount", ScaledValue.constant(0.0D)).forGetter(Stagger::amount),
                ScaledValue.COMPACT_CODEC.optionalFieldOf("duration", ScaledValue.constant(0.0D)).forGetter(Stagger::duration)
        ).apply(instance, Stagger::new));

        public Stagger {
            definition = definition == null ? Optional.empty() : definition;
        }

        @Override
        public CodecType<SkillExecutionFeature> getType() {
            return AscensionSkillExecutionFeatureTypes.STAGGER.get();
        }

        @Override
        public void apply(SkillExecutionContext context) {
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
            ExecutionSubject subject,
            RuntimeAction action,
            DefinitionRef<BarrierDefinition> definition,
            ScaledValue amount
    ) implements SkillExecutionFeature {
        public static final MapCodec<Barrier> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                ExecutionSubject.CODEC.optionalFieldOf("subject", ExecutionSubject.TARGET).forGetter(Barrier::subject),
                RuntimeAction.CODEC.optionalFieldOf("action", RuntimeAction.APPLY).forGetter(Barrier::action),
                DefinitionRef.codec(BarrierDefinition.CODEC).fieldOf("definition").forGetter(Barrier::definition),
                ScaledValue.COMPACT_CODEC.optionalFieldOf("amount", ScaledValue.constant(0.0D)).forGetter(Barrier::amount)
        ).apply(instance, Barrier::new));

        @Override
        public CodecType<SkillExecutionFeature> getType() {
            return AscensionSkillExecutionFeatureTypes.BARRIER.get();
        }

        @Override
        public void apply(SkillExecutionContext context) {
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
            ExecutionSubject subject,
            DefinitionRef<VirtualProjectileDefinition> definition,
            VirtualProjectileDefinition.Direction direction
    ) implements SkillExecutionFeature {
        public static final MapCodec<Projectile> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                ExecutionSubject.CODEC.optionalFieldOf("subject", ExecutionSubject.CASTER).forGetter(Projectile::subject),
                DefinitionRef.codec(VirtualProjectileDefinition.CODEC).fieldOf("definition").forGetter(Projectile::definition),
                VirtualProjectileDefinition.Direction.CODEC.optionalFieldOf("direction", VirtualProjectileDefinition.Direction.LOOK).forGetter(Projectile::direction)
        ).apply(instance, Projectile::new));

        @Override
        public CodecType<SkillExecutionFeature> getType() {
            return AscensionSkillExecutionFeatureTypes.PROJECTILE.get();
        }

        @Override
        public void apply(SkillExecutionContext context) {
            Resolved<VirtualProjectileDefinition> resolved = SkillDefinitions.projectile(context, definition);
            if (resolved != null) {
                VirtualProjectiles.spawn(context, resolved.id(), direction);
            }
        }
    }

    public record Field(
            ExecutionSubject subject,
            RuntimeAction action,
            DefinitionRef<AreaFieldDefinition> definition
    ) implements SkillExecutionFeature {
        public static final MapCodec<Field> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                ExecutionSubject.CODEC.optionalFieldOf("subject", ExecutionSubject.POSITION).forGetter(Field::subject),
                RuntimeAction.CODEC.optionalFieldOf("action", RuntimeAction.APPLY).forGetter(Field::action),
                DefinitionRef.codec(AreaFieldDefinition.CODEC).fieldOf("definition").forGetter(Field::definition)
        ).apply(instance, Field::new));

        @Override
        public CodecType<SkillExecutionFeature> getType() {
            return AscensionSkillExecutionFeatureTypes.FIELD.get();
        }

        @Override
        public void apply(SkillExecutionContext context) {
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
            ExecutionSubject subject,
            RuntimeAction action,
            DefinitionRef<AnchorNetworkDefinition> definition
    ) implements SkillExecutionFeature {
        public static final MapCodec<Network> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                ExecutionSubject.CODEC.optionalFieldOf("subject", ExecutionSubject.POSITION).forGetter(Network::subject),
                RuntimeAction.CODEC.optionalFieldOf("action", RuntimeAction.APPLY).forGetter(Network::action),
                DefinitionRef.codec(AnchorNetworkDefinition.CODEC).fieldOf("definition").forGetter(Network::definition)
        ).apply(instance, Network::new));

        @Override
        public CodecType<SkillExecutionFeature> getType() {
            return AscensionSkillExecutionFeatureTypes.NETWORK.get();
        }

        @Override
        public void apply(SkillExecutionContext context) {
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
            ExecutionSubject subject,
            RuntimeAction action,
            DefinitionRef<OwnerBoundConstructDefinition> definition,
            ScaledValue amount
    ) implements SkillExecutionFeature {
        public static final MapCodec<Construct> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                ExecutionSubject.CODEC.optionalFieldOf("subject", ExecutionSubject.CASTER).forGetter(Construct::subject),
                RuntimeAction.CODEC.optionalFieldOf("action", RuntimeAction.APPLY).forGetter(Construct::action),
                DefinitionRef.codec(OwnerBoundConstructDefinition.CODEC).fieldOf("definition").forGetter(Construct::definition),
                ScaledValue.COMPACT_CODEC.optionalFieldOf("amount", ScaledValue.constant(0.0D)).forGetter(Construct::amount)
        ).apply(instance, Construct::new));

        @Override
        public CodecType<SkillExecutionFeature> getType() {
            return AscensionSkillExecutionFeatureTypes.CONSTRUCT.get();
        }

        @Override
        public void apply(SkillExecutionContext context) {
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
            ExecutionSubject subject,
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
    ) implements SkillExecutionFeature {
        public static final MapCodec<Move> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                ExecutionSubject.CODEC.optionalFieldOf("subject", ExecutionSubject.CASTER).forGetter(Move::subject),
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
        public CodecType<SkillExecutionFeature> getType() {
            return AscensionSkillExecutionFeatureTypes.MOVE.get();
        }

        @Override
        public void apply(SkillExecutionContext context) {
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

        private void directional(SkillExecutionContext context, LivingEntity mover) {
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

        private void targetPosition(SkillExecutionContext context, LivingEntity mover) {
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
                AscensionCraft.LOGGER.debug("Movement feature for {} failed: {} from {} toward {}", context.skill(), result.failureReason(), result.origin(), destination);
            }
        }

        private void anchor(SkillExecutionContext context, LivingEntity mover) {
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
            ExecutionSubject subject,
            AnchorAction action,
            Identifier anchor,
            ScaledValue duration
    ) implements SkillExecutionFeature {
        public static final MapCodec<Anchor> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                ExecutionSubject.CODEC.optionalFieldOf("subject", ExecutionSubject.CASTER).forGetter(Anchor::subject),
                AnchorAction.CODEC.fieldOf("action").forGetter(Anchor::action),
                Identifier.CODEC.fieldOf("anchor").forGetter(Anchor::anchor),
                ScaledValue.COMPACT_CODEC.optionalFieldOf("duration", ScaledValue.constant(0.0D)).forGetter(Anchor::duration)
        ).apply(instance, Anchor::new));

        @Override
        public CodecType<SkillExecutionFeature> getType() {
            return AscensionSkillExecutionFeatureTypes.ANCHOR.get();
        }

        @Override
        public void apply(SkillExecutionContext context) {
            if (action == AnchorAction.CLEAR) {
                MovementService.removeAnchor(context.caster(), anchor);
            } else {
                double value = duration.resolve(context.scaledValueContext());
                MovementService.setAnchor(context.caster(), anchor, !Double.isFinite(value) || value <= 0.0D ? 0L : Math.round(value));
            }
        }
    }

    public record Visual(
            ExecutionSubject subject,
            DefinitionRef<RuntimeVisualDefinition> definition,
            ScaledValue duration,
            boolean follow,
            boolean rotateWithSubject,
            Vec3 offset,
            PointMode points,
            ScaledValue progress,
            ScaledValue primary,
            ScaledValue secondary,
            int stage
    ) implements SkillExecutionFeature {
        public static final MapCodec<Visual> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                ExecutionSubject.CODEC.optionalFieldOf("subject", ExecutionSubject.ORIGIN).forGetter(Visual::subject),
                DefinitionRef.codec(RuntimeVisualDefinition.CODEC).fieldOf("definition").forGetter(Visual::definition),
                ScaledValue.COMPACT_CODEC.optionalFieldOf("duration", ScaledValue.constant(20.0D)).forGetter(Visual::duration),
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
            offset = offset == null ? Vec3.ZERO : offset;
            points = points == null ? PointMode.NONE : points;
        }

        @Override
        public CodecType<SkillExecutionFeature> getType() {
            return AscensionSkillExecutionFeatureTypes.VISUAL.get();
        }

        @Override
        public void apply(SkillExecutionContext context) {
            Resolved<RuntimeVisualDefinition> resolved = SkillDefinitions.visual(context, definition);
            if (resolved == null) {
                return;
            }
            LivingEntity attached = follow ? context.entity(subject) : null;
            Vec3 base = attached == null
                    ? context.position()
                    : attached.position().add(0.0D, attached.getBbHeight() * 0.5D, 0.0D);
            UUID runtimeId = UUID.randomUUID();
            List<Vec3> resolvedPoints = points.resolve(context);
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
                    resolved.value()
            ));
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

        public List<Vec3> resolve(SkillExecutionContext context) {
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
