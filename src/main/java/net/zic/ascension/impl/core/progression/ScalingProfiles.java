package net.zic.ascension.impl.core.progression;

import com.mojang.datafixers.util.Either;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.Identifier;
import net.zic.zenithlib.registry.RegistryHelper;

public final class ScalingProfiles {
    private ScalingProfiles() {}

    public static <T> T resolve(RegistryAccess access, RegistryHelper.DataPackRegistry<T> registry, Either<Identifier, T> reference) {
        return reference.map(id -> {
            T value = access.lookup(registry.key()).map(r -> r.getValue(id)).orElse(null);
            if (value == null) throw new IllegalStateException("Missing stat scaling profile: " + id + " in " + registry.key());
            return value;
        }, value -> value);
    }
}
