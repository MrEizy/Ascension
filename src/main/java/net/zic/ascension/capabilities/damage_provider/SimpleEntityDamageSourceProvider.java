package net.zic.ascension.capabilities.damage_provider;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.zic.ascension.api.capabilities.damage_provider.AscensionDamageSourceProvider;
import net.zic.ascension.common.data_attachements.AscensionAttachments;

public record SimpleEntityDamageSourceProvider(Entity entity,Identifier path) implements AscensionDamageSourceProvider {
    @Override
    public Identifier getPath() {
        return path;
    }
}
