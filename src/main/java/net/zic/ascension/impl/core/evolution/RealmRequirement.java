package net.zic.ascension.impl.core.evolution;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import net.zic.ascension.api.ascension.core.path.PathInstance;
import net.zic.ascension.api.ascension.core.path.realm.Realm;
import net.zic.ascension.api.ascension.core.source.AscensionOriginSourceHelper;
import net.zic.ascension.api.rpg_engine.source.OriginSource;

/**
 * The source has to be at least this realm on this path. Realm numbers are the same indices the path datapack uses (the first realm is 0).
 */
public record RealmRequirement(Identifier path, int majorRealm, int minorRealm) {
    public static final Codec<RealmRequirement> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Identifier.CODEC.fieldOf("path").forGetter(RealmRequirement::path),
            Codec.intRange(0, 1024).fieldOf("major_realm").forGetter(RealmRequirement::majorRealm),
            Codec.intRange(0, 1024).optionalFieldOf("minor_realm", 0).forGetter(RealmRequirement::minorRealm)
    ).apply(instance, RealmRequirement::new));

    public boolean test(OriginSource source) {
        PathInstance<?> instance = source == null ? null : AscensionOriginSourceHelper.getPathInstance(source, path);
        if (instance == null) {
            return false;
        }
        Realm current = instance.getCurrentRealm();
        if (current.majorRealm() < 0) {
            return false;
        }
        return current.majorRealm() > majorRealm
                || (current.majorRealm() == majorRealm && current.minorRealm() >= minorRealm);
    }
}