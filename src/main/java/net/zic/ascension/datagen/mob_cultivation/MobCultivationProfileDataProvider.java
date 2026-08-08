package net.zic.ascension.datagen.mob_cultivation;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;

import java.util.Map;

public final class MobCultivationProfileDataProvider extends MobCultivationJsonProvider {
    private static final double DEFAULT_ELITE_CHANCE = 0.025D;
    private static final double DEFAULT_ANCIENT_CHANCE = 0.08D;

    public MobCultivationProfileDataProvider(PackOutput output) {
        super(output, "profiles");
    }

    @Override
    protected void addEntries(Map<Identifier, JsonObject> entries) {
        add(entries, "zombie", EntityType.ZOMBIE)
                .foundation(5.0D, 1.0D, 3.0D)
                .subPath("elemental/death", 4.0D)
                .subPath("elemental/poison", 2.0D)
                .subPath("weapon/fist", 2.0D)
                .subPathCount(1, 2)
                .traits("undead")
                .skillPools("generic/body", "generic/soul", "species/zombie")
                .lootProfile("undead")
                .stats(1.0D, 1.15D, 1.10D, 0.85D, 0.90D)
                .growth(0.90D, 0.85D, 1.0D)
                .defaultElite()
                .save();

        add(entries, "skeleton", EntityType.SKELETON)
                .foundation(1.0D, 3.0D, 4.0D)
                .subPath("weapon/bow", 7.0D)
                .subPath("elemental/death", 3.0D)
                .subPath("elemental/yin", 2.0D)
                .subPath("elemental/ice", 1.0D)
                .subPathCount(1, 2)
                .traits("undead")
                .skillPools("generic/essence", "generic/soul", "species/skeleton")
                .lootProfile("undead")
                .stats(1.0D, 0.85D, 0.90D, 1.20D, 1.20D)
                .growth(1.0D, 1.0D, 1.0D)
                .defaultElite()
                .save();

        add(entries, "creeper", EntityType.CREEPER)
                .foundation(1.0D, 6.0D, 1.0D)
                .subPath("elemental/lightning", 5.0D)
                .subPath("elemental/wood", 2.0D)
                .subPath("elemental/fire", 2.0D)
                .subPathCount(1, 2)
                .traits("elemental_body")
                .skillPools("generic/essence")
                .lootProfile("elemental")
                .stats(1.0D, 0.90D, 1.25D, 1.0D, 1.05D)
                .growth(1.05D, 1.15D, 1.0D)
                .defaultElite()
                .save();

        add(entries, "spider", EntityType.SPIDER)
                .foundation(4.0D, 2.0D, 1.0D)
                .subPath("elemental/poison", 6.0D)
                .subPath("elemental/wood", 2.0D)
                .subPath("weapon/fist", 2.0D)
                .subPathCount(1, 2)
                .traits("pack_creature")
                .skillPools("generic/body", "species/spider")
                .lootProfile("beast")
                .stats(1.0D, 0.90D, 1.0D, 1.30D, 0.85D)
                .growth(1.0D, 0.95D, 1.0D)
                .defaultElite()
                .save();

        add(entries, "enderman", EntityType.ENDERMAN)
                .foundation(1.0D, 3.0D, 6.0D)
                .subPath("elemental/space", 7.0D)
                .subPath("elemental/dark", 3.0D)
                .subPath("elemental/illusion", 2.0D)
                .subPathCount(1, 2)
                .traits("spiritual_body")
                .skillPools("generic/soul", "species/enderman")
                .lootProfile("default")
                .stats(1.0D, 0.85D, 0.95D, 1.25D, 1.35D)
                .growth(1.15D, 1.25D, 1.0D)
                .defaultElite()
                .initialRealm(0, 1, 1, 2)
                .save();

        add(entries, "blaze", EntityType.BLAZE)
                .foundation(0.5D, 4.0D, 6.0D)
                .subPath("elemental/fire", 10.0D)
                .subPath("elemental/yang", 2.0D)
                .subPathCount(1, 2)
                .traits("elemental_body", "spiritual_body")
                .skillPools("generic/essence", "generic/soul", "species/blaze")
                .lootProfile("elemental")
                .stats(1.05D, 0.80D, 0.75D, 1.10D, 1.60D)
                .growth(1.20D, 1.35D, 1.0D)
                .elite(0.04D, DEFAULT_ANCIENT_CHANCE, 1)
                .initialRealm(0, 1, 1, 2)
                .save();

        add(entries, "witch", EntityType.WITCH)
                .foundation(0.5D, 4.0D, 5.0D)
                .subPath("elemental/poison", 6.0D)
                .subPath("elemental/water", 3.0D)
                .subPath("elemental/wood", 3.0D)
                .subPathCount(1, 2)
                .skillPools("generic/essence", "generic/soul")
                .lootProfile("default")
                .stats(1.0D, 0.90D, 0.70D, 0.95D, 1.45D)
                .growth(1.10D, 1.20D, 1.0D)
                .defaultElite()
                .save();

        add(entries, "slime", EntityType.SLIME)
                .foundation(6.0D, 2.0D, 0.5D)
                .subPath("elemental/water", 4.0D)
                .subPath("elemental/earth", 2.0D)
                .subPath("weapon/fist", 2.0D)
                .subPathCount(1, 2)
                .traits("regenerating")
                .skillPools("generic/body")
                .lootProfile("beast")
                .stats(1.0D, 1.30D, 1.0D, 0.85D, 0.75D)
                .growth(1.0D, 1.10D, 1.0D)
                .defaultElite()
                .save();

        add(entries, "wolf", EntityType.WOLF)
                .foundation(6.0D, 1.0D, 2.0D)
                .subPath("elemental/wind", 4.0D)
                .subPath("weapon/fist", 4.0D)
                .subPath("elemental/wood", 1.0D)
                .subPathCount(1, 2)
                .traits("pack_creature")
                .skillPools("generic/body", "species/wolf")
                .lootProfile("beast")
                .stats(1.0D, 1.0D, 1.15D, 1.25D, 1.0D)
                .growth(1.05D, 1.0D, 1.0D)
                .defaultElite()
                .save();

        add(entries, "iron_golem", EntityType.IRON_GOLEM)
                .foundation(8.0D, 1.0D, 0.2D)
                .subPath("elemental/metal", 8.0D)
                .subPath("elemental/earth", 4.0D)
                .subPath("weapon/fist", 5.0D)
                .subPathCount(1, 2)
                .traits("armoured_body")
                .skillPools("generic/body")
                .lootProfile("default")
                .stats(1.15D, 1.50D, 1.45D, 0.55D, 0.65D)
                .growth(0.60D, 0.70D, 1.0D)
                .elite(0.015D, DEFAULT_ANCIENT_CHANCE, 1)
                .initialRealm(1, 2, 0, 2)
                .save();

        add(entries, "guardian", EntityType.GUARDIAN)
                .foundation(3.0D, 5.0D, 1.0D)
                .subPath("elemental/water", 8.0D)
                .subPath("elemental/lightning", 3.0D)
                .subPath("weapon/spear", 2.0D)
                .subPathCount(1, 2)
                .traits("armoured_body", "elemental_body")
                .skillPools("generic/essence")
                .lootProfile("elemental")
                .stats(1.0D, 1.25D, 1.0D, 0.90D, 1.20D)
                .growth(1.0D, 1.15D, 1.0D)
                .defaultElite()
                .save();

        add(entries, "piglin", EntityType.PIGLIN)
                .foundation(3.0D, 3.0D, 1.0D)
                .subPath("elemental/fire", 5.0D)
                .subPath("elemental/metal", 4.0D)
                .subPath("weapon/sword", 3.0D)
                .subPath("weapon/axe", 3.0D)
                .subPathCount(1, 2)
                .traits("pack_creature")
                .skillPools("generic/body", "generic/essence")
                .lootProfile("default")
                .stats(1.0D, 1.05D, 1.15D, 1.0D, 0.90D)
                .growth(1.05D, 1.10D, 1.0D)
                .defaultElite()
                .save();

        add(entries, "wither", EntityType.WITHER)
                .foundation(1.0D, 3.0D, 8.0D)
                .subPath("elemental/death", 9.0D)
                .subPath("elemental/dark", 5.0D)
                .subPath("elemental/poison", 3.0D)
                .subPathCount(2, 3)
                .traits("undead", "spiritual_body")
                .skillPools("generic/soul", "generic/essence")
                .lootProfile("boss")
                .stats(1.20D, 1.35D, 1.20D, 1.0D, 1.65D)
                .growth(1.20D, 1.25D, 1.0D)
                .elite(1.0D, 0.20D, 2)
                .initialRealm(2, 3, 1, 2)
                .save();

        add(entries, "ender_dragon", EntityType.ENDER_DRAGON)
                .foundation(3.0D, 4.0D, 6.0D)
                .subPath("elemental/space", 9.0D)
                .subPath("elemental/dark", 4.0D)
                .subPath("elemental/fire", 3.0D)
                .subPath("elemental/lightning", 2.0D)
                .subPathCount(2, 3)
                .traits("elemental_body", "spiritual_body", "armoured_body")
                .skillPools("generic/body", "generic/essence", "generic/soul")
                .lootProfile("boss")
                .stats(1.30D, 1.60D, 1.45D, 1.10D, 1.60D)
                .growth(1.25D, 1.30D, 1.0D)
                .elite(1.0D, 0.35D, 2)
                .initialRealm(3, 4, 1, 2)
                .save();

        add(entries, "elder_guardian", EntityType.ELDER_GUARDIAN)
                .foundation(1.0D, 4.0D, 3.0D)
                .subPath("elemental/water", 6.0D)
                .subPath("elemental/metal", 2.5D)
                .subPath("space", 1.0D)
                .subPathCount(1, 2)
                .traits("armoured_body", "spiritual_body")
                .skillPools("generic/essence", "generic/soul")
                .lootProfile("boss")
                .stats(1.20D, 1.35D, 0.85D, 1.00D, 1.40D)
                .growth(1.10D, 1.15D, 1.15D)
                .elite(1.0D, 0.20D, 2)
                .initialRealm(2, 3, 1, 2)
                .save();

        add(entries, "piglin_brute", EntityType.PIGLIN_BRUTE)
                .foundation(5.0D, 2.0D, 1.0D)
                .subPath("elemental/fire", 3.0D)
                .subPath("elemental/metal", 3.5D)
                .subPath("weapon/axe", 6.0D)
                .subPath("weapon/fist", 2.0D)
                .subPathCount(1, 2)
                .traits("armoured_body")
                .skillPools("generic/body")
                .lootProfile("default")
                .stats(1.15D, 1.20D, 1.35D, 0.95D, 0.80D)
                .growth(1.00D, 0.95D, 1.05D)
                .defaultElite()
                .initialRealm(0, 1, 1, 2)
                .save();

        add(entries, "wither_skeleton", EntityType.WITHER_SKELETON)
                .foundation(4.0D, 1.0D, 4.0D)
                .subPath("elemental/death", 5.0D)
                .subPath("elemental/fire", 2.5D)
                .subPath("elemental/dark", 2.0D)
                .subPath("weapon/sword", 5.0D)
                .subPathCount(1, 2)
                .traits("undead", "armoured_body")
                .skillPools("generic/body", "generic/soul")
                .lootProfile("undead")
                .stats(1.10D, 1.25D, 1.35D, 0.95D, 1.10D)
                .growth(1.05D, 1.00D, 1.10D)
                .defaultElite()
                .initialRealm(0, 1, 1, 2)
                .save();

        add(entries, "drowned", EntityType.DROWNED)
                .foundation(4.0D, 1.0D, 3.0D)
                .subPath("elemental/water", 5.0D)
                .subPath("elemental/death", 3.0D)
                .subPath("weapon/spear", 5.0D)
                .subPathCount(1, 2)
                .traits("undead")
                .skillPools("generic/body", "generic/soul")
                .lootProfile("undead")
                .stats(1.05D, 1.15D, 1.15D, 0.90D, 1.00D)
                .growth(0.95D, 1.05D, 1.05D)
                .defaultElite()
                .save();

        add(entries, "husk", EntityType.HUSK)
                .foundation(5.0D, 1.0D, 2.0D)
                .subPath("elemental/death", 3.0D)
                .subPath("elemental/earth", 3.0D)
                .subPath("elemental/fire", 1.5D)
                .subPath("weapon/fist", 3.0D)
                .subPathCount(1, 2)
                .traits("undead")
                .skillPools("generic/body")
                .lootProfile("undead")
                .stats(1.05D, 1.20D, 1.20D, 0.85D, 0.80D)
                .growth(0.90D, 0.85D, 1.00D)
                .defaultElite()
                .save();

        add(entries, "stray", EntityType.STRAY)
                .foundation(1.0D, 3.0D, 4.0D)
                .subPath("elemental/ice", 5.0D)
                .subPath("elemental/death", 2.0D)
                .subPath("weapon/bow", 6.0D)
                .subPathCount(1, 2)
                .traits("undead")
                .skillPools("generic/essence", "generic/soul", "species/skeleton")
                .lootProfile("undead")
                .stats(1.05D, 0.90D, 0.90D, 1.15D, 1.20D)
                .growth(1.00D, 1.00D, 1.05D)
                .defaultElite()
                .save();

        add(entries, "squid", EntityType.SQUID)
                .foundation(1.0D, 4.0D, 2.0D)
                .subPath("elemental/water", 6.0D)
                .subPathCount(1, 1)
                .traits("pack_creature")
                .skillPools("generic/essence")
                .lootProfile("beast")
                .stats(0.95D, 0.85D, 0.70D, 1.00D, 1.10D)
                .growth(0.95D, 1.10D, 1.00D)
                .save();

        add(entries, "glow_squid", EntityType.GLOW_SQUID)
                .foundation(1.0D, 3.0D, 4.0D)
                .subPath("elemental/water", 5.0D)
                .subPath("elemental/yin", 2.5D)
                .subPathCount(1, 2)
                .traits("spiritual_body")
                .skillPools("generic/essence", "generic/soul")
                .lootProfile("beast")
                .stats(1.00D, 0.75D, 0.65D, 0.95D, 1.35D)
                .growth(1.00D, 1.20D, 1.05D)
                .defaultElite()
                .save();

        add(entries, "magma_cube", EntityType.MAGMA_CUBE)
                .foundation(3.0D, 4.0D, 1.0D)
                .subPath("elemental/fire", 6.0D)
                .subPath("weapon/fist", 2.0D)
                .subPathCount(1, 2)
                .traits("elemental_body", "regenerating")
                .skillPools("generic/body", "generic/essence")
                .lootProfile("elemental")
                .stats(1.10D, 1.30D, 1.15D, 0.85D, 0.85D)
                .growth(1.00D, 1.15D, 1.10D)
                .defaultElite()
                .save();

        add(entries, "ghast", EntityType.GHAST)
                .foundation(0.5D, 4.0D, 5.0D)
                .subPath("elemental/fire", 5.0D)
                .subPath("space", 1.5D)
                .subPath("elemental/dark", 2.0D)
                .subPathCount(1, 2)
                .traits("spiritual_body", "elemental_body")
                .skillPools("generic/essence", "generic/soul")
                .lootProfile("elemental")
                .stats(1.10D, 0.75D, 0.70D, 0.95D, 1.55D)
                .growth(1.05D, 1.15D, 1.10D)
                .defaultElite()
                .initialRealm(0, 1, 1, 2)
                .save();

        add(entries, "ravager", EntityType.RAVAGER)
                .foundation(7.0D, 1.0D, 1.0D)
                .subPath("elemental/earth", 3.0D)
                .subPath("weapon/fist", 5.0D)
                .subPath("weapon/mace", 2.0D)
                .subPathCount(1, 2)
                .traits("armoured_body")
                .skillPools("generic/body")
                .lootProfile("beast")
                .stats(1.20D, 1.45D, 1.45D, 0.80D, 0.75D)
                .growth(0.95D, 0.85D, 1.05D)
                .elite(0.30D, 0.08D, 1)
                .initialRealm(1, 1, 0, 2)
                .save();

        add(entries, "warden", EntityType.WARDEN)
                .foundation(5.0D, 1.0D, 4.0D)
                .subPath("elemental/dark", 4.0D)
                .subPath("elemental/death", 2.0D)
                .subPath("weapon/fist", 5.0D)
                .subPathCount(1, 2)
                .traits("armoured_body", "spiritual_body")
                .skillPools("generic/body", "generic/soul")
                .lootProfile("boss")
                .stats(1.35D, 1.55D, 1.50D, 0.80D, 1.25D)
                .growth(0.90D, 0.85D, 1.15D)
                .elite(1.0D, 0.25D, 2)
                .initialRealm(2, 3, 0, 2)
                .save();



    }













