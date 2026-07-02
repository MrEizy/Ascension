package net.zic.ascension.capabilities.damage_provider;

import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.zic.ascension.api.capabilities.damage_provider.AscensionDamageSourceProvider;
import net.zic.ascension.common.item.components.AscensionComponents;

public record SimpleItemDamageSourceProvider(ItemStack stack,Identifier path) implements AscensionDamageSourceProvider {
    @Override
    public Identifier getPath() {
        return path;
    }
}
