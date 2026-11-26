package net.zic.ascension.impl.core.path.realm_change.condition;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.zic.ascension.api.ascension.core.path.PathInstance;
import net.zic.ascension.api.ascension.core.path.realm.realm_change.RealmChangeActionCondition;
import net.zic.ascension.api.ascension.core.progression.ProgressDirection;
import net.zic.ascension.api.ascension.datapack.progresison.ProgressActionConditionType;
import net.zic.ascension.api.rpg_engine.source.OriginSource;
import net.zic.ascension.impl.datapack.progression.AscensionProgressActionConditionTypes;

import java.util.OptionalInt;

public record RealmStageRangeCondition(int major, int minMinor, int maxMinor) implements RealmChangeActionCondition {
    public static final MapCodec<RealmStageRangeCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.intRange(-1, Integer.MAX_VALUE).optionalFieldOf("major", -1).forGetter(RealmStageRangeCondition::major),
            Codec.intRange(0, Integer.MAX_VALUE).fieldOf("min_minor").forGetter(RealmStageRangeCondition::minMinor),
            Codec.intRange(0, Integer.MAX_VALUE).optionalFieldOf("max_minor", Integer.MAX_VALUE).forGetter(RealmStageRangeCondition::maxMinor)
    ).apply(instance, RealmStageRangeCondition::new));

    @Override
    public boolean test(OriginSource source, Identifier path, PathInstance<?> pathInstance,
                        int majorRealm, int minorRealm, ProgressDirection direction) {
        return majorRealm >= 0 && (major == -1 || major == majorRealm)
                && minorRealm >= minMinor && minorRealm <= maxMinor;
    }

    @Override
    public OptionalInt getEarliestMajorRealm() {
        return major < 0 ? OptionalInt.of(0) : OptionalInt.of(major);
    }

    @Override
    public Component getDescription(RegistryAccess access) {
        return Component.translatable("ascension.tooltip.progression.condition.selected_realms");
    }

    @Override
    public ProgressActionConditionType getType() {
        return AscensionProgressActionConditionTypes.REALM_STAGE_RANGE.get();
    }
}
