package net.zic.ascension.api.ascension.core.damage;

import net.minecraft.resources.Identifier;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.rpg_engine.damage.RPGEngineDamageSource;
import net.zic.ascension.api.rpg_engine.damage.RPGEngineDamageTypeHolder;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public final class AscensionDamageTypeHolders {
    public static final Identifier PATH = AscensionCraft.prefix("damage_container");
    public static final Identifier CLASSIFICATIONS = AscensionCraft.prefix("damage_classifications");
    public static final Identifier ATTRIBUTION = AscensionCraft.prefix("damage_attribution");
    public static final Identifier PROFILE = AscensionCraft.prefix("damage_profile");

    private AscensionDamageTypeHolders() {
    }

    public static void attachPath(RPGEngineDamageSource source, Identifier path) {
        if (source != null && path != null) {
            source.addDamageTypeHolder(PATH, new Path(path));
        }
    }

    public static void attachClassifications(RPGEngineDamageSource source, Set<Identifier> classifications) {
        if (source == null || classifications == null || classifications.isEmpty()) {
            return;
        }

        LinkedHashSet<Identifier> merged = new LinkedHashSet<>();
        if (source.getDamageTypeHolder(CLASSIFICATIONS) instanceof Classifications existing) {
            merged.addAll(existing.values());
        }
        merged.addAll(classifications);
        source.addDamageTypeHolder(CLASSIFICATIONS, new Classifications(merged));
    }

    public static void attachAttribution(RPGEngineDamageSource source, Attribution attribution) {
        if (source != null && attribution != null) {
            source.addDamageTypeHolder(ATTRIBUTION, attribution);
        }
    }

    public static void attachProfile(RPGEngineDamageSource source, AscensionDamageProfile profile) {
        if (source != null && profile != null) {
            source.addDamageTypeHolder(PROFILE, profile);
        }
    }

    public record Path(Identifier path) implements RPGEngineDamageTypeHolder {
    }

    public record Classifications(Set<Identifier> values) implements RPGEngineDamageTypeHolder {
        public Classifications {
            values = values == null ? Set.of() : Set.copyOf(values);
        }

        public boolean contains(Identifier classification) {
            return classification != null && values.contains(classification);
        }
    }

    public record Attribution(UUID owner, UUID caster, Identifier skill, Optional<Identifier> technique, Optional<Identifier> projectileDefinition, Optional<UUID> projectileRuntime) implements RPGEngineDamageTypeHolder {
        public Attribution {
            technique = technique == null ? Optional.empty() : technique;
            projectileDefinition = projectileDefinition == null ? Optional.empty() : projectileDefinition;
            projectileRuntime = projectileRuntime == null ? Optional.empty() : projectileRuntime;
        }
    }
}