    // Actual Stuff

    @Override
    public String getName() {
        return "Ascension Mob Cultivation Profiles";
    }

    private static ProfileBuilder add(Map<Identifier, JsonObject> entries, String name, EntityType<?> entityType) {
        return new ProfileBuilder(entries, id(name), entityType);
    }

    private static final class ProfileBuilder {
        private final Map<Identifier, JsonObject> entries;
        private final Identifier id;
        private final JsonObject root = new JsonObject();
        private final JsonObject foundationWeights = new JsonObject();
        private final JsonObject subPathWeights = new JsonObject();
        private final JsonArray traits = new JsonArray();
        private final JsonArray skillPools = new JsonArray();

        private ProfileBuilder(Map<Identifier, JsonObject> entries, Identifier id, EntityType<?> entityType) {
            this.entries = entries;
            this.id = id;

            root.addProperty("priority", 10);
            JsonObject target = new JsonObject();
            target.addProperty("entity", BuiltInRegistries.ENTITY_TYPE.getKey(entityType).toString());
            root.add("target", target);
        }

        private ProfileBuilder foundation(double body, double essence, double soul) {
            foundationWeights.addProperty(id("foundation/body").toString(), body);
            foundationWeights.addProperty(id("foundation/essence").toString(), essence);
            foundationWeights.addProperty(id("foundation/soul").toString(), soul);
            return this;
        }

