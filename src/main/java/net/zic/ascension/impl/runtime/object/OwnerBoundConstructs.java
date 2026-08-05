package net.zic.ascension.impl.runtime.object;

import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.ascension.core.CoreRegistries;
import net.zic.ascension.api.ascension.core.runtime.OwnerBoundConstructDefinition;
import net.zic.ascension.api.ascension.core.runtime.RuntimeVisualDefinition;
import net.zic.ascension.api.ascension.core.runtime.RuntimeVisualState;
import net.zic.ascension.api.ascension.core.skill.castable.feature.SkillExecutionAttribution;
import net.zic.ascension.api.ascension.core.skill.castable.feature.SkillExecutionContext;
import net.zic.ascension.api.ascension.core.skill.castable.feature.SkillExecutionFeature;
import net.zic.ascension.api.ascension.core.skill.DefinitionRef;
import net.zic.ascension.api.ascension.core.skill.SkillDefinitions.Resolved;
import net.zic.ascension.api.ascension.core.skill.SkillDefinitions;
import net.zic.ascension.api.rpg_engine.damage.RPGEngineEntityDamagedEvent;
import net.zic.ascension.impl.runtime.projectile.ProjectileImpactResponses;
import net.zic.ascension.impl.runtime.object.RuntimeVisualSync;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.zic.ascension.api.ascension.core.projectile.NormalProjectileDefinition;

@EventBusSubscriber(modid = AscensionCraft.MOD_ID)
public final class OwnerBoundConstructs {
    public static final Identifier INTERCEPTED = AscensionCraft.prefix("construct_intercepted");
    public static final Identifier REMAINING_DAMAGE = AscensionCraft.prefix("construct_remaining_damage");
    public static final Identifier STABILITY_LOSS = AscensionCraft.prefix("construct_stability_loss");
    public static final Identifier STABILITY = AscensionCraft.prefix("construct_stability");
    public static final Identifier MAXIMUM_STABILITY = AscensionCraft.prefix("construct_maximum_stability");
    private static final List<Instance> CONSTRUCTS = new ArrayList<>();

    private OwnerBoundConstructs() {
    }

    public static UUID spawn(
            SkillExecutionContext context,
            Identifier definitionId
    ) {
        OwnerBoundConstructDefinition definition = SkillDefinitions.resolveStored(
                OwnerBoundConstructDefinition.class,
                context.skill(),
                definitionId,
                CoreRegistries.CONSTRUCT_REGISTRY,
                context.level().registryAccess()
        );
        if (definition == null) {
            return null;
        }

        double duration = definition.duration().resolve(context.scaledValueContext());
        double stability = definition.stability().resolve(context.scaledValueContext());
        if (!Double.isFinite(duration)
                || duration <= 0.0D
                || !Double.isFinite(stability)
                || stability <= 0.0D) {
            return null;
        }

        boolean interceptsDamage = false;
        double interceptionAbsorption = 0.0D;
        double interceptionStabilityCost = 1.0D;
        boolean interceptionOverflow = true;
        int interceptionPriority = 0;
        if (definition.interception().isPresent()) {
            OwnerBoundConstructDefinition.Interception interception = definition.interception().get();
            interceptionAbsorption = interception.absorption().resolve(context.scaledValueContext());
            interceptionStabilityCost = interception.stabilityCost().resolve(context.scaledValueContext());
            interceptsDamage = Double.isFinite(interceptionAbsorption)
                    && interceptionAbsorption > 0.0D
                    && Double.isFinite(interceptionStabilityCost)
                    && interceptionStabilityCost > 0.0D;
            interceptionOverflow = interception.overflow();
            interceptionPriority = interception.priority();
        }

        Instance construct = new Instance(
                context.level().dimension(),
                context.caster().getUUID(),
                context.skill(),
                definitionId,
                context.charge(),
                context.variables(),
                stability,
                context.level().getGameTime() + Math.max(1L, Math.round(duration)),
                resolvePosition(context.caster(), definition),
                interceptsDamage,
                interceptionAbsorption,
                interceptionStabilityCost,
                interceptionOverflow,
                interceptionPriority
        );
        CONSTRUCTS.add(construct);
        syncVisual(context.level(), construct, definition, true);
        return construct.runtimeId();
    }

    public static boolean remove(UUID runtimeId) {
        return runtimeId != null && CONSTRUCTS.removeIf(value -> value.runtimeId().equals(runtimeId));
    }

