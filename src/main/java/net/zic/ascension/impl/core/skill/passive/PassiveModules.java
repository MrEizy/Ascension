package net.zic.ascension.impl.core.skill.passive;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
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
import net.zic.ascension.api.ascension.core.skill.PassiveModule;
import net.zic.ascension.api.ascension.datapack.CodecType;
import net.zic.ascension.api.ascension.datapack.CodecHelpers;
import net.zic.ascension.api.ascension.value.ScaledValue;
import net.zic.ascension.impl.runtime.weapon.WeaponSwingSpec;
import net.minecraft.world.phys.Vec3;
import net.zic.ascension.api.rpg_engine.source.OriginSource;
import net.zic.ascension.api.ascension.core.source.AscensionOriginSourceHelper;
import net.zic.zenithlib.common.ZenithRegistries;
import net.zic.zenithlib.registry.RegistryHelper;
import net.zic.zenithlib.stats.Stat;
import net.zic.zenithlib.value_containers.ValueContainer;
import net.zic.zenithlib.value_containers.ValueContainerModifier;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@EventBusSubscriber(modid = AscensionCraft.MOD_ID)
public final class PassiveModules {
    public static final Registry<CodecType<PassiveModule>> REGISTRY = RegistryHelper.registry(
            AscensionCraft.MOD_ID,
            "passive_module_type"
    );
    private static final DeferredRegister<CodecType<PassiveModule>> TYPES = DeferredRegister.create(
            REGISTRY,
            AscensionCraft.MOD_ID
    );

    public static final DeferredHolder<CodecType<PassiveModule>, CodecType<PassiveModule>> STATS =
            register("stats", Stats.CODEC);
    public static final DeferredHolder<CodecType<PassiveModule>, CodecType<PassiveModule>> DEFENSE =
            register("defense", Defense.CODEC);
    public static final DeferredHolder<CodecType<PassiveModule>, CodecType<PassiveModule>> RESOURCES =
            register("resources", Resources.CODEC);
    public static final DeferredHolder<CodecType<PassiveModule>, CodecType<PassiveModule>> PROJECTILES =
            register("projectiles", Projectiles.CODEC);
    public static final DeferredHolder<CodecType<PassiveModule>, CodecType<PassiveModule>> WEAPON_SWING =
            register("weapon_swing", WeaponSwing.CODEC);
    public static final DeferredHolder<CodecType<PassiveModule>, CodecType<PassiveModule>> WEAPON_DAMAGE =
            register("weapon_damage", WeaponDamage.CODEC);

    private PassiveModules() {
    }

    private static DeferredHolder<CodecType<PassiveModule>, CodecType<PassiveModule>> register(
            String name,
            MapCodec<? extends PassiveModule> codec
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
    ) implements PassiveModule {
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
        public CodecType<PassiveModule> getType() {
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
    ) implements PassiveModule {
        public static final MapCodec<Defense> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                ScaledValue.COMPACT_CODEC.optionalFieldOf("flat_reduction", ScaledValue.constant(0.0D)).forGetter(Defense::flatReduction),
                ScaledValue.COMPACT_CODEC.optionalFieldOf("percentage_reduction", ScaledValue.constant(0.0D)).forGetter(Defense::percentageReduction),
                ScaledValue.COMPACT_CODEC.optionalFieldOf("stagger_resistance", ScaledValue.constant(0.0D)).forGetter(Defense::staggerResistance),
                BarrierDefinition.DamageFilter.CODEC.optionalFieldOf("filter", BarrierDefinition.DamageFilter.EMPTY).forGetter(Defense::filter)
        ).apply(instance, Defense::new));

