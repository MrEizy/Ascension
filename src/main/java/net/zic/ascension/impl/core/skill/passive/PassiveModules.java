package net.zic.ascension.impl.core.skill.passive;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
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
import net.zic.ascension.api.ascension.value.ScaledValue;
import net.zic.ascension.api.rpg_engine.source.OriginSource;
import net.zic.ascension.api.ascension.core.source.AscensionOriginSourceHelper;
import net.zic.zenithlib.common.ZenithRegistries;
import net.zic.zenithlib.registry.RegistryHelper;
import net.zic.zenithlib.stats.Stat;
import net.zic.zenithlib.value_containers.ValueContainer;
import net.zic.zenithlib.value_containers.ValueContainerModifier;

import java.util.List;
import java.util.Map;

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
}
