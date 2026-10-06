package net.zic.ascension.impl.core.skill.passive;

import com.mojang.serialization.Codec;
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
import net.zic.ascension.api.ascension.core.path.bonus.PathBonus;
import net.zic.ascension.api.ascension.core.path.bonus.PathBonusHolder;
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

import net.zic.zenithlib.stats.ZenithStatHelper;
import net.zic.zenithlib.value_containers.typed.Modifier;
import net.zic.zenithlib.value_containers.typed.ModifierHolder;
import net.zic.zenithlib.value_containers.typed.ValueContainerCodecHelper;
import net.zic.zenithlib.value_containers.typed.ValueContainerHelpers;

import java.util.HashMap;
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
    public static final DeferredHolder<CodecType<PassiveModifier>, CodecType<PassiveModifier>> COMBAT = register("combat", Combat.CODEC);
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
            Map<Identifier, ModifierHolder<Double>> statModifiers,
            Map<PathBonus, ModifierHolder<Double>> pathBonusModifiers
    ) implements PassiveModifier {
        public static final MapCodec<Stats> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                ValueContainerCodecHelper.containersCodec(Codec.DOUBLE).optionalFieldOf("stats", Map.of()).forGetter(Stats::statModifiers),
                PathBonusHolder.MODIFIER_CODEC.optionalFieldOf("path_bonuses", Map.of()).forGetter(Stats::pathBonusModifiers)
        ).apply(instance, Stats::new));

        public Stats {

            statModifiers = statModifiers == null ? Map.of() : Map.copyOf(statModifiers);
            pathBonusModifiers = pathBonusModifiers == null ? Map.of() : Map.copyOf(pathBonusModifiers);
        }

        @Override
        public CodecType<PassiveModifier> getType() {
            return STATS.get();
        }

        @Override
        public void apply(OriginSource source, Identifier skillId) {
            for (Map.Entry<Identifier, ModifierHolder<Double>> modifiers : statModifiers.entrySet()) {
                Stat stat = ZenithStatHelper.stat(modifiers.getKey());
                for (Modifier<Double> modifier : modifiers.getValue().flat()) {
                    source.addFlatStatModifier(stat, modifier);
                }
                for (Modifier<Double> modifier : modifiers.getValue().multiplier()) {
                    source.addMultiplierStatModifier(stat, modifier);
                }
            }
            for (Map.Entry<PathBonus, ModifierHolder<Double>> modifiers : pathBonusModifiers.entrySet()) {
                PathBonus bonus = modifiers.getKey();
                for (Modifier<Double> modifier : modifiers.getValue().flat()) {
                    AscensionOriginSourceHelper.addBonusFlatModifier(
                            source,
                            bonus.category(),
                            bonus.path(),
                            modifier
                    );
                }
                for (Modifier<Double> modifier : modifiers.getValue().multiplier()) {
                    AscensionOriginSourceHelper.addBonusMultiplierModifier(
                            source,
                            bonus.category(),
                            bonus.path(),
                            modifier
                    );
                }
            }
        }

        @Override
        public void remove(OriginSource source, Identifier skillId) {
            for (Map.Entry<Identifier, ModifierHolder<Double>> modifiers : statModifiers.entrySet()) {
                Stat stat = ZenithStatHelper.stat(modifiers.getKey());
                for (Modifier<Double> modifier : modifiers.getValue().flat()) {
                    source.removeStatModifier(stat, modifier.id());
                }
                for (Modifier<Double> modifier : modifiers.getValue().multiplier()) {
                    source.removeStatModifier(stat, modifier.id());
                }
            }
            for (Map.Entry<PathBonus, ModifierHolder<Double>> modifiers : pathBonusModifiers.entrySet()) {
                PathBonus bonus = modifiers.getKey();
                for (Modifier<Double> modifier : modifiers.getValue().flat()) {
                    AscensionOriginSourceHelper.removeBonusModifier(
                            source,
                            bonus.category(),
                            bonus.path(),
                            modifier.id()
                    );
                }
                for (Modifier<Double> modifier : modifiers.getValue().multiplier()) {
                    AscensionOriginSourceHelper.removeBonusModifier(
                            source,
                            bonus.category(),
                            bonus.path(),
                            modifier.id()
                    );
                }
            }
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

    public record Combat(
            ScaledValue outgoingDamage,
            ScaledValue incomingDamage,
            ScaledValue lifesteal
    ) implements PassiveModifier {
        public static final MapCodec<Combat> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                ScaledValue.COMPACT_CODEC.optionalFieldOf("outgoing_damage", ScaledValue.constant(0.0D)).forGetter(Combat::outgoingDamage),
                ScaledValue.COMPACT_CODEC.optionalFieldOf("incoming_damage", ScaledValue.constant(0.0D)).forGetter(Combat::incomingDamage),
                ScaledValue.COMPACT_CODEC.optionalFieldOf("lifesteal", ScaledValue.constant(0.0D)).forGetter(Combat::lifesteal)
        ).apply(instance, Combat::new));

        public Combat {
            outgoingDamage = outgoingDamage == null ? ScaledValue.constant(0.0D) : outgoingDamage;
            incomingDamage = incomingDamage == null ? ScaledValue.constant(0.0D) : incomingDamage;
            lifesteal = lifesteal == null ? ScaledValue.constant(0.0D) : lifesteal;
        }

        @Override
        public CodecType<PassiveModifier> getType() {
            return COMBAT.get();
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