    public static boolean remove(ServerLevel level, UUID runtimeId) {
        if (level == null || runtimeId == null) {
            return false;
        }
        Iterator<Instance> iterator = CONSTRUCTS.iterator();
        while (iterator.hasNext()) {
            Instance construct = iterator.next();
            if (!construct.runtimeId().equals(runtimeId)) {
                continue;
            }
            iterator.remove();
            finish(level, construct, Removal.EXPLICIT);
            return true;
        }
        return false;
    }

    public static int removeOwned(UUID ownerId, Identifier definitionId) {
        int before = CONSTRUCTS.size();
        CONSTRUCTS.removeIf(value -> value.ownerId().equals(ownerId)
                && (definitionId == null || value.definitionId().equals(definitionId)));
        return before - CONSTRUCTS.size();
    }

    public static int removeOwned(ServerLevel level, UUID ownerId, Identifier definitionId) {
        if (level == null || ownerId == null) {
            return 0;
        }
        int removed = 0;
        Iterator<Instance> iterator = CONSTRUCTS.iterator();
        while (iterator.hasNext()) {
            Instance construct = iterator.next();
            if (!construct.ownerId().equals(ownerId)
                    || definitionId != null && !construct.definitionId().equals(definitionId)) {
                continue;
            }
            iterator.remove();
            finish(level, construct, Removal.EXPLICIT);
            removed++;
        }
        return removed;
    }

    public static double modifyStability(UUID runtimeId, double amount) {
        if (runtimeId == null || !Double.isFinite(amount)) {
            return 0.0D;
        }
        for (Instance construct : CONSTRUCTS) {
            if (construct.runtimeId().equals(runtimeId)) {
                return construct.modifyStability(amount);
            }
        }
        return 0.0D;
    }

    public static List<OwnerBoundConstructDefinition.View> findOwned(UUID ownerId, Identifier definitionId) {
        return CONSTRUCTS.stream()
                .filter(value -> value.ownerId().equals(ownerId))
                .filter(value -> definitionId == null || value.definitionId().equals(definitionId))
                .map(value -> (OwnerBoundConstructDefinition.View) value)
                .toList();
    }

    public static void applyDamage(RPGEngineEntityDamagedEvent.Pre event) {
        if (!(event.getEntity().level() instanceof ServerLevel level)) {
            return;
        }
        double remaining = event.getDamage();
        if (!Double.isFinite(remaining) || remaining <= 0.0D) {
            return;
        }

        List<Instance> candidates = CONSTRUCTS.stream()
                .filter(construct -> construct.dimension().equals(level.dimension()))
                .filter(construct -> construct.ownerId().equals(event.getEntity().getUUID()))
                .filter(Instance::interceptsDamage)
                .sorted(Comparator.comparingInt(Instance::interceptionPriority).reversed())
                .toList();

        for (Instance construct : candidates) {
            if (remaining <= 0.0D) {
                break;
            }
            if (!CONSTRUCTS.contains(construct)) {
                continue;
            }
            OwnerBoundConstructDefinition definition = SkillDefinitions.resolveStored(OwnerBoundConstructDefinition.class, construct.skillId(), construct.definitionId(), CoreRegistries.CONSTRUCT_REGISTRY, level.registryAccess());
            if (definition == null || definition.interception().isEmpty()) {
                continue;
            }
            OwnerBoundConstructDefinition.Interception interception = definition.interception().get();
            if (!interception.filter().accepts(event.getSource())) {
                continue;
            }

            double requested = remaining * construct.interceptionAbsorption();
            double capacity = construct.stability() / construct.interceptionStabilityCost();
            double covered = Math.min(requested, capacity);
            double prevented = construct.interceptionOverflow() ? covered : requested;
            double stabilityLoss = Math.min(
                    construct.stability(),
                    requested * construct.interceptionStabilityCost()
            );
            if (prevented <= 0.0D || stabilityLoss <= 0.0D) {
                continue;
            }

            construct.modifyStability(-stabilityLoss);
            remaining = Math.max(0.0D, remaining - prevented);
            if (remaining <= 0.0D) {
                ProjectileImpactResponses.apply(event.getSource(), event.getEntity(), interception.projectileResponse());
            }
            LivingEntity responseTarget = event.getSource().getEntity() instanceof LivingEntity attacker
                    && attacker != event.getEntity()
                    ? attacker
                    : null;
            execute(
                    level,
                    construct,
                    interception.onIntercept(),
                    event.getEntity() instanceof ServerPlayer owner ? owner : null,
                    responseTarget,
                    prevented,
                    remaining,
                    stabilityLoss
            );
            if (!CONSTRUCTS.contains(construct)) {
                continue;
            }

            if (construct.stability() <= 0.0D) {
                CONSTRUCTS.remove(construct);
                finish(level, construct, Removal.BROKEN);
            } else {
                syncVisual(level, construct, definition, true);
            }
        }

        event.setDamage(remaining);
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Pre event) {
        Iterator<Instance> iterator = CONSTRUCTS.iterator();
        while (iterator.hasNext()) {
            Instance construct = iterator.next();
            ServerLevel level = event.getServer().getLevel(construct.dimension());
            if (level == null) {
                iterator.remove();
                continue;
            }
            Entity entity = level.getEntity(construct.ownerId());
            OwnerBoundConstructDefinition definition = SkillDefinitions.resolveStored(OwnerBoundConstructDefinition.class, construct.skillId(), construct.definitionId(), CoreRegistries.CONSTRUCT_REGISTRY, level.registryAccess());
            Removal removal = null;
            if (!(entity instanceof ServerPlayer owner) || owner.isRemoved()) {
                removal = Removal.EXPLICIT;
            } else if (definition == null) {
                removal = Removal.EXPLICIT;
            } else if (construct.stability() <= 0.0D) {
                removal = Removal.BROKEN;
            } else if (level.getGameTime() >= construct.expiresAt()) {
                removal = Removal.EXPIRED;
            }

            if (removal != null) {
                iterator.remove();
                finish(level, construct, removal);
                continue;
            }
            tick(level, (ServerPlayer) entity, construct, definition);
        }
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        CONSTRUCTS.clear();
    }

