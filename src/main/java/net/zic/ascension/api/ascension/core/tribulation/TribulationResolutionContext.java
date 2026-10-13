package net.zic.ascension.api.ascension.core.tribulation;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.LivingEntity;
import net.zic.ascension.api.ascension.core.source.AscensionOriginSourceHelper;
import net.zic.ascension.api.rpg_engine.source.OriginSource;
import org.jspecify.annotations.Nullable;

public record TribulationResolutionContext(
        MinecraftServer server,
        TribulationInstance<?> instance,
        TribulationOutcome outcome,
        @Nullable LivingEntity target
) {
    public @Nullable OriginSource targetSource() {
        return target == null ? null : AscensionOriginSourceHelper.getEntitySource(target);
    }
}
