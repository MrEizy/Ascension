package net.zic.ascension.impl.core.skill.passive;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.minecraft.util.StringRepresentable;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.ascension.core.projectile.NormalProjectileDefinition;
import net.zic.ascension.api.ascension.core.resource.ResourceModifiers;
import net.zic.ascension.api.ascension.core.runtime.BarrierDefinition;
import net.zic.ascension.api.ascension.core.skill.passive.PassiveModifier;
import net.zic.ascension.api.ascension.core.source.AscensionOriginSourceHelper;
import net.zic.ascension.api.ascension.datapack.CodecType;
import net.zic.ascension.api.ascension.value.ScaledValue;
import net.zic.ascension.api.rpg_engine.source.OriginSource;
import net.zic.zenithlib.common.ZenithRegistries;
import net.zic.zenithlib.registry.RegistryHelper;
import net.zic.zenithlib.stats.Stat;
import net.zic.zenithlib.value_containers.ValueContainer;
import net.zic.zenithlib.value_containers.ValueContainerModifier;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@EventBusSubscriber(modid = AscensionCraft.MOD_ID)
public final class PassiveModifiers {
    public static final Registry<CodecType<PassiveModifier>> REGISTRY = RegistryHelper.registry(
            AscensionCraft.MOD_ID,
            "passive_modifier_type"
    );
    private static final DeferredRegister<CodecType<PassiveModifier>> TYPES = DeferredRegister.create(
            REGISTRY,
            AscensionCraft.MOD_ID
    );

    public static final DeferredHolder<CodecType<PassiveModifier>, CodecType<PassiveModifier>> STATS = register("stats", Stats.CODEC);
    public static final DeferredHolder<CodecType<PassiveModifier>, CodecType<PassiveModifier>> DEFENSE = register("defense", Defense.CODEC);
    public static final DeferredHolder<CodecType<PassiveModifier>, CodecType<PassiveModifier>> RESOURCES = register("resources", Resources.CODEC);
    public static final DeferredHolder<CodecType<PassiveModifier>, CodecType<PassiveModifier>> PROJECTILES = register("projectiles", Projectiles.CODEC);
    public static final DeferredHolder<CodecType<PassiveModifier>, CodecType<PassiveModifier>> WEAPON_DAMAGE = register("weapon_damage", WeaponDamage.CODEC);
    public static final DeferredHolder<CodecType<PassiveModifier>, CodecType<PassiveModifier>> MOVEMENT = register("movement", Movement.CODEC);

    private PassiveModifiers() {
    }

    private static DeferredHolder<CodecType<PassiveModifier>, CodecType<PassiveModifier>> register(
            String name,
            MapCodec<? extends PassiveModifier> codec
    ) {
        return TYPES.register(name, () -> new CodecType<>(codec));
    }

    public static void register(IEventBus eventBus) {
        TYPES.register(eventBus);
    }

    @SubscribeEvent
    public static void registerRegistry(NewRegistryEvent event) {
        event.register(REGISTRY);
    }

