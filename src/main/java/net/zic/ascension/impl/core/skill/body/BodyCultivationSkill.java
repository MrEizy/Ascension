package net.zic.ascension.impl.core.skill.body;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.zic.ascension.api.ascension.core.resource.ResourceTransactionRequest;
import net.zic.ascension.api.ascension.core.skill.Skill;
import net.zic.ascension.api.ascension.core.skill.SkillData;
import net.zic.ascension.api.ascension.datapack.skill.SkillType;
import net.zic.ascension.api.ascension.value.ScaledValue;
import net.zic.ascension.api.rpg_engine.source.OriginSource;
import net.zic.ascension.impl.datapack.skill.AscensionSkillTypes;

import java.util.List;
import java.util.Optional;

public final class BodyCultivationSkill implements Skill {
    private final Component name;
    private final Component description;
    private final Identifier path;
    private final int priority;
    private final List<Stimulus> stimuli;
    private final ScaledValue maximumTempering;
    private final Conversion conversion;
    private final List<Multiplier> multipliers;

    public BodyCultivationSkill(
            Component name,
            Component description,
            Identifier path,
            int priority,
            List<Stimulus> stimuli,
            ScaledValue maximumTempering,
            Conversion conversion,
            List<Multiplier> multipliers
    ) {
        this.name = name;
        this.description = description;
        this.path = path;
        this.priority = priority;
        this.stimuli = stimuli == null ? List.of() : List.copyOf(stimuli);
        this.maximumTempering = maximumTempering == null
                ? ScaledValue.constant(100.0D)
                : maximumTempering;
        this.conversion = conversion == null ? Conversion.DEFAULT : conversion;
        this.multipliers = multipliers == null ? List.of() : List.copyOf(multipliers);
    }

    public Identifier path() {
        return path;
    }

    public int priority() {
        return priority;
    }

    public List<Stimulus> stimuli() {
        return stimuli;
    }

    public ScaledValue maximumTempering() {
        return maximumTempering;
    }

    public Conversion conversion() {
        return conversion;
    }

    public List<Multiplier> multipliers() {
        return multipliers;
    }

    @Override
    public SkillType getType() {
        return AscensionSkillTypes.BODY_CULTIVATION_SKILL_TYPE.get();
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
        return new Data(0.0D);
    }

    @Override
    public SkillData loadData(ValueInput input, RegistryAccess access) {
        return new Data(input.getDoubleOr("tempering", 0.0D));
    }

    @Override
    public SkillData loadData(ByteBuf buf) {
        return new Data(buf.readDouble());
    }

