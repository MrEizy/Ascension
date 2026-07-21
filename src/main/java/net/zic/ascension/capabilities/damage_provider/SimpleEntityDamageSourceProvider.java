package net.zic.ascension.capabilities.damage_provider;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.zic.ascension.api.ascension.capabilities.damage_provider.AscensionDamageSourceProvider;

public record SimpleEntityDamageSourceProvider(Entity entity,Identifier path) implements AscensionDamageSourceProvider {
    @Override
    public Identifier getPath() {
        return path;
    }
}
