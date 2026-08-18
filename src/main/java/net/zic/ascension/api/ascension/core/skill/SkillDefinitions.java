package net.zic.ascension.api.ascension.core.skill;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.Identifier;
import net.zic.ascension.api.ascension.core.CoreRegistries;
import net.zic.ascension.api.ascension.core.control.StaggerDefinition;
import net.zic.ascension.api.ascension.core.effect.SkillEffectDefinition;
import net.zic.ascension.api.ascension.core.projectile.VirtualProjectileDefinition;
import net.zic.ascension.api.ascension.core.runtime.AnchorNetworkDefinition;
import net.zic.ascension.api.ascension.core.runtime.AreaFieldDefinition;
import net.zic.ascension.api.ascension.core.runtime.BarrierDefinition;
import net.zic.ascension.api.ascension.core.runtime.OwnerBoundConstructDefinition;
import net.zic.ascension.api.ascension.core.runtime.RuntimeVisualDefinition;
import net.zic.ascension.api.ascension.core.skill.castable.action.SkillActionContext;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public record SkillDefinitions(
        Map<String, SkillEffectDefinition> effects,
        Map<String, VirtualProjectileDefinition> projectiles,
        Map<String, AreaFieldDefinition> fields,
        Map<String, AnchorNetworkDefinition> networks,
        Map<String, OwnerBoundConstructDefinition> constructs,
        Map<String, BarrierDefinition> barriers,
        Map<String, StaggerDefinition> stagger,
        Map<String, RuntimeVisualDefinition> visuals
) {
    private static final Map<Class<?>, Map<Identifier, Object>> CACHE = new ConcurrentHashMap<>();

    public static final SkillDefinitions EMPTY = new SkillDefinitions(
            Map.of(), Map.of(), Map.of(), Map.of(), Map.of(), Map.of(), Map.of(), Map.of()
    );

    public static final MapCodec<SkillDefinitions> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            map(SkillEffectDefinition.CODEC).optionalFieldOf("effects", Map.of()).forGetter(SkillDefinitions::effects),
            map(VirtualProjectileDefinition.CODEC).optionalFieldOf("projectiles", Map.of()).forGetter(SkillDefinitions::projectiles),
            map(AreaFieldDefinition.CODEC).optionalFieldOf("fields", Map.of()).forGetter(SkillDefinitions::fields),
            map(AnchorNetworkDefinition.CODEC).optionalFieldOf("networks", Map.of()).forGetter(SkillDefinitions::networks),
            map(OwnerBoundConstructDefinition.CODEC).optionalFieldOf("constructs", Map.of()).forGetter(SkillDefinitions::constructs),
            map(BarrierDefinition.CODEC).optionalFieldOf("barriers", Map.of()).forGetter(SkillDefinitions::barriers),
            map(StaggerDefinition.CODEC).optionalFieldOf("stagger", Map.of()).forGetter(SkillDefinitions::stagger),
            map(RuntimeVisualDefinition.CODEC).optionalFieldOf("visuals", Map.of()).forGetter(SkillDefinitions::visuals)
    ).apply(instance, SkillDefinitions::new));

    public SkillDefinitions {
        effects = copy(effects);
        projectiles = copy(projectiles);
        fields = copy(fields);
        networks = copy(networks);
        constructs = copy(constructs);
        barriers = copy(barriers);
        stagger = copy(stagger);
        visuals = copy(visuals);
    }

    public static Resolved<SkillEffectDefinition> effect(
            SkillActionContext context,
            DefinitionRef<SkillEffectDefinition> reference
    ) {
        return remember(SkillEffectDefinition.class, resolve(context, reference, "effect", SkillDefinitions::effects, SkillEffectDefinition.class, CoreRegistries.SKILL_EFFECT_REGISTRY));
    }

    public static Resolved<VirtualProjectileDefinition> projectile(
            SkillActionContext context,
            DefinitionRef<VirtualProjectileDefinition> reference
    ) {
        return remember(VirtualProjectileDefinition.class, resolve(context, reference, "projectile", SkillDefinitions::projectiles, VirtualProjectileDefinition.class, CoreRegistries.VIRTUAL_PROJECTILE_REGISTRY));
    }

    public static Resolved<AreaFieldDefinition> field(
            SkillActionContext context,
            DefinitionRef<AreaFieldDefinition> reference
    ) {
        return remember(AreaFieldDefinition.class, resolve(context, reference, "field", SkillDefinitions::fields, AreaFieldDefinition.class, CoreRegistries.AREA_FIELD_REGISTRY));
    }

    public static Resolved<AnchorNetworkDefinition> network(
            SkillActionContext context,
            DefinitionRef<AnchorNetworkDefinition> reference
    ) {
        return remember(AnchorNetworkDefinition.class, resolve(context, reference, "network", SkillDefinitions::networks, AnchorNetworkDefinition.class, CoreRegistries.ANCHOR_NETWORK_REGISTRY));
    }

    public static Resolved<OwnerBoundConstructDefinition> construct(
            SkillActionContext context,
            DefinitionRef<OwnerBoundConstructDefinition> reference
    ) {
        return remember(OwnerBoundConstructDefinition.class, resolve(context, reference, "construct", SkillDefinitions::constructs, OwnerBoundConstructDefinition.class, CoreRegistries.CONSTRUCT_REGISTRY));
    }

    public static Resolved<BarrierDefinition> barrier(
            SkillActionContext context,
            DefinitionRef<BarrierDefinition> reference
    ) {
        return remember(BarrierDefinition.class, resolve(context, reference, "barrier", SkillDefinitions::barriers, BarrierDefinition.class, CoreRegistries.BARRIER_REGISTRY));
    }

    public static Resolved<StaggerDefinition> stagger(
            SkillActionContext context,
            DefinitionRef<StaggerDefinition> reference
    ) {
        return remember(StaggerDefinition.class, resolve(context, reference, "stagger", SkillDefinitions::stagger, StaggerDefinition.class, CoreRegistries.STAGGER_REGISTRY));
    }

    public static Resolved<RuntimeVisualDefinition> visual(
            SkillActionContext context,
            DefinitionRef<RuntimeVisualDefinition> reference
    ) {
        Resolved<RuntimeVisualDefinition> resolved = resolve(
                context,
                reference,
                "visual",
                SkillDefinitions::visuals,
                RuntimeVisualDefinition.class,
                CoreRegistries.RUNTIME_VISUAL_REGISTRY
        );
        if (resolved == null && reference != null && reference.global().isPresent()) {
            return new Resolved<>(reference.global().get(), null);
        }
        return remember(RuntimeVisualDefinition.class, resolved);
    }

    private static <T> Resolved<T> resolve(
            SkillActionContext context,
            DefinitionRef<T> reference,
            String category,
            MapSelector<T> selector,
            Class<T> type,
            net.zic.zenithlib.registry.RegistryHelper.DataPackRegistry<T> registry
    ) {
        if (context == null || reference == null) {
            return null;
        }
        if (reference.inline().isPresent()) {
            T value = reference.inline().get();
            return new Resolved<>(inlineId(context.skill(), category, value), value);
        }
        if (reference.global().isPresent()) {
            Identifier id = reference.global().get();
            T value = cached(type, id, registry, context.level().registryAccess());
            return value == null ? null : new Resolved<>(id, value);
        }
        SkillDefinitions definitions = definitions(context);
        String name = reference.local().orElse(null);
        T value = name == null ? null : selector.select(definitions).get(name);
        return value == null ? null : new Resolved<>(localId(context.skill(), category, name), value);
    }

    public static SkillDefinitions definitions(SkillActionContext context) {
        if (context == null || context.skill() == null) {
            return EMPTY;
        }
        Skill skill = CoreRegistries.safeAccess(
                CoreRegistries.SKILL_REGISTRY,
                context.skill(),
                context.level().registryAccess()
        );
        if (skill instanceof Owner owner) {
            owner.definitions().prime(context.skill());
            return owner.definitions();
        }
        return EMPTY;
    }

    public static void prime(RegistryAccess access, Identifier skillId) {
        if (access == null || skillId == null) {
            return;
        }
        Skill skill = CoreRegistries.safeAccess(CoreRegistries.SKILL_REGISTRY, skillId, access);
        if (skill instanceof Owner owner) {
            owner.definitions().prime(skillId);
        }
    }

    public static <T> T resolveStored(
            Class<T> type,
            Identifier skillId,
            Identifier definitionId,
            net.zic.zenithlib.registry.RegistryHelper.DataPackRegistry<T> registry,
            RegistryAccess access
    ) {
        prime(access, skillId);
        return cached(type, definitionId, registry, access);
    }

    public void prime(Identifier skill) {
        if (skill == null) {
            return;
        }
        clearSkill(skill);
        effects.forEach((name, value) -> remember(SkillEffectDefinition.class, new Resolved<>(localId(skill, "effect", name), value)));
        projectiles.forEach((name, value) -> remember(VirtualProjectileDefinition.class, new Resolved<>(localId(skill, "projectile", name), value)));
        fields.forEach((name, value) -> remember(AreaFieldDefinition.class, new Resolved<>(localId(skill, "field", name), value)));
        networks.forEach((name, value) -> remember(AnchorNetworkDefinition.class, new Resolved<>(localId(skill, "network", name), value)));
        constructs.forEach((name, value) -> remember(OwnerBoundConstructDefinition.class, new Resolved<>(localId(skill, "construct", name), value)));
        barriers.forEach((name, value) -> remember(BarrierDefinition.class, new Resolved<>(localId(skill, "barrier", name), value)));
        stagger.forEach((name, value) -> remember(StaggerDefinition.class, new Resolved<>(localId(skill, "stagger", name), value)));
        visuals.forEach((name, value) -> remember(RuntimeVisualDefinition.class, new Resolved<>(localId(skill, "visual", name), value)));
    }

    public static Identifier localId(Identifier skill, String category, String name) {
        return Identifier.fromNamespaceAndPath(
                skill.getNamespace(),
                "skill/" + skill.getPath() + "/" + category + "/" + sanitize(name)
        );
    }

    public static Identifier inlineId(Identifier skill, String category, Object value) {
        return Identifier.fromNamespaceAndPath(
                skill.getNamespace(),
                "skill/" + skill.getPath() + "/" + category + "/inline_" + Integer.toUnsignedString(value.hashCode(), 36)
        );
    }

    private static String sanitize(String value) {
        return value.toLowerCase(java.util.Locale.ROOT).replaceAll("[^a-z0-9/._-]", "_");
    }

    private static <T> Codec<Map<String, T>> map(Codec<T> codec) {
        return Codec.unboundedMap(Codec.STRING, codec);
    }

    private static <T> Map<String, T> copy(Map<String, T> values) {
        return values == null ? Map.of() : Map.copyOf(values);
    }


    public static <T> Resolved<T> remember(Class<T> type, Resolved<T> definition) {
        if (definition != null && definition.value() != null) {
            CACHE.computeIfAbsent(type, ignored -> new ConcurrentHashMap<>()).put(definition.id(), definition.value());
        }
        return definition;
    }

    public static <T> T cached(Class<T> type, Identifier id, Supplier<T> fallback) {
        Map<Identifier, Object> values = CACHE.get(type);
        Object value = values == null ? null : values.get(id);
        if (type.isInstance(value)) {
            return type.cast(value);
        }
        T resolved = fallback == null ? null : fallback.get();
        if (resolved != null) {
            CACHE.computeIfAbsent(type, ignored -> new ConcurrentHashMap<>()).put(id, resolved);
        }
        return resolved;
    }

    public static <T> T cached(
            Class<T> type,
            Identifier id,
            net.zic.zenithlib.registry.RegistryHelper.DataPackRegistry<T> registry,
            RegistryAccess access
    ) {
        return cached(type, id, () -> CoreRegistries.safeAccess(registry, id, access));
    }

    public static void clearCache() {
        CACHE.clear();
    }

    private static void clearSkill(Identifier skill) {
        String prefix = "skill/" + skill.getPath() + "/";
        for (Map<Identifier, Object> values : CACHE.values()) {
            values.keySet().removeIf(id -> id.getNamespace().equals(skill.getNamespace()) && id.getPath().startsWith(prefix));
        }
    }

    public record Resolved<T>(Identifier id, T value) {
    }

    public interface Owner {
        SkillDefinitions definitions();
    }

    @FunctionalInterface
    private interface MapSelector<T> {
        Map<String, T> select(SkillDefinitions definitions);
    }
}
