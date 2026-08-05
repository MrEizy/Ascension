package net.zic.ascension;

import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.*;

public class AscensionCommonConfig {
    private final ModConfigSpec.ConfigValue<List<? extends String>> ORE_SIGHT_ORE_COLORS;
    // Pill Cauldron
    public final ModConfigSpec.ConfigValue<List<? extends String>> PILL_CAULDRON_HEAT_ITEMS;
    public final ModConfigSpec.IntValue PILL_CAULDRON_HEAT_LOSS_INTERVAL;
    public final ModConfigSpec.IntValue PILL_CAULDRON_HEAT_LOSS_AMOUNT;
    public final ModConfigSpec.IntValue PILL_CAULDRON_MAX_HEAT;

    // Artifacts
    public final ModConfigSpec.IntValue REPAIR_INTERVAL;
    public final ModConfigSpec.IntValue REPAIR_AMOUNT;

    // Armor Durability
    public final ModConfigSpec.BooleanValue ARMOR_DURABILITY_LIMIT_ENABLED;
    public final ModConfigSpec.DoubleValue ARMOR_DURABILITY_MAX_LOSS_PERCENT;
    public final ModConfigSpec.IntValue ARMOR_DURABILITY_MIN_LOSS;

    // Starter Kit Config
    public final ModConfigSpec.BooleanValue STARTER_KIT_ENABLED;
    public final ModConfigSpec.ConfigValue<List<? extends String>> STARTER_KIT_ITEMS;

    // Weapon Projections
    public final ModConfigSpec.BooleanValue WEAPON_PROJECTION_BLOCK_BREAKING;