    private static void tick(
            ServerLevel level,
            ServerPlayer owner,
            Instance construct,
            OwnerBoundConstructDefinition definition
    ) {
        construct.setPosition(resolvePosition(owner, definition));
        syncVisual(level, construct, definition, false);
    }

    private static void finish(
            ServerLevel level,
            Instance construct,
            Removal removal
    ) {
        OwnerBoundConstructDefinition definition = SkillDefinitions.resolveStored(OwnerBoundConstructDefinition.class, construct.skillId(), construct.definitionId(), CoreRegistries.CONSTRUCT_REGISTRY, level.registryAccess());
        removeRuntime(level, construct, definition);
        if (definition == null) {
            return;
        }
        Entity entity = level.getEntity(construct.ownerId());
        if (!(entity instanceof ServerPlayer owner)) {
            return;
        }
        if (removal == Removal.BROKEN) {
            execute(level, construct, definition.onBreak(), owner, owner, 0.0D, 0.0D, 0.0D);
        } else if (removal == Removal.EXPIRED) {
            execute(level, construct, definition.onExpire(), owner, owner, 0.0D, 0.0D, 0.0D);
        }
    }

    private static void execute(
            ServerLevel level,
            Instance construct,
            List<SkillExecutionFeature> features,
            ServerPlayer owner,
            LivingEntity target,
            double intercepted,
            double remaining,
            double stabilityLoss
    ) {
        if (features.isEmpty() || owner == null) {
            return;
        }
        Map<Identifier, Double> variables = new LinkedHashMap<>(construct.variables());
        variables.put(INTERCEPTED, intercepted);
        variables.put(REMAINING_DAMAGE, remaining);
        variables.put(STABILITY_LOSS, stabilityLoss);
        variables.put(STABILITY, construct.stability());
        variables.put(MAXIMUM_STABILITY, construct.maximumStability());
        SkillExecutionContext context = new SkillExecutionContext(
                level,
                owner,
                construct.skillId(),
                target,
                construct.position(),
                construct.charge(),
                variables,
                SkillExecutionAttribution.direct(owner)
        );
        for (SkillExecutionFeature feature : features) {
            feature.apply(context);
        }
    }

