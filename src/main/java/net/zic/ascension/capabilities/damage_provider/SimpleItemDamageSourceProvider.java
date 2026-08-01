package net.zic.ascension.capabilities.damage_provider;

import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.zic.ascension.api.ascension.capabilities.damage_provider.AscensionDamageSourceProvider;

public record SimpleItemDamageSourceProvider(ItemStack stack,Identifier path) implements AscensionDamageSourceProvider {
    @Override
    public Identifier getPath() {
        return path;
    }
}