    public record Stats(
            List<ValueContainer.BaseModifier> baseStats,
            Map<Identifier, List<ValueContainerModifier>> statModifiers,
            List<ValueContainer.BaseModifier> baseAffinities,
            Map<Identifier, List<ValueContainerModifier>> affinityModifiers
    ) implements PassiveModifier {
        public static final MapCodec<Stats> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                ValueContainer.BASE_MODIFIER_CODEC.listOf().optionalFieldOf("base_stats", List.of()).forGetter(Stats::baseStats),
                ValueContainerModifier.MAP_CODEC.optionalFieldOf("stat_modifiers", Map.of()).forGetter(Stats::statModifiers),
                ValueContainer.BASE_MODIFIER_CODEC.listOf().optionalFieldOf("base_affinities", List.of()).forGetter(Stats::baseAffinities),
                ValueContainerModifier.MAP_CODEC.optionalFieldOf("affinity_modifiers", Map.of()).forGetter(Stats::affinityModifiers)
        ).apply(instance, Stats::new));

        public Stats {
            baseStats = baseStats == null ? List.of() : List.copyOf(baseStats);
            statModifiers = statModifiers == null ? Map.of() : Map.copyOf(statModifiers);
            baseAffinities = baseAffinities == null ? List.of() : List.copyOf(baseAffinities);
            affinityModifiers = affinityModifiers == null ? Map.of() : Map.copyOf(affinityModifiers);
        }

        @Override
        public CodecType<PassiveModifier> getType() {
            return STATS.get();
        }

        @Override
        public void apply(OriginSource source, Identifier skillId) {
            for (ValueContainer.BaseModifier modifier : baseStats) {
                Stat stat = ZenithRegistries.STAT_REGISTRY.getValue(modifier.container());
                if (stat != null) {
                    source.addStat(stat, modifier.val());
                }
            }
            statModifiers.forEach((statId, modifiers) -> {
                Stat stat = ZenithRegistries.STAT_REGISTRY.getValue(statId);
                if (stat != null) {
                    modifiers.forEach(modifier -> source.addStatModifier(stat, modifier));
                }
            });
            for (ValueContainer.BaseModifier modifier : baseAffinities) {
                AscensionOriginSourceHelper.addAffinity(source, modifier.container(), modifier.val());
            }
            affinityModifiers.forEach((path, modifiers) -> modifiers.forEach(
                    modifier -> AscensionOriginSourceHelper.addAffinityModifier(source, path, modifier)
            ));
        }

        @Override
        public void remove(OriginSource source, Identifier skillId) {
            for (ValueContainer.BaseModifier modifier : baseStats) {
                Stat stat = ZenithRegistries.STAT_REGISTRY.getValue(modifier.container());
                if (stat != null) {
                    source.removeStat(stat, modifier.val());
                }
            }
            statModifiers.forEach((statId, modifiers) -> {
                Stat stat = ZenithRegistries.STAT_REGISTRY.getValue(statId);
                if (stat != null) {
                    modifiers.forEach(modifier -> source.removeStatModifier(stat, modifier.getIdentifier()));
                }
            });
            for (ValueContainer.BaseModifier modifier : baseAffinities) {
                AscensionOriginSourceHelper.removeAffinity(source, modifier.container(), modifier.val());
            }
            affinityModifiers.forEach((path, modifiers) -> modifiers.forEach(
                    modifier -> AscensionOriginSourceHelper.removeAffinityModifier(source, path, modifier.getIdentifier())
            ));
        }
    }

    public record Defense(
            ScaledValue flatReduction,
            ScaledValue percentageReduction,
            ScaledValue staggerResistance,
            BarrierDefinition.DamageFilter filter
    ) implements PassiveModifier {
        public static final MapCodec<Defense> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                ScaledValue.COMPACT_CODEC.optionalFieldOf("flat_reduction", ScaledValue.constant(0.0D)).forGetter(Defense::flatReduction),
                ScaledValue.COMPACT_CODEC.optionalFieldOf("percentage_reduction", ScaledValue.constant(0.0D)).forGetter(Defense::percentageReduction),
                ScaledValue.COMPACT_CODEC.optionalFieldOf("stagger_resistance", ScaledValue.constant(0.0D)).forGetter(Defense::staggerResistance),
                BarrierDefinition.DamageFilter.CODEC.optionalFieldOf("filter", BarrierDefinition.DamageFilter.EMPTY).forGetter(Defense::filter)
        ).apply(instance, Defense::new));

        @Override
        public CodecType<PassiveModifier> getType() {
            return DEFENSE.get();
        }
    }

    public record Resources(List<ResourceModifiers.Definition> modifiers) implements PassiveModifier {
        public static final MapCodec<Resources> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                ResourceModifiers.Definition.CODEC.codec().listOf().optionalFieldOf("modifiers", List.of()).forGetter(Resources::modifiers)
        ).apply(instance, Resources::new));

        public Resources {
            modifiers = modifiers == null ? List.of() : List.copyOf(modifiers);
        }

        @Override
        public CodecType<PassiveModifier> getType() {
            return RESOURCES.get();
        }
    }

    public record Projectiles(List<NormalProjectileDefinition> profiles) implements PassiveModifier {
        public static final MapCodec<Projectiles> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                NormalProjectileDefinition.CODEC.listOf().optionalFieldOf("profiles", List.of()).forGetter(Projectiles::profiles)
        ).apply(instance, Projectiles::new));

        public Projectiles {
            profiles = profiles == null ? List.of() : List.copyOf(profiles);
        }

        @Override
        public CodecType<PassiveModifier> getType() {
            return PROJECTILES.get();
        }
    }

    public record Movement(boolean allowFlight, double flightSpeedMultiplier, boolean noFallDamage) implements PassiveModifier {
        public static final MapCodec<Movement> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                com.mojang.serialization.Codec.BOOL.optionalFieldOf("allow_flight", false).forGetter(Movement::allowFlight),
                com.mojang.serialization.Codec.doubleRange(0.05D, 20.0D).optionalFieldOf("flight_speed_multiplier", 1.0D).forGetter(Movement::flightSpeedMultiplier),
                com.mojang.serialization.Codec.BOOL.optionalFieldOf("no_fall_damage", false).forGetter(Movement::noFallDamage)
        ).apply(instance, Movement::new));

        @Override
        public CodecType<PassiveModifier> getType() {
            return MOVEMENT.get();
        }

        @Override
        public void apply(OriginSource source, Identifier skillId) {
            source.getAttachedEntities().forEach(entity -> applyToEntity(entity, skillId));
        }

        @Override
        public void remove(OriginSource source, Identifier skillId) {
            source.getAttachedEntities().forEach(entity -> removeFromEntity(entity, skillId));
        }

        @Override
        public void applyToEntity(LivingEntity entity, Identifier skillId) {
            if (allowFlight) {
                applyModifier(entity.getAttribute(NeoForgeMod.CREATIVE_FLIGHT), modifierId(skillId, "flight"), 1.0D, AttributeModifier.Operation.ADD_VALUE);
            }
            if (flightSpeedMultiplier != 1.0D) {
                applyModifier(entity.getAttribute(Attributes.FLYING_SPEED), modifierId(skillId, "flight_speed"), flightSpeedMultiplier - 1.0D, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
            }
            if (noFallDamage) {
                applyModifier(entity.getAttribute(Attributes.FALL_DAMAGE_MULTIPLIER), modifierId(skillId, "fall_damage"), -1.0D, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
            }
        }

        @Override
        public void removeFromEntity(LivingEntity entity, Identifier skillId) {
            removeModifier(entity.getAttribute(NeoForgeMod.CREATIVE_FLIGHT), modifierId(skillId, "flight"));
            removeModifier(entity.getAttribute(Attributes.FLYING_SPEED), modifierId(skillId, "flight_speed"));
            removeModifier(entity.getAttribute(Attributes.FALL_DAMAGE_MULTIPLIER), modifierId(skillId, "fall_damage"));
        }

        private static void applyModifier(AttributeInstance instance, Identifier id, double value, AttributeModifier.Operation operation) {
            if (instance != null) {
                instance.addOrUpdateTransientModifier(new AttributeModifier(id, value, operation));
            }
        }

        private static void removeModifier(AttributeInstance instance, Identifier id) {
            if (instance != null) {
                instance.removeModifier(id);
            }
        }

        private static Identifier modifierId(Identifier skillId, String suffix) {
            return Identifier.fromNamespaceAndPath(skillId.getNamespace(), "skill/" + skillId.getPath() + "/movement/" + suffix);
        }
    }

    public record WeaponDamage(
            Optional<Identifier> weaponTag,
            Match match,
            ScaledValue multiplier
    ) implements PassiveModifier {
        public static final MapCodec<WeaponDamage> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Identifier.CODEC.optionalFieldOf("weapon_tag").forGetter(WeaponDamage::weaponTag),
                Match.CODEC.optionalFieldOf("match", Match.HELD_WEAPON).forGetter(WeaponDamage::match),
                ScaledValue.COMPACT_CODEC.optionalFieldOf("multiplier", ScaledValue.constant(1.0D)).forGetter(WeaponDamage::multiplier)
        ).apply(instance, WeaponDamage::new));

        public WeaponDamage {
            weaponTag = weaponTag == null ? Optional.empty() : weaponTag;
            match = match == null ? Match.HELD_WEAPON : match;
            multiplier = multiplier == null ? ScaledValue.constant(1.0D) : multiplier;
        }

        @Override
        public CodecType<PassiveModifier> getType() {
            return WEAPON_DAMAGE.get();
        }

        public enum Match implements StringRepresentable {
            HELD_WEAPON("held_weapon"),
            EMPTY_HAND_OR_TAG("empty_hand_or_tag"),
            ARROW("arrow"),
            TRIDENT("trident");

            public static final com.mojang.serialization.Codec<Match> CODEC = StringRepresentable.fromEnum(Match::values);
            private final String name;

            Match(String name) {
                this.name = name;
            }

            @Override
            public String getSerializedName() {
                return name;
            }
        }
    }
}