        @Override
        public CodecType<PassiveModule> getType() {
            return DEFENSE.get();
        }
    }

    public record Resources(List<ResourceModifiers.Definition> modifiers) implements PassiveModule {
        public static final MapCodec<Resources> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                ResourceModifiers.Definition.CODEC.codec().listOf().optionalFieldOf("modifiers", List.of()).forGetter(Resources::modifiers)
        ).apply(instance, Resources::new));

        public Resources {
            modifiers = modifiers == null ? List.of() : List.copyOf(modifiers);
        }

        @Override
        public CodecType<PassiveModule> getType() {
            return RESOURCES.get();
        }
    }

    public record Projectiles(List<NormalProjectileDefinition> profiles) implements PassiveModule {
        public static final MapCodec<Projectiles> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                NormalProjectileDefinition.CODEC.listOf().optionalFieldOf("profiles", List.of()).forGetter(Projectiles::profiles)
        ).apply(instance, Projectiles::new));

        public Projectiles {
            profiles = profiles == null ? List.of() : List.copyOf(profiles);
        }

        @Override
        public CodecType<PassiveModule> getType() {
            return PROJECTILES.get();
        }
    }


    /**
     * Datapack-configurable normal-attack projection. The runtime event service
     * reads this module when the client reports an attack input.
     */
    public record WeaponSwing(
            Identifier path,
            Identifier weaponTag,
            String vfxType,
            String fallbackColor,
            Map<Identifier, String> techniqueColors,
            Vec3 radius,
            double baseDamage,
            double knockback,
            int duration,
            float rotationZ,
            Vec3 movement,
            double qiCost,
            int minimumInterval,
            RealmScaling realmScaling,
            List<Identifier> classifications,
            Extras extras
    ) implements PassiveModule {
        public static final MapCodec<WeaponSwing> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Identifier.CODEC.fieldOf("path").forGetter(WeaponSwing::path),
                Identifier.CODEC.optionalFieldOf("weapon_tag", Identifier.fromNamespaceAndPath("minecraft", "swords"))
                        .forGetter(WeaponSwing::weaponTag),
                Codec.STRING.optionalFieldOf("vfx_type", "sword_swing").forGetter(WeaponSwing::vfxType),
                Codec.STRING.optionalFieldOf("fallback_color", "blue").forGetter(WeaponSwing::fallbackColor),
                Codec.unboundedMap(Identifier.CODEC, Codec.STRING).optionalFieldOf("technique_colors", Map.of())
                        .forGetter(WeaponSwing::techniqueColors),
                CodecHelpers.VEC3.optionalFieldOf("radius", new Vec3(2.0D, 2.0D, 2.0D)).forGetter(WeaponSwing::radius),
                Codec.DOUBLE.optionalFieldOf("base_damage", 4.0D).forGetter(WeaponSwing::baseDamage),
                Codec.DOUBLE.optionalFieldOf("knockback", 1.0D).forGetter(WeaponSwing::knockback),
                Codec.intRange(1, 1200).optionalFieldOf("duration", 10).forGetter(WeaponSwing::duration),
                Codec.FLOAT.optionalFieldOf("rotation_z", 0.0F).forGetter(WeaponSwing::rotationZ),
                CodecHelpers.VEC3.optionalFieldOf("movement", Vec3.ZERO).forGetter(WeaponSwing::movement),
                Codec.DOUBLE.optionalFieldOf("qi_cost", 2.0D).forGetter(WeaponSwing::qiCost),
                Codec.intRange(1, 1200).optionalFieldOf("minimum_interval", 1).forGetter(WeaponSwing::minimumInterval),
                RealmScaling.CODEC.optionalFieldOf("realm_scaling", RealmScaling.DEFAULT).forGetter(WeaponSwing::realmScaling),
                Identifier.CODEC.listOf().optionalFieldOf("classifications", List.of()).forGetter(WeaponSwing::classifications),
                Extras.CODEC.forGetter(WeaponSwing::extras)
        ).apply(instance, WeaponSwing::new));

        public WeaponSwing {
            vfxType = vfxType == null || vfxType.isBlank() ? "sword_swing" : vfxType;
            fallbackColor = fallbackColor == null || fallbackColor.isBlank() ? "blue" : fallbackColor;
            techniqueColors = techniqueColors == null ? Map.of() : Map.copyOf(techniqueColors);
            radius = radius == null ? new Vec3(2.0D, 2.0D, 2.0D) : radius;
            baseDamage = Double.isFinite(baseDamage) ? Math.max(0.0D, baseDamage) : 0.0D;
            knockback = Double.isFinite(knockback) ? Math.max(0.0D, knockback) : 0.0D;
            duration = Math.clamp(duration, 1, 1200);
            movement = movement == null ? Vec3.ZERO : movement;
            qiCost = Double.isFinite(qiCost) ? Math.max(0.0D, qiCost) : 0.0D;
            minimumInterval = Math.clamp(minimumInterval, 1, 1200);
            realmScaling = realmScaling == null ? RealmScaling.DEFAULT : realmScaling;
            classifications = classifications == null ? List.of() : List.copyOf(classifications);
            extras = extras == null ? Extras.DEFAULT : extras;
        }

        @Override
        public CodecType<PassiveModule> getType() {
            return WEAPON_SWING.get();
        }

        public record RealmScaling(
                double baseBonus,
                double bonusPerMajorRealm,
                double bonusPerMinorRealm,
                double maximumBonus
        ) {
            public static final RealmScaling DEFAULT = new RealmScaling(0.10D, 0.22D, 0.025D, 5.0D);
            public static final Codec<RealmScaling> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    Codec.DOUBLE.optionalFieldOf("base_bonus", DEFAULT.baseBonus).forGetter(RealmScaling::baseBonus),
                    Codec.DOUBLE.optionalFieldOf("bonus_per_major_realm", DEFAULT.bonusPerMajorRealm)
                            .forGetter(RealmScaling::bonusPerMajorRealm),
                    Codec.DOUBLE.optionalFieldOf("bonus_per_minor_realm", DEFAULT.bonusPerMinorRealm)
                            .forGetter(RealmScaling::bonusPerMinorRealm),
                    Codec.DOUBLE.optionalFieldOf("maximum_bonus", DEFAULT.maximumBonus).forGetter(RealmScaling::maximumBonus)
            ).apply(instance, RealmScaling::new));

            public RealmScaling {
                baseBonus = Double.isFinite(baseBonus) ? baseBonus : DEFAULT.baseBonus;
                bonusPerMajorRealm = Double.isFinite(bonusPerMajorRealm)
                        ? bonusPerMajorRealm : DEFAULT.bonusPerMajorRealm;
                bonusPerMinorRealm = Double.isFinite(bonusPerMinorRealm)
                        ? bonusPerMinorRealm : DEFAULT.bonusPerMinorRealm;
                maximumBonus = Double.isFinite(maximumBonus) ? Math.max(0.0D, maximumBonus) : DEFAULT.maximumBonus;
            }
        }

        public record Extras(
                boolean allowEmptyHand,
                WeaponSwingSpec.HitShape hitShape,
                WeaponSwingSpec.BlockImpact blockImpact,
                Optional<PassiveHitEffect> hitEffect,
                int priority
        ) {
            public static final Extras DEFAULT = new Extras(
                    false,
                    WeaponSwingSpec.HitShape.AUTO,
                    WeaponSwingSpec.BlockImpact.NONE,
                    Optional.empty(),
                    0
            );
            public static final MapCodec<Extras> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                    Codec.BOOL.optionalFieldOf("allow_empty_hand", false).forGetter(Extras::allowEmptyHand),
                    WeaponSwingSpec.HitShape.CODEC.optionalFieldOf("hit_shape", WeaponSwingSpec.HitShape.AUTO)
                            .forGetter(Extras::hitShape),
                    WeaponSwingSpec.BlockImpact.CODEC.optionalFieldOf("block_impact", WeaponSwingSpec.BlockImpact.NONE)
                            .forGetter(Extras::blockImpact),
                    PassiveHitEffect.CODEC.optionalFieldOf("hit_effect").forGetter(Extras::hitEffect),
                    Codec.INT.optionalFieldOf("priority", 0).forGetter(Extras::priority)
            ).apply(instance, Extras::new));

            public Extras {
                hitShape = hitShape == null ? WeaponSwingSpec.HitShape.AUTO : hitShape;
                blockImpact = blockImpact == null ? WeaponSwingSpec.BlockImpact.NONE : blockImpact;
                hitEffect = hitEffect == null ? Optional.empty() : hitEffect;
            }
        }

        public record PassiveHitEffect(String definition, int duration, double potency) {
            public static final Codec<PassiveHitEffect> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    Codec.STRING.fieldOf("definition").forGetter(PassiveHitEffect::definition),
                    Codec.intRange(1, 72000).optionalFieldOf("duration", 20).forGetter(PassiveHitEffect::duration),
                    Codec.DOUBLE.optionalFieldOf("potency", 1.0D).forGetter(PassiveHitEffect::potency)
            ).apply(instance, PassiveHitEffect::new));

            public PassiveHitEffect {
                definition = definition == null ? "" : definition;
                duration = Math.clamp(duration, 1, 72000);
                potency = Double.isFinite(potency) ? Math.max(0.0D, potency) : 0.0D;
            }
        }
    }

    /** Realm-scaled bonus for the underlying weapon hit, arrow, or trident. */
    public record WeaponDamage(
            Identifier path,
            Optional<Identifier> weaponTag,
            Match match,
            WeaponSwing.RealmScaling realmScaling
    ) implements PassiveModule {
        public static final MapCodec<WeaponDamage> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Identifier.CODEC.fieldOf("path").forGetter(WeaponDamage::path),
                Identifier.CODEC.optionalFieldOf("weapon_tag").forGetter(WeaponDamage::weaponTag),
                Match.CODEC.optionalFieldOf("match", Match.HELD_WEAPON).forGetter(WeaponDamage::match),
                WeaponSwing.RealmScaling.CODEC.optionalFieldOf(
                        "realm_scaling",
                        WeaponSwing.RealmScaling.DEFAULT
                ).forGetter(WeaponDamage::realmScaling)
        ).apply(instance, WeaponDamage::new));

        public WeaponDamage {
            weaponTag = weaponTag == null ? Optional.empty() : weaponTag;
            match = match == null ? Match.HELD_WEAPON : match;
            realmScaling = realmScaling == null ? WeaponSwing.RealmScaling.DEFAULT : realmScaling;
        }

        @Override
        public CodecType<PassiveModule> getType() {
            return WEAPON_DAMAGE.get();
        }

        public enum Match implements StringRepresentable {
            HELD_WEAPON("held_weapon"),
            EMPTY_HAND_OR_TAG("empty_hand_or_tag"),
            ARROW("arrow"),
            TRIDENT("trident");

            public static final Codec<Match> CODEC = StringRepresentable.fromEnum(Match::values);
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