    private static void syncVisual(
            ServerLevel level,
            Instance construct,
            OwnerBoundConstructDefinition definition,
            boolean force
    ) {
        Entity entity = level.getEntity(construct.ownerId());
        if (!(entity instanceof ServerPlayer owner)) {
            return;
        }
        SkillExecutionContext context = constructContext(level, owner, construct);
        VisualSelection visual = resolveVisual(context, definition, construct);
        if (visual == null && construct.visualId() != null) {
            RuntimeVisualSync.remove(
                    level,
                    visualState(
                            construct,
                            definition,
                            construct.visualId(),
                            construct.visualStage(),
                            0L,
                            SkillDefinitions.cached(RuntimeVisualDefinition.class, construct.visualId(), null)
                    )
            );
            construct.setVisualState(null, 0);
            return;
        }
        if (visual == null) {
            return;
        }

        boolean visualChanged = construct.visualId() == null
                || !visual.id().equals(construct.visualId())
                || visual.stage() != construct.visualStage();
        boolean stabilityChanged = Math.abs(construct.stability() - construct.syncedStability())
                >= Math.max(1.0D, construct.maximumStability() * 0.05D);
        if (!force && !visualChanged && !stabilityChanged) {
            return;
        }

        RuntimeVisualState state = visualState(
                construct,
                definition,
                visual.id(),
                visual.stage(),
                construct.expiresAt(),
                visual.definition()
        );
        if (construct.visualId() == null) {
            RuntimeVisualSync.spawn(level, state);
        } else if (!construct.visualId().equals(visual.id())) {
            RuntimeVisualSync.remove(
                    level,
                    visualState(
                            construct,
                            definition,
                            construct.visualId(),
                            construct.visualStage(),
                            0L,
                            SkillDefinitions.cached(RuntimeVisualDefinition.class, construct.visualId(), null)
                    )
            );
            RuntimeVisualSync.spawn(level, state);
        } else {
            RuntimeVisualSync.update(level, state);
        }
        construct.setVisualState(visual.id(), visual.stage());
        construct.markStabilitySynced();
    }

    private static void removeRuntime(
            ServerLevel level,
            Instance construct,
            OwnerBoundConstructDefinition definition
    ) {
        if (construct.visualId() == null || definition == null) {
            return;
        }
        RuntimeVisualSync.remove(
                level,
                visualState(
                        construct,
                        definition,
                        construct.visualId(),
                        construct.visualStage(),
                        0L,
                        SkillDefinitions.cached(RuntimeVisualDefinition.class, construct.visualId(), null)
                )
        );
    }

    private static VisualSelection resolveVisual(
            SkillExecutionContext context,
            OwnerBoundConstructDefinition definition,
            Instance construct
    ) {
        double fraction = construct.maximumStability() <= 0.0D
                ? 0.0D
                : construct.stability() / construct.maximumStability();
        for (int index = 0; index < definition.visualStages().size(); index++) {
            OwnerBoundConstructDefinition.VisualStage stage = definition.visualStages().get(index);
            if (fraction <= stage.maximumStabilityFraction()) {
                Resolved<RuntimeVisualDefinition> resolved = SkillDefinitions.visual(context, stage.visual());
                return resolved == null ? null : new VisualSelection(resolved.id(), resolved.value(), index + 1);
            }
        }
        Resolved<RuntimeVisualDefinition> resolved = definition.visual()
                .map(reference -> SkillDefinitions.visual(context, reference))
                .orElse(null);
        return resolved == null ? null : new VisualSelection(resolved.id(), resolved.value(), 0);
    }

    private static SkillExecutionContext constructContext(
            ServerLevel level,
            ServerPlayer owner,
            Instance construct
    ) {
        return new SkillExecutionContext(
                level,
                owner,
                construct.skillId(),
                null,
                construct.position(),
                construct.charge(),
                construct.variables(),
                SkillExecutionAttribution.direct(owner)
        );
    }

    private static RuntimeVisualState visualState(
            Instance construct,
            OwnerBoundConstructDefinition definition,
            Identifier visual,
            int stage,
            long expiresAt,
            RuntimeVisualDefinition visualDefinition
    ) {
        float progress = construct.maximumStability() <= 0.0D
                ? 0.0F
                : (float) Math.clamp(
                        construct.stability() / construct.maximumStability(),
                        0.0D,
                        1.0D
                );
        return new RuntimeVisualState(
                construct.runtimeId(),
                visual,
                construct.ownerId(),
                construct.position(),
                definition.offset(),
                List.of(),
                List.of(),
                expiresAt,
                stage,
                RuntimeVisualState.OWNER_RELATIVE
                        | (definition.rotateWithOwner() ? RuntimeVisualState.ROTATE_WITH_OWNER : 0),
                progress,
                construct.runtimeId().getMostSignificantBits(),
                construct.stability(),
                construct.maximumStability(),
                visualDefinition
        );
    }

