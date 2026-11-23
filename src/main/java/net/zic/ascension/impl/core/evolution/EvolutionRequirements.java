package net.zic.ascension.impl.core.evolution;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.zic.ascension.api.ascension.core.source.AscensionOriginSourceHelper;
import net.zic.ascension.api.rpg_engine.source.OriginSource;
import net.zic.ascension.impl.core.connection.ConnectionRequirements;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Optional;

/**
 * Everything an evolution needs. The "evolution" object of an evolution_bloodline / evolution_physique:
 * <ul>
 *   <li>into: the definition it evolves into (required)</li>
 *   <li>catalyst: the bloodline / physique item that has to be right clicked to trigger it, one id or a list</li>
 *   <li>absorb_catalyst: an item or block id that has to be absorbed with an absorption_cultivation skill that lists it with
 *       evolution_type, one id or a list</li>
 *   <li>at least one of catalyst and absorb_catalyst is required, both can be set and either one triggers the evolution</li>
 *   <li>purity (bloodline only, 1-100): the purity of the bloodline the player already has. The purity of the catalyst item does not matter</li>
 *   <li>result_purity (bloodline only, 1-100): the purity the evolved bloodline starts with, default 1</li>
 *   <li>realm: {path, major_realm, minor_realm}, the player must be at least that realm on that path</li>
 *   <li>path: the player must have this path, one id or a list (any of them)</li>
 * </ul>
 * Every requirement that is set has to be met.
 */
public record EvolutionRequirements(Identifier into, List<Identifier> catalysts, List<Identifier> absorbCatalysts, int minPurity,
                                    int resultPurity, Optional<RealmRequirement> realm, List<Identifier> paths) {

    public static final Codec<EvolutionRequirements> BLOODLINE_CODEC = RecordCodecBuilder.<EvolutionRequirements>create(instance -> instance.group(
            Identifier.CODEC.fieldOf("into").forGetter(EvolutionRequirements::into),
            ConnectionRequirements.IDS.optionalFieldOf("catalyst", List.of()).forGetter(EvolutionRequirements::catalysts),
            ConnectionRequirements.IDS.optionalFieldOf("absorb_catalyst", List.of()).forGetter(EvolutionRequirements::absorbCatalysts),
            Codec.intRange(1, 100).optionalFieldOf("purity", 0).forGetter(EvolutionRequirements::minPurity),
            Codec.intRange(1, 100).optionalFieldOf("result_purity", 1).forGetter(EvolutionRequirements::resultPurity),
            RealmRequirement.CODEC.optionalFieldOf("realm").forGetter(EvolutionRequirements::realm),
            ConnectionRequirements.IDS.optionalFieldOf("path", List.of()).forGetter(EvolutionRequirements::paths)
    ).apply(instance, EvolutionRequirements::new)).flatXmap(EvolutionRequirements::needsCatalyst, DataResult::success);

    public static final Codec<EvolutionRequirements> PHYSIQUE_CODEC = RecordCodecBuilder.<EvolutionRequirements>create(instance -> instance.group(
            Identifier.CODEC.fieldOf("into").forGetter(EvolutionRequirements::into),
            ConnectionRequirements.IDS.optionalFieldOf("catalyst", List.of()).forGetter(EvolutionRequirements::catalysts),
            ConnectionRequirements.IDS.optionalFieldOf("absorb_catalyst", List.of()).forGetter(EvolutionRequirements::absorbCatalysts),
            RealmRequirement.CODEC.optionalFieldOf("realm").forGetter(EvolutionRequirements::realm),
            ConnectionRequirements.IDS.optionalFieldOf("path", List.of()).forGetter(EvolutionRequirements::paths)
    ).apply(instance, (into, catalysts, absorbCatalysts, realm, paths) ->
            new EvolutionRequirements(into, catalysts, absorbCatalysts, 0, 1, realm, paths))).flatXmap(EvolutionRequirements::needsCatalyst, DataResult::success);

    public EvolutionRequirements {
        catalysts = catalysts == null ? List.of() : List.copyOf(catalysts);
        absorbCatalysts = absorbCatalysts == null ? List.of() : List.copyOf(absorbCatalysts);
        realm = realm == null ? Optional.empty() : realm;
        paths = paths == null ? List.of() : List.copyOf(paths);
    }

    private static DataResult<EvolutionRequirements> needsCatalyst(EvolutionRequirements evolution) {
        return evolution.catalysts().isEmpty() && evolution.absorbCatalysts().isEmpty()
                ? DataResult.error(() -> "An evolution needs a catalyst or an absorb_catalyst")
                : DataResult.success(evolution);
    }

    /**
     * null if everything is met, otherwise a message naming the first requirement that is not.
     *
     * @param heldPurity the purity of the bloodline the player already has and is evolving, ignored for physiques
     */
    public @Nullable Component failureMessage(OriginSource source, RegistryAccess access, int heldPurity) {
        if (minPurity > 0 && heldPurity < minPurity) {
            return Component.translatable("ascension.evolution.requires.purity", minPurity, heldPurity);
        }
        if (!paths.isEmpty() && paths.stream().noneMatch(id -> AscensionOriginSourceHelper.hasPath(source, id))) {
            MutableComponent names = Component.empty();
            for (int i = 0; i < paths.size(); i++) {
                if (i > 0) {
                    names.append(" / ");
                }
                names.append(ConnectionRequirements.pathName(paths.get(i), access));
            }
            return Component.translatable("ascension.evolution.requires.path", names);
        }
        if (realm.isPresent() && !realm.get().test(source)) {
            RealmRequirement required = realm.get();
            return Component.translatable("ascension.evolution.requires.realm",
                    ConnectionRequirements.pathName(required.path(), access), required.majorRealm(), required.minorRealm());
        }
        return null;
    }
}