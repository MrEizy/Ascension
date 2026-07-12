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

        // Configs
        add("ascension.config.hud", "HUD Settings");
        add("ascension.config.showExactValues", "Show Values");


        // Creative Tabs
        add("creativetab.ascension.artifact_items", "Ascension Artifacts");


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


        // GUI
        add("gui.ascension.introspection.title", "Introspection");
        add("gui.ascension.introspection.main", "Main");
        add("gui.ascension.introspection.stats", "Stats");
        add("gui.ascension.introspection.skills", "Skills");
        add("gui.ascension.introspection.cultivation", "Cultivation");
        add("gui.ascension.introspection.physique", "Physique");
        add("gui.ascension.introspection.bloodlines", "Bloodlines");
        add("gui.ascension.introspection.bloodline_count", "Bloodlines (%s)");
        add("gui.ascension.introspection.bloodline_multiple", "%s +%s");
        add("gui.ascension.introspection.bloodline_purity", "Purity: %s%%");
        add("gui.ascension.introspection.technique", "Technique");
        add("gui.ascension.introspection.none", "None");
        add("gui.ascension.introspection.no_physique", "No physique");
        add("gui.ascension.introspection.no_bloodlines", "No bloodlines");
        add("gui.ascension.introspection.no_paths", "No cultivation paths");
        add("gui.ascension.introspection.no_paths_description", "This character has not acquired a cultivation path.");
        add("gui.ascension.introspection.no_skills", "No skills");
        add("gui.ascension.introspection.no_skills_description", "This character has not acquired any skills.");
        add("gui.ascension.introspection.no_stats", "No Ascension stats are currently available.");
        add("gui.ascension.introspection.data_unavailable", "Synchronized Ascension data is temporarily unavailable.");
        add("gui.ascension.introspection.missing_registry_entry", "The synchronized identifier is not present in the client registry.");
        add("gui.ascension.introspection.empty_slot", "Empty");
        add("gui.ascension.introspection.castable_skill", "Castable skill. Select it, then click a hotbar slot below to assign or remove it.");

        add("gui.ascension.introspection.attribute.health", "Health");
        add("gui.ascension.introspection.attribute.damage", "Damage");
        add("gui.ascension.introspection.attribute.armor", "Armor");
        add("gui.ascension.introspection.attribute.toughness", "Toughness");
        add("gui.ascension.introspection.attribute.attack_speed", "Attack Speed");
        add("gui.ascension.introspection.attribute.speed", "Speed");
        add("gui.ascension.introspection.attribute.jump", "Jump");
        add("gui.ascension.introspection.attribute.step_height", "Step Height");
        add("gui.ascension.introspection.attribute.mining", "Mining");


        // Starter Screen Lang
        add("gui.ascension.starter.title", "Starting Selection");
        add("gui.ascension.starter.choose_bloodline", "Choose Your Bloodline");
        add("gui.ascension.starter.choose_physique", "Choose Your Physique");


        // Stats
        add("stat.ascension.vitality", "Vitality");
        add("stat.ascension.agility", "Agility");
        add("stat.ascension.strength", "Strength");
        add("stat.ascension.intelligence", "Intelligence");



        //Physiques
        add("ascension.physique.sword_bone","Sword Bone");


        //Keybinds
        add("key.category.minecraft.ascension", "Ascension");
        add("key.ascension.cycle_mode", "Cycle Modes");
        add("key.ascension.open_introspection", "Open Introspection");


        //Classifications
        add("zenith.ascension.category.bloodline", "Bloodline");
        add("zenith.ascension.category.physique", "Physique");
        add("zenith.ascension.category.technique", "Technique");

        add("zenith.ascension.tier.ordinary", "Ordinary");
        add("zenith.ascension.tier.profound", "Profound");
        add("zenith.ascension.tier.heaven", "Heaven");
        add("zenith.ascension.tier.saint", "Saint");
        add("zenith.ascension.tier.god", "God");
        add("zenith.ascension.tier.heavens_path", "Heaven's Path");



        // DATAPACK LANG STUFF

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

        //Techniques
        add("ascension.technique.mount_hua_sword_manual.name", "Plum Blossom Sword Manual");
        add("ascension.technique.mount_hua_sword_manual.desc", "A sword manual of Mount Hua that binds breath and sword as one, its gentle, falling-petal forms hiding a killing edge just a heartbeat behind");

        //Skills
        add("ascension.skill.plum_blossom_breathing.name", "Plum Blossom Breathing");
        add("ascension.skill.plum_blossom_breathing.desc", "The first breathing method taught at Mount Hua, circulating qi until it flows naturally into the sword, letting sword and essence grow together as one");

    }
}