        private ProfileBuilder subPath(String path, double weight) {
            subPathWeights.addProperty(id(path).toString(), weight);
            return this;
        }

        private ProfileBuilder subPathCount(int minimum, int maximum) {
            JsonObject count = new JsonObject();
            count.addProperty("min", minimum);
            count.addProperty("max", maximum);
            root.add("sub_path_count", count);
            return this;
        }

        private ProfileBuilder traits(String... values) {
            for (String value : values) {
                traits.add(id(value).toString());
            }
            return this;
        }

        private ProfileBuilder skillPools(String... values) {
            for (String value : values) {
                skillPools.add(id(value).toString());
            }
            return this;
        }

        private ProfileBuilder lootProfile(String value) {
            root.addProperty("loot_profile", id(value).toString());
            return this;
        }

        private ProfileBuilder stats(double multiplier, double vitality, double strength, double agility, double spirit) {
            root.addProperty("stat_multiplier", multiplier);
            JsonObject biases = new JsonObject();
            biases.addProperty("vitality", vitality);
            biases.addProperty("strength", strength);
            biases.addProperty("agility", agility);
            biases.addProperty("spirit", spirit);
            root.add("stat_biases", biases);
            return this;
        }

        private ProfileBuilder growth(double multiplier, double atmosphericQiSensitivity, double lootMultiplier) {
            root.addProperty("growth_multiplier", multiplier);
            root.addProperty("atmospheric_qi_sensitivity", atmosphericQiSensitivity);
            root.addProperty("loot_multiplier", lootMultiplier);
            root.addProperty("show_particles", true);
            return this;
        }

