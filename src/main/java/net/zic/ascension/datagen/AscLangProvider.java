package net.zic.ascension.datagen;

import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;
import net.zic.ascension.AscensionCraft;

public class AscLangProvider extends LanguageProvider {
    public AscLangProvider(PackOutput output) {
        super(output, AscensionCraft.MOD_ID, "en_us");
    }

    @Override
    protected void addTranslations() {

        // World preset
        add("generator.ascension.ascension", "Jianghu");

        // Biomes
        add("biome.ascension.greystone_foothills", "Greystone Foothills");
        add("biome.ascension.azure_cloud_range", "Azure Cloud Range");
        add("biome.ascension.heavenreach_peaks", "Heavenreach Peaks");
        add("biome.ascension.ancient_grove", "Ancient Grove");
        add("biome.ascension.jadebloom_forest", "Jadebloom Forest");
        add("biome.ascension.misty_woods", "Misty Woods");
        add("biome.ascension.verdant_moor", "Verdant Moor");
        add("biome.ascension.golden_steppe", "Golden Steppe");



        // Configs
        add("ascension.config.hud", "HUD Settings");
        add("ascension.config.showExactValues", "Show Values");

        // Attributes
        add("attributes.ascension.max_qi", "Maximum Qi");
        add("attributes.ascension.qi_regen_rate", "Qi Regeneration");
        add("attributes.ascension.health_regen_rate", "Health Regeneration");
        add("attributes.ascension.max_stamina", "Maximum Stamina");
        add("attributes.ascension.stamina_regen_rate", "Stamina Regeneration");
        add("attributes.ascension.stamina_regen_delay", "Stamina Regeneration Delay");


        // Creative Tabs
        add("creativetab.ascension.items", "Ascension Items");
        add("creativetab.ascension.blocks", "Ascension Blocks");
        add("creativetab.ascension.alchemy", "Ascension Alchemy");
        add("creativetab.ascension.physique_transfers", "Physiques");
        add("creativetab.ascension.bloodline_transfers", "Bloodlines");
        add("creativetab.ascension.technique_transfers", "Techniques");


        // Creative Tab Sections
        add("creative_section.ascension.artifacts", "Ascension Artifacts");
        add("creative_section.ascension.materials", "Ascension Materials");




        // Items
        add("item.ascension.tablet_of_destruction_human", "Tablet of Destruction - Human");
        add("item.ascension.tablet_of_destruction_earth", "Tablet of Destruction - Earth");
        add("item.ascension.tablet_of_destruction_heaven", "Tablet of Destruction - Heaven");
        add("item.ascension.tablet_of_destruction_ascendant", "Tablet of Destruction - Ascendant");

        add("item.ascension.tablet_of_destruction_human.cooldown", "§cTablet is on cooldown.");
        add("item.ascension.tablet_of_destruction_earth.cooldown", "§cTablet is on cooldown.");
        add("item.ascension.tablet_of_destruction_heaven.cooldown", "§cTablet is on cooldown.");
        add("item.ascension.tablet_of_destruction_ascendant.cooldown", "§cTablet is on cooldown.");

        add("item.ascension.tablet_of_destruction_earth.hint", "§Press [V] to cycle block drop mode.");
        add("item.ascension.tablet_of_destruction_heaven.link_info", "Shift + Right-click a container to link it.");
        add("item.ascension.tablet_of_destruction_heaven.linked_to", "Linked: %s @ %d, %d, %d");

        add("ascension.tablet.ascendant.shape", "Shape: ");
        add("ascension.tablet.ascendant.shape.shapeless", "Shapeless");
        add("ascension.tablet.ascendant.shape.tunnel", "Mining Tunnel");
        add("ascension.tablet.ascendant.shape.escape", "Escape Tunnel");
        add("ascension.tablet.ascendant.shape.dome", "Dome");
        add("ascension.tablet.ascendant.shape_info", "Hold SHIFT + [Scroll] to cycle shape.");

        add("item.ascension.tablet.link_invalid", "§cThat block cannot be linked.");
        add("item.ascension.tablet.link_success", "§aLinked to %s @ %d, %d, %d");
        add("item.ascension.tablet.unlink_success", "§eContainer unlinked.");

        add("ascension.tablet.cooldown", "§cTablet is on cooldown.");
        add("ascension.tablet.drop_mode", "Drop Mode: ");
        add("ascension.tablet.drop_mode.off", "Off");
        add("ascension.tablet.drop_mode.on", "On");
        add("ascension.tablet.cycle_mode_info", "Press [V] to cycle drop mode.");




        add("item.ascension.jade_bottle", "Jade Bottle");


        //Item Ores
        add("item.ascension.raw_black_iron", "Raw Black Iron");
        add("item.ascension.black_iron_ingot", "Black Iron Ingot");
        add("item.ascension.black_iron_nugget", "Black Iron Nugget");
        add("item.ascension.raw_frost_silver", "Raw Frost Silver");
        add("item.ascension.frost_silver_ingot", "Frost Silver Ingot");
        add("item.ascension.frost_silver_nugget", "Frost Silver Nugget");
        add("item.ascension.jade", "Jade");



        //Herb Items
        add("item.ascension.jade_dew_grass", "Jade Dew Grass");
        add("item.ascension.jade_dew_grass_seeds", "Jade Dew Grass Seed");
        add("item.ascension.ginseng", "Ginseng");
        add("item.ascension.fire_ginseng", "Fire Ginseng");
        add("item.ascension.snow_ginseng", "Snow Ginseng");
        add("item.ascension.nine_sun_fire_root", "Nine-Sun Fire Root");
        add("item.ascension.moonwell_jade_lotus", "Moonwell Jade Lotus");
        add("item.ascension.heavenly_thunder_peach", "Heavenly Thunder Peach");
        add("item.ascension.lingzhi_mushroom", "Lingzhi");
        add("item.ascension.blood_lingzhi_mushroom", "Blood Lingzhi");
        add("item.ascension.white_jade_orchid", "White Jade Orchid");
        add("item.ascension.peach", "Peach");

        // Herb Tooltips
        add("ascension.herb.tooltip.aged_name", "%s-Year %s");
        add("ascension.herb.tooltip.type", "Herb");
        add("ascension.herb.tooltip.age", "Age");
        add("ascension.herb.tooltip.age_value", "%s Years");
        add("ascension.herb.tooltip.quality", "Quality");
        add("ascension.herb.tooltip.origin", "Origin");
        add("ascension.herb.origin.wild", "Wild");
        add("ascension.herb.origin.cultivated", "Cultivated");
        add("ascension.herb.quality.poor", "Poor");
        add("ascension.herb.quality.common", "Common");
        add("ascension.herb.quality.good", "Good");
        add("ascension.herb.quality.superior", "Superior");
        add("ascension.herb.quality.perfect", "Perfect");
        add("ascension.herb.related.type.seed", "Herb Seed");
        add("ascension.herb.related.type.plant", "Herb Plant");
        add("ascension.herb.related.seed.description", "Used to grow %s.");
        add("ascension.herb.related.plant.description", "A placeable specimen of %s.");
        add("ascension.herb.related.seed.target", "Grows Into");
        add("ascension.herb.related.plant.target", "Produces");


        // Herb descriptions for Tooltips
        add("ascension.herb.jade_dew_grass.description", "A delicate herb whose leaves gather cool beads of dew.");
        add("ascension.herb.ginseng.description", "A medicinal root prized for the strength it gathers with age.");
        add("ascension.herb.fire_ginseng.description", "A blazing ginseng root that carries a distinct fiery nature.");
        add("ascension.herb.snow_ginseng.description", "A frozen ginseng root adapted to cold and snowy lands.");
        add("ascension.herb.nine_sun_fire_root.description", "A sun-fed root that thrives in scorching lands and holds heat within its flesh.");
        add("ascension.herb.moonwell_jade_lotus.description", "A pale jade lotus nourished by water Qi, rain, and the cold radiance of the night sky.");
        add("ascension.herb.heavenly_thunder_peach.description", "A rare peach whose flesh ripens slowly until storms and thunder drive its spiritual nature awake.");
        add("ascension.herb.lingzhi_mushroom.description", "A medicinal fungus that grows from old wood.");
        add("ascension.herb.blood_lingzhi_mushroom.description", "A crimson Lingzhi that devoured the blood from dead Ghast rending it nothing but bones.");
        add("ascension.herb.peach.description", "W.I.P");
        add("ascension.herb.white_jade_orchid.description", "W.I.P");


        //Block Ores
        add("block.ascension.black_iron_ore", "Black Iron Ore");
        add("block.ascension.black_iron_block", "Black Iron Block");
        add("block.ascension.frost_silver_ore", "Frost Silver Ore");
        add("block.ascension.frost_silver_block", "Frost Silver Block");

        add("block.ascension.jade_ore", "Jade Ore");
        add("block.ascension.jade_block", "Jade Block");


        //Fluids
        add("item.ascension.liquified_spiritual_qi_bucket", "Liquified Spiritual Qi Bucket");
        add("block.ascension.liquified_spiritual_qi_block", "Liquified Spiritual Qi");


        //Wood Blocks
        add("block.ascension.peach_log", "Peach Log");
        add("block.ascension.peach_wood", "Peach Wood");
        add("block.ascension.stripped_peach_log", "Stripped Peach Log");
        add("block.ascension.stripped_peach_wood", "Stripped Peach Wood");
        add("block.ascension.peach_planks", "Peach Planks");
        add("block.ascension.peach_leaves", "Peach Leaves");
        add("block.ascension.peach_sapling", "Peach Sapling");
        add("block.ascension.potted_peach_sapling", "Potted Peach Sapling");

        //Block Entity
        add("block.ascension.fermenting_barrel", "Fermenting Barrel");


        //Herb Blocks
        add("block.ascension.cultivation_soil", "Cultivation Soil");
        add("block.ascension.jade_dew_grass_crop", "Jade Dew Grass");
        add("block.ascension.ginseng_crop", "Ginseng");
        add("block.ascension.fire_ginseng_crop", "Fire Ginseng");
        add("block.ascension.snow_ginseng_crop", "Snow Ginseng");
        add("block.ascension.nine_sun_fire_root_crop", "Nine-Sun Fire Root");
        add("block.ascension.moonwell_jade_lotus_crop", "Moonwell Jade Lotus");
        add("block.ascension.heavenly_thunder_peach_pod", "Heavenly Thunder Peach Pod");
        add("block.ascension.lingzhi_mushroom_b", "Lingzhi Mushroom");
        add("block.ascension.blood_lingzhi_mushroom_b", "Blood Lingzhi Mushroom");
        add("block.ascension.peach_pod", "Peach Pod");
        add("block.ascension.white_jade_orchid_crop", "White Jade Orchid");



        // GUI
        add("gui.ascension.introspection.title", "Introspection");
        add("gui.ascension.introspection.main", "Main");
        add("gui.ascension.introspection.stats", "Stats");
        add("gui.ascension.introspection.skills", "Skills");
        add("gui.ascension.introspection.cultivation", "Cultivation");
        add("gui.ascension.introspection.paths", "Paths");
        add("gui.ascension.introspection.techniques", "Techniques");
        add("gui.ascension.introspection.physique", "Physique");
        add("gui.ascension.introspection.bloodlines", "Bloodlines");
        add("gui.ascension.introspection.bloodline_count", "Bloodlines (%s)");
        add("gui.ascension.introspection.bloodline_multiple", "%s +%s");
        add("gui.ascension.introspection.bloodline_purity", "Purity: %s%%");
        add("gui.ascension.introspection.technique", "Technique");
        add("gui.ascension.introspection.cultivation_technique", "Cultivation Technique");
        add("gui.ascension.introspection.battle_style", "Battle Style");
        add("gui.ascension.introspection.none", "None");
        add("gui.ascension.introspection.no_physique", "No physique");
        add("gui.ascension.introspection.no_bloodlines", "No bloodlines");
        add("gui.ascension.introspection.no_paths", "No cultivation paths");
        add("gui.ascension.introspection.no_paths_description", "This character has not acquired a cultivation path.");
        add("gui.ascension.introspection.no_techniques", "No techniques");
        add("gui.ascension.introspection.no_techniques_description", "This character has not acquired any techniques.");
        add("gui.ascension.introspection.no_skills", "No skills");
        add("gui.ascension.introspection.no_skills_description", "This character has not acquired any skills.");
        add("gui.ascension.introspection.no_stats", "No Ascension stats are currently available.");
        add("gui.ascension.introspection.data_unavailable", "Synchronized Ascension data is temporarily unavailable.");
        add("gui.ascension.introspection.missing_registry_entry", "The synchronized identifier is not present in the client registry.");
        add("gui.ascension.introspection.empty_slot", "Empty");
        add("gui.ascension.introspection.castable_skill", "Castable skill. Select it, then click a hotbar slot below to assign or remove it.");
        add("gui.ascension.introspection.toggle_passive.enable", "Enable passive");
        add("gui.ascension.introspection.toggle_passive.disable", "Disable passive");
        add("gui.ascension.introspection.back", "Back");
        add("gui.ascension.introspection.skill_mastery", "Mastery: %s");
        add("gui.ascension.introspection.path_progress", "Path: %s%%");
        add("gui.ascension.introspection.skill_slots.open", "Show skill slots");
        add("gui.ascension.introspection.skill_slots.close", "Hide skill slots");
        add("gui.ascension.introspection.cultivation_suppression.enable", "Enable foundation growth");
        add("gui.ascension.introspection.cultivation_suppression.disable", "Disable foundation growth");
        add("gui.ascension.starter.confirm", "Confirm selection");
        add("gui.ascension.starter.cancel", "Clear selection");
        add("gui.ascension.introspection.attribute.health", "Health");
        add("gui.ascension.introspection.attribute.damage", "Damage");
        add("gui.ascension.introspection.attribute.armor", "Armor");
        add("gui.ascension.introspection.attribute.toughness", "Toughness");
        add("gui.ascension.introspection.attribute.attack_speed", "Attack Speed");
        add("gui.ascension.introspection.attribute.speed", "Speed");
        add("gui.ascension.introspection.attribute.jump", "Jump");
        add("gui.ascension.introspection.attribute.step_height", "Step Height");
        add("gui.ascension.introspection.attribute.mining", "Mining");
        add("gui.ascension.introspection.recovery", "Recovery");
        add("gui.ascension.introspection.recovery.health", "Health");
        add("gui.ascension.introspection.recovery.qi", "Qi");
        add("gui.ascension.introspection.recovery.stamina", "Stamina");
        add("gui.ascension.introspection.recovery.per_second", "%s/s");
        add("gui.ascension.introspection.stat_tooltip.raw", "Raw: %s");
        add("gui.ascension.introspection.stat_tooltip.realm_effectiveness", "Realm Effectiveness: ×%s");
        add("gui.ascension.introspection.stat_tooltip.effective", "Effective: %s");



        // Starter Screen Lang
        add("gui.ascension.starter.title", "Starting Selection");
        add("gui.ascension.starter.choose_bloodline", "Choose Your Bloodline");
        add("gui.ascension.starter.choose_physique", "Choose Your Physique");
        add("gui.ascension.starter.physique_paths", "Paths: %s");
        add("gui.ascension.starter.incomplete_notice", "Starter selection incomplete. It will reopen when you rejoin or respawn.");
        add("gui.ascension.path_progress.tooltip", "%s (%s%%)");



        // Stats
        add("stat.ascension.vitality", "Vitality");
        add("stat.ascension.agility", "Agility");
        add("stat.ascension.strength", "Strength");
        add("stat.ascension.spirit", "Spirit");



        //Key Items
        add("item.ascension.physique_essence", "Physique Essence");
        add("item.ascension.bloodline_essence", "Bloodline Essence");
        add("item.ascension.technique_manual", "Technique Manual");
        add("item.ascension.technique_manual.battle_style", "Battle Style");
        add("item.ascension.technique_manual.cultivation_technique", "Cultivation Technique");

        // Transfer Item Tooltips
        add("ascension.tooltip.bloodline.purity", "Purity");
        add("ascension.tooltip.bloodline.purity_growth", "Purity Growth");
        add("ascension.tooltip.physique.stats_affinities", "Stats & Affinities");
        add("ascension.tooltip.physique.stats", "Stats");
        add("ascension.tooltip.physique.affinities", "Affinities");
        add("ascension.tooltip.technique.max_realm", "Max Realm");
        add("ascension.tooltip.technique.realm_growth", "Realm Growth");

        // Progression Descriptions
        add("ascension.tooltip.progression.affinity", "%s Affinity");
        add("ascension.tooltip.progression.path_bonus", "%s %s");
        add("ascension.tooltip.progression.unlock", "Unlock");
        add("ascension.tooltip.progression.level", "Lv. %s");
        add("ascension.tooltip.progression.beyond_comprehension", "Beyond Comprehension");
        add("ascension.tooltip.value.percent", "%s%%");
        add("ascension.tooltip.progression.condition.conditional", "Conditional");
        add("ascension.tooltip.progression.condition.on_acquisition", "On Acquisition");
        add("ascension.tooltip.progression.condition.at_purity", "At %s%% Purity");
        add("ascension.tooltip.progression.condition.each_purity", "On Acquisition + Each %s%% Purity");
        add("ascension.tooltip.progression.condition.purity_range", "Each %s%% Purity (%s-%s%%)");
        add("ascension.tooltip.progression.condition.each_realm", "On Learning + Each Realm");
        add("ascension.tooltip.progression.condition.each_minor_realm", "Each Minor Realm");
        add("ascension.tooltip.progression.condition.each_major_realm", "Each Major Realm");
        add("ascension.tooltip.progression.condition.on_learn", "On Learning");
        add("ascension.tooltip.progression.condition.major_realm", "Major Realm %s");
        add("ascension.tooltip.progression.condition.major_realms", "Major Realms %s");
        add("ascension.tooltip.progression.condition.minor_realm", "Minor Realm %s");
        add("ascension.tooltip.progression.condition.minor_realms", "Minor Realms %s");
        add("ascension.tooltip.progression.condition.selected_realms", "Selected Realms");

        //Artifacts Tooltips
        add("ascension.tooltip.jade_bottle.page", "Jade Bottle");
        add("ascension.tooltip.jade_bottle", "Jade Bottle");
        add("ascension.tooltip.jade_bottle.contained", "Contained");
        add("ascension.tooltip.jade_bottle.contained_pill", "Contained Pill");
        add("ascension.tooltip.jade_bottle.expiration", "Expiration");



        //Keybinds
        add("key.category.minecraft.ascension", "Ascension");
        add("key.ascension.cycle_mode", "Cycle Modes");
        add("key.ascension.open_introspection", "Open Introspection");
        add("key.ascension.open_skill_wheel", "Open Skill Wheel");
        add("key.ascension.skill.skill_cast", "Cast Skill");



        //Classifications
        add("zenith.ascension.category.bloodline", "Bloodline");
        add("zenith.ascension.category.physique", "Physique");
        add("zenith.ascension.category.technique", "Technique");
        add("zenith.ascension.category.artifacts", "Artifacts");
        add("zenith.ascension.category.herbs", "Herbs");

        add("zenith.ascension.tier.ordinary", "Ordinary");
        add("zenith.ascension.tier.profound", "Profound");
        add("zenith.ascension.tier.heaven", "Heaven");
        add("zenith.ascension.tier.saint", "Saint");
        add("zenith.ascension.tier.god", "God");
        add("zenith.ascension.tier.heavens_path", "Heaven's Path");



        // DATAPACK LANG STUFF

        // Paths

        //Paths - Foundation
        add("ascension.path.foundation.essence.name", "Essence");
        add("ascension.path.foundation.essence.desc", "cultivate the essence that permeates existence");
        add("ascension.path.foundation.body.name", "Body");
        add("ascension.path.foundation.body.desc", "temper the flesh until it rivals iron and stone");
        add("ascension.path.foundation.soul.name", "Soul");
        add("ascension.path.foundation.soul.desc", "awaken the spirit within and forge it into an unyielding will");

        //Paths - Elemental
        add("ascension.path.elemental.water.name", "Water");
        add("ascension.path.elemental.water.desc", "cultivate the essence of life");
        add("ascension.path.elemental.ice.name", "Ice");
        add("ascension.path.elemental.ice.desc", "cultivate the cold");
        add("ascension.path.elemental.fire.name", "Fire");
        add("ascension.path.elemental.fire.desc", "cultivate the flame that consumes and purifies");
        add("ascension.path.elemental.earth.name", "Earth");
        add("ascension.path.elemental.earth.desc", "cultivate the weight and permanence of stone");
        add("ascension.path.elemental.metal.name", "Metal");
        add("ascension.path.elemental.metal.desc", "cultivate the edge and hardness of forged ore");
        add("ascension.path.elemental.wood.name", "Wood");
        add("ascension.path.elemental.wood.desc", "cultivate the growth and resilience of living green");
        add("ascension.path.elemental.wind.name", "Wind");
        add("ascension.path.elemental.wind.desc", "cultivate the freedom of the ceaseless sky");
        add("ascension.path.elemental.lightning.name", "Lightning");
        add("ascension.path.elemental.lightning.desc", "cultivate the wrath of the heavens made manifest");
        add("ascension.path.elemental.blood.name", "Blood");
        add("ascension.path.elemental.blood.desc", "cultivate the vital fluid that fuels rage and life alike");
        add("ascension.path.elemental.life.name", "Life");
        add("ascension.path.elemental.life.desc", "cultivate the force that grows, heals, and endures");
        add("ascension.path.elemental.death.name", "Death");
        add("ascension.path.elemental.death.desc", "cultivate the silence that follows all things");
        add("ascension.path.elemental.light.name", "Light");
        add("ascension.path.elemental.light.desc", "cultivate the radiance that drives back the dark");
        add("ascension.path.elemental.dark.name", "Dark");
        add("ascension.path.elemental.dark.desc", "cultivate the shadow that swallows all light");
        add("ascension.path.elemental.poison.name", "Poison");
        add("ascension.path.elemental.poison.desc", "cultivate the blight that corrupts from within");
        add("ascension.path.elemental.yin.name", "Yin");
        add("ascension.path.elemental.yin.desc", "cultivate the receptive, cold, and hidden half of existence");
        add("ascension.path.elemental.yang.name", "Yang");
        add("ascension.path.elemental.yang.desc", "cultivate the active, radiant, and assertive half of existence");
        add("ascension.path.elemental.space.name", "Space");
        add("ascension.path.elemental.space.desc", "cultivate mastery over distance and dimension");
        add("ascension.path.elemental.time.name", "Time");
        add("ascension.path.elemental.time.desc", "cultivate mastery over the flow of moments");
        add("ascension.path.elemental.gravity.name", "Gravity");
        add("ascension.path.elemental.gravity.desc", "cultivate the pull that binds all things down");
        add("ascension.path.elemental.sound.name", "Sound");
        add("ascension.path.elemental.sound.desc", "cultivate the vibration that carries through all things");
        add("ascension.path.elemental.illusion.name", "Illusion");
        add("ascension.path.elemental.illusion.desc", "cultivate the veil that bends perception and truth");
        add("ascension.path.elemental.karma.name", "Karma");
        add("ascension.path.elemental.karma.desc", "cultivate the threads of cause and consequence");
        add("ascension.path.elemental.fate.name", "Fate");
        add("ascension.path.elemental.fate.desc", "cultivate the destiny woven for all living things");
        add("ascension.path.elemental.star.name", "Star");
        add("ascension.path.elemental.star.desc", "cultivate the light and fire of the distant heavens");
        add("ascension.path.elemental.moon.name", "Moon");
        add("ascension.path.elemental.moon.desc", "cultivate the quiet tide-pulling light of the night sky");
        add("ascension.path.elemental.sun.name", "Sun");
        add("ascension.path.elemental.sun.desc", "cultivate the searing, life-giving light of day");
        add("ascension.path.elemental.chaos.name", "Chaos");
        add("ascension.path.elemental.chaos.desc", "cultivate the untamed disorder that existed before creation");
        add("ascension.path.elemental.order.name", "Order");
        add("ascension.path.elemental.order.desc", "cultivate the immutable law that binds the cosmos together");
        add("ascension.path.elemental.reincarnation.name", "Reincarnation");
        add("ascension.path.elemental.reincarnation.desc", "cultivate the endless wheel of death and rebirth");

        //Paths - Weapon
        add("ascension.path.weapon.sword.name", "Sword");
        add("ascension.path.weapon.sword.desc", "Slice the heavens");
        add("ascension.path.weapon.blade.name", "Blade");
        add("ascension.path.weapon.blade.desc", "cultivate the swift, cleaving edge of the single-edged blade");
        add("ascension.path.weapon.spear.name", "Spear");
        add("ascension.path.weapon.spear.desc", "cultivate the reach and precision of the thrusting spear");
        add("ascension.path.weapon.knife.name", "Knife");
        add("ascension.path.weapon.knife.desc", "cultivate the speed and precision of close-quarter steel");
        add("ascension.path.weapon.bow.name", "Bow");
        add("ascension.path.weapon.bow.desc", "cultivate the patience and precision of the drawn string");
        add("ascension.path.weapon.fist.name", "Fist");
        add("ascension.path.weapon.fist.desc", "cultivate the body and will behind an unarmed strike");
        add("ascension.path.weapon.shield.name", "Shield");
        add("ascension.path.weapon.shield.desc", "cultivate the unmoving resolve of one who stands between");
        add("ascension.path.weapon.mace.name", "Mace");
        add("ascension.path.weapon.mace.desc", "cultivate the raw crushing weight of blunt force");
        add("ascension.path.weapon.staff.name", "Staff");
        add("ascension.path.weapon.staff.desc", "cultivate the flowing, versatile forms of the long staff");
        add("ascension.path.weapon.axe.name", "Axe");
        add("ascension.path.weapon.axe.desc", "cultivate the heavy, splitting momentum of the axe");
        add("ascension.path.weapon.hammer.name", "Hammer");
        add("ascension.path.weapon.hammer.desc", "cultivate the earth-shaking force of the war hammer");
        add("ascension.path.weapon.whip.name", "Whip");
        add("ascension.path.weapon.whip.desc", "cultivate the flexible, coiling strikes of the whip");
        add("ascension.path.weapon.halberd.name", "Halberd");
        add("ascension.path.weapon.halberd.desc", "cultivate the sweeping reach of blade and pole combined");
        add("ascension.path.weapon.fan.name", "Fan");
        add("ascension.path.weapon.fan.desc", "cultivate the elegant, deceptive art of the folding fan");
        add("ascension.path.weapon.umbrella.name", "Umbrella");
        add("ascension.path.weapon.umbrella.desc", "cultivate the deceptive grace of the warding canopy, sheltering blow and blade alike");



        //Physiques

        //Ordinary Physiques
        add("ascension.physique.five_elements_imbalance.name", "Five Elements Imbalance");
        add("ascension.physique.five_elements_imbalance.desc", "The five elements within the body are in chaos, causing cultivation instability and frequent qi deviations. Those who persevere with this may be surprised.");
        add("ascension.physique.brittle_bone_body.name", "Brittle Bone Body");
        add("ascension.physique.brittle_bone_body.desc", "Bones are fragile and prone to breaking under stress, severely limiting physical combat and body cultivation. Those born with this physique can only cultivate Soul.");
        add("ascension.physique.heavy_bone_physique.name", "Heavy Bone Physique");
        add("ascension.physique.heavy_bone_physique.desc", "Bones are abnormally heavy, making the user slow and lumbering, though slightly resistant to physical force. Those who cultivate Body with this physique will achieve success.");
        add("ascension.physique.iron_blossom_body.name", "Iron Blossom Body");
        add("ascension.physique.iron_blossom_body.desc", "A body as unyielding as stone yet still capable of growth, standing firm behind a shield like a flower rooted in bedrock.");
        add("ascension.physique.cursed_body_physique.name", "Cursed Body Physique");
        add("ascension.physique.cursed_body_physique.desc", "A body afflicted by a powerful curse that weakens normal cultivation and attracts misfortune. The Curse may only be mitigated with Demonic Techniques.");
        add("ascension.physique.dampened_fire_body_physique.name", "Dampened Fire Body");
        add("ascension.physique.dampened_fire_body_physique.desc", "A body with a natural dampness that extinguishes fire, making fire techniques unusable.");
        add("ascension.physique.sword_body_flawed_physique.name", "Sword Body (Flawed)");
        add("ascension.physique.sword_body_flawed_physique.desc", "Natural affinity for swords but with a flaw that causes instability in the user's qi and sword intent.");
        add("ascension.physique.ice_soul_body_flawed_physique.name", "Ice Soul Body (Flawed)");
        add("ascension.physique.ice_soul_body_flawed_physique.desc", "Strong ice affinity but the cold energy damages the body over time, causing frostbite and organ decay.");
        add("ascension.physique.fire_spirit_body_unstable_physique.name", "Fire Spirit Body (Unstable)");
        add("ascension.physique.fire_spirit_body_unstable_physique.desc", "Strong fire affinity but the fire energy is volatile and hard to control, often burning the user from within.");
        add("ascension.physique.thunder_body_unstable_physique.name", "Thunder Body (Unstable)");
        add("ascension.physique.thunder_body_unstable_physique.desc", "Lightning affinity but the electricity damages the body's meridians and nerves with each use.");
        add("ascension.physique.trembling_earth_body_unstable_physique.name", "Trembling Earth Body (Unstable)");
        add("ascension.physique.trembling_earth_body_unstable_physique.desc", "Earth affinity but the user's body trembles uncontrollably, making fine movements and precise techniques difficult. The tremors also destabilize the ground around them.");
        add("ascension.physique.shallow_core_body_physique.name", "Shallow Core Body");
        add("ascension.physique.shallow_core_body_physique.desc", "The dantian is shallow and cannot hold much qi, severely limiting combat endurance.");
        add("ascension.physique.frostbitten_vein_body_physique.name", "Frostbitten Vein Body");
        add("ascension.physique.frostbitten_vein_body_physique.desc", "Veins are permanently frostbitten, causing pain when qi circulates and reducing overall flow.");
        add("ascension.physique.miasma_lungs_body_physique.name", "Miasma Lungs Body");
        add("ascension.physique.miasma_lungs_body_physique.desc", "Lungs are filled with miasma, making breathing difficult and qi circulation weak and tainted.");
        add("ascension.physique.weak_spirit_body_physique.name", "Weak Spirit Body");
        add("ascension.physique.weak_spirit_body_physique.desc", "A body with weak spiritual roots, making it difficult to absorb and cultivate qi. Progress is excruciatingly slow.");
        add("ascension.physique.sword_bone.name", "Sword Bone");
        add("ascension.physique.sword_bone.desc", "Your bones are forged into blades, letting out a hum as they resonate with your blade");
        add("ascension.physique.starlit_meridians.name", "Starlit Meridians");
        add("ascension.physique.starlit_meridians.desc", "Fine meridians branch close to the skin, gathering faint star qi more readily than ordinary channels.");
        add("ascension.physique.iron_ant_bones.name", "Iron Ant Bones");
        add("ascension.physique.iron_ant_bones.desc", "The bones are compact and dark, bearing weight with ease and lending the limbs significant force.");
        add("ascension.physique.tiger_marrow.name", "Tiger Marrow");
        add("ascension.physique.tiger_marrow.desc", "The marrow is dense and vigorous, continually nourishing the bones and lending the body a tiger's force.");
        add("ascension.physique.ember_touched_meridians.name", "Ember-Touched Meridians");
        add("ascension.physique.ember_touched_meridians.desc", "The meridians remain naturally warm, allowing fire qi to circulate with less strain and making lesser flame spirits easier to accommodate.");

        //Profound Physiques
        add("ascension.physique.jade_furnace.name", "Jade Furnace Physique");
        add("ascension.physique.jade_furnace.desc", "The flesh tempers qi like a living furnace, drawing fire through the body and tempering them like volcanic channels.");
        add("ascension.physique.thousand_edge_meridians.name", "Thousand Edge Meridians");
        add("ascension.physique.thousand_edge_meridians.desc", "Meridians branch through the limbs like sharpened blades, carrying metal qi cleanly into swords, sabres, and knives.");
        add("ascension.physique.moonjade_marrow.name", "Moonjade Marrow");
        add("ascension.physique.moonjade_marrow.desc", "The marrow carries a cool jade lustre.");
        add("ascension.physique.golden_sun_heart.name", "Golden Sun Heart");
        add("ascension.physique.golden_sun_heart.desc", "A dense knot of yang qi rests around the heart, emblazing your blood.");
        add("ascension.physique.firefly_spirit_eyes.name", "Firefly Spirit Eyes");
        add("ascension.physique.firefly_spirit_eyes.desc", "Faint lights gather behind the pupils, making wandering spiritual qi easier to perceive and follow.");
        add("ascension.physique.hundred_venom_dantian.name", "Hundred Venom Dantian");
        add("ascension.physique.hundred_venom_dantian.desc", "The dantian tolerates poisonous and impure qi that would cripple ordinary cultivators, refining it without easily fouling the meridians.");
        add("ascension.physique.gale_cheetah_meridians.name", "Gale Cheetah Meridians");
        add("ascension.physique.gale_cheetah_meridians.desc", "Broad, springing meridians carry qi cleanly through the legs and hips.");
        add("ascension.physique.lion_roar_chest.name", "Lion-Roar Chest");
        add("ascension.physique.lion_roar_chest.desc", "The chest and lungs form a deep resonant chamber. Breath carries qi with force, giving sound cultivation a firm foundation.");
        add("ascension.physique.cold_spring_dantian.name", "Cold Spring Dantian");
        add("ascension.physique.cold_spring_dantian.desc", "The dantian resembles a clear, cold spring. Water qi settles easily within it, while colder currents can condense without disrupting circulation.");
        add("ascension.physique.thunder_spirit_veins.name", "Thunder Spirit Veins");
        add("ascension.physique.thunder_spirit_veins.desc", "Fine channels run through the limbs like branching lightning, letting violent thunder qi pass through flesh with unusual speed.");

        //Heaven Physiques
        add("ascension.physique.lunar_dream_eyes.name", "Lunar Dream Eyes");
        add("ascension.physique.lunar_dream_eyes.desc", "Eyes born beneath an unachievable moon perceive the seam between waking thought and dream.");
        add("ascension.physique.seven_star_bones.name", "Seven-Star Bones");
        add("ascension.physique.seven_star_bones.desc", "Seven points of star qi are sealed within the skeleton, tempering the bones over time.");
        add("ascension.physique.heavenly_river_dantian.name", "Heavenly River Dantian");
        add("ascension.physique.heavenly_river_dantian.desc", "The dantian forms a broad, tranquil sea where star qi drifts in a slow current.");
        add("ascension.physique.mantis_blade.name", "Mantis Blade Physique");
        add("ascension.physique.mantis_blade.desc", "The tendons and forearms naturally align with short cutting motions, making blade arts unnervingly quick and precise.");
        add("ascension.physique.jade_cocoon_body.name", "Jade Cocoon Body");
        add("ascension.physique.jade_cocoon_body.desc", "When qi is circulated inward, the flesh settles into a cocoon-like state that promotes recovery.");
        add("ascension.physique.nine_life_demon_cat_body.name", "Nine-Life Demon Cat Body");
        add("ascension.physique.nine_life_demon_cat_body.desc", "A rare demon-cat constitution whose souls clings stubbornly to life.");
        add("ascension.physique.shadow_leopard_veins.name", "Shadow Leopard Veins");
        add("ascension.physique.shadow_leopard_veins.desc", "Dark qi settles naturally within meridians running through the limbs.");
        add("ascension.physique.five_element_spirit_vessel.name", "Five-Element Spirit Vessel");
        add("ascension.physique.five_element_spirit_vessel.desc", "An unusually balanced vessel whose essence can nourish spirits of wood, fire, earth, metal, and water without strongly rejecting any one of them.");
        add("ascension.physique.spirit_furnace_dantian.name", "Spirit Furnace Dantian");
        add("ascension.physique.spirit_furnace_dantian.desc", "A furnace-like chamber forms within the dantian, giving living flame qi a place to settle and be nurtured without scorching its host.");

        // Saint Physiques
        add("ascension.physique.great_sun_sacred_body.name", "Great Sun Sacred Body");
        add("ascension.physique.great_sun_sacred_body.desc", "A constitution whose blood, marrow, and meridians carry the heat of a rising sun. Yang qi gathers without urging, cold struggle to take root, and every circulation tempers the body as though beneath blistering light.");

        // God Physiques
        add("ascension.physique.primordial_chaos_dao_body.name", "Primordial Chaos Dao Body");
        add("ascension.physique.primordial_chaos_dao_body.desc", "A constitution born with an essence sea that accepts both chaos and order. Opposing forces settle within the same circulation without immediate collapse, while space, time, and fate find unusually little resistance within its meridians.");
        add("ascension.physique.ethereal_grandmist_connate_physique.name", "Ethereal Grandmist Connate Physique");
        add("ascension.physique.ethereal_grandmist_connate_physique.desc", "A connate soul constitution filled with pale grandmist. Starlight sinks into the mist while gravity gathers around the spirit like a tide, making vast and weighty soul arts simple to cultivate.");
        add("ascension.physique.azure_ashura_divine_general_form.name", "Azure Ashura Divine General Form");
        add("ascension.physique.azure_ashura_divine_general_form.desc", "A divine war-form whose blood moves like an azure tide through a resilient frame. Water qi tempers its violence, blood qi feeds its recovery, and every fist carries the momentum of a general.");

        // Heavens Path Physiques
        add("ascension.physique.nine_heavens_myriad_worlds_dao_body.name", "Nine Heavens Myriad Worlds Dao Body");
        add("ascension.physique.nine_heavens_myriad_worlds_dao_body.desc", "An unimaginable physique said to mirror the structure of the heavens themselves. Qi turns through flesh, marrow, and acupoints in eonic cycles, as though every point of the body holds the shadow of a world. Spatio-temporal rivers nourish the body, flooding the hidden shadows and letting them flourish. Laws and Daos seek out the worlds within the folds of this body, fighting for acceptance.");
        add("ascension.physique.soul_sovereignty_revolution_throne_trinity.name", "Threefold Soul Sovereignty Throne");
        add("ascension.physique.soul_sovereignty_revolution_throne_trinity.desc", "Three sovereign thrones revolve within the soul, each presiding over thought, dreams, and the pull of fate. The body is merely their heavenly court; the spirit is the divine kingdom, and lesser souls struggle to shake its rule.");
        add("ascension.physique.thirty_three_divines_chaotic_vessel_meridians.name", "Thirty Three Divines Chaotic Vessel Meridians");
        add("ascension.physique.thirty_three_divines_chaotic_vessel_meridians.desc", "Thirty-three impossible vessel meridians overlap the ordinary channels, each carrying a different current through the essence sea. Chaos rushes between them without tearing the body apart, while divine light threads the disorder into patterns of sheer purity.");



        //Bloodlines
        add("ascension.bloodline.human.name", "Human");
        add("ascension.bloodline.human.desc", "The most common and adaptable of mortal bloodlines, balanced in body and spirit");
        add("ascension.bloodline.barbarian.name", "Barbarian");
        add("ascension.bloodline.barbarian.desc", "Descendants of the untamed tribes beyond the sect lands, their bodies honed by hardship into raw, unyielding power");
        add("ascension.bloodline.beastkin.name", "Beastkin");
        add("ascension.bloodline.beastkin.desc", "Mortals born with a trace of ancient spirit beast blood in their veins, granting fierce vitality and primal instinct");
        add("ascension.bloodline.scalekin.name", "Scalekin");
        add("ascension.bloodline.scalekin.desc", "Descendants of the deep-water jiao, their skin bears faint scales and their bodies endure where lesser mortals would drown");
        add("ascension.bloodline.frostkin.name", "Frostkin");
        add("ascension.bloodline.frostkin.desc", "Descendants of the tribes that endured the frozen wastes for generations, their blood carries an innate resistance to the cold and a body built to endure");
        add("ascension.bloodline.verdantblood.name", "Verdantblood");
        add("ascension.bloodline.verdantblood.desc", "A mortal lineage steeped in old forest qi, whose blood recovers stubbornly and takes to wood cultivation with ease.");
        add("ascension.bloodline.moonveil_fox.name", "Moonveil Fox Bloodline");
        add("ascension.bloodline.moonveil_fox.desc", "A spirit-beast lineage carrying the blood of moon foxes.");
        add("ascension.bloodline.azure_thunder_dragon.name", "Azure Thunder Dragon Bloodline");
        add("ascension.bloodline.azure_thunder_dragon.desc", "A rare draconic inheritance in which storm and deep water mingle in the blood.");
        add("ascension.bloodline.falling_star.name", "Falling Star Bloodline");
        add("ascension.bloodline.falling_star.desc", "A mortal lineage born of those living within meteor fields.");
        add("ascension.bloodline.moon_white_crane.name", "Moon-White Crane Bloodline");
        add("ascension.bloodline.moon_white_crane.desc", "A spirit-beast lineage descended from glimmering cranes that nest above moonlit cloud seas.");
        add("ascension.bloodline.purple_star_qilin.name", "Purple Star Qilin Bloodline");
        add("ascension.bloodline.purple_star_qilin.desc", "An auspicious qilin lineage tied to the purple sovereign star.");
        add("ascension.bloodline.black_iron_ant.name", "Black-Iron Ant Bloodline");
        add("ascension.bloodline.black_iron_ant.desc", "A low-grade demonic insect lineage descended from black-iron ants, prized for dense flesh and strength far beyond their size.");
        add("ascension.bloodline.jade_cicada.name", "Jade Cicada Bloodline");
        add("ascension.bloodline.jade_cicada.desc", "A spirit-insect lineage known for shedding its shell as it matures.");
        add("ascension.bloodline.golden_silkworm.name", "Golden Silkworm Bloodline");
        add("ascension.bloodline.golden_silkworm.desc", "A rare gu lineage descended from golden silkworms raised on medicinal poisons. Its qi is dense and venomous, and its threads harden like metal.");
        add("ascension.bloodline.wind_chasing_leopard.name", "Wind-Chasing Leopard Bloodline");
        add("ascension.bloodline.wind_chasing_leopard.desc", "A minor spirit-beast lineage descended from mountain leopards that hunt along windy ridges.");
        add("ascension.bloodline.golden_maned_lion.name", "Golden-Maned Lion Bloodline");
        add("ascension.bloodline.golden_maned_lion.desc", "A proud spirit-beast lineage whose warm yang blood strengthens the body and gives the voice a natural resonance with qi.");
        add("ascension.bloodline.white_tiger.name", "White Tiger Bloodline");
        add("ascension.bloodline.white_tiger.desc", "A rare lineage carrying a trace of the White Tiger, sacred beast of the western heavens.");
        add("ascension.bloodline.ember_spirit.name", "Ember Spirit Bloodline");
        add("ascension.bloodline.ember_spirit.desc", "A minor lineage altered by generations spent beside small fire spirits. Its qi runs warm and answers readily to flame.");
        add("ascension.bloodline.deep_spring_spirit.name", "Deep Spring Spirit Bloodline");
        add("ascension.bloodline.deep_spring_spirit.desc", "A spirit-blooded lineage descended from beings born in old mountain springs. Its qi is cool, patient, and difficult to exhaust.");
        add("ascension.bloodline.verdant_flame_spirit.name", "Verdant Flame Spirit Bloodline");
        add("ascension.bloodline.verdant_flame_spirit.desc", "A rare lineage carrying the nature of a living green flame that feeds upon spiritual herbs. Its fire burns cleanly and is highly prized by refiners.");
        add("ascension.bloodline.sun_crowned_phoenix.name", "Sun-Crowned Phoenix Bloodline");
        add("ascension.bloodline.sun_crowned_phoenix.desc", "A phoenix lineage whose blood burns with a clear solar flame. Wounds close beneath its heat, exhausted qi rekindles readily, and each rise in purity strengthens the faint cycle of death and renewal hidden within the blood.");
        add("ascension.bloodline.void_devouring_kunpeng.name", "Void-Devouring Kunpeng Bloodline");
        add("ascension.bloodline.void_devouring_kunpeng.desc", "A lineage carrying a trace of the Kunpeng, the vast spirit beast said to cross sea and sky in a single transformation. Its blood hungers for distance itself, causing storms of wind and space with every beat.");
        add("ascension.bloodline.eternal_tribulation_ancestral_dragon.name", "Eternal Tribulation Ancestral Dragon Bloodline");
        add("ascension.bloodline.eternal_tribulation_ancestral_dragon.desc", "An impossible draconic inheritance said to descend from an ancestral dragon that tempered itself beneath heavenly tribulation before the laws of the world had fully settled. Lightning strengthened its blood, chaos fed its marrow, and every drop contains an immeasurable tyranny.");
        add("ascension.bloodline.celestial_war_tiger.name", "Celestial War Tiger Bloodline");
        add("ascension.bloodline.celestial_war_tiger.desc", "A tiger lineage descended from beasts that prowled ancient celestial battlefields, bringing ruin to all in their way.");
        add("ascension.bloodline.formless_river_dragon.name", "Formless River Dragon Bloodline");
        add("ascension.bloodline.formless_river_dragon.desc", "A river-dragon lineage whose blood refuses a fixed shape.");
        add("ascension.bloodline.dreamless_sovereign_turtle.name", "Dreamless Sovereign Turtle Bloodline");
        add("ascension.bloodline.dreamless_sovereign_turtle.desc", "An ancient sovereign-turtle lineage whose spirit rests in absolute silence.");
        add("ascension.bloodline.wilderness_stalker_dragon_turtle.name", "Wilderness Stalker Dragon Turtle Bloodline");
        add("ascension.bloodline.wilderness_stalker_dragon_turtle.desc", "A divine dragon-turtle lineage born in untamed continents beyond known lands. Its presence sinks into earth and ancient growth, masking immense strength beneath patience, erupting the moment it strikes.");
        add("ascension.bloodline.stargazing_virtuous_roc.name", "Stargazing Virtuous Roc Bloodline");
        add("ascension.bloodline.stargazing_virtuous_roc.desc", "A mythical roc lineage said to navigate by the sovereign stars rather than the horizon. Auspicious winds gather beneath its wings, causing swirls in fate with each beat.");
        add("ascension.bloodline.eighteen_winged_three_legged_glass_crow.name", "Eighteen-Winged Three-Legged Glass Crow Bloodline");
        add("ascension.bloodline.eighteen_winged_three_legged_glass_crow.desc", "An impossible solar-crow lineage bearing eighteen crystalline wings and three radiant legs. Its transparent blood refracts heavenly fire into searing light, as though a fragment of the true sun had learned to fly.");
        add("ascension.bloodline.virtuous_freedom_grand_immortal.name", "Virtuous Freedom Grand Immortal Bloodline");
        add("ascension.bloodline.virtuous_freedom_grand_immortal.desc", "A transcendent immortal lineage said to have cast off every worldly shackle without abandoning virtue.");


        //Techniques
        add("ascension.technique.eagle_claw_palm_technique.name", "Eagle Claw Palm Technique");
        add("ascension.technique.eagle_claw_palm_technique.desc", "A body art modeled on the hooked talons of mountain eagles, conditioning grip, forearms, and striking lines for sudden tearing palms.");
        add("ascension.technique.grasping_sand_technique.name", "Grasping Sand Technique");
        add("ascension.technique.grasping_sand_technique.desc", "An ordinary palm art built around rooted footing and crushing hand control, training the practitioner to seize what would normally slip through the fingers.");
        add("ascension.technique.iron_fist_tempering_manual.name", "Iron Fist Tempering Manual");
        add("ascension.technique.iron_fist_tempering_manual.desc", "A rough body-tempering manual that hardens knuckles, wrists, and forearms through repeated circulation and impact until the hands strike with the weight of iron.");
        add("ascension.technique.lightness_technique.name", "Lightness Technique");
        add("ascension.technique.lightness_technique.desc", "A common body art of weight transfer and explosive footwork, allowing short bursts of speed and daring leaps.");
        add("ascension.technique.martial_transcendence_vol_1.name", "Martial Transcendence, Volume I");
        add("ascension.technique.martial_transcendence_vol_1.desc", "The first volume of a progressive body scripture, stripping weakness from the mortal frame and establishing the foundation required for transcendence.");
        add("ascension.technique.nine_paths_of_truth.name", "Nine Paths of Truth");
        add("ascension.technique.nine_paths_of_truth.desc", "An eccentric essence scripture that seeks truth through refinements of the first realm, widening the same foundation again and again rather than rushing forward.");
        add("ascension.technique.shadowless_art.name", "Shadowless Art");
        add("ascension.technique.shadowless_art.desc", "A qigong essence art that threads qi through each step until motion seems to outrun its own shadow.");
        add("ascension.technique.sword_draw_manual.name", "Sword Draw Manual");
        add("ascension.technique.sword_draw_manual.desc", "A plain sword manual devoted to the instant between stillness and steel, cultivating the sword through a single decisive draw.");
        add("ascension.technique.burning_bones_technique.name", "Burning Bones Technique");
        add("ascension.technique.burning_bones_technique.desc", "A profound body technique that kindles fire within the marrow and uses the heat to temper bone from the inside out.");
        add("ascension.technique.martial_transcendence_vol_2.name", "Martial Transcendence, Volume II");
        add("ascension.technique.martial_transcendence_vol_2.desc", "The second volume rebuilds the refined mortal frame under harsher circulation, forcing bone, blood, and tendon to resonate as one body.");
        add("ascension.technique.rigid_stance_summation_technique.name", "Rigid Stance Summation Technique");
        add("ascension.technique.rigid_stance_summation_technique.desc", "A profound defensive body art that collects dozens of bracing principles into one immovable stance, turning endurance and structure into a shield.");
        add("ascension.technique.martial_transcendence_vol_3.name", "Martial Transcendence, Volume III");
        add("ascension.technique.martial_transcendence_vol_3.desc", "The third volume carries the martial body beyond mere refinement, making flesh, bone, breath, and will act as a single heaven-defying weapon.");
        add("ascension.technique.mount_hua_sword_manual.name", "Plum Blossom Sword Manual");
        add("ascension.technique.mount_hua_sword_manual.desc", "A sword manual of Mount Hua that binds breath and sword as one, its gentle, falling-petal forms hiding a killing edge just a heartbeat behind");
        add("ascension.technique.sustained_spirit_art.name", "Sustained Spirit Art");
        add("ascension.technique.sustained_spirit_art.desc", "An ordinary soul art that draws cold spiritual energy inward, quieting needless bodily activity to preserve nourishment and endurance");
        add("ascension.technique.imperial_seven_stances.name", "Seven Stance Imperial Army Art");
        add("ascension.technique.imperial_seven_stances.desc", "Seven practical sword stances drilled into the mortal armies of the Yonmeng Empire; modest alone, dependable in formation.");
        add("ascension.technique.celestial_constellation_circulation.name", "Celestial Constellation Circulation");
        add("ascension.technique.celestial_constellation_circulation.desc", "A soul inheritance that maps the night sky within the spirit and expresses its constellations through celestial archery");
        add("ascension.technique.guardian_dharma_manual.name", "Guardian Dharma Manual");
        add("ascension.technique.guardian_dharma_manual.desc", "A guardian manual that orders soul, life, and fist into endurance and a projected Dharma");
        add("ascension.technique.reapers_calling.name", "Reaper's Calling");
        add("ascension.technique.reapers_calling.desc", "Walk beside death, understand the burdens of the fallen, and guide each departing soul toward the turning river of samsara");
        add("ascension.technique.locust_bleeding_manual.name", "Locust Bleeding Manual");
        add("ascension.technique.locust_bleeding_manual.desc", "A cruel essence manual that nests blood-feeding locust qi inside a victim and lets the weakening swarm seek nearby flesh.");
        add("ascension.technique.nine_revolutions_slaughter_wheel.name", "Nine Revolutions Slaughter Wheel Technique");
        add("ascension.technique.nine_revolutions_slaughter_wheel.desc", "Carve nine interlocking revolutions into the essence sea, condensing a slaughter wheel that grows with each cycle.");
        add("ascension.technique.slaughter_wheel_battle_manual.name", "Slaughter Wheel Battle Manual");
        add("ascension.technique.slaughter_wheel_battle_manual.desc", "A combat scripture that teaches applications of the Slaughter Wheel.");
        add("ascension.technique.sanguine_abyss_technique.name", "Sanguine Abyss Technique");
        add("ascension.technique.sanguine_abyss_technique.desc", "A forbidden scripture that trades the cultivator's own blood and vitality for a swifter, crueler path through Essence, Blood, and Dark.");



        //Skills
        add("ascension.skill.divine_sense.name", "Divine Sense");
        add("ascension.skill.divine_sense.desc", "Release a pulse of spiritual perception that reveals nearby beings and items through obstacles.");

        add("ascension.skill.eagle_claw_rending_palm.name", "Eagle-Claw Rending Palm");
        add("ascension.skill.eagle_claw_rending_palm.desc", "Hook the hand like a talon and drive a short tearing palm through the opponent's guard.");
        add("ascension.skill.hooked_talon_grip.name", "Hooked Talon Grip");
        add("ascension.skill.hooked_talon_grip.desc", "Conditioned fingers and wrists lend greater force to empty-hand and fist-weapon strikes.");
        add("ascension.skill.grasping_sand_palm.name", "Grasping Sand Palm");
        add("ascension.skill.grasping_sand_palm.desc", "Drive a compact palm through the space ahead, catching several nearby opponents in the same crushing motion.");
        add("ascension.skill.rooted_grip.name", "Rooted Grip");
        add("ascension.skill.rooted_grip.desc", "A low, settled stance makes the body harder to shift and slightly softens incoming blows.");
        add("ascension.skill.iron_fist_tempering.name", "Iron Fist Tempering");
        add("ascension.skill.iron_fist_tempering.desc", "Circulate body qi through hand, wrist, and forearm, tempering the striking frame through repeated pressure.");
        add("ascension.skill.tempered_knuckles.name", "Tempered Knuckles");
        add("ascension.skill.tempered_knuckles.desc", "Hardened hands transfer more force through unarmed and fist-weapon attacks.");
        add("ascension.skill.iron_breaking_fist.name", "Iron-Breaking Fist");
        add("ascension.skill.iron_breaking_fist.desc", "Commit the tempered arm to a heavy forward punch capable of knocking an opponent off balance.");
        add("ascension.skill.featherstep.name", "Featherstep");
        add("ascension.skill.featherstep.desc", "Shift the body's weight in an instant and skim rapidly across the ground.");
        add("ascension.skill.cloud_leaping_step.name", "Cloud-Leaping Step");
        add("ascension.skill.cloud_leaping_step.desc", "Carry the principles of lightness into the air, bursting along the direction of your gaze.");
        add("ascension.skill.skimming_stride.name", "Skimming Stride");
        add("ascension.skill.skimming_stride.desc", "Habitual lightness training makes every ordinary movement quicker and less burdened.");
        add("ascension.skill.martial_transcendence_vol_1_circulation.name", "First Transcendence Circulation");
        add("ascension.skill.martial_transcendence_vol_1_circulation.desc", "Run body qi through the major muscle groups to refine the mortal frame without wasting motion.");
        add("ascension.skill.refined_mortal_frame.name", "Refined Mortal Frame");
        add("ascension.skill.refined_mortal_frame.desc", "The first stage of transcendence leaves flesh and musculature denser, stronger, and harder to exhaust.");
        add("ascension.skill.transcendent_impact.name", "Transcendent Impact");
        add("ascension.skill.transcendent_impact.desc", "Release the force gathered through the refined frame in one short, direct body strike.");
        add("ascension.skill.nine_paths_circulation.name", "Nine Paths Circulation");
        add("ascension.skill.nine_paths_circulation.desc", "Guide essence through repeated internal routes, polishing the same foundation from a different angle with every cycle.");
        add("ascension.skill.truth_seeking_channels.name", "Truth-Seeking Channels");
        add("ascension.skill.truth_seeking_channels.desc", "Repeated refinement steadies the essence channels, strengthening both spiritual clarity and bodily endurance.");
        add("ascension.skill.shadowless_step.name", "Shadowless Step");
        add("ascension.skill.shadowless_step.desc", "Push essence through the legs in a single pulse and cross the ground before the trailing shadow seems to catch up.");
        add("ascension.skill.unseen_circulation.name", "Unseen Circulation");
        add("ascension.skill.unseen_circulation.desc", "A quiet, economical circulation keeps the body light and the spirit alert even outside deliberate movement.");
        add("ascension.skill.sword_draw_meditation.name", "Sheathed Sword Meditation");
        add("ascension.skill.sword_draw_meditation.desc", "Cultivate the sword while it remains sheathed, binding breath and intent to the moment before the draw.");
        add("ascension.skill.one_breath_draw.name", "One-Breath Draw");
        add("ascension.skill.one_breath_draw.desc", "Release sword, breath, and forward momentum in one clean cut before returning to stillness.");
        add("ascension.skill.sheathed_edge.name", "Sheathed Edge");
        add("ascension.skill.sheathed_edge.desc", "Long practice at the draw sharpens ordinary sword attacks with a small but constant edge.");
        add("ascension.skill.burning_bone_tempering.name", "Burning Bone Tempering");
        add("ascension.skill.burning_bone_tempering.desc", "Draw fire through the marrow and let its heat temper the skeleton while body cultivation advances.");
        add("ascension.skill.cinder_bones.name", "Cinder Bones");
        add("ascension.skill.cinder_bones.desc", "Fire-tempered bones endure impact with greater stubbornness and resist being staggered.");
        add("ascension.skill.marrow_flame_fist.name", "Marrow-Flame Fist");
        add("ascension.skill.marrow_flame_fist.desc", "Drive heat from the marrow into a close-range strike that scorches the target from the point of impact.");
        add("ascension.skill.martial_transcendence_vol_2_circulation.name", "Second Transcendence Circulation");
        add("ascension.skill.martial_transcendence_vol_2_circulation.desc", "Force body qi through bone and tendon in a harsher circuit, reforging the frame established by the first volume.");
        add("ascension.skill.reforged_mortal_frame.name", "Reforged Mortal Frame");
        add("ascension.skill.reforged_mortal_frame.desc", "The second stage of transcendence binds strength, endurance, and speed into a sturdier whole.");
        add("ascension.skill.bone_resonance_strike.name", "Bone-Resonance Strike");
        add("ascension.skill.bone_resonance_strike.desc", "Set the skeleton ringing with gathered body qi and discharge that resonance through a compact blow.");
        add("ascension.skill.rigid_stance_breathing.name", "Rigid Stance Breathing");
        add("ascension.skill.rigid_stance_breathing.desc", "Root the feet, slow the breath, and cultivate while holding the body's structure against imagined pressure.");
        add("ascension.skill.summated_guard.name", "Summated Guard");
        add("ascension.skill.summated_guard.desc", "Layered stance principles reduce incoming force and make the body increasingly difficult to stagger.");
        add("ascension.skill.mountain_receives_strike.name", "Mountain Receives the Strike");
        add("ascension.skill.mountain_receives_strike.desc", "Brace every joint into one structure and raise a short-lived guard whose durability follows your Vitality.");
        add("ascension.skill.martial_transcendence_vol_3_circulation.name", "Third Transcendence Circulation");
        add("ascension.skill.martial_transcendence_vol_3_circulation.desc", "Circulate body qi through the entire frame as one system, refining flesh and spirit toward a genuinely transcendent body.");
        add("ascension.skill.body_beyond_mortal_limits.name", "Body Beyond Mortal Limits");
        add("ascension.skill.body_beyond_mortal_limits.desc", "A transcendent frame multiplies the body's core qualities while hardening it against damage and stagger.");
        add("ascension.skill.transcendent_body_shockwave.name", "Transcendent Body Shockwave");
        add("ascension.skill.transcendent_body_shockwave.desc", "Stamp and release the body's gathered force outward, battering every nearby opponent with a physical shockwave.");
        add("ascension.skill.heaven_breaking_fist.name", "Heaven-Breaking Fist");
        add("ascension.skill.heaven_breaking_fist.desc", "Hold the whole body behind one fist, charge its force, and release a blow whose power rises with the gathered moment.");
        add("ascension.skill.plum_blossom_breathing.name", "Plum Blossom Breathing");
        add("ascension.skill.plum_blossom_breathing.desc", "The first breathing method taught at Mount Hua, circulating qi until it flows naturally into the sword, letting sword and essence grow together as one");
        add("ascension.skill.stillfrost_breathing.name", "Stillfrost Breathing");
        add("ascension.skill.stillfrost_breathing.desc", "Breathe slowly and draw frost into the soul, cultivating inner stillness while refining Soul through Ice");
        add("ascension.skill.sustained_spirit.name", "Sustained Spirit");
        add("ascension.skill.sustained_spirit.desc", "Cold spiritual stillness suppresses wasteful movement, reducing exhaustion and stamina spent through travel");
        add("ascension.skill.frostbound_stillness.name", "Frostbound Stillness");
        add("ascension.skill.frostbound_stillness.desc", "Gather cold spiritual stillness before releasing it across nearby creatures, building frost and imposing Frozen Form.");
        add("ascension.skill.imperial_first_stance.name", "First Stance: Measured Draw");
        add("ascension.skill.imperial_first_stance.desc", "A disciplined opening cut taught to every recruit; quick, cheap, and deliberately unremarkable.");
        add("ascension.skill.imperial_second_stance.name", "Second Stance: Guarded Advance");
        add("ascension.skill.imperial_second_stance.desc", "Advance behind the blade before releasing a short, cautious cut.");
        add("ascension.skill.imperial_third_stance.name", "Third Stance: Banner Sweep");
        add("ascension.skill.imperial_third_stance.desc", "A broad waist-high sweep intended to clear space around a marching standard.");
        add("ascension.skill.imperial_fourth_stance.name", "Fourth Stance: Spearhead Thrust");
        add("ascension.skill.imperial_fourth_stance.desc", "Compress the sword line into a narrow thrust patterned after an infantry spearhead.");
        add("ascension.skill.imperial_fifth_stance.name", "Fifth Stance: Formation Guard");
        add("ascension.skill.imperial_fifth_stance.desc", "Brace for a moment and form a thin defensive screen barely strong enough to blunt a clean hit.");
        add("ascension.skill.imperial_sixth_stance.name", "Sixth Stance: Encircling Cut");
        add("ascension.skill.imperial_sixth_stance.desc", "Turn through a compact circular cut designed for enemies pressing from several sides.");
        add("ascension.skill.imperial_seventh_stance.name", "Seventh Stance: Imperial Execution");
        add("ascension.skill.imperial_seventh_stance.desc", "Commit to the art's strongest finishing thrust, formidable only by ordinary mortal standards.");
        add("ascension.skill.bleeding_locust_swarm.name", "Bleeding Locust Swarm");
        add("ascension.skill.bleeding_locust_swarm.desc", "Seed a victim with short-lived locust qi that deals light damage and rarely leaps to nearby creatures.");
        add("ascension.skill.celestial_constellation_circulation.name", "Draco's Coiling Star Circulation");
        add("ascension.skill.celestial_constellation_circulation.desc", "Circulate Soul through Star and Bow, drawing seven points of the inner night sky into a single spiritual map");
        add("ascension.skill.orions_celestial_draw.name", "Orion's Celestial Draw");
        add("ascension.skill.orions_celestial_draw.desc", "Strengthen ranged projectiles with Agility and Spirit, guiding them toward targets marked by Hound-Star Pursuit");
        add("ascension.skill.hound_star_pursuit.name", "Canis Major: Hound-Star Pursuit");
        add("ascension.skill.hound_star_pursuit.desc", "Fix a Hound-Star mark upon a quarry; repeated marks make evasive movement increasingly demanding");
        add("ascension.skill.aquila_crossing.name", "Aquila Crossing");
        add("ascension.skill.aquila_crossing.desc", "Follow a projected celestial line and cross rapidly toward the point held in your sight");
        add("ascension.skill.scorpius_star_nail_formation.name", "Scorpius Star-Nail Formation");
        add("ascension.skill.scorpius_star_nail_formation.desc", "Plant three stellar anchors around a chosen point.");
        add("ascension.skill.sagittarius_heaven_piercing_arc.name", "Sagittarius Heaven-Piercing Arc");
        add("ascension.skill.sagittarius_heaven_piercing_arc.desc", "Condense a homing spectral arrow who grows with charge.");
        add("ascension.skill.sevenfold_celestial_hunt.name", "Ursa Major: Sevenfold Celestial Hunt");
        add("ascension.skill.sevenfold_celestial_hunt.desc", "Project Ursa Major into the battlefield, creating a seven-node domain.");
        add("ascension.skill.fourfold_dharma_circulation.name", "Fourfold Dharma Circulation");
        add("ascension.skill.fourfold_dharma_circulation.desc", "Cycle Soul, Life, Order, and Fist through one disciplined circuit, strengthening the foundation of the guardian path");
        add("ascension.skill.ordered_vajra_body.name", "Ordered Vajra Body");
        add("ascension.skill.ordered_vajra_body.desc", "Forge body and spirit into a disciplined Vajra, increasing Vitality, Spirit, and Strength");
        add("ascension.skill.golden_bell_of_living_law.name", "Golden Bell of Living Law");
        add("ascension.skill.golden_bell_of_living_law.desc", "Manifest a durable Golden Bell that absorbs incoming damage.");
        add("ascension.skill.guardian_dharma_idol.name", "Guardian Dharma Idol");
        add("ascension.skill.guardian_dharma_idol.desc", "Manifest a guardian Dharma.");
        add("ascension.skill.samsaras_witness.name", "Samsara's Witness");
        add("ascension.skill.samsaras_witness.desc", "The burdens witnessed along the six roads settle into an clarity of soul and body");
        add("ascension.skill.nine_revolutions_circulation.name", "Nine Revolutions Circulation");
        add("ascension.skill.nine_revolutions_circulation.desc", "Circulates essence through nine meridian routes, grinding into fuel for the forming Slaughter Wheel.");
        add("ascension.skill.unending_revolution.name", "Unending Revolution");
        add("ascension.skill.unending_revolution.desc", "The internal wheel never truly stills.");
        add("ascension.skill.slaughter_wheel_manifestation.name", "Slaughter Wheel Manifestation");
        add("ascension.skill.slaughter_wheel_manifestation.desc", "Launches a spinning essence wheel that bores through multiple enemies.");
        add("ascension.skill.revolving_butchers_guard.name", "Revolving Butcher's Guard");
        add("ascension.skill.revolving_butchers_guard.desc", "Manifests a rotating wheel around the cultivator that protects the cultivator.");
        add("ascension.skill.ninefold_carnage_circuit.name", "Ninefold Carnage Circuit");
        add("ascension.skill.ninefold_carnage_circuit.desc", "Pins a revolving slaughter circuit to the ground, grinding any enemies caught within it.");
        add("ascension.skill.heaven_grinding_slaughter_wheel.name", "Heaven-Grinding Slaughter Wheel");
        add("ascension.skill.heaven_grinding_slaughter_wheel.desc", "Charge the completed wheel and release it through the surrounding space, crushing nearby enemies beneath your revolutions.");
        add("ascension.skill.sanguine_circulation.name", "Sanguine Circulation");
        add("ascension.skill.sanguine_circulation.desc", "Open the veins and let cultivation drink directly from the blood, converting vitality into swift, unstable progress for as long as the wound is held open.");
        add("ascension.skill.malefic_sense.name", "Malefic Sense");
        add("ascension.skill.malefic_sense.desc", "Cast a hungry, crimson-black awareness outward, hunting the heartbeat and fear of every living thing within reach.");
        add("ascension.skill.blood_thirst.name", "Blood Thirst");
        add("ascension.skill.blood_thirst.desc", "Killing and wounding feed a rising thirst, each fresh spill of blood lending the cultivator a fleeting, compounding savagery.");
        add("ascension.skill.blood_frenzy.name", "Blood Frenzy");
        add("ascension.skill.blood_frenzy.desc", "W.I.P");
        add("ascension.skill.abyssal_culling.name", "Abyssal Culling");
        add("ascension.skill.abyssal_culling.desc", "Charge the abyss behind a single blade and release it as one culling stroke, paid for in the cultivator's own held-back blood.");

        // Mob Cultivation
        add("ascension.mob_cultivation.presence", "Spiritual Qi shifts through the air Something unnerving is nearby");
    }
}