    public record Stimulus(
            ResourceTransactionRequest.SourceSelector source,
            ScaledValue gain
    ) {
        public static final Codec<Stimulus> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ResourceTransactionRequest.SourceSelector.CODEC.codec().optionalFieldOf("source", ResourceTransactionRequest.SourceSelector.any()).forGetter(Stimulus::source),
                ScaledValue.COMPACT_CODEC.optionalFieldOf("gain", ScaledValue.constant(1.0D)).forGetter(Stimulus::gain)
        ).apply(instance, Stimulus::new));

        public Stimulus {
            source = source == null ? ResourceTransactionRequest.SourceSelector.any() : source;
            gain = gain == null ? ScaledValue.constant(1.0D) : gain;
        }
    }

    public record Multiplier(
            ResourceTransactionRequest.SourceSelector source,
            StateCondition condition,
            ScaledValue multiplier
    ) {
        public static final Codec<Multiplier> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ResourceTransactionRequest.SourceSelector.CODEC.codec().optionalFieldOf("source", ResourceTransactionRequest.SourceSelector.any()).forGetter(Multiplier::source),
                StateCondition.CODEC.optionalFieldOf("condition", StateCondition.ANY).forGetter(Multiplier::condition),
                ScaledValue.COMPACT_CODEC.optionalFieldOf("multiplier", ScaledValue.constant(1.0D)).forGetter(Multiplier::multiplier)
        ).apply(instance, Multiplier::new));

        public Multiplier {
            source = source == null ? ResourceTransactionRequest.SourceSelector.any() : source;
            condition = condition == null ? StateCondition.ANY : condition;
            multiplier = multiplier == null ? ScaledValue.constant(1.0D) : multiplier;
        }
    }

    public record StateCondition(
            boolean unarmored,
            Optional<Integer> minimumArmorPieces,
            Optional<Integer> maximumArmorPieces,
            Optional<Identifier> effect,
            int minimumEffectAmplifier,
            Optional<Double> minimumHealthFraction,
            Optional<Double> maximumHealthFraction,
            Optional<Boolean> onFire,
            Optional<Boolean> inWater,
            Optional<Boolean> sprinting,
            Optional<Boolean> swimming,
            boolean negate
    ) {
        public static final StateCondition ANY = new StateCondition(
                false,
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                0,
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                false
        );

        public static final Codec<StateCondition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.BOOL.optionalFieldOf("unarmored", false).forGetter(StateCondition::unarmored),
                Codec.intRange(0, 4).optionalFieldOf("minimum_armor_pieces").forGetter(StateCondition::minimumArmorPieces),
                Codec.intRange(0, 4).optionalFieldOf("maximum_armor_pieces").forGetter(StateCondition::maximumArmorPieces),
                Identifier.CODEC.optionalFieldOf("effect").forGetter(StateCondition::effect),
                Codec.intRange(0, 255).optionalFieldOf("minimum_effect_amplifier", 0).forGetter(StateCondition::minimumEffectAmplifier),
                Codec.doubleRange(0.0D, 1.0D).optionalFieldOf("minimum_health_fraction").forGetter(StateCondition::minimumHealthFraction),
                Codec.doubleRange(0.0D, 1.0D).optionalFieldOf("maximum_health_fraction").forGetter(StateCondition::maximumHealthFraction),
                Codec.BOOL.optionalFieldOf("on_fire").forGetter(StateCondition::onFire),
                Codec.BOOL.optionalFieldOf("in_water").forGetter(StateCondition::inWater),
                Codec.BOOL.optionalFieldOf("sprinting").forGetter(StateCondition::sprinting),
                Codec.BOOL.optionalFieldOf("swimming").forGetter(StateCondition::swimming),
                Codec.BOOL.optionalFieldOf("negate", false).forGetter(StateCondition::negate)
        ).apply(instance, StateCondition::new));

        public StateCondition {
            minimumArmorPieces = minimumArmorPieces == null ? Optional.empty() : minimumArmorPieces;
            maximumArmorPieces = maximumArmorPieces == null ? Optional.empty() : maximumArmorPieces;
            effect = effect == null ? Optional.empty() : effect;
            minimumEffectAmplifier = Math.max(0, minimumEffectAmplifier);
            minimumHealthFraction = minimumHealthFraction == null ? Optional.empty() : minimumHealthFraction;
            maximumHealthFraction = maximumHealthFraction == null ? Optional.empty() : maximumHealthFraction;
            onFire = onFire == null ? Optional.empty() : onFire;
            inWater = inWater == null ? Optional.empty() : inWater;
            sprinting = sprinting == null ? Optional.empty() : sprinting;
            swimming = swimming == null ? Optional.empty() : swimming;
        }

        public boolean matches(LivingEntity entity) {
            if (entity == null) {
                return false;
            }

            final int armorPieces = countArmorPieces(entity);

            boolean result = (!unarmored || armorPieces == 0)
                    && minimumArmorPieces
                    .map(minimum -> armorPieces >= minimum)
                    .orElse(true)
                    && maximumArmorPieces
                    .map(maximum -> armorPieces <= maximum)
                    .orElse(true)
                    && matchesEffect(entity)
                    && matchesHealth(entity)
                    && onFire
                    .map(expected -> entity.isOnFire() == expected)
                    .orElse(true)
                    && inWater
                    .map(expected -> entity.isInWater() == expected)
                    .orElse(true)
                    && sprinting
                    .map(expected -> entity.isSprinting() == expected)
                    .orElse(true)
                    && swimming
                    .map(expected -> entity.isSwimming() == expected)
                    .orElse(true);

            return negate ? !result : result;
        }

        private static int countArmorPieces(LivingEntity entity) {
            int count = 0;

            if (!entity.getItemBySlot(EquipmentSlot.HEAD).isEmpty()) {
                count++;
            }

            if (!entity.getItemBySlot(EquipmentSlot.CHEST).isEmpty()) {
                count++;
            }

            if (!entity.getItemBySlot(EquipmentSlot.LEGS).isEmpty()) {
                count++;
            }

            if (!entity.getItemBySlot(EquipmentSlot.FEET).isEmpty()) {
                count++;
            }

            return count;
        }

        private boolean matchesEffect(LivingEntity entity) {
            if (effect.isEmpty()) {
                return true;
            }

            Identifier expected = effect.get();

            for (MobEffectInstance instance : entity.getActiveEffects()) {
                boolean matches = instance.getEffect()
                        .unwrapKey()
                        .map(key -> expected.equals(key.identifier()))
                        .orElse(false);

                if (matches && instance.getAmplifier() >= minimumEffectAmplifier) {
                    return true;
                }
            }

            return false;
        }

        private boolean matchesHealth(LivingEntity entity) {
            double maximum = Math.max(
                    1.0E-9D,
                    entity.getMaxHealth()
            );

            double fraction = Math.clamp(
                    entity.getHealth() / maximum,
                    0.0D,
                    1.0D
            );

            return minimumHealthFraction
                    .map(minimum -> fraction >= minimum)
                    .orElse(true)
                    && maximumHealthFraction
                    .map(maximumFraction -> fraction <= maximumFraction)
                    .orElse(true);
        }
    }

    public record Conversion(
            ScaledValue rate,
            ScaledValue progressPerTempering,
            int interval,
            List<ResourceCost> costs
    ) {
        public static final Conversion DEFAULT = new Conversion(
                ScaledValue.constant(1.0D),
                ScaledValue.constant(1.0D),
                1,
                List.of()
        );

        public static final Codec<Conversion> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ScaledValue.COMPACT_CODEC.optionalFieldOf("rate", ScaledValue.constant(1.0D)).forGetter(Conversion::rate),
                ScaledValue.COMPACT_CODEC.optionalFieldOf("progress_per_tempering", ScaledValue.constant(1.0D)).forGetter(Conversion::progressPerTempering),
                Codec.intRange(1, 1200).optionalFieldOf("interval", 1).forGetter(Conversion::interval),
                ResourceCost.CODEC.listOf().fieldOf("costs").forGetter(Conversion::costs)
        ).apply(instance, Conversion::new));

        public Conversion {
            rate = rate == null ? ScaledValue.constant(1.0D) : rate;
            progressPerTempering = progressPerTempering == null ? ScaledValue.constant(1.0D) : progressPerTempering;
            interval = Math.max(1, interval);
            costs = costs == null ? List.of() : List.copyOf(costs);
        }
    }

    public record ResourceCost(Identifier resource, ScaledValue amount) {
        public static final Codec<ResourceCost> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Identifier.CODEC.fieldOf("resource").forGetter(ResourceCost::resource),
                ScaledValue.COMPACT_CODEC.fieldOf("amount").forGetter(ResourceCost::amount)
        ).apply(instance, ResourceCost::new));

        public ResourceCost {
            amount = amount == null ? ScaledValue.constant(0.0D) : amount;
        }
    }

    public static final class Data implements SkillData {
        private double tempering;
        private boolean dirty;

        public Data(double tempering) {
            this.tempering = sanitize(tempering);
        }

        public double tempering() {
            return tempering;
        }

        public void setTempering(double tempering) {
            double resolved = sanitize(tempering);

            if (Math.abs(this.tempering - resolved) <= 1.0E-9D) {
                return;
            }

            this.tempering = resolved;
            this.dirty = true;
        }

        public boolean consumeDirty() {
            boolean value = dirty;
            dirty = false;
            return value;
        }

        @Override
        public SkillType getType() {
            return AscensionSkillTypes.BODY_CULTIVATION_SKILL_TYPE.get();
        }

        @Override
        public void write(ValueOutput output, RegistryAccess access) {
            output.putDouble("tempering", tempering);
        }

        @Override
        public void encode(ByteBuf buf, RegistryAccess access) {
            buf.writeDouble(tempering);
        }

        private static double sanitize(double value) {
            return Double.isFinite(value) ? Math.max(0.0D, value) : 0.0D;
        }
    }
}