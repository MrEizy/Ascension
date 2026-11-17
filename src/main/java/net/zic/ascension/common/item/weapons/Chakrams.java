package net.zic.ascension.common.item.weapons;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.zic.ascension.AscensionCraft;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public final class Chakrams {

    private static final Map<ChakramTier, Supplier<Item>> CHAKRAMS = new EnumMap<>(ChakramTier.class);

    private Chakrams() {}

    public static ResourceKey<Item> itemKey(ChakramTier tier) {
        return ResourceKey.create(Registries.ITEM, AscensionCraft.prefix(tier.itemName()));
    }

    public static Item createChakram(ChakramTier tier) {
        return new ChakramItem(tier, ChakramItem.buildProperties(tier));
    }

    public static void bind(ChakramTier tier, Supplier<Item> item) {
        CHAKRAMS.put(tier, item);
    }

    public static List<Item> allChakrams() {
        List<Item> out = new ArrayList<>();
        for (ChakramTier t : ChakramTier.values()) {
            Supplier<Item> s = CHAKRAMS.get(t);
            if (s != null) out.add(s.get());
        }
        return out;
    }
}