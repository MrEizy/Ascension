package net.zic.ascension.impl.core.progression;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.zic.ascension.api.ascension.core.path.PathInstance;
import net.zic.ascension.api.ascension.core.progression.*;
import net.zic.ascension.api.ascension.datapack.progresison.ProgressActionType;
import net.zic.ascension.api.rpg_engine.source.OriginSource;
import net.zic.ascension.configuration.ConfigurationRegistries;
import net.zic.ascension.configuration.cultivation.RealmStatScaling;
import net.zic.ascension.impl.datapack.progression.AscensionProgressActionTypes;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public record GiveScaledRealmStatsAction(UUID uuid, Identifier id, Either<Identifier, RealmStatScaling> scaling,
                                         Map<Identifier, Double> stats) implements ProgressAction {
    public static final MapCodec<GiveScaledRealmStatsAction> CODEC = RecordCodecBuilder.<GiveScaledRealmStatsAction>mapCodec(instance -> instance.group(
            Identifier.CODEC.optionalFieldOf("id", Identifier.parse("ascension:default")).forGetter(GiveScaledRealmStatsAction::id),
            Codec.either(Identifier.CODEC, RealmStatScaling.CODEC).fieldOf("scaling").forGetter(GiveScaledRealmStatsAction::scaling),
            Codec.unboundedMap(Identifier.CODEC, Codec.DOUBLE.validate(value -> Double.isFinite(value)
                    ? DataResult.success(value) : DataResult.error(() -> "Realm base stat must be finite"))).fieldOf("stats").forGetter(GiveScaledRealmStatsAction::stats)
    ).apply(instance, (id, scaling, stats) -> new GiveScaledRealmStatsAction(UUID.randomUUID(), id, scaling, Map.copyOf(stats))));

    @Override public UUID getUniqueId() { return uuid; }
    @Override public ProgressActionType getType() { return AscensionProgressActionTypes.GIVE_SCALED_REALM_STATS.get(); }

    @Override
    public void run(UUID holderId, OriginSource source, Identifier contextIdentifier, Object contextData,
                    ProgressDirection direction, String salt) {
        if (!(contextData instanceof PathInstance<?> path)) return;
        int major = path.getCurrentMajorRealm();
        int minor = path.getCurrentMinorRealm();
        int max = major < 0 ? 0 : path.getPath().getMaxMinorRealm(major);
        RealmStatScaling profile = ScalingProfiles.resolve(source.getRegistryAccess(), ConfigurationRegistries.REALM_STAT_SCALING_REGISTRY, scaling);
        stats.forEach((stat, base) -> ScaledStatModifiers.setFlat(source, stat,
                ScaledStatModifiers.id("realm", contextIdentifier, id, stat, "flat"), profile.at(base, major, minor, max)));
        source.updateEntityStatHolder();
    }

    @Override
    public List<ProgressActionDescription> getDescriptions(RegistryAccess access) {
        return stats.entrySet().stream().map(entry -> ProgressActionDescription.numeric(
                ProgressionDescriptionUtil.statName(entry.getKey()).copy().append(" (total at first Low)"),
                Component.literal(ProgressionDescriptionUtil.signedNumber(entry.getValue())), entry.getValue())).toList();
    }
}
