package net.zic.ascension.impl.core.technique;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.util.Mth;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.zic.ascension.api.ascension.core.requirement.RequirementHolder;
import net.zic.ascension.api.ascension.core.technique.Technique;
import net.zic.ascension.api.ascension.core.technique.TechniqueData;
import net.zic.ascension.api.ascension.core.technique.TechniqueInstance;
import net.zic.ascension.api.ascension.datapack.technique.TechniqueType;
import net.zic.ascension.api.rpg_engine.source.OriginSource;
import net.zic.ascension.api.rpg_engine.source.data_source.util.SerializerHandler;
import net.zic.ascension.api.rpg_engine.source.data_source.util.SyncHandler;
import net.zic.ascension.api.tooltip.AscensionItemTooltipDefinition;
import net.zic.ascension.impl.datapack.technique.AscensionTechniqueTypes;
import org.jspecify.annotations.Nullable;

import java.util.Locale;
import java.util.Optional;

public record AlchemyTechnique(Component name, Component description, Refinement refinement, boolean basic,
                               Optional<AscensionItemTooltipDefinition> itemTooltip,
                               RequirementHolder requirements) implements Technique<AlchemyTechnique.Data> {
    private static final SerializerHandler<Data> SERIALIZER = new SerializerHandler<>() {
        @Override public Data read(ValueInput input, RegistryAccess access) { return new Data(); }
        @Override public void write(Data data, ValueOutput output, RegistryAccess access) { }
    };
    private static final SyncHandler<Data> SYNC = new SyncHandler.UnitSyncHandler<>();

    public static final MapCodec<AlchemyTechnique> CODEC = RecordCodecBuilder.<AlchemyTechnique>mapCodec(instance -> instance.group(
            ComponentSerialization.CODEC.fieldOf("name").forGetter(AlchemyTechnique::name),
            ComponentSerialization.CODEC.fieldOf("description").forGetter(AlchemyTechnique::description),
            Refinement.CODEC.fieldOf("refinement").forGetter(AlchemyTechnique::refinement),
            Codec.BOOL.optionalFieldOf("basic", false).forGetter(AlchemyTechnique::basic),
            AscensionItemTooltipDefinition.CODEC.optionalFieldOf("item_tooltip").forGetter(AlchemyTechnique::itemTooltip),
            RequirementHolder.CODEC.optionalFieldOf("requirements", RequirementHolder.EMPTY).forGetter(AlchemyTechnique::requirements)
    ).apply(instance, AlchemyTechnique::new));

    public AlchemyTechnique {
        itemTooltip = itemTooltip == null ? Optional.empty() : itemTooltip;
        requirements = requirements == null ? RequirementHolder.EMPTY : requirements;
    }

    @Override public TechniqueType getType() { return AscensionTechniqueTypes.ALCHEMY_TECHNIQUE_TYPE.get(); }
    @Override public Component getName(@Nullable Data data) { return name; }
    @Override public Component getDescription(@Nullable Data data) { return description; }
    @Override public void onAdded(OriginSource source, TechniqueInstance<Data> instance) { }
    @Override public void onRemoved(OriginSource source, TechniqueInstance<Data> instance) { }
    @Override public Data newData(RegistryAccess access) { return new Data(); }
    @Override public SerializerHandler<Data> serializerHandler() { return SERIALIZER; }
    @Override public SyncHandler<Data> syncHandler() { return SYNC; }

    public Stage stage() { return refinement.stage(); }

    public static final class Data implements TechniqueData { }

    public enum Stage {
        EXTRACTION, HEATING, STABILIZATION, CONDENSATION;

        public static final Codec<Stage> CODEC = Codec.STRING.comapFlatMap(name -> {
            try { return DataResult.success(Stage.valueOf(name.toUpperCase(Locale.ROOT))); }
            catch (IllegalArgumentException exception) {
                return DataResult.error(() -> "Unknown alchemy technique stage: " + name);
            }
        }, stage -> stage.name().toLowerCase(Locale.ROOT));
    }

    public record Refinement(Stage stage, double heat, double control, double yield,
                             double purityDelta, double instabilityDelta, double toxicityDelta,
                             double safeEnergyBonus, double explosionEnergyBonus,
                             double qualityBonus, double focusPrecision) {
        public static final Codec<Refinement> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Stage.CODEC.fieldOf("stage").forGetter(Refinement::stage),
                Codec.DOUBLE.optionalFieldOf("heat", 1.0D).forGetter(Refinement::heat),
                Codec.DOUBLE.optionalFieldOf("control", 2.5D).forGetter(Refinement::control),
                Codec.DOUBLE.optionalFieldOf("yield", 1.0D).forGetter(Refinement::yield),
                Codec.DOUBLE.optionalFieldOf("purity_delta", 0.0D).forGetter(Refinement::purityDelta),
                Codec.DOUBLE.optionalFieldOf("instability_delta", 0.0D).forGetter(Refinement::instabilityDelta),
                Codec.DOUBLE.optionalFieldOf("toxicity_delta", 0.0D).forGetter(Refinement::toxicityDelta),
                Codec.DOUBLE.optionalFieldOf("safe_energy_bonus", 0.0D).forGetter(Refinement::safeEnergyBonus),
                Codec.DOUBLE.optionalFieldOf("explosion_energy_bonus", 0.0D).forGetter(Refinement::explosionEnergyBonus),
                Codec.DOUBLE.optionalFieldOf("quality_bonus", 0.0D).forGetter(Refinement::qualityBonus),
                Codec.DOUBLE.optionalFieldOf("focus_precision", 0.0D).forGetter(Refinement::focusPrecision)
        ).apply(instance, Refinement::new));

        public Refinement {
            if (stage == null) throw new IllegalArgumentException("Missing alchemy refinement stage");
            heat = bounded(heat, 0.1D, 4.0D, 1.0D);
            control = bounded(control, 0.1D, 8.0D, 2.5D);
            yield = bounded(yield, 0.25D, 2.0D, 1.0D);
            purityDelta = bounded(purityDelta, -0.25D, 0.25D, 0.0D);
            instabilityDelta = bounded(instabilityDelta, -1.0D, 1.0D, 0.0D);
            toxicityDelta = bounded(toxicityDelta, -1.0D, 1.0D, 0.0D);
            safeEnergyBonus = bounded(safeEnergyBonus, -2.5D, 4.0D, 0.0D);
            explosionEnergyBonus = bounded(explosionEnergyBonus, -3.0D, 5.0D, 0.0D);
            qualityBonus = bounded(qualityBonus, -0.15D, 0.15D, 0.0D);
            focusPrecision = bounded(focusPrecision, 0.0D, 0.4D, 0.0D);
        }

        private static double bounded(double value, double min, double max, double fallback) {
            return Double.isFinite(value) ? Mth.clamp(value, min, max) : fallback;
        }

        public static Refinement neutral(Stage stage) {
            return new Refinement(stage, 1.0D, 2.5D, 1.0D, 0.0D, 0.0D, 0.0D,
                    0.0D, 0.0D, 0.0D, 0.0D);
        }
    }
}