    private static Vec3 resolvePosition(
            ServerPlayer owner,
            OwnerBoundConstructDefinition definition
    ) {
        Vec3 offset = definition.offset();
        if (definition.rotateWithOwner()) {
            double radians = Math.toRadians(-owner.getYRot());
            double cos = Math.cos(radians);
            double sin = Math.sin(radians);
            offset = new Vec3(
                    offset.x * cos - offset.z * sin,
                    offset.y,
                    offset.x * sin + offset.z * cos
            );
        }
        return owner.position().add(offset);
    }

    public enum Removal {
        EXPLICIT,
        BROKEN,
        EXPIRED
    }

    private record VisualSelection(Identifier id, RuntimeVisualDefinition definition, int stage) {
    }

    private static final class Instance implements OwnerBoundConstructDefinition.View {
        private final UUID runtimeId;
        private final ResourceKey<Level> dimension;
        private final UUID ownerId;
        private final Identifier skillId;
        private final Identifier definitionId;
        private final double charge;
        private final Map<Identifier, Double> variables;
        private final double maximumStability;
        private final long expiresAt;
        private final boolean interceptsDamage;
        private final double interceptionAbsorption;
        private final double interceptionStabilityCost;
        private final boolean interceptionOverflow;
        private final int interceptionPriority;
        private double stability;
        private Vec3 position;
        private Identifier visualId;
        private int visualStage;
        private double syncedStability;

        private Instance(
                ResourceKey<Level> dimension,
                UUID ownerId,
                Identifier skillId,
                Identifier definitionId,
                double charge,
                Map<Identifier, Double> variables,
                double stability,
                long expiresAt,
                Vec3 position,
                boolean interceptsDamage,
                double interceptionAbsorption,
                double interceptionStabilityCost,
                boolean interceptionOverflow,
                int interceptionPriority
        ) {
            this.runtimeId = UUID.randomUUID();
            this.dimension = dimension;
            this.ownerId = ownerId;
            this.skillId = skillId;
            this.definitionId = definitionId;
            this.charge = charge;
            this.variables = variables == null ? Map.of() : Map.copyOf(variables);
            this.maximumStability = Math.max(0.0D, stability);
            this.stability = this.maximumStability;
            this.expiresAt = expiresAt;
            this.position = position;
            this.interceptsDamage = interceptsDamage;
            this.interceptionAbsorption = Math.clamp(interceptionAbsorption, 0.0D, 1.0D);
            this.interceptionStabilityCost = Math.max(0.000001D, interceptionStabilityCost);
            this.interceptionOverflow = interceptionOverflow;
            this.interceptionPriority = interceptionPriority;
        }

        @Override
        public UUID runtimeId() {
            return runtimeId;
        }

        public ResourceKey<Level> dimension() {
            return dimension;
        }

        @Override
        public UUID ownerId() {
            return ownerId;
        }

        public Identifier skillId() {
            return skillId;
        }

        @Override
        public Identifier definitionId() {
            return definitionId;
        }

        public double charge() {
            return charge;
        }

        public Map<Identifier, Double> variables() {
            return variables;
        }

        @Override
        public double stability() {
            return stability;
        }

        @Override
        public double maximumStability() {
            return maximumStability;
        }

        public double modifyStability(double amount) {
            double previous = stability;
            stability = Math.clamp(stability + amount, 0.0D, maximumStability);
            return stability - previous;
        }

        public boolean interceptsDamage() {
            return interceptsDamage;
        }

        public double interceptionAbsorption() {
            return interceptionAbsorption;
        }

        public double interceptionStabilityCost() {
            return interceptionStabilityCost;
        }

        public boolean interceptionOverflow() {
            return interceptionOverflow;
        }

        public int interceptionPriority() {
            return interceptionPriority;
        }

        @Override
        public Vec3 position() {
            return position;
        }

        public void setPosition(Vec3 position) {
            this.position = position;
        }

        public Identifier visualId() {
            return visualId;
        }

        public int visualStage() {
            return visualStage;
        }

        public void setVisualState(Identifier visualId, int visualStage) {
            this.visualId = visualId;
            this.visualStage = Math.max(0, visualStage);
            this.syncedStability = stability;
        }

        public double syncedStability() {
            return syncedStability;
        }

        public void markStabilitySynced() {
            syncedStability = stability;
        }

        @Override
        public long expiresAt() {
            return expiresAt;
        }
    }
}