    public AscensionCommonConfig(ModConfigSpec.Builder builder) {
        builder.push("PillCauldron");
        builder.comment("Heat items for Pill Cauldron",
                "Format: [\"modid:item_id,heat_value\", \"modid:item_id,heat_value\"]",
                "Example: [\"ascension:flame,8\", \"ascension:soul_flame,16\", \"ascension:crimson_lotus_flame,200\"]");
        PILL_CAULDRON_HEAT_ITEMS = builder.defineList("heat_items",
                () -> new ArrayList<>(Arrays.asList(
                        "ascension:flame,8,0,0",
                        "ascension:soul_flame,16,5,0",
                        "ascension:crimson_lotus_flame,200,15,1"
                )),
                this::validateHeatItemEntry
        );

        builder.comment("Heat loss settings for Pill Cauldron");
        PILL_CAULDRON_HEAT_LOSS_INTERVAL = builder
                .comment("Interval in ticks between heat loss (20 ticks = 1 second) [Default: 20]")
                .defineInRange("heat_loss_interval", 20, 1, Integer.MAX_VALUE);

        PILL_CAULDRON_HEAT_LOSS_AMOUNT = builder
                .comment("Amount of heat lost each interval [Default: 1]")
                .defineInRange("heat_loss_amount", 1, 0, Integer.MAX_VALUE);

        PILL_CAULDRON_MAX_HEAT = builder
                .comment("Maximum heat capacity of the Pill Cauldron [Default: 1000]")
                .defineInRange("max_heat", 1000, 100, Integer.MAX_VALUE);

        builder.pop();

        builder.push("Artifacts");
        builder.comment("Artifact Modifiers");
        REPAIR_INTERVAL = builder
                .comment("Interval in ticks between Repairs [Default: 100]")
                .defineInRange("repairInterval", 100, 1, Integer.MAX_VALUE);
        REPAIR_AMOUNT = builder
                .comment("Amount of durability to Repair each interval [Default: 2]")
                .defineInRange("repairAmount", 2, 1, Integer.MAX_VALUE);
        builder.pop();

        // Armor Durability
        builder.push("ArmorDurability");

        ARMOR_DURABILITY_LIMIT_ENABLED = builder
                .comment("Enable armor durability loss limiting [Default: true]")
                .define("enabled", true);

        ARMOR_DURABILITY_MAX_LOSS_PERCENT = builder
                .comment(
                        "Maximum percentage of an armor piece's max durability that can be lost from one hit.",
                        "Example: 0.01 = 1% max durability loss per hit.",
                        "Set lower for tougher armor durability. [Default: 0.01]"
                )
                .defineInRange("maxLossPercentPerHit", 0.01D, 0.0D, 1.0D);

        ARMOR_DURABILITY_MIN_LOSS = builder
                .comment(
                        "Minimum armor durability lost when armor is damaged.",
                        "Usually keep this at 1 so armor can still wear down. [Default: 1]"
                )
                .defineInRange("minLossPerHit", 1, 0, Integer.MAX_VALUE);

        builder.pop();

        // Starter Kit Section
        builder.push("StarterKit");
        builder.comment("Starter Kit settings for new players");

        STARTER_KIT_ENABLED = builder
                .comment("Enable starter kit for new players [Default: true]")
                .define("enabled", true);

        builder.comment(
                "Starter kit items configuration",
                "Format: [\"modid:item_id,amount,enabled\", \"modid:item_id,amount,enabled\"]",
                "Example: [\"minecraft:stone_sword,1,true\", \"minecraft:bread,5,true\"]",
                "Set enabled to false to disable specific items from the starter kit"
        );
        STARTER_KIT_ITEMS = builder.defineList("items",
                () -> new ArrayList<>(Arrays.asList(
                        "ascension:spatial_rupture_talisman_t1,1,true",
                        "ascension:fasting_pill_t1,8,true"
                )),
                this::validateStarterKitEntry
        );
        builder.pop();

        builder.push("WeaponProjections");
        WEAPON_PROJECTION_BLOCK_BREAKING = builder
                .comment(
                        "Allow weapon projections to break blocks when their block_impact mode is 'break'.",
                        "Disable this to keep projection entity damage and collision while preventing terrain damage. [Default: true]"
                )
                .define("blockBreakingEnabled", true);
        builder.pop();


        builder.push("OreSight");
        builder.comment("Ore Sight skill configuration",
                "Format: [\"modid:block_id,hex_color,enabled\", \"modid:block_id,hex_color,enabled\"]",
                "Example: [\"minecraft:coal_ore,0x2F2F2F,true\", \"minecraft:diamond_ore,0x00FFFF,true\"]",
                "Hex colors should be in 0xRRGGBB format");
        ORE_SIGHT_ORE_COLORS = builder.defineList("ore_colors",
                () -> new ArrayList<>(Arrays.asList(
                        "minecraft:coal_ore,0x2F2F2F,true",
                        "minecraft:deepslate_coal_ore,0x2F2F2F,true",
                        "minecraft:iron_ore,0xD8D8D8,true",
                        "minecraft:deepslate_iron_ore,0xD8D8D8,true",
                        "minecraft:copper_ore,0xB87333,true",
                        "minecraft:deepslate_copper_ore,0xB87333,true",
                        "minecraft:gold_ore,0xFFD700,true",
                        "minecraft:deepslate_gold_ore,0xFFD700,true",
                        "minecraft:diamond_ore,0x00FFFF,true",
                        "minecraft:deepslate_diamond_ore,0x00FFFF,true",
                        "minecraft:emerald_ore,0x00FF00,true",
                        "minecraft:deepslate_emerald_ore,0x00FF00,true",
                        "minecraft:lapis_ore,0x0000FF,true",
                        "minecraft:deepslate_lapis_ore,0x0000FF,true",
                        "minecraft:redstone_ore,0xFF0000,true",
                        "minecraft:deepslate_redstone_ore,0xFF0000,true",
                        "minecraft:nether_quartz_ore,0xF0F0F0,true",
                        "minecraft:ancient_debris,0x8B4513,true"
                )),
                this::validateOreColorEntry
        );
        builder.pop();

    }

