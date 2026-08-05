package net.zic.ascension.api.ascension.capabilities.damage_provider;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;

public interface AscensionDamageSourceProvider {
    Identifier getPath();

    record EntitySource(Entity entity, Identifier path) implements AscensionDamageSourceProvider {
        @Override
        public Identifier getPath() {
            return path;
        }
    }

    record ItemSource(ItemStack stack, Identifier path) implements AscensionDamageSourceProvider {
        @Override
        public Identifier getPath() {
            return path;
        }
    }
}