        private ProfileBuilder defaultElite() {
            return elite(DEFAULT_ELITE_CHANCE, DEFAULT_ANCIENT_CHANCE, 1);
        }

        private ProfileBuilder elite(double chance, double ancientChance, int extraTraitRolls) {
            JsonObject elite = new JsonObject();
            elite.addProperty("enabled", true);
            elite.addProperty("chance", chance);
            elite.addProperty("ancient_chance", ancientChance);
            elite.addProperty("extra_trait_rolls", extraTraitRolls);

            JsonObject traitWeights = new JsonObject();
            traitWeights.addProperty(id("regenerating").toString(), 1.0D);
            traitWeights.addProperty(id("armoured_body").toString(), 1.0D);
            traitWeights.addProperty(id("spiritual_body").toString(), 0.7D);
            traitWeights.addProperty(id("elemental_body").toString(), 0.7D);
            traitWeights.addProperty(id("pack_creature").toString(), 0.35D);
            elite.add("trait_weights", traitWeights);

            root.add("elite", elite);
            return this;
        }

        private ProfileBuilder initialRealm(int majorMin, int majorMax, int minorMin, int minorMax) {
            JsonObject realm = new JsonObject();
            realm.addProperty("major_min", majorMin);
            realm.addProperty("major_max", majorMax);
            realm.addProperty("minor_min", minorMin);
            realm.addProperty("minor_max", minorMax);
            root.add("initial_realm", realm);
            return this;
        }

        private void save() {
            root.add("foundation_path_weights", foundationWeights);
            root.add("sub_path_weights", subPathWeights);
            root.add("traits", traits);
            root.add("skill_pools", skillPools);
            entries.put(id, root);
        }
    }
}
