package net.zic.ascension.impl.core.evolution;

import com.mojang.serialization.Codec;
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
 *   <li>catalyst: the bloodline / physique item that has to be used to trigger it, one id or a list (required)</li>
 *   <li>purity (bloodline only, 1-100): the purity of the bloodline the player already has. The purity of the catalyst item does not matter</li>
 *   <li>result_purity (bloodline only, 1-100): the purity the evolved bloodline starts with, default 1</li>
 *   <li>realm: {path, major_realm, minor_realm}, the player must be at least that realm on that path</li>
 *   <li>path: the player must have this path, one id or a list (any of them)</li>
 * </ul>
 * Everything except into and catalyst is optional, and every requirement that is set has to be met.
 */
public record EvolutionRequirements(Identifier into, List<Identifier> catalysts, int minPurity, int resultPurity,
                                    Optional<RealmRequirement> realm, List<Identifier> paths) {

    public static final Codec<EvolutionRequirements> BLOODLINE_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Identifier.CODEC.fieldOf("into").forGetter(EvolutionRequirements::into),
            ConnectionRequirements.IDS.fieldOf("catalyst").forGetter(EvolutionRequirements::catalysts),
            Codec.intRange(1, 100).optionalFieldOf("purity", 0).forGetter(EvolutionRequirements::minPurity),
            Codec.intRange(1, 100).optionalFieldOf("result_purity", 1).forGetter(EvolutionRequirements::resultPurity),
            RealmRequirement.CODEC.optionalFieldOf("realm").forGetter(EvolutionRequirements::realm),
            ConnectionRequirements.IDS.optionalFieldOf("path", List.of()).forGetter(EvolutionRequirements::paths)
    ).apply(instance, EvolutionRequirements::new));

    public static final Codec<EvolutionRequirements> PHYSIQUE_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Identifier.CODEC.fieldOf("into").forGetter(EvolutionRequirements::into),
            ConnectionRequirements.IDS.fieldOf("catalyst").forGetter(EvolutionRequirements::catalysts),
            RealmRequirement.CODEC.optionalFieldOf("realm").forGetter(EvolutionRequirements::realm),
            ConnectionRequirements.IDS.optionalFieldOf("path", List.of()).forGetter(EvolutionRequirements::paths)
    ).apply(instance, (into, catalysts, realm, paths) -> new EvolutionRequirements(into, catalysts, 0, 1, realm, paths)));

    public EvolutionRequirements {
        catalysts = catalysts == null ? List.of() : List.copyOf(catalysts);
        realm = realm == null ? Optional.empty() : realm;
        paths = paths == null ? List.of() : List.copyOf(paths);
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