    // Add this validation method:
    private boolean validateOreColorEntry(Object entry) {
        if (!(entry instanceof String)) return false;
        String entryStr = (String) entry;
        String[] parts = entryStr.split(",");
        if (parts.length != 3) return false;
        try {
            // Validate hex color
            if (!parts[1].trim().startsWith("0x")) return false;
            Integer.parseInt(parts[1].trim().substring(2), 16);
            Boolean.parseBoolean(parts[2].trim());
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    // Add this method to parse ore color config:
    public Map<String, OreColorConfig> getOreColorConfig() {
        Map<String, OreColorConfig> configMap = new HashMap<>();
        for (String entry : ORE_SIGHT_ORE_COLORS.get()) {
            String[] parts = entry.split(",");
            if (parts.length == 3) {
                try {
                    String blockId = parts[0].trim();
                    int color = Integer.parseInt(parts[1].trim().substring(2), 16);
                    boolean enabled = Boolean.parseBoolean(parts[2].trim());
                    configMap.put(blockId, new OreColorConfig(color, enabled));
                } catch (NumberFormatException e) {
                    AscensionCraft.LOGGER.warn("Invalid ore color entry: {}", entry);
                }
            }
        }
        return configMap;
    }

    // Add this record at the class level:
    public record OreColorConfig(int color, boolean enabled) {}


    // New validation method for starter kit entries
    private boolean validateStarterKitEntry(Object entry) {
        if (!(entry instanceof String)) return false;
        String entryStr = (String) entry;
        String[] parts = entryStr.split(",");
        if (parts.length != 3) return false;
        try {
            Integer.parseInt(parts[1].trim());
            Boolean.parseBoolean(parts[2].trim());
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private boolean validateHeatItemEntry(Object entry) {
        if (!(entry instanceof String)) return false;
        String[] parts = ((String) entry).split(",");
        // Accept both old 2-part and new 4-part format
        if (parts.length != 2 && parts.length != 4) return false;
        try {
            Integer.parseInt(parts[1].trim());
            if (parts.length == 4) {
                Integer.parseInt(parts[2].trim());
                Integer.parseInt(parts[3].trim());
            }
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public Map<String, Integer> getHeatItems() {
        Map<String, Integer> heatItems = new HashMap<>();
        for (String entry : PILL_CAULDRON_HEAT_ITEMS.get()) {
            String[] parts = entry.split(",");
            if (parts.length >= 2) {
                try {
                    heatItems.put(parts[0].trim(), Integer.parseInt(parts[1].trim()));
                } catch (NumberFormatException e) {
                    AscensionCraft.LOGGER.warn("Invalid heat item entry: {}", entry);
                }
            }
        }
        return heatItems;
    }

    public int getFlameStandPurityBonus(String itemId) {
        for (String entry : PILL_CAULDRON_HEAT_ITEMS.get()) {
            String[] parts = entry.split(",");
            if (parts.length >= 3 && parts[0].trim().equals(itemId)) {
                try {
                    return Integer.parseInt(parts[2].trim());
                } catch (NumberFormatException e) {
                    return 0;
                }
            }
        }
        return 0;
    }

    public int getFlameStandRealmBonus(String itemId) {
        for (String entry : PILL_CAULDRON_HEAT_ITEMS.get()) {
            String[] parts = entry.split(",");
            if (parts.length >= 4 && parts[0].trim().equals(itemId)) {
                try {
                    return Integer.parseInt(parts[3].trim());
                } catch (NumberFormatException e) {
                    return 0;
                }
            }
        }
        return 0;
    }

    public List<StarterKitItem> getStarterKitItems() {
        List<StarterKitItem> items = new ArrayList<>();
        for (String entry : STARTER_KIT_ITEMS.get()) {
            String[] parts = entry.split(",");
            if (parts.length == 3) {
                try {
                    String itemId = parts[0].trim();
                    int amount = Integer.parseInt(parts[1].trim());
                    boolean enabled = Boolean.parseBoolean(parts[2].trim());
                    if (enabled) {
                        items.add(new StarterKitItem(itemId, amount));
                    }
                } catch (NumberFormatException e) {
                    AscensionCraft.LOGGER.warn("Invalid starter kit entry: {}", entry);
                }
            }
        }
        return items;
    }

    public record StarterKitItem(String itemId, int amount) {}
}