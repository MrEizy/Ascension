package net.zic.ascension.impl.core.technique;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import net.zic.ascension.api.ascension.core.path.realm.Realm;
import net.zic.ascension.api.ascension.core.progression.ProgressDirection;
import net.zic.ascension.api.rpg_engine.source.OriginSource;
import net.zic.ascension.impl.core.progression.GiveStatsAction;
import net.zic.ascension.impl.core.progression.ProgressionModifiers;
import net.zic.ascension.impl.core.progression.modifier.ModifierConflictAction;
import net.zic.zenithlib.value_containers.typed.ModifierHolder;
import net.zic.zenithlib.value_containers.typed.ValueContainerCodecHelper;

import java.util.Map;
import java.util.function.UnaryOperator;

public record TechniqueRealmStatBonus(Identifier path, Realm realm, Map<Identifier, ModifierHolder<Double>> stats) {
    public static final Codec<TechniqueRealmStatBonus> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Identifier.CODEC.fieldOf("path").forGetter(TechniqueRealmStatBonus::path),
            Realm.CODEC.fieldOf("realm").forGetter(TechniqueRealmStatBonus::realm),
            ValueContainerCodecHelper.containersCodec(Codec.DOUBLE).fieldOf("stats").forGetter(TechniqueRealmStatBonus::stats)
    ).apply(instance, TechniqueRealmStatBonus::new));

    public TechniqueRealmStatBonus {
        stats = Map.copyOf(stats);
    }

    public void apply(OriginSource source, Identifier techniqueId, ProgressDirection direction) {
        String salt = techniqueId.toString() + "/" + path + "/" + realm.asSalt();
        ProgressionModifiers.applyStats(source, stats, ModifierConflictAction.SALT,
                UnaryOperator.identity(), direction.value, salt);
    }

    public GiveStatsAction descriptionAction() {
        return GiveStatsAction.from(stats, ModifierConflictAction.SALT);
    }
}
