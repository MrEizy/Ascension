package net.zic.ascension.datagen.mob_cultivation;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.Map;

public final class MobCultivationLootDataProvider extends MobCultivationJsonProvider {
    public MobCultivationLootDataProvider(PackOutput output) {
        super(output, "loot_pools");
    }

    @Override
    protected void addEntries(Map<Identifier, JsonObject> entries) {
        pool(entries, "default")
                .entry(Items.IRON_NUGGET, 8.0D, 1, 3)
                .entry(Items.REDSTONE, 5.0D, 2, 1, 3)
                .entry(Items.LAPIS_LAZULI, 3.0D, 3, 1, 2)
                .entry(Items.GOLD_NUGGET, 2.0D, 4, 1, 3)
                .entry(Items.EMERALD, 0.8D, 6)
                .entry(Items.DIAMOND, 0.25D, 8)
                .save();

        pool(entries, "beast")
                .entry(Items.LEATHER, 7.0D, 1, 3)
                .entry(Items.RABBIT_HIDE, 4.0D, 1, 2)
                .entry(Items.BONE, 3.0D, 1, 2)
                .entry(Items.GOLD_NUGGET, 1.5D, 4, 1, 2)
                .entry(Items.EMERALD, 0.7D, 6)
                .save();

        pool(entries, "elemental")
                .entry(Items.BLAZE_POWDER, 7.0D, 1, 3)
                .entry(Items.GLOWSTONE_DUST, 5.0D, 1, 3)
                .entry(Items.AMETHYST_SHARD, 3.0D, 3, 1, 3)
                .entry(Items.PRISMARINE_CRYSTALS, 2.0D, 4, 1, 2)
                .entry(Items.DIAMOND, 0.4D, 8)
                .save();

        pool(entries, "undead")
                .entry(Items.BONE, 8.0D, 1, 3)
                .entry(Items.ROTTEN_FLESH, 6.0D, 1, 3)
                .entry(Items.AMETHYST_SHARD, 3.0D, 2, 1, 3)
                .entry(Items.ECHO_SHARD, 0.7D, 6)
                .entry(Items.GOLD_INGOT, 0.8D, 5)
                .save();

        pool(entries, "boss")
                .entry(Items.GOLD_INGOT, 6.0D, 2, 5)
                .entry(Items.EMERALD, 5.0D, 1, 3)
                .entry(Items.DIAMOND, 3.0D, 1, 2)
                .entry(Items.ENCHANTED_BOOK, 1.5D, 5)
                .entry(Items.GOLDEN_APPLE, 0.8D, 6)
                .entry(Items.NETHERITE_SCRAP, 0.5D, 8)
                .save();
    }

    @Override
    public String getName() {
        return "Ascension Mob Cultivation Loot Pools";
    }

    private static LootPoolBuilder pool(Map<Identifier, JsonObject> entries, String name) {
        return new LootPoolBuilder(entries, id(name));
    }

    private static final class LootPoolBuilder {
        private final Map<Identifier, JsonObject> entries;
        private final Identifier id;
        private final JsonArray values = new JsonArray();

        private LootPoolBuilder(Map<Identifier, JsonObject> entries, Identifier id) {
            this.entries = entries;
            this.id = id;
        }

        private LootPoolBuilder entry(Item item, double weight, int minimumCount, int maximumCount) {
            return entry(item, weight, 0, minimumCount, maximumCount);
        }

        private LootPoolBuilder entry(Item item, double weight, int minimumRealmScore) {
            return entry(item, weight, minimumRealmScore, 1, 1);
        }

        private LootPoolBuilder entry(Item item, double weight, int minimumRealmScore, int minimumCount, int maximumCount) {
            JsonObject entry = new JsonObject();
            entry.addProperty("item", BuiltInRegistries.ITEM.getKey(item).toString());
            entry.addProperty("weight", weight);
            if (minimumRealmScore > 0) {
                entry.addProperty("minimum_realm_score", minimumRealmScore);
            }
            if (minimumCount != 1) {
                entry.addProperty("minimum_count", minimumCount);
            }
            if (maximumCount != minimumCount) {
                entry.addProperty("maximum_count", maximumCount);
            } else if (minimumCount != 1) {
                entry.addProperty("maximum_count", maximumCount);
            }
            values.add(entry);
            return this;
        }

        private void save() {
            JsonObject root = new JsonObject();
            root.add("entries", values);
            entries.put(id, root);
        }
    }
}
