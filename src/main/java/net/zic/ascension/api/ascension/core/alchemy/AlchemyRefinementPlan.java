package net.zic.ascension.api.ascension.core.alchemy;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Registry;
import net.zic.ascension.api.ascension.core.CoreRegistries;
import net.zic.ascension.api.ascension.core.technique.Technique;
import net.zic.ascension.impl.core.technique.AlchemyTechnique;
import net.minecraft.world.entity.player.Player;
import net.zic.ascension.api.ascension.core.source.AscensionOriginSourceHelper;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.zic.ascension.impl.core.technique.AlchemyTechnique.Stage;
import net.zic.ascension.api.ascension.core.alchemy.medicinal.MedicinalEffectDefinition;
import net.zic.ascension.api.ascension.core.alchemy.medicinal.MedicinalRegistries;
import org.jspecify.annotations.Nullable;

import java.util.Comparator;
import java.util.List;

public record AlchemyRefinementPlan(Identifier extraction, Identifier heating, Identifier stabilization,
                                    Identifier condensation, @Nullable Identifier focus) {
    public static final AlchemyRefinementPlan DEFAULT = new AlchemyRefinementPlan(
            Identifier.fromNamespaceAndPath("ascension", "balanced_extraction"),
            Identifier.fromNamespaceAndPath("ascension", "steady_heating"),
            Identifier.fromNamespaceAndPath("ascension", "balanced_stabilization"),
            Identifier.fromNamespaceAndPath("ascension", "conservative_condensation"), null);

    public static final Codec<AlchemyRefinementPlan> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Identifier.CODEC.optionalFieldOf("extraction", DEFAULT.extraction()).forGetter(AlchemyRefinementPlan::extraction),
            Identifier.CODEC.optionalFieldOf("heating", DEFAULT.heating()).forGetter(AlchemyRefinementPlan::heating),
            Identifier.CODEC.optionalFieldOf("stabilization", DEFAULT.stabilization()).forGetter(AlchemyRefinementPlan::stabilization),
            Identifier.CODEC.optionalFieldOf("condensation", DEFAULT.condensation()).forGetter(AlchemyRefinementPlan::condensation),
            Identifier.CODEC.optionalFieldOf("focus").forGetter(plan -> java.util.Optional.ofNullable(plan.focus()))
    ).apply(instance, (extraction, heating, stabilization, condensation, focus) ->
            new AlchemyRefinementPlan(extraction, heating, stabilization, condensation, focus.orElse(null))));

    public AlchemyRefinementPlan {
        if (extraction == null || heating == null || stabilization == null || condensation == null)
            throw new IllegalArgumentException("Refinement plan requires all four stages");
    }

    public Identifier selected(Stage stage) {
        return switch (stage) {
            case EXTRACTION -> extraction;
            case HEATING -> heating;
            case STABILIZATION -> stabilization;
            case CONDENSATION -> condensation;
        };
    }

    public AlchemyRefinementPlan with(Stage stage, Identifier id) {
        return switch (stage) {
            case EXTRACTION -> new AlchemyRefinementPlan(id, heating, stabilization, condensation, focus);
            case HEATING -> new AlchemyRefinementPlan(extraction, id, stabilization, condensation, focus);
            case STABILIZATION -> new AlchemyRefinementPlan(extraction, heating, id, condensation, focus);
            case CONDENSATION -> new AlchemyRefinementPlan(extraction, heating, stabilization, id, focus);
        };
    }

    public AlchemyRefinementPlan withFocus(@Nullable Identifier effect) {
        return new AlchemyRefinementPlan(extraction, heating, stabilization, condensation, effect);
    }

    public static List<Identifier> choices(RegistryAccess access, Stage stage) {
        if (access == null) return List.of();
        Registry<Technique<?>> registry = CoreRegistries.TECHNIQUE_REGISTRY.get(access);
        return registry.entrySet().stream().filter(entry -> entry.getValue() instanceof AlchemyTechnique technique && technique.stage() == stage)
                .map(entry -> entry.getKey().identifier()).sorted(Comparator.comparing(Identifier::toString)).toList();
    }

    public static List<Identifier> focusChoices(RegistryAccess access) {
        if (access == null) return List.of();
        Registry<MedicinalEffectDefinition> registry = MedicinalRegistries.EFFECTS.get(access);
        return registry.entrySet().stream().filter(entry -> !entry.getValue().adverse())
                .map(entry -> entry.getKey().identifier()).sorted(Comparator.comparing(Identifier::toString)).toList();
    }

    public static boolean isLearnedOrBasic(RegistryAccess access, Player player, Identifier id) {
        Technique<?> technique = CoreRegistries.TECHNIQUE_REGISTRY.get(access).getValue(id);
        if (!(technique instanceof AlchemyTechnique alchemy)) return false;
        if (alchemy.basic()) return true;
        return AscensionOriginSourceHelper.hasTechnique(AscensionOriginSourceHelper.getEntitySource(player), id);
    }

    public int index(RegistryAccess access, Stage stage) {
        return Math.max(0, choices(access, stage).indexOf(selected(stage)));
    }

    public int focusIndex(RegistryAccess access) {
        return focus == null ? 0 : Math.max(0, focusChoices(access).indexOf(focus) + 1);
    }

    public AlchemyRefinementPlan indexed(RegistryAccess access, Stage stage, int index) {
        List<Identifier> options = choices(access, stage);
        return options.isEmpty() ? this : with(stage, options.get(Mth.clamp(index, 0, options.size() - 1)));
    }

    public AlchemyRefinementPlan indexedFocus(RegistryAccess access, int index) {
        List<Identifier> options = focusChoices(access);
        return withFocus(index <= 0 || options.isEmpty() ? null : options.get(Mth.clamp(index - 1, 0, options.size() - 1)));
    }
